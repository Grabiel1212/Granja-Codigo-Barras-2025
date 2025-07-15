package vista;

import service.GeneradorService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;
import java.awt.Font;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class PanelImportarExcel extends JDialog {

    private final GeneradorGU parent;
    private JProgressBar progressBar;
    private JComboBox<String> cmbFormato;
    private JTable tablaPreview;
    private DefaultTableModel tableModel;
    private Workbook workbook;
    private Sheet sheet;
    private int columnaCodigoBarras = -1;
    private File archivoSeleccionado;

    public PanelImportarExcel(GeneradorGU parent) {
        super(parent, "Importar desde Excel", true);
        this.parent = parent;
        setSize(900, 700);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        // Panel principal con estilo
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(parent.COLOR_FONDO);

        // Título
        JLabel lblTitulo = new JLabel("Importar desde Excel", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(parent.COLOR_PRIMARIO);
        mainPanel.add(lblTitulo, BorderLayout.NORTH);

        // Panel de contenido
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(parent.COLOR_FONDO);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        // Panel de controles
        JPanel controlPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        controlPanel.setBackground(parent.COLOR_FONDO);
        controlPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(parent.COLOR_BORDE, 1),
                "Configuración"
        ));

        // Panel de formato
        JPanel formatoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        formatoPanel.setBackground(parent.COLOR_FONDO);
        formatoPanel.add(new JLabel("Formato de código:"));
        cmbFormato = new JComboBox<>(new String[]{"CODE_128", "EAN_13"});
        cmbFormato.setPreferredSize(new Dimension(150, 30));
        formatoPanel.add(cmbFormato);
        controlPanel.add(formatoPanel);

        // Botón para seleccionar archivo
        JButton btnSeleccionar = new JButton("Seleccionar Archivo Excel");
        btnSeleccionar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSeleccionar.setBackground(new Color(52, 152, 219));  // Azul vibrante
        btnSeleccionar.setForeground(Color.WHITE);              // Texto blanco
        btnSeleccionar.setFocusPainted(false);                 // Sin borde de foco
        btnSeleccionar.setOpaque(true);                        // NECESARIO para pintar fondo
        btnSeleccionar.setContentAreaFilled(true);             // NECESARIO para pintar fondo
        btnSeleccionar.setBorderPainted(false);                // Opcional para estética

        btnSeleccionar.addActionListener(e -> seleccionarArchivoExcel());
        btnSeleccionar.setPreferredSize(new Dimension(250, 40));
        controlPanel.add(btnSeleccionar);

        contentPanel.add(controlPanel, BorderLayout.NORTH);

        // Panel de previsualización
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(parent.COLOR_FONDO);
        previewPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(parent.COLOR_BORDE, 1),
                "Previsualización de datos"
        ));

        // Modelo de tabla
        tableModel = new DefaultTableModel();
        tablaPreview = new JTable(tableModel);
        tablaPreview.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaPreview.setRowHeight(28);
        tablaPreview.setGridColor(new Color(200, 200, 200)); // Color de líneas entre celdas
        tablaPreview.setBackground(Color.WHITE);
        tablaPreview.setForeground(Color.BLACK);
        tablaPreview.setSelectionBackground(new Color(52, 152, 219)); // Azul para fila seleccionada
        tablaPreview.setSelectionForeground(Color.WHITE);
        tablaPreview.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

// Cabecera de tabla
        JTableHeader header = tablaPreview.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(parent.COLOR_PRIMARIO); // Azul vibrante
        header.setForeground(Color.WHITE); // Texto blanco
        header.setOpaque(true);

