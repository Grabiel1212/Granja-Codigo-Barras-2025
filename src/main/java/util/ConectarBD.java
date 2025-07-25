/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.NoArgsConstructor;

/**
 *
 * @author juang
 */

public class ConectarBD {

  private static final String URL = "jdbc:mysql://localhost:3306/ULTIMARQUETSAC?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

   // private static final String URL = "jdbc:mysql://192.168.56.1:3306/ULTIMARQUETSAC?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final String USUARIO = "root";
    private static final String CONTRASENA = "1212";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL cargado correctamente.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Error al cargar el driver de MySQL:");
            e.printStackTrace();
        }
    }

    public static Connection obtenerConexion() {
        try {
            Connection conn = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
            System.out.println("✅ Conexión exitosa a la base de datos.");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar con la base de datos:");
            e.printStackTrace();
            return null;
        }
    }

    public static void cerrarConexion(Connection conexion) {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("🔒 Conexión cerrada correctamente.");
                }
            } catch (SQLException e) {
                System.err.println("❌ Error al cerrar la conexión:");
                e.printStackTrace();
            }
        }
    }


}
