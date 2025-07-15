package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    private List<String> cabecerasExcel = new ArrayList<>();
    private Sheet sheet;

    public PanelImportarCodigo(GeneradorGU parent) {
        super(parent, "Importar Códigos desde Excel", true);
        this.generadorGU = parent;
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Panel superior para selección de columnas
        JPanel panelSuperior = new JPanel(new GridLayout(3, 2, 10, 10));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      btnCargarExcel = new JButton("Cargar Archivo Excel");
btnCargarExcel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

btnCargarExcel.setBackground(new Color(100, 149, 237)); // Azul claro estilo cornflower blue
btnCargarExcel.setForeground(Color.WHITE);
btnCargarExcel.setFocusPainted(false);
btnCargarExcel.setBorderPainted(false);
btnCargarExcel.setOpaque(true); // Obligatorio si quieres color de fondo visible en algunos LAFs


        panelSuperior.add(btnCargarExcel);
        panelSuperior.add(new JLabel()); // Espacio en blanco

        panelSuperior.add(new JLabel("Columna para Nombres:"));
        cmbColumnaNombre = new JComboBox<>();
        cmbColumnaNombre.setEnabled(false);
        panelSuperior.add(cmbColumnaNombre);

        panelSuperior.add(new JLabel("Columna para Códigos:"));
        cmbColumnaCodigo = new JComboBox<>();
        cmbColumnaCodigo.setEnabled(false);
        panelSuperior.add(cmbColumnaCodigo);

        add(panelSuperior, BorderLayout.NORTH);

        // Modelo de tabla
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
        modeloTabla.addColumn("Seleccionar");
        modeloTabla.addColumn("Nombre");
        modeloTabla.addColumn("Código de Barras");

        tablaProductos = new JTable(modeloTabla);
        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(30);
        tablaProductos.setRowHeight(25);
        tablaProductos.setSelectionBackground(new Color(200, 220, 255));

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Productos a importar"));
        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelInferior = new JPanel();
        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.X_AXIS));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panelInferior.add(Box.createHorizontalGlue());

        // Inicializar botones
        btnSeleccionarTodos = new JButton("Seleccionar Todos");
        btnDeseleccionarTodos = new JButton("Deseleccionar Todos");
        btnImportarSeleccionados = new JButton("Importar Seleccionados");
        btnCancelar = new JButton("Cancelar");

        // Configurar colores
        btnSeleccionarTodos.setBackground(new Color(60, 150, 60));
        btnDeseleccionarTodos.setBackground(new Color(200, 60, 60));
        btnImportarSeleccionados.setBackground(new Color(50, 100, 200));
        btnCancelar.setBackground(new Color(150, 150, 150));

        btnSeleccionarTodos.setForeground(Color.WHITE);
        btnDeseleccionarTodos.setForeground(Color.WHITE);
        btnImportarSeleccionados.setForeground(Color.WHITE);
        btnCancelar.setForeground(Color.WHITE);

        // Hacer los botones opacos para mostrar colores
        for (JButton btn : new JButton[]{btnSeleccionarTodos, btnDeseleccionarTodos,
            btnImportarSeleccionados, btnCancelar}) {
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setMargin(new Insets(5, 10, 5, 10));
        }

        panelInferior.add(btnSeleccionarTodos);
        panelInferior.add(Box.createRigidArea(new Dimension(10, 0)));
        panelInferior.add(btnDeseleccionarTodos);
        panelInferior.add(Box.createRigidArea(new Dimension(10, 0)));
        panelInferior.add(btnImportarSeleccionados);
        panelInferior.add(Box.createRigidArea(new Dimension(10, 0)));
        panelInferior.add(btnCancelar);

        add(panelInferior, BorderLayout.SOUTH);

        // Listeners
        btnCargarExcel.addActionListener(e -> cargarExcel());
        btnSeleccionarTodos.addActionListener(e -> seleccionarTodos(true));
        btnDeseleccionarTodos.addActionListener(e -> seleccionarTodos(false));
        btnCancelar.addActionListener(e -> dispose());
        btnImportarSeleccionados.addActionListener(e -> importarProductos());

        // Listener para actualizar vista previa al cambiar selección de columnas
        cmbColumnaNombre.addActionListener(e -> actualizarVistaPrevia());
        cmbColumnaCodigo.addActionListener(e -> actualizarVistaPrevia());
    }

    private void cargarExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (.xlsx)", "xlsx"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            modeloTabla.setRowCount(0);
            cmbColumnaNombre.removeAllItems();
            cmbColumnaCodigo.removeAllItems();
            cabecerasExcel.clear();

            try (FileInputStream fis = new FileInputStream(archivo); 
                 Workbook workbook = new XSSFWorkbook(fis)) {

                sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                // Leer cabeceras
                for (Cell cell : headerRow) {
                    cabecerasExcel.add(cell.toString());
                }

                // Llenar comboboxes con cabeceras
                for (String cabecera : cabecerasExcel) {
                    cmbColumnaNombre.addItem(cabecera);
                    cmbColumnaCodigo.addItem(cabecera);
                }

                cmbColumnaNombre.setEnabled(true);
                cmbColumnaCodigo.setEnabled(true);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al leer el archivo: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarVistaPrevia() {
        if (sheet == null || cmbColumnaNombre.getSelectedIndex() < 0 || cmbColumnaCodigo.getSelectedIndex() < 0) {
            return;
        }

        modeloTabla.setRowCount(0);

        int nombreColIndex = cmbColumnaNombre.getSelectedIndex();
        int codigoColIndex = cmbColumnaCodigo.getSelectedIndex();

        // Leer filas (empezando desde la fila 1 para omitir cabeceras)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            Cell nombreCell = row.getCell(nombreColIndex);
            Cell codigoCell = row.getCell(codigoColIndex);

            String nombre = (nombreCell != null) ? obtenerValorCelda(nombreCell) : "";
            String codigo = (codigoCell != null) ? obtenerValorCelda(codigoCell) : "";

            if (!nombre.isEmpty() && !codigo.isEmpty()) {
                modeloTabla.addRow(new Object[]{true, nombre, codigo});
            }
        }
    }

    private String obtenerValorCelda(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double num = cell.getNumericCellValue();
                    if (num == (int) num) {
                        return String.valueOf((int) num);
                    } else {
                        return String.valueOf(num);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
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
    int filaError = 0;
        
        

       for (int i = 0; i < modeloTabla.getRowCount(); i++) {
        if ((Boolean) modeloTabla.getValueAt(i, 0)) {
            String nombre = (String) modeloTabla.getValueAt(i, 1);
            String codigo = (String) modeloTabla.getValueAt(i, 2);
            filaError = i + 1;  // Guardar número de fila para reporte

            try {
                // Validación básica del código
                if (codigo == null || codigo.trim().isEmpty()) {
                    throw new IllegalArgumentException("Código vacío");
                }
                
                // Generar imagen
                BufferedImage imagen = servicio.generarImagenCodigo(
                    codigo, BarcodeFormat.CODE_128, 350, 120
                );
                
                // Verificar que la imagen se generó correctamente
                if (imagen == null) {
                    throw new RuntimeException("Imagen no generada");
                }
                
                productosImportados.add(new Producto(nombre, codigo, imagen, BarcodeFormat.CODE_128));
                contador++;
            } catch (Exception ex) {
                errores++;
                erroresDetalle.append("\nFila ")
                              .append(filaError)
                              .append(": ")
                              .append(ex.getMessage())
                              .append(" [")
                              .append(nombre)
                              .append("]");
                System.err.println("Error en fila " + filaError + ": " + ex.getMessage());
            }
        }
    }

        if (contador > 0) {
            generadorGU.agregarProductosImportados(productosImportados);
            dispose();
            generadorGU.mostrarListaProductos();

            JOptionPane.showMessageDialog(this,
                    "Se importaron " + contador + " productos exitosamente!"
                    + (errores > 0 ? "\nErrores: " + errores : ""),
                    "Importación Exitosa", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se seleccionaron productos para importar",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
        
        // Mostrar detalles de errores
    if (errores > 0) {
        JOptionPane.showMessageDialog(this,
            "Se importaron " + contador + " productos\n" +
            "Errores: " + errores + erroresDetalle.toString(),
            "Importación con errores", JOptionPane.WARNING_MESSAGE);
    }
    }
}