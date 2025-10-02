/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;
import com.google.zxing.BarcodeFormat;
import java.awt.image.BufferedImage;
import lombok.AllArgsConstructor;

/**
 *
 * @author juang
 */

@AllArgsConstructor
public class Producto {
    
    public String nombre;
    public String codigo;
    public BufferedImage imagen;
    public BarcodeFormat  formato;
    
      public String precio; // Nuevo atributo precio
    
    // Constructor sobrecargado para mantener compatibilidad
    public Producto(String nombre, String codigo, BufferedImage imagen, BarcodeFormat formato) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.imagen = imagen;
        this.formato = formato;
        this.precio = ""; // Precio por defecto vacío
    }
    
}
