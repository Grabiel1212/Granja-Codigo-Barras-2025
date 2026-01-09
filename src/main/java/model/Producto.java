package model;

import java.awt.image.BufferedImage;

import com.google.zxing.BarcodeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Producto {

    public String nombre;
    public String codigo; // Código de barras (EAN13, etc.)
    public String codigoTexto; // Nuevo campo: código de texto (ej: AR-NO, ARR-Z)
    public BufferedImage imagen;
    public BarcodeFormat formato;
    public String precio;

    // Constructor con 5 parámetros (para compatibilidad con el código que no usa
    // codigoTexto)
    public Producto(String nombre, String codigo, BufferedImage imagen, BarcodeFormat formato, String precio) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.codigoTexto = ""; // Por defecto vacío
        this.imagen = imagen;
        this.formato = formato;
        this.precio = precio;
    }

    // Constructor con 6 parámetros
    public Producto(String nombre, String codigo, String codigoTexto, BufferedImage imagen, BarcodeFormat formato,
            String precio) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.codigoTexto = codigoTexto;
        this.imagen = imagen;
        this.formato = formato;
        this.precio = precio;
    }
}