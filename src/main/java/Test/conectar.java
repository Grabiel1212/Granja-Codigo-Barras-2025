/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Test;
import java.sql.Connection;
import util.ConectarBD;
/**
 *
 * @author juang
 */
public class conectar {
    
    public static void main(String[] args) {
        
        Connection con = ConectarBD.obtenerConexion();
        
        System.out.println(con);
        
    }
    
}
