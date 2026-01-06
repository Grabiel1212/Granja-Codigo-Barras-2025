package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import model.Producto;

public class PanelImportarTexto extends JDialog {

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
    private JComboBox<String> cmbColumnaPrecio;
    private JComboBox<String> cmbColumnaCantidad;
    private JComboBox<String> cmbHojasExcel;
    private List<String> cabecerasExcel = new ArrayList<>();
    private Workbook workbook;
    private Sheet sheet;

    // Colores para la interfaz
    private Color colorPrimario = new Color(41, 128, 185);
    private Color colorFondo = new Color(245, 245, 245);
    private Color colorBorde = new Color(220, 220, 220);
    private Color colorTexto = new Color(60, 60, 60);

    public PanelImportarTexto(GeneradorGU parent) {
        super(parent, "Importar Texto desde Excel", true);
        this.generadorGU = parent;
        setSize(1000, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        getContentPane().setBackground(colorFondo);

        // Panel superior para selección de archivo y columnas
        JPanel panelSuperior = crearPanelSuperior();
        add(panelSuperior, BorderLayout.NORTH);

        // Crear tabla para vista previa
        crearTablaVistaPrevia();

        // Panel inferior con botones
        JPanel panelInferior = crearPanelInferior();
        add(panelInferior, BorderLayout.SOUTH);

        // Configurar listeners
        configurarListeners();
    }

    private JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel(new GridBagLayout());
        panelSuperior.setBackground(colorFondo);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, colorBorde),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

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
        JLabel lblNombre = createStyledLabel("Columna para Nombre:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelSuperior.add(lblNombre, gbc);

        cmbColumnaNombre = createStyledComboBox();
        cmbColumnaNombre.setEnabled(false);
        gbc.gridx = 1;
        gbc.gridy = 3;
        panelSuperior.add(cmbColumnaNombre, gbc);

        // Selector de columna código
        JLabel lblCodigo = createStyledLabel("Columna para Código:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panelSuperior.add(lblCodigo, gbc);

        cmbColumnaCodigo = createStyledComboBox();
        cmbColumnaCodigo.setEnabled(false);
        gbc.gridx = 1;
        gbc.gridy = 4;
        panelSuperior.add(cmbColumnaCodigo, gbc);

        // Selector de columna precio
        JLabel lblPrecio = createStyledLabel("Columna para Precio:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        panelSuperior.add(lblPrecio, gbc);

        cmbColumnaPrecio = createStyledComboBox();
        cmbColumnaPrecio.setEnabled(false);
        gbc.gridx = 1;
        gbc.gridy = 5;
        panelSuperior.add(cmbColumnaPrecio, gbc);

        // Selector de columna cantidad (opcional)
        JLabel lblCantidad = createStyledLabel("Columna para Cantidad (opcional):");
        gbc.gridx = 0;
        gbc.gridy = 6;
        panelSuperior.add(lblCantidad, gbc);

        cmbColumnaCantidad = createStyledComboBox();
        cmbColumnaCantidad.setEnabled(false);
        cmbColumnaCantidad.addItem("(No usar)");
        gbc.gridx = 1;
        gbc.gridy = 6;
        panelSuperior.add(cmbColumnaCantidad, gbc);

        return panelSuperior;
    }

    private void crearTablaVistaPrevia() {
        // Modelo de tabla - clave para mostrar checkboxes
        modeloTabla = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                if (columnIndex == 3) {
                    return Integer.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        modeloTabla.addColumn("✓");
        modeloTabla.addColumn("Nombre del Producto");
        modeloTabla.addColumn("Código");
        modeloTabla.addColumn("Cantidad");
        modeloTabla.addColumn("Precio");

        tablaProductos = new JTable(modeloTabla);

        // Configurar el renderizador para centrar los checkboxes
        tablaProductos.getColumnModel().getColumn(0).setCellRenderer(new CheckboxRenderer());

        // Personalizar apariencia de la tabla
        tablaProductos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaProductos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaProductos.getTableHeader().setBackground(new Color(230, 230, 230));
        tablaProductos.getTableHeader().setForeground(new Color(70, 70, 70));
        tablaProductos.setRowHeight(28);
        tablaProductos.setSelectionBackground(new Color(200, 220, 255));
        tablaProductos.setGridColor(new Color(220, 220, 220));
        tablaProductos.setShowGrid(true);
        tablaProductos.setIntercellSpacing(new Dimension(1, 1));
        tablaProductos.setFillsViewportHeight(true);

        TableColumnModel columnModel = tablaProductos.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(0).setMaxWidth(50);
        columnModel.getColumn(1).setPreferredWidth(250);
        columnModel.getColumn(2).setPreferredWidth(150);
        columnModel.getColumn(3).setPreferredWidth(80);
        columnModel.getColumn(4).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                        "Vista previa de productos para tarjeta",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 13),
                        new Color(80, 80, 80))));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel crearPanelInferior() {
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelInferior.setBackground(colorFondo);
        panelInferior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, colorBorde),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Botones con estilo consistente
        btnSeleccionarTodos = createStyledButton("Seleccionar Todos", new Color(39, 174, 96));
        btnDeseleccionarTodos = createStyledButton("Deseleccionar Todos", new Color(231, 76, 60));
        btnImportarSeleccionados = createStyledButton("Previsualizar Tarjetas", colorPrimario);
        btnCancelar = createStyledButton("Cancelar", new Color(149, 165, 166));

