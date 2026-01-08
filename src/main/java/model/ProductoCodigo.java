package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoCodigo {
    private String nombre;
    private String codigo;
    private String precio;
    private Integer cantidad;

    // Constructor adicional sin cantidad (para compatibilidad)
    public ProductoCodigo(String nombre, String codigo, String precio) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.precio = precio;
        this.cantidad = 1;
    }
}