package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
    private JComboBox<String> cmbColumnaCantidad;
    private JComboBox<String> cmbHojasExcel;
    private JComboBox<String> cmbColumnaPrecio;
    private List<String> cabecerasExcel = new ArrayList<>();
    private Workbook workbook;
    private Sheet sheet;

    public PanelImportarCodigo(GeneradorGU parent) {
        super(parent, "Importar Códigos desde Excel", true);
        this.generadorGU = parent;
        setSize(1000, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Configurar colores principales
        // Configurar colores principales
        java.awt.Color colorPrimario = new java.awt.Color(41, 128, 185);
        java.awt.Color colorSecundario = new java.awt.Color(52, 152, 219);
        java.awt.Color colorFondo = new java.awt.Color(245, 245, 245);
        java.awt.Color colorBorde = new java.awt.Color(220, 220, 220);
        java.awt.Color colorTexto = new java.awt.Color(60, 60, 60);

        getContentPane().setBackground(colorFondo);

        // Panel superior para selección de archivo y columnas
        JPanel panelSuperior = new JPanel(new GridBagLayout());
        panelSuperior.setBackground(colorFondo);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, colorBorde),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Botón cargar Excel
        btnCargarExcel = createStyledButton("Cargar Archivo Excel", colorPrimario);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panelSuperior.add(btnCargarExcel, gbc);

        // Separador
        JSeparator separator = new JSeparator();
        separator.setForeground(colorBorde);
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 10, 0);
        panelSuperior.add(separator, gbc);

        // Configuración de controles
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Selector de hoja
        JLabel lblHoja = createStyledLabel("Hoja de Excel:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelSuperior.add(lblHoja, gbc);

        cmbHojasExcel = createStyledComboBox();
        cmbHojasExcel.setEnabled(false);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panelSuperior.add(cmbHojasExcel, gbc);

        // Selector de columna nombre
        JLabel lblNombre = createStyledLabel("Columna para Nombres:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelSuperior.add(lblNombre, gbc);

        cmbColumnaNombre = createStyledComboBox();
        cmbColumnaNombre.setEnabled(false);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panelSuperior.add(cmbColumnaNombre, gbc);

        // Selector de columna código
        JLabel lblCodigo = createStyledLabel("Columna para Códigos:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panelSuperior.add(lblCodigo, gbc);

        cmbColumnaCodigo = createStyledComboBox();
        cmbColumnaCodigo.setEnabled(false);
        gbc.gridx = 1;
        gbc.gridy = 4;
        panelSuperior.add(cmbColumnaCodigo, gbc);

        // Selector de columna cantidad
        JLabel lblCantidad = createStyledLabel("Columna para Cantidad (opcional):");
        gbc.gridx = 0;
        gbc.gridy = 5;
        panelSuperior.add(lblCantidad, gbc);

        cmbColumnaCantidad = createStyledComboBox();
        cmbColumnaCantidad.setEnabled(false);
        cmbColumnaCantidad.addItem("(No usar)");
        gbc.gridx = 1;
        gbc.gridy = 5;
        panelSuperior.add(cmbColumnaCantidad, gbc);

        // Selector de columna precio
        JLabel lblPrecio = createStyledLabel("Columna para Precio (opcional):");
        gbc.gridx = 0;
        gbc.gridy = 6;
        panelSuperior.add(lblPrecio, gbc);

        cmbColumnaPrecio = createStyledComboBox();
        cmbColumnaPrecio.setEnabled(false);
        cmbColumnaPrecio.addItem("(No usar)");
        gbc.gridx = 1;
        gbc.gridy = 6;
        panelSuperior.add(cmbColumnaPrecio, gbc);

        add(panelSuperior, BorderLayout.NORTH);

        // Modelo de tabla - clave para mostrar checkboxes
        modeloTabla = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class; // Esto hace que aparezcan checkboxes automáticamente
                }
                if (columnIndex == 3) {
                    return Integer.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Solo la columna de checkboxes es editable
            }
        };

        modeloTabla.addColumn("✓");
        modeloTabla.addColumn("Nombre del Producto");
        modeloTabla.addColumn("Código de Barras");
        modeloTabla.addColumn("Cantidad");
        modeloTabla.addColumn("Precio");

        tablaProductos = new JTable(modeloTabla);

        // Configurar el renderizador para centrar los checkboxes
        tablaProductos.getColumnModel().getColumn(0).setCellRenderer(new CheckboxRenderer());

        // Personalizar apariencia de la tabla
        // Personalizar apariencia de la tabla
        tablaProductos.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        tablaProductos.getTableHeader().setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        tablaProductos.getTableHeader().setBackground(new java.awt.Color(230, 230, 230));
        tablaProductos.getTableHeader().setForeground(new java.awt.Color(70, 70, 70));
        tablaProductos.setRowHeight(28);
        tablaProductos.setSelectionBackground(new java.awt.Color(200, 220, 255));
        tablaProductos.setGridColor(new java.awt.Color(220, 220, 220));
        tablaProductos.setShowGrid(true);
        tablaProductos.setIntercellSpacing(new java.awt.Dimension(1, 1));
        tablaProductos.setFillsViewportHeight(true);

        TableColumnModel columnModel = tablaProductos.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(0).setMaxWidth(50);
        columnModel.getColumn(1).setPreferredWidth(250);
        columnModel.getColumn(2).setPreferredWidth(180);
        columnModel.getColumn(3).setPreferredWidth(80);
        columnModel.getColumn(4).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new java.awt.Color(180, 180, 180), 1, true),
                        "Vista previa de productos",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13),
                        new java.awt.Color(80, 80, 80)
                )
        ));
        scrollPane.getViewport().setBackground(java.awt.Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        // Panel inferior con botones mejorados
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelInferior.setBackground(colorFondo);
        panelInferior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, colorBorde),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Botones con estilo consistente
        btnSeleccionarTodos = createStyledButton("Seleccionar Todos", new java.awt.Color(39, 174, 96));
        btnDeseleccionarTodos = createStyledButton("Deseleccionar Todos", new java.awt.Color(231, 76, 60));
        btnImportarSeleccionados = createStyledButton("Importar Seleccionados", colorPrimario);
        btnCancelar = createStyledButton("Cancelar", new java.awt.Color(149, 165, 166));

        panelInferior.add(btnSeleccionarTodos);
        panelInferior.add(btnDeseleccionarTodos);
        panelInferior.add(Box.createHorizontalStrut(20));
        panelInferior.add(btnImportarSeleccionados);
        panelInferior.add(btnCancelar);

        add(panelInferior, BorderLayout.SOUTH);

        // Listeners
        btnCargarExcel.addActionListener(e -> cargarExcel());
        cmbHojasExcel.addActionListener(e -> cambiarHoja());
        cmbColumnaNombre.addActionListener(e -> actualizarVistaPrevia());
        cmbColumnaCodigo.addActionListener(e -> actualizarVistaPrevia());
        cmbColumnaCantidad.addActionListener(e -> actualizarVistaPrevia());
        cmbColumnaPrecio.addActionListener(e -> actualizarVistaPrevia());
        btnSeleccionarTodos.addActionListener(e -> seleccionarTodos(true));
        btnDeseleccionarTodos.addActionListener(e -> seleccionarTodos(false));
        btnCancelar.addActionListener(e -> dispose());
        btnImportarSeleccionados.addActionListener(e -> importarProductos());
    }

    // Renderizador personalizado para checkboxes centrados
    class CheckboxRenderer extends JCheckBox implements TableCellRenderer {

        public CheckboxRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            // Configurar el estado del checkbox basado en el valor de la celda
            if (value instanceof Boolean) {
                setSelected((Boolean) value);
            } else {
                setSelected(false);
            }

            // Cambiar colores basado en selección
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            return this;
        }
    }

    // Método para crear botones con estilo consistente
    private JButton createStyledButton(String text, java.awt.Color bgColor) {
        javax.swing.JButton button = new javax.swing.JButton(text);
        button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(java.awt.Color.WHITE);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(bgColor.darker(), 1),
                javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Efecto hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // Método para crear etiquetas con estilo
    // Método para crear etiquetas con estilo
    private javax.swing.JLabel createStyledLabel(String text) {
        javax.swing.JLabel label = new javax.swing.JLabel(text);
        label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        label.setForeground(new java.awt.Color(70, 70, 70));
        return label;
    }

// Método para crear combos con estilo
    private javax.swing.JComboBox<String> createStyledComboBox() {
        javax.swing.JComboBox<String> combo = new javax.swing.JComboBox<>();
        combo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        combo.setBackground(java.awt.Color.WHITE);
        combo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
                javax.swing.BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        return combo;
    }

    // Los métodos restantes (cargarExcel, cambiarHoja, actualizarVistaPrevia, etc.)
    // se mantienen exactamente igual que en tu código original
    private void cargarExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (.xlsx)", "xlsx"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            modeloTabla.setRowCount(0);
            cmbColumnaNombre.removeAllItems();
            cmbColumnaCodigo.removeAllItems();
            cmbColumnaCantidad.removeAllItems();
            cmbHojasExcel.removeAllItems();
            cabecerasExcel.clear();

            try (FileInputStream fis = new FileInputStream(archivo)) {
                if (workbook != null) {
                    workbook.close();
                }

                workbook = new XSSFWorkbook(fis);

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
            cmbColumnaCantidad.removeAllItems();
            cmbColumnaPrecio.removeAllItems();

            cmbColumnaCantidad.addItem("(No usar)");
            cmbColumnaPrecio.addItem("(No usar)");

            for (Cell cell : headerRow) {
                cabecerasExcel.add(cell.toString());
            }

            for (String cabecera : cabecerasExcel) {
                cmbColumnaNombre.addItem(cabecera);
                cmbColumnaCodigo.addItem(cabecera);
                cmbColumnaCantidad.addItem(cabecera);
                cmbColumnaPrecio.addItem(cabecera);
            }

            cmbColumnaNombre.setEnabled(true);
            cmbColumnaCodigo.setEnabled(true);
            cmbColumnaCantidad.setEnabled(true);
            cmbColumnaPrecio.setEnabled(true);
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
        int cantidadColIndex = -1;
        int precioColIndex = -1;

        if (cmbColumnaCantidad.getSelectedIndex() > 0) {
            cantidadColIndex = cmbColumnaCantidad.getSelectedIndex() - 1;
        }

        if (cmbColumnaPrecio.getSelectedIndex() > 0) {
            precioColIndex = cmbColumnaPrecio.getSelectedIndex() - 1;
        }

        int maxFilas = Math.min(sheet.getLastRowNum(), 10000);

        for (int i = 1; i <= maxFilas; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            Cell nombreCell = row.getCell(nombreColIndex);
            Cell codigoCell = row.getCell(codigoColIndex);
            Cell cantidadCell = (cantidadColIndex >= 0 && cantidadColIndex < row.getLastCellNum())
                    ? row.getCell(cantidadColIndex) : null;
            Cell precioCell = (precioColIndex >= 0 && precioColIndex < row.getLastCellNum())
                    ? row.getCell(precioColIndex) : null;

            String nombre = (nombreCell != null) ? obtenerValorCelda(nombreCell) : "";
            String codigo = (codigoCell != null) ? obtenerValorCelda(codigoCell) : "";
            String precio = "";

            if (precioCell != null) {
                precio = obtenerValorCelda(precioCell);
                try {
                    double precioNum = Double.parseDouble(precio.replace(",", "."));
                    precio = String.format("S/ %.2f", precioNum);
                } catch (NumberFormatException ex) {
                    // Mantener valor original si no es número
                }
            }

            int cantidad = 1;

            if (cantidadCell != null) {
                try {
                    String cantidadStr = obtenerValorCelda(cantidadCell);
                    cantidad = (int) Double.parseDouble(cantidadStr);
                } catch (NumberFormatException ex) {
                    cantidad = 1;
                }
            }

            if (!nombre.isEmpty() || !codigo.isEmpty()) {
                modeloTabla.addRow(new Object[]{true, nombre, codigo, cantidad, precio});
            }
        }

        tablaProductos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = tablaProductos.getColumnModel();
        columnModel.getColumn(1).setPreferredWidth(250);
        columnModel.getColumn(2).setPreferredWidth(180);
        columnModel.getColumn(3).setPreferredWidth(80);
        columnModel.getColumn(4).setPreferredWidth(100);
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
                String precio = (String) modeloTabla.getValueAt(i, 4);
                int cantidad = 1;

                Object cantidadObj = modeloTabla.getValueAt(i, 3);
                if (cantidadObj instanceof Integer) {
                    cantidad = (Integer) cantidadObj;
                } else if (cantidadObj instanceof String) {
                    try {
                        cantidad = Integer.parseInt((String) cantidadObj);
                    } catch (NumberFormatException ex) {
                        cantidad = 1;
                    }
                }

                int filaExcel = i + 2;

                try {
                    if (codigo == null || codigo.trim().isEmpty()) {
                        throw new Exception("Código vacío");
                    }

                    BufferedImage imagen = servicio.generarImagenCodigo(
                            codigo, BarcodeFormat.CODE_128, 350, 120
                    );

                    if (imagen == null) {
                        throw new Exception("Error generando imagen");
                    }

                    for (int j = 0; j < cantidad; j++) {
                        productosImportados.add(new Producto(nombre, codigo, imagen, BarcodeFormat.CODE_128, precio));
                        contador++;
                    }
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
