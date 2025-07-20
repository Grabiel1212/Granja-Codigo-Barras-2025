package service;

import model.ProductoExcel;
import util.ConectarBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportadorService {
    
    public ImportResult importarProductosDesdeExcel(String nombreArchivo, List<ProductoExcel> productos) {
        ImportResult result = new ImportResult();
        Connection conn = null;
        try {
            conn = ConectarBD.obtenerConexion();
            conn.setAutoCommit(false);

            // 1. Obtener todos los códigos existentes en la base de datos
            Set<String> codigosExistentes = obtenerTodosLosCodigos(conn);
            
            // 2. Insertar archivo Excel
            int archivoId = insertarArchivoExcel(conn, nombreArchivo);
            
            // 3. Procesar productos
            for (ProductoExcel producto : productos) {
                String codigo = producto.getCodigo();
                String nombre = producto.getNombre();
                
                // Verificar si el código ya existe
                if (codigosExistentes.contains(codigo)) {
                    result.agregarError("Código duplicado: " + codigo + " - " + nombre);
                    continue;
                }
                
                // Insertar nuevo código
                insertarCodigoYProducto(conn, codigo, nombre, archivoId);
                codigosExistentes.add(codigo);  // Agregar a memoria para evitar duplicados en esta importación
                result.incrementarProductosImportados();
            }
            
            conn.commit();
        } catch (SQLException e) {
            result.agregarError("Error en base de datos: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
        } finally {
            ConectarBD.cerrarConexion(conn);
        }
        return result;
    }

    private Set<String> obtenerTodosLosCodigos(Connection conn) throws SQLException {
        Set<String> codigos = new HashSet<>();
        String sql = "SELECT Codigo FROM CodigosGenerados";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                codigos.add(rs.getString("Codigo"));
            }
        }
        return codigos;
    }

    private int insertarArchivoExcel(Connection conn, String nombreArchivo) throws SQLException {
        String sql = "INSERT INTO ArchivosExcel (NombreArchivo) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nombreArchivo);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener ID del archivo");
    }

    private void insertarCodigoYProducto(Connection conn, String codigo, String nombre, int archivoId) throws SQLException {
        // Insertar código
        String sqlCodigo = "INSERT INTO CodigosGenerados (Codigo, ArchivoID) VALUES (?, ?)";
        int codigoGenId;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sqlCodigo, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, codigo);
            pstmt.setInt(2, archivoId);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    codigoGenId = rs.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener ID de código generado");
                }
            }
        }

        // Insertar producto
        String sqlProducto = "INSERT INTO Productos (CodigoGeneradoID, NombreProducto) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlProducto)) {
            pstmt.setInt(1, codigoGenId);
            pstmt.setString(2, nombre);
            pstmt.executeUpdate();
        }
    }
}