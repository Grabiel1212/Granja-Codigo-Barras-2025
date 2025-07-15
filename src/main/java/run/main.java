/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package run;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import vista.GeneradorGU;

/**
 *
 * @author juang
 */
public class main {
    public static void main(String[] args) {
        // Establecer look & feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new GeneradorGU();  // ← Llamada a tu ventana principal
        });
    }
}