        panelInferior.add(btnSeleccionarTodos);
        panelInferior.add(btnDeseleccionarTodos);
        panelInferior.add(Box.createHorizontalStrut(20));
        panelInferior.add(btnImportarSeleccionados);
        panelInferior.add(btnCancelar);

        return panelInferior;
    }

    private void configurarListeners() {
        btnCargarExcel.addActionListener(e -> cargarExcel());
        cmbHojasExcel.addActionListener(e -> cambiarHoja());
        cmbColumnaNombre.addActionListener(e -> actualizarVistaPrevia());
        cmbColumnaCodigo.addActionListener(e -> actualizarVistaPrevia());
        cmbColumnaPrecio.addActionListener(e -> actualizarVistaPrevia());
        cmbColumnaCantidad.addActionListener(e -> actualizarVistaPrevia());
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

            if (value instanceof Boolean) {
                setSelected((Boolean) value);
            } else {
                setSelected(false);
            }

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
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(70, 70, 70));
        return label;
    }

    // Método para crear combos con estilo
    private JComboBox<String> createStyledComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        return combo;
    }

    private void cargarExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos Excel (.xlsx)", "xlsx"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            modeloTabla.setRowCount(0);
            cmbColumnaNombre.removeAllItems();
            cmbColumnaCodigo.removeAllItems();
            cmbColumnaPrecio.removeAllItems();
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
            cmbColumnaPrecio.removeAllItems();
            cmbColumnaCantidad.removeAllItems();

            cmbColumnaCantidad.addItem("(No usar)");

            for (Cell cell : headerRow) {
                cabecerasExcel.add(cell.toString());
            }

            for (String cabecera : cabecerasExcel) {
                cmbColumnaNombre.addItem(cabecera);
                cmbColumnaCodigo.addItem(cabecera);
                cmbColumnaPrecio.addItem(cabecera);
                cmbColumnaCantidad.addItem(cabecera);
            }

            cmbColumnaNombre.setEnabled(true);
            cmbColumnaCodigo.setEnabled(true);
            cmbColumnaPrecio.setEnabled(true);
            cmbColumnaCantidad.setEnabled(true);
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
                || cmbColumnaCodigo.getSelectedIndex() < 0
                || cmbColumnaPrecio.getSelectedIndex() < 0) {
            return;
        }

        modeloTabla.setRowCount(0);

        int nombreColIndex = cmbColumnaNombre.getSelectedIndex();
        int codigoColIndex = cmbColumnaCodigo.getSelectedIndex();
        int precioColIndex = cmbColumnaPrecio.getSelectedIndex();
        int cantidadColIndex = -1;

        if (cmbColumnaCantidad.getSelectedIndex() > 0) {
            cantidadColIndex = cmbColumnaCantidad.getSelectedIndex() - 1;
        }

        int maxFilas = Math.min(sheet.getLastRowNum(), 10000);

        for (int i = 1; i <= maxFilas; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            Cell nombreCell = row.getCell(nombreColIndex);
            Cell codigoCell = row.getCell(codigoColIndex);
            Cell precioCell = row.getCell(precioColIndex);
            Cell cantidadCell = (cantidadColIndex >= 0 && cantidadColIndex < row.getLastCellNum())
                    ? row.getCell(cantidadColIndex)
                    : null;

            String nombre = (nombreCell != null) ? obtenerValorCelda(nombreCell) : "";
            String codigo = (codigoCell != null) ? obtenerValorCelda(codigoCell) : "";
            String precio = (precioCell != null) ? obtenerValorCelda(precioCell) : "";

            if (!precio.isEmpty()) {
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

            if (!nombre.isEmpty() || !codigo.isEmpty() || !precio.isEmpty()) {
                modeloTabla.addRow(new Object[] { true, nombre, codigo, cantidad, precio });
            }
        }

        tablaProductos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel columnModel = tablaProductos.getColumnModel();
        columnModel.getColumn(1).setPreferredWidth(250);
        columnModel.getColumn(2).setPreferredWidth(150);
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
                    if (nombre == null || nombre.trim().isEmpty()) {
                        throw new Exception("Nombre vacío");
                    }
                    if (codigo == null || codigo.trim().isEmpty()) {
                        throw new Exception("Código vacío");
                    }
                    if (precio == null || precio.trim().isEmpty()) {
                        throw new Exception("Precio vacío");
                    }

                    for (int j = 0; j < cantidad; j++) {
                        productosImportados.add(new Producto(nombre, codigo, null, null, precio));
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
            // Cerrar este panel y abrir el panel de previsualización
            dispose();
            PanelPrevisualizarTarjetas panelPrevisualizar = new PanelPrevisualizarTarjetas(generadorGU,
                    productosImportados);
            panelPrevisualizar.setVisible(true);

            if (errores > 0) {
                JOptionPane.showMessageDialog(this,
                        "Importación completada con errores:\n"
                                + "• Productos importados: " + contador + "\n"
                                + "• Errores: " + errores + erroresDetalle.toString(),
                        "Resultado de Importación",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Se importaron " + contador + " productos exitosamente!",
                        "Importación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se importó ningún producto. Verifique los datos seleccionados.",
                    "Sin productos",
                    JOptionPane.WARNING_MESSAGE);
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