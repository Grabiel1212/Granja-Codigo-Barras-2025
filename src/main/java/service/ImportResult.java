package service;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private int productosImportados;
    private List<String> errores = new ArrayList<>();

    public void incrementarProductosImportados() {
        productosImportados++;
    }

    public void agregarError(String error) {
        errores.add(error);
    }

    // Getters
    public int getProductosImportados() {
        return productosImportados;
    }

    public List<String> getErrores() {
        return errores;
    }
}