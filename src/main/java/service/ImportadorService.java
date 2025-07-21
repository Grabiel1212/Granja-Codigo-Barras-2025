package service;

import model.ProductoExcel;
import util.ConectarBD;
import java.sql.*;
import java.util.*;

public class ImportadorService {

    private static final Set<String> CODIGOS_GLOBALES = new HashSet<>();

    // Se carga una sola vez al arrancar la clase
    static {
        cargarCodigosExistentes();
    }

    // Método para recargar la caché desde la base de datos
    public static void cargarCodigosExistentes() {
        CODIGOS_GLOBALES.clear();
        Connection conn = null;
        try {
            conn = ConectarBD.obtenerConexion();
            String sql = "SELECT Codigo FROM CodigosGenerados";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    CODIGOS_GLOBALES.add(rs.getString("Codigo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar códigos existentes: " + e.getMessage());
        } finally {
            ConectarBD.cerrarConexion(conn);
        }
    }

    public ImportResult importarProductosDesdeExcel(String nombreArchivo, List<ProductoExcel> productos) {
        ImportResult result = new ImportResult();
        Connection conn = null;
        try {
            conn = ConectarBD.obtenerConexion();
            conn.setAutoCommit(false);

            Set<String> codigosExistentes = obtenerTodosLosCodigos(conn);
            int archivoId = insertarArchivoExcel(conn, nombreArchivo);

            for (ProductoExcel producto : productos) {
                String codigo = producto.getCodigo();
                String nombre = producto.getNombre();

                if (codigosExistentes.contains(codigo)) {
                    result.agregarError("Código duplicado: " + codigo + " - " + nombre);
                    continue;
                }

                insertarCodigoYProducto(conn, codigo, nombre, archivoId);
                codigosExistentes.add(codigo);
                result.incrementarProductosImportados();
            }

            conn.commit();
            actualizarCache(); // 🔄 Muy importante

        } catch (SQLException e) {
            result.agregarError("Error en base de datos: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                }
            }
        } finally {
            ConectarBD.cerrarConexion(conn);
        }
        return result;
    }

    private Set<String> obtenerTodosLosCodigos(Connection conn) throws SQLException {
        Set<String> codigos = new HashSet<>();
        String sql = "SELECT Codigo FROM CodigosGenerados";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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

        String sqlProducto = "INSERT INTO Productos (CodigoGeneradoID, NombreProducto) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlProducto)) {
            pstmt.setInt(1, codigoGenId);
            pstmt.setString(2, nombre);
            pstmt.executeUpdate();
        }
    }

    public static boolean existeEnBD(String codigo) {
        return CODIGOS_GLOBALES.contains(codigo);
    }

    public static Set<String> getCodigosGlobales() {
        return Collections.unmodifiableSet(CODIGOS_GLOBALES);
    }

    // 🔄 Método público para actualizar caché
    public static void actualizarCache() {
        cargarCodigosExistentes();
        GeneradorService.actualizarCache(new HashSet<>(CODIGOS_GLOBALES));
    }
}
