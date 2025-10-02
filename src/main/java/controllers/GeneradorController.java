/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import model.Producto;
import service.GeneradorService;
import com.google.zxing.BarcodeFormat;
import vista.GeneradorGU;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author juang
 */
public class GeneradorController {
    private final GeneradorService generadorService;
    private final List<Producto> listaProductos;
    private final GeneradorGU vista;

    public GeneradorController(GeneradorGU vista) {
        this.vista = vista;
        this.generadorService = new GeneradorService();
        this.listaProductos = new ArrayList<>();
    }

    public String generarEAN13() {
        return generadorService.generarEAN13();
    }

    public String generarCodigoUnico() {
        return generadorService.generarCodigoUnico();
    }

    public BufferedImage generarImagenCodigo(String codigo, BarcodeFormat formato, int ancho, int alto) {
        return generadorService.generarImagenCodigo(codigo, formato, ancho, alto);
    }

    public void guardarProducto(String nombre, String codigo, BufferedImage imagen, BarcodeFormat formato, String precio) {
        listaProductos.add(new Producto(nombre, codigo, imagen, formato, precio));
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(vista, 
            "<html><div style='text-align:center;'>" + mensaje + "</div></html>", 
            "Error", JOptionPane.ERROR_MESSAGE);
    }
}