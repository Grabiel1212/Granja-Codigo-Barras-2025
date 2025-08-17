package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import model.Producto;
import com.google.zxing.BarcodeFormat;
import java.awt.image.BufferedImage;
import service.GeneradorService;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.awt.Color;

public class PanelImportarCodigo extends JDialog {

    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JButton btnCargarExcel;
    private JButton btnImportarSeleccionados;
    private JButton btnSeleccionarTodos;
    private JButton btnDeseleccionarTodos;
    private JButton btnCancelar;
    private GeneradorGU generadorGU;
    private List<Producto> productosImportados = new ArrayList<>();
    private JComboBox<String> cmbColumnaNombre;
    private JComboBox<String> cmbColumnaCodigo;
    private JComboBox<String> cmbHojasExcel;
    private List<String> cabecerasExcel = new ArrayList<>();
    private Workbook workbook;
    private Sheet sheet;

    public PanelImportarCodigo(GeneradorGU parent) {
        super(parent, "Importar Códigos desde Excel", true);
        this.generadorGU = parent;
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Panel superior para selección de archivo y columnas
        JPanel panelSuperior = new JPanel(new GridLayout(4, 2, 10, 10));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

 btnCargarExcel = new JButton("Cargar Archivo Excel");
btnCargarExcel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

// Azul moderno (Royal Blue)
btnCargarExcel.setBackground(new Color(25, 118, 210));  
btnCargarExcel.setForeground(Color.WHITE);

// Mantener fondo sólido
btnCargarExcel.setOpaque(true);
btnCargarExcel.setContentAreaFilled(true); // <<--- importante: true
btnCargarExcel.setFocusPainted(false);
btnCargarExcel.setBorderPainted(false);
btnCargarExcel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        
        panelSuperior.add(btnCargarExcel);

        // ComboBox para selección de hojas
        panelSuperior.add(new JLabel("Hoja:"));
        cmbHojasExcel = new JComboBox<>();
        cmbHojasExcel.setEnabled(false);
        panelSuperior.add(cmbHojasExcel);
        panelSuperior.add(new JLabel()); // Espacio vacío

        // Selectores de columnas
        panelSuperior.add(new JLabel("Columna para Nombres:"));
        cmbColumnaNombre = new JComboBox<>();
        cmbColumnaNombre.setEnabled(false);
        panelSuperior.add(cmbColumnaNombre);

        panelSuperior.add(new JLabel("Columna para Códigos:"));
        cmbColumnaCodigo = new JComboBox<>();
        cmbColumnaCodigo.setEnabled(false);
        panelSuperior.add(cmbColumnaCodigo);

        add(panelSuperior, BorderLayout.NORTH);

        // Modelo de tabla con mejoras visuales
        modeloTabla = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
        modeloTabla.addColumn("✓");
        modeloTabla.addColumn("Nombre del Producto");
        modeloTabla.addColumn("Código de Barras");

        tablaProductos = new JTable(modeloTabla);
        tablaProductos.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        tablaProductos.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

        tablaProductos.setRowHeight(25);
        tablaProductos.setSelectionBackground(new Color(200, 220, 255));
        tablaProductos.setGridColor(new Color(220, 220, 220));
        tablaProductos.setShowGrid(true);

        // Ajustar ancho de columnas
        TableColumnModel columnModel = tablaProductos.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(30);
        columnModel.getColumn(0).setMaxWidth(50);
        columnModel.getColumn(1).setPreferredWidth(300);
        columnModel.getColumn(2).setPreferredWidth(200);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150)),
                "Vista previa de productos",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12),
                new Color(70, 70, 70)
        ));

        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con botones mejorados
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Botones con estilo consistente
        btnSeleccionarTodos = createStyledButton("Seleccionar Todos", new Color(60, 179, 113)); // MediumSeaGreen
        btnDeseleccionarTodos = createStyledButton("Deseleccionar Todos", new Color(220, 80, 60)); // Tomato
        btnImportarSeleccionados = createStyledButton("Importar Seleccionados", new Color(65, 105, 225)); // RoyalBlue
        btnCancelar = createStyledButton("Cancelar", new Color(120, 120, 120)); // Gray

        panelInferior.add(btnSeleccionarTodos);
        panelInferior.add(btnDeseleccionarTodos);
        panelInferior.add(btnImportarSeleccionados);
        panelInferior.add(btnCancelar);

        add(panelInferior, BorderLayout.SOUTH);

        // Listeners
        btnCargarExcel.addActionListener(e -> cargarExcel());
        cmbHojasExcel.addActionListener(e -> cambiarHoja());
        cmbColumnaNombre.addActionListener(e -> actualizarVistaPrevia());
        cmbColumnaCodigo.addActionListener(e -> actualizarVistaPrevia());
        btnSeleccionarTodos.addActionListener(e -> seleccionarTodos(true));
        btnDeseleccionarTodos.addActionListener(e -> seleccionarTodos(false));
        btnCancelar.addActionListener(e -> dispose());
        btnImportarSeleccionados.addActionListener(e -> importarProductos());
    }

    // Método para crear botones con estilo consistente
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);

        // 🔹 Forzar que el botón use color sólido
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        // 🔹 Quitar borde blanco predeterminado y aplicar uno sólido
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 2), // borde sólido
                BorderFactory.createEmptyBorder(6, 18, 6, 18) // padding interno
        ));

        // 🔹 Cursor tipo "mano" al pasar encima
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void cargarExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (.xlsx)", "xlsx"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            modeloTabla.setRowCount(0);
            cmbColumnaNombre.removeAllItems();
            cmbColumnaCodigo.removeAllItems();
            cmbHojasExcel.removeAllItems();
            cabecerasExcel.clear();

            try (FileInputStream fis = new FileInputStream(archivo)) {
                // Cerrar workbook anterior si existe
                if (workbook != null) {
                    workbook.close();
                }

                workbook = new XSSFWorkbook(fis);

                // Llenar combo de hojas
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    cmbHojasExcel.addItem(workbook.getSheetName(i));
                }

                cmbHojasExcel.setEnabled(true);
                cmbHojasExcel.setSelectedIndex(0);
                cambiarHoja();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al leer el archivo: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void cambiarHoja() {
        if (workbook == null || cmbHojasExcel.getSelectedIndex() < 0) {
            return;
        }

        try {
            sheet = workbook.getSheetAt(cmbHojasExcel.getSelectedIndex());
            Row headerRow = sheet.getRow(0);
            cabecerasExcel.clear();
            cmbColumnaNombre.removeAllItems();
            cmbColumnaCodigo.removeAllItems();

            // Leer cabeceras
            for (Cell cell : headerRow) {
                cabecerasExcel.add(cell.toString());
            }

            // Llenar combos con cabeceras
            for (String cabecera : cabecerasExcel) {
                cmbColumnaNombre.addItem(cabecera);
                cmbColumnaCodigo.addItem(cabecera);
            }

            cmbColumnaNombre.setEnabled(true);
            cmbColumnaCodigo.setEnabled(true);
            actualizarVistaPrevia();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar hoja: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarVistaPrevia() {
        if (sheet == null
                || cmbColumnaNombre.getSelectedIndex() < 0
                || cmbColumnaCodigo.getSelectedIndex() < 0) {
            return;
        }

        modeloTabla.setRowCount(0);

        int nombreColIndex = cmbColumnaNombre.getSelectedIndex();
        int codigoColIndex = cmbColumnaCodigo.getSelectedIndex();

        // Leer hasta 100 filas para vista previa
        int maxFilas = Math.min(sheet.getLastRowNum(), 100000000);

        for (int i = 1; i <= maxFilas; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            Cell nombreCell = row.getCell(nombreColIndex);
            Cell codigoCell = row.getCell(codigoColIndex);

            String nombre = (nombreCell != null) ? obtenerValorCelda(nombreCell) : "";
            String codigo = (codigoCell != null) ? obtenerValorCelda(codigoCell) : "";

            // Mostrar filas con datos válidos
            if (!nombre.isEmpty() || !codigo.isEmpty()) {
                modeloTabla.addRow(new Object[]{true, nombre, codigo});
            }
        }

        // Ajustar ancho de columnas automáticamente
        tablaProductos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = tablaProductos.getColumnModel();
        columnModel.getColumn(1).setPreferredWidth(300);
        columnModel.getColumn(2).setPreferredWidth(200);
    }

    private String obtenerValorCelda(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private void seleccionarTodos(boolean seleccionar) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            modeloTabla.setValueAt(seleccionar, i, 0);
        }
    }

    private void importarProductos() {
        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay datos para importar",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        productosImportados.clear();
        GeneradorService servicio = new GeneradorService();
        int contador = 0;
        int errores = 0;
        StringBuilder erroresDetalle = new StringBuilder();

        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            if ((Boolean) modeloTabla.getValueAt(i, 0)) {
                String nombre = (String) modeloTabla.getValueAt(i, 1);
                String codigo = (String) modeloTabla.getValueAt(i, 2);
                int filaExcel = i + 2;  // Fila real en Excel (+1 por cabecera +1 por índice base 1)

                try {
                    // Validación básica
                    if (codigo == null || codigo.trim().isEmpty()) {
                        throw new Exception("Código vacío");
                    }

                    // Generar imagen del código de barras
                    BufferedImage imagen = servicio.generarImagenCodigo(
                            codigo, BarcodeFormat.CODE_128, 350, 120
                    );

                    if (imagen == null) {
                        throw new Exception("Error generando imagen");
                    }

                    productosImportados.add(new Producto(nombre, codigo, imagen, BarcodeFormat.CODE_128));
                    contador++;
                } catch (Exception ex) {
                    errores++;
                    erroresDetalle.append("\nFila ")
                            .append(filaExcel)
                            .append(": ")
                            .append(ex.getMessage())
                            .append(" (")
                            .append(nombre)
                            .append(")");
                }
            }
        }

        if (contador > 0) {
            generadorGU.agregarProductosImportados(productosImportados);
            dispose();
            generadorGU.mostrarListaProductos();
        }

        // Mostrar resumen de importación
        if (errores > 0) {
            JOptionPane.showMessageDialog(this,
                    "Importación completada con errores:\n"
                    + "• Productos importados: " + contador + "\n"
                    + "• Errores: " + errores + erroresDetalle.toString(),
                    "Resultado de Importación",
                    JOptionPane.WARNING_MESSAGE);
        } else if (contador > 0) {
            JOptionPane.showMessageDialog(this,
                    "Se importaron " + contador + " productos exitosamente!",
                    "Importación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se seleccionaron productos válidos para importar",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }

        // Cerrar workbook al finalizar
        try {
            if (workbook != null) {
                workbook.close();
            }
        } catch (Exception ex) {
            System.err.println("Error cerrando workbook: " + ex.getMessage());
        }
    }

    @Override
    public void dispose() {
        try {
            if (workbook != null) {
                workbook.close();
            }
        } catch (Exception ex) {
            System.err.println("Error cerrando workbook: " + ex.getMessage());
        }
        super.dispose();
    }
}
