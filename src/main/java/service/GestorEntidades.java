/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author juang
 */


import java.io.*;
import java.util.*;

public class GestorEntidades {
    private static final String DIR_ENTIDADES = "entidades/";
    private static final Map<String, Set<String>> cacheCodigos = new HashMap<>();
    
    public static List<String> listarEntidades() {
        List<String> entidades = new ArrayList<>();
        File directorio = new File(DIR_ENTIDADES);
        if (directorio.exists() && directorio.isDirectory()) {
            for (File archivo : directorio.listFiles()) {
                if (archivo.isFile()) {
                    String nombre = archivo.getName();
                    if (nombre.contains("_")) {
                        String entidad = nombre.substring(0, nombre.lastIndexOf('_'));
                        if (!entidades.contains(entidad)) {
                            entidades.add(entidad);
                        }
                    }
                }
            }
        }
        return entidades;
    }
    
    public static void guardarCodigo(String entidad, String formato, String codigo) {
        crearDirectorio();
        String archivo = DIR_ENTIDADES + entidad + "_" + formato + ".txt";
        
        // Agregar a caché
        String key = entidad + "_" + formato;
        if (!cacheCodigos.containsKey(key)) {
            cargarCodigos(entidad, formato);
        }
        cacheCodigos.get(key).add(codigo);
        
        // Guardar en archivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, true))) {
            writer.write(codigo);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean codigoExiste(String entidad, String formato, String codigo) {
        String key = entidad + "_" + formato;
        if (!cacheCodigos.containsKey(key)) {
            cargarCodigos(entidad, formato);
        }
        return cacheCodigos.get(key).contains(codigo);
    }
    
    private static void cargarCodigos(String entidad, String formato) {
        String key = entidad + "_" + formato;
        Set<String> codigos = new HashSet<>();
        String archivo = DIR_ENTIDADES + entidad + "_" + formato + ".txt";
        
        if (new File(archivo).exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    codigos.add(linea.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        cacheCodigos.put(key, codigos);
    }
    
    private static void crearDirectorio() {
        File directorio = new File(DIR_ENTIDADES);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
    }
}