// Forzar que se pinte el fondo
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBackground(parent.COLOR_PRIMARIO);
                label.setForeground(Color.WHITE);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setOpaque(true);
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaPreview);
        previewPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(previewPanel, BorderLayout.CENTER);

        // Panel de acciones
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setBackground(parent.COLOR_FONDO);

        JButton btnProcesar = new JButton("Generar Códigos");
        btnProcesar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProcesar.setBackground(new Color(46, 204, 113)); // Verde
        btnProcesar.setForeground(Color.WHITE);
        btnProcesar.setFocusPainted(false);
        btnProcesar.setOpaque(true);
        btnProcesar.setContentAreaFilled(true);
        btnProcesar.setBorderPainted(false);
        btnProcesar.setPreferredSize(new Dimension(200, 45));
        btnProcesar.addActionListener(e -> procesarArchivoExcel());
        actionPanel.add(btnProcesar);

        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setBackground(parent.COLOR_PRIMARIO);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setOpaque(true);
        btnGuardar.setContentAreaFilled(true);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setPreferredSize(new Dimension(200, 45));
        btnGuardar.addActionListener(e -> guardarCambios());
        actionPanel.add(btnGuardar);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.setBackground(parent.COLOR_ACENTO);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setOpaque(true);
        btnCerrar.setContentAreaFilled(true);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setPreferredSize(new Dimension(150, 45));
        btnCerrar.addActionListener(e -> dispose());
        actionPanel.add(btnCerrar);

        contentPanel.add(actionPanel, BorderLayout.SOUTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Barra de progreso
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void seleccionarArchivoExcel() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Archivos Excel", "xls", "xlsx");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = fileChooser.getSelectedFile();
            cargarDatosExcel(archivoSeleccionado);
        }
    }

    private void cargarDatosExcel(File archivo) {
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Cargando datos del Excel...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    // Leer archivo Excel
                    FileInputStream inputStream = new FileInputStream(archivo);
                    workbook = archivo.getName().endsWith(".xlsx")
                            ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);

                    sheet = workbook.getSheetAt(0);
                    columnaCodigoBarras = -1;

                    // Buscar columna "Código Barras"
                    Row headerRow = sheet.getRow(0);
                    for (Cell cell : headerRow) {
                        if ("Código Barras".equalsIgnoreCase(cell.getStringCellValue().trim())) {
                            columnaCodigoBarras = cell.getColumnIndex();
                            break;
                        }
                    }

                    if (columnaCodigoBarras == -1) {
                        throw new Exception("No se encontró la columna 'Código Barras'");
                    }

                    // Preparar datos para la tabla
                    Vector<String> columnNames = new Vector<>();
                    for (Cell cell : headerRow) {
                        columnNames.add(cell.getStringCellValue());
                    }

                    Vector<Vector<Object>> data = new Vector<>();
                    int rowLimit = Math.min(sheet.getLastRowNum(), 100); // Limitar a 100 filas para previsualización

                    for (int i = 1; i <= rowLimit; i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }

                        Vector<Object> rowData = new Vector<>();
                        for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                            switch (cell.getCellType()) {
                                case STRING:
                                    rowData.add(cell.getStringCellValue());
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        rowData.add(cell.getDateCellValue());
                                    } else {
                                        rowData.add(cell.getNumericCellValue());
                                    }
                                    break;
                                case BOOLEAN:
                                    rowData.add(cell.getBooleanCellValue());
                                    break;
                                case FORMULA:
                                    rowData.add(cell.getCellFormula());
                                    break;
                                default:
                                    rowData.add("");
                            }
                        }
                        data.add(rowData);
                    }

                    // Actualizar tabla en el EDT
                    SwingUtilities.invokeLater(() -> {
                        tableModel.setDataVector(data, columnNames);
                        resaltarColumnaCodigo();
                        resizeTableColumns();
                        progressBar.setVisible(false);
                    });

                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PanelImportarExcel.this,
                                "Error: " + e.getMessage(),
                                "Error al cargar Excel", JOptionPane.ERROR_MESSAGE);
                        progressBar.setVisible(false);
                    });
                }
                return null;
            }
        }.execute();
    }

    private void resaltarColumnaCodigo() {
        // Resaltar la columna de código de barras
        if (columnaCodigoBarras >= 0) {
            TableColumnModel columnModel = tablaPreview.getColumnModel();
            TableColumn column = columnModel.getColumn(columnaCodigoBarras);

            // Configurar renderizador personalizado
            column.setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    String codigo = value != null ? value.toString() : "";
                    if (codigo.isEmpty()) {
                        c.setBackground(new Color(255, 230, 230)); // Rojo claro para vacíos
                        setText("SIN CÓDIGO");
                        setForeground(Color.RED);
                    } else {
                        c.setBackground(new Color(230, 255, 230)); // Verde claro para existentes
                        setText(codigo);
                        setForeground(Color.BLACK);
                    }

                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                        setForeground(table.getSelectionForeground());
                    }

                    return c;
                }
            });
        }
    }

    private void resizeTableColumns() {
        for (int column = 0; column < tablaPreview.getColumnCount(); column++) {
            TableColumn tableColumn = tablaPreview.getColumnModel().getColumn(column);
            int preferredWidth = 80; // Ancho mínimo

            // Calcular ancho máximo del contenido de la columna
            for (int row = 0; row < tablaPreview.getRowCount(); row++) {
                TableCellRenderer cellRenderer = tablaPreview.getCellRenderer(row, column);
                Component comp = tablaPreview.prepareRenderer(cellRenderer, row, column);
                int width = comp.getPreferredSize().width + tablaPreview.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
            }

            // Limitar el ancho máximo
            preferredWidth = Math.min(preferredWidth, 300);
            tableColumn.setPreferredWidth(preferredWidth);
        }
    }

    private void procesarArchivoExcel() {
        if (workbook == null || sheet == null) {
            JOptionPane.showMessageDialog(this,
                    "Primero seleccione un archivo Excel",
                    "Archivo no cargado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Generando códigos...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                int contadorGenerados = 0;

                // 🔒 Guardar códigos ya existentes para evitar duplicados
                Set<String> codigosExistentes = new HashSet<>();

                // Paso 1: recorrer todas las filas y guardar códigos que ya existen
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }

                    Cell cell = row.getCell(columnaCodigoBarras);
                    if (cell != null && cell.getCellType() == CellType.STRING) {
                        String codigoExistente = cell.getStringCellValue().trim();
                        if (!codigoExistente.isEmpty()) {
                            codigosExistentes.add(codigoExistente);
                        }
                    }
                }

                // Paso 2: generar nuevos códigos donde falten, evitando duplicados
                try {
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }

                        Cell codigoCell = row.getCell(columnaCodigoBarras);
                        String codigo = "";

                        boolean necesitaCodigo = codigoCell == null
                                || codigoCell.getCellType() == CellType.BLANK
                                || (codigoCell.getCellType() == CellType.STRING
                                && codigoCell.getStringCellValue().trim().isEmpty());

                        if (necesitaCodigo) {
                            // Generar código único
                            String formato = cmbFormato.getSelectedItem().toString();
                            do {
                                if ("EAN_13".equals(formato)) {
                                    codigo = GeneradorService.generarEAN13();
                                } else {
                                    codigo = GeneradorService.generarCodigoUnico();
                                }
                            } while (codigosExistentes.contains(codigo)); // Repetir si ya existe

                            codigosExistentes.add(codigo); // Añadir para evitar duplicados siguientes

                            if (codigoCell == null) {
                                codigoCell = row.createCell(columnaCodigoBarras);
                            }
                            codigoCell.setCellValue(codigo);
                            contadorGenerados++;

                            // Actualizar tabla si es visible (solo primeras 100 filas)
                            if (i <= 100) {
                                String codigoFinal = codigo;
                                int filaFinal = i;
                                int columnaFinal = columnaCodigoBarras;

                                SwingUtilities.invokeLater(() -> {
                                    tableModel.setValueAt(codigoFinal, filaFinal - 1, columnaFinal);
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PanelImportarExcel.this,
                                "Error: " + e.getMessage(),
                                "Error al procesar Excel", JOptionPane.ERROR_MESSAGE);
                    });
                    return -1;
                }

                return contadorGenerados;
            }

            @Override
            protected void done() {
                try {
                    int contadorGenerados = get();
                    if (contadorGenerados >= 0) {
                        JOptionPane.showMessageDialog(PanelImportarExcel.this,
                                "Archivo procesado exitosamente!\n"
                                + "Códigos generados: " + contadorGenerados,
                                "Proceso Completado", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PanelImportarExcel.this,
                            "Error: " + e.getMessage(),
                            "Error al procesar Excel", JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setVisible(false);
                }
            }
        }.execute();
    }

    private void guardarCambios() {
        if (workbook == null || archivoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay cambios para guardar",
                    "Archivo no modificado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo Excel");
        fileChooser.setSelectedFile(archivoSeleccionado);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Archivos Excel", "xlsx", "xls");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Guardando cambios...");

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    try {
                        FileOutputStream outputStream = new FileOutputStream(fileToSave);
                        workbook.write(outputStream);
                        workbook.close();
                        outputStream.close();

                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(PanelImportarExcel.this,
                                    "Archivo guardado exitosamente en:\n" + fileToSave.getAbsolutePath(),
                                    "Guardado Completado", JOptionPane.INFORMATION_MESSAGE);
                        });

                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(PanelImportarExcel.this,
                                    "Error: " + e.getMessage(),
                                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                    return null;
                }

                @Override
                protected void done() {
                    progressBar.setVisible(false);
                }
            }.execute();
        }
    }
}
