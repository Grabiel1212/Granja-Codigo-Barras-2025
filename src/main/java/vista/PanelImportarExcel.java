package vista;

import service.GeneradorService;
import service.ImportadorService;
import model.ProductoExcel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import util.ConectarBD;
import helpers.ValidadorEAN13;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;
import service.ImportResult;

public class PanelImportarExcel extends JDialog {

    private final GeneradorGU parent;
    private JProgressBar progressBar;
    private JComboBox<String> cmbFormato;
    private JTable tablaPreview;
    private DefaultTableModel tableModel;
    private Workbook workbook;
    private Sheet sheet;
    private int columnaCodigoBarras = -1;
    private int columnaNombre = -1;
    private File archivoSeleccionado;
    private Set<String> codigosExistentesBD = new HashSet<>();
    private String nombreArchivoActual;
    private ImportadorService importadorService = new ImportadorService();
    private JComboBox<String> cmbHojas; // Cambiado de tabla a combo box

    private static class ColumnasHoja {

        int colCodigo;
        int colNombre;

        ColumnasHoja(int colCodigo, int colNombre) {
            this.colCodigo = colCodigo;
            this.colNombre = colNombre;
        }
    }

    public PanelImportarExcel(GeneradorGU parent) {
        super(parent, "Importar desde Excel", true);
        this.parent = parent;
        setSize(900, 700);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        // Configurar colores profesionales
        java.awt.Color colorFondo = new java.awt.Color(245, 245, 245);
        java.awt.Color colorPrimario = new java.awt.Color(41, 128, 185);
        java.awt.Color colorBorde = new java.awt.Color(200, 200, 200);
        java.awt.Color colorTexto = new java.awt.Color(50, 50, 50);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(colorFondo);

        // Título con estilo profesional
        JLabel lblTitulo = new JLabel("Importar desde Excel", SwingConstants.CENTER);
        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));

        lblTitulo.setForeground(colorPrimario);
        lblTitulo.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(lblTitulo, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(colorFondo);

        // Panel de configuración con borde elegante
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBackground(java.awt.Color.WHITE);

        configPanel.setBorder(new CompoundBorder(
                new LineBorder(colorBorde, 1),
                new EmptyBorder(10, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 10, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Sección de selección de hojas
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblHojas = new JLabel("Seleccione una hoja:");
        lblHojas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lblHojas.setForeground(colorTexto);
        configPanel.add(lblHojas, gbc);

        // Cambiamos a combo box para selección única
        cmbHojas = new JComboBox<>();
        cmbHojas.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        cmbHojas.setPreferredSize(new Dimension(300, 30));
        gbc.gridy = 1;
        configPanel.add(cmbHojas, gbc);

        // Listener para cambiar la previsualización al seleccionar hoja
        cmbHojas.addActionListener(e -> {
            if (cmbHojas.getSelectedItem() != null && workbook != null) {
                cargarDatosHoja((String) cmbHojas.getSelectedItem());
            }
        });

        // Sección de formato
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel lblFormato = new JLabel("Formato de código:");
        lblFormato.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        configPanel.add(lblFormato, gbc);

        cmbFormato = new JComboBox<>(new String[]{"EAN_13"});
        cmbFormato.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        cmbFormato.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1;
        configPanel.add(cmbFormato, gbc);

        // Botón de selección con estilo moderno
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        JButton btnSeleccionar = new JButton("Seleccionar Archivo Excel");
        btnSeleccionar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        btnSeleccionar.setBackground(colorPrimario);
        btnSeleccionar.setForeground(java.awt.Color.WHITE);
        btnSeleccionar.setFocusPainted(false);
        btnSeleccionar.setOpaque(true);
        btnSeleccionar.setContentAreaFilled(true);
        btnSeleccionar.setBorderPainted(false);
        btnSeleccionar.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnSeleccionar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSeleccionar.addActionListener(e -> seleccionarArchivoExcel());

        // Efecto hover para el botón
        btnSeleccionar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSeleccionar.setBackground(colorPrimario.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnSeleccionar.setBackground(colorPrimario);
            }
        });
        configPanel.add(btnSeleccionar, gbc);

        contentPanel.add(configPanel, BorderLayout.NORTH);

        // Panel de previsualización con diseño profesional
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(colorFondo);
        previewPanel.setBorder(new CompoundBorder(
                new EmptyBorder(10, 0, 0, 0),
                new TitledBorder(
                        new LineBorder(colorBorde, 1),
                        "Previsualización de datos",
                        TitledBorder.LEADING,
                        TitledBorder.TOP,
                        new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14),
                        colorTexto
                )
        ));

        tableModel = new DefaultTableModel();
        tablaPreview = new JTable(tableModel);
        tablaPreview.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        tablaPreview.setRowHeight(28);
        tablaPreview.setGridColor(new java.awt.Color(220, 220, 220));
        tablaPreview.setBackground(java.awt.Color.WHITE);
        tablaPreview.setForeground(colorTexto);
        tablaPreview.setSelectionBackground(new java.awt.Color(52, 152, 219, 100));
        tablaPreview.setSelectionForeground(colorTexto);
        tablaPreview.setShowVerticalLines(true);
        tablaPreview.setShowHorizontalLines(true);

        // Cabecera de tabla profesional
        JTableHeader header = tablaPreview.getTableHeader();
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        header.setBackground(colorPrimario);
        header.setForeground(java.awt.Color.WHITE);
        header.setOpaque(true);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
                label.setBackground(colorPrimario);
                label.setForeground(java.awt.Color.WHITE);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaPreview);
        scrollPane.setBorder(BorderFactory.createLineBorder(colorBorde));
        previewPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(previewPanel, BorderLayout.CENTER);

        // Panel de botones con diseño moderno
        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        actionPanel.setBackground(colorFondo);
        actionPanel.setBorder(new EmptyBorder(20, 0, 10, 0));

        // Botón Procesar
        JButton btnProcesar = crearBotonAccion("Generar Códigos", new java.awt.Color(46, 204, 113));
        btnProcesar.addActionListener(e -> procesarArchivoExcel());
        actionPanel.add(btnProcesar);

        // Botón Guardar
        JButton btnGuardar = crearBotonAccion("Guardar Cambios", colorPrimario);
        btnGuardar.addActionListener(e -> guardarCambios());
        actionPanel.add(btnGuardar);

        // Botón Cerrar
        JButton btnCerrar = crearBotonAccion("Cerrar", new java.awt.Color(231, 76, 60));
        btnCerrar.addActionListener(e -> dispose());
        actionPanel.add(btnCerrar);

        contentPanel.add(actionPanel, BorderLayout.SOUTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Barra de progreso con estilo
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        progressBar.setBorder(new EmptyBorder(5, 0, 0, 0));
        progressBar.setBackground(new java.awt.Color(230, 230, 230));
        progressBar.setForeground(colorPrimario);
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JButton crearBotonAccion(String texto, java.awt.Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        boton.setBackground(color);
        boton.setForeground(java.awt.Color.WHITE);
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setBorder(new EmptyBorder(12, 20, 12, 20));

        return boton;
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
        nombreArchivoActual = archivo.getName();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                importadorService.actualizarCache();

                try (FileInputStream inputStream = new FileInputStream(archivo)) {
                    workbook = archivo.getName().endsWith(".xlsx")
                            ? new XSSFWorkbook(inputStream)
                            : new HSSFWorkbook(inputStream);

                    // Cargar nombres de hojas en el combo
                    SwingUtilities.invokeLater(() -> {
                        cmbHojas.removeAllItems();
                        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                            cmbHojas.addItem(workbook.getSheetName(i));
                        }
                        // Seleccionar la primera hoja por defecto
                        if (workbook.getNumberOfSheets() > 0) {
                            cmbHojas.setSelectedIndex(0);
                        }
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

    // Nuevo método para cargar datos de una hoja específica
    private void cargarDatosHoja(String nombreHoja) {
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Cargando hoja...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    sheet = workbook.getSheet(nombreHoja);
                    if (sheet == null) {
                        throw new Exception("Hoja no encontrada: " + nombreHoja);
                    }

                    columnaCodigoBarras = -1;
                    columnaNombre = -1;

                    // Leer fila de encabezado
                    Row headerRow = sheet.getRow(0);
                    if (headerRow == null) {
                        throw new Exception("La hoja está vacía.");
                    }

                    // Buscar columnas de interés
                    for (Cell cell : headerRow) {
                        String header = cell.getStringCellValue().trim().toLowerCase();
                        if (header.contains("código") || header.contains("codigo")) {
                            columnaCodigoBarras = cell.getColumnIndex();
                        } else if (header.contains("producto") || header.contains("descripcion")) {
                            columnaNombre = cell.getColumnIndex();
                        }
                    }

                    if (columnaCodigoBarras == -1) {
                        throw new Exception("No se encontró la columna 'Código Barras'");
                    }
                    if (columnaNombre == -1) {
                        throw new Exception("No se encontró la columna 'Nombre' o 'Producto'");
                    }

                    // Preparar nombres de columnas
                    Vector<String> columnNames = new Vector<>();
                    for (Cell cell : headerRow) {
                        columnNames.add(cell.getStringCellValue());
                    }

                    // Leer datos del Excel (máx. 10000 filas para previsualización)
                    Vector<Vector<Object>> data = new Vector<>();
                    int rowLimit = Math.min(sheet.getLastRowNum(), 10000);

                    for (int i = 1; i <= rowLimit; i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }

                        Vector<Object> rowData = new Vector<>();
                        for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            switch (cell.getCellType()) {
                                case STRING ->
                                    rowData.add(cell.getStringCellValue());
                                case NUMERIC ->
                                    rowData.add(DateUtil.isCellDateFormatted(cell)
                                            ? cell.getDateCellValue() : cell.getNumericCellValue());
                                case BOOLEAN ->
                                    rowData.add(cell.getBooleanCellValue());
                                case FORMULA ->
                                    rowData.add(cell.getCellFormula());
                                default ->
                                    rowData.add("");
                            }
                        }
                        data.add(rowData);
                    }

                    // Cargar códigos ya existentes de la BD (si necesario)
                    cargarCodigosExistentesBD();

                    // Actualizar la tabla en el EDT
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
                                "Error al cargar hoja", JOptionPane.ERROR_MESSAGE);
                        progressBar.setVisible(false);
                    });
                }
                return null;
            }
        }.execute();
    }

// Método para buscar columnas en una hoja
    private ColumnasHoja buscarColumnas(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            return null;
        }

        int colCodigo = -1;
        int colNombre = -1;

        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue().trim().toLowerCase();
            if (header.contains("código") || header.contains("codigo")) {
                colCodigo = cell.getColumnIndex();
            } else if (header.contains("producto") || header.contains("descripcion")) {
                colNombre = cell.getColumnIndex();
            }
        }

        if (colCodigo == -1 || colNombre == -1) {
            return null;
        }

        return new ColumnasHoja(colCodigo, colNombre);
    }

    private void cargarCodigosExistentesBD() {
        Connection conn = null;
        try {
            conn = ConectarBD.obtenerConexion();
            String sql = "SELECT Codigo FROM CodigosGenerados";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    codigosExistentesBD.add(rs.getString("Codigo"));
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar códigos existentes: " + e.getMessage(),
                    "Error de base de datos", JOptionPane.ERROR_MESSAGE);
        } finally {
            ConectarBD.cerrarConexion(conn);
        }
    }

    private String obtenerValorCelda(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private void resaltarColumnaCodigo() {
        if (columnaCodigoBarras >= 0) {
            TableColumnModel columnModel = tablaPreview.getColumnModel();
            TableColumn column = columnModel.getColumn(columnaCodigoBarras);

            column.setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    String codigo = value != null ? value.toString() : "";
                    if (codigo.isEmpty()) {
                        c.setBackground(new java.awt.Color(255, 230, 230));

                        setText("SIN CÓDIGO");
                        setForeground(java.awt.Color.RED);
                    } else {
                        c.setBackground(new java.awt.Color(230, 255, 230));
                        setText(codigo);
                        setForeground(java.awt.Color.BLACK);
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
            int preferredWidth = 80;

            for (int row = 0; row < tablaPreview.getRowCount(); row++) {
                TableCellRenderer cellRenderer = tablaPreview.getCellRenderer(row, column);
                Component comp = tablaPreview.prepareRenderer(cellRenderer, row, column);
                int width = comp.getPreferredSize().width + tablaPreview.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
            }

            preferredWidth = Math.min(preferredWidth, 300);
            tableColumn.setPreferredWidth(preferredWidth);
        }
    }

    private void procesarArchivoExcel() {
        if (workbook == null) {
            JOptionPane.showMessageDialog(this,
                    "Primero seleccione un archivo Excel",
                    "Archivo no cargado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener la hoja seleccionada
        String nombreHoja = (String) cmbHojas.getSelectedItem();
        if (nombreHoja == null) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una hoja",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Generando códigos...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() {
                importadorService.actualizarCache();
                int contadorGenerados = 0;
                Set<String> codigosEnArchivo = new HashSet<>();

                try {
                    Sheet sheet = workbook.getSheet(nombreHoja);
                    if (sheet == null) {
                        throw new Exception("Hoja no encontrada: " + nombreHoja);
                    }

                    ColumnasHoja columnas = buscarColumnas(sheet);
                    if (columnas == null) {
                        throw new Exception("La hoja no tiene las columnas requeridas");
                    }

                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) {
                            continue;
                        }

                        // Verificar si la fila tiene datos
                        Cell nombreCell = row.getCell(columnas.colNombre);
                        String nombreActual = obtenerValorCelda(nombreCell);

                        // Si el nombre está vacío, considerar fila vacía y saltar
                        if (nombreActual == null || nombreActual.trim().isEmpty()) {
                            continue;
                        }

                        Cell codigoCell = row.getCell(columnas.colCodigo);
                        String codigoActual = obtenerValorCelda(codigoCell);

                        if (codigoActual.isEmpty()) {
                            String formato = cmbFormato.getSelectedItem().toString();
                            String nuevoCodigo;
                            boolean esUnico;

                            do {
                                if ("EAN_13".equals(formato)) {
                                    nuevoCodigo = GeneradorService.generarEAN13();
                                } else {
                                    nuevoCodigo = GeneradorService.generarCodigoUnico();
                                }

                                esUnico = !codigosExistentesBD.contains(nuevoCodigo)
                                        && !codigosEnArchivo.contains(nuevoCodigo);

                            } while (!esUnico);

                            if (codigoCell == null) {
                                codigoCell = row.createCell(columnas.colCodigo);
                            }
                            codigoCell.setCellValue(nuevoCodigo);
                            codigosEnArchivo.add(nuevoCodigo);
                            contadorGenerados++;

                            // Actualizar previsualización si es visible
                            if (i <= 100) {
                                final String codigoFinal = nuevoCodigo;
                                final int rowIndex = i;
                                SwingUtilities.invokeLater(() -> {
                                    tableModel.setValueAt(codigoFinal, rowIndex - 1, columnas.colCodigo);
                                });
                            }
                        } else {
                            codigosEnArchivo.add(codigoActual);
                        }
                    }
                    return contadorGenerados;
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(PanelImportarExcel.this,
                                "Error: " + e.getMessage(),
                                "Error al procesar Excel", JOptionPane.ERROR_MESSAGE);
                    });
                    return -1;
                }
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
                        // Guardar archivo Excel
                        FileOutputStream outputStream = new FileOutputStream(fileToSave);
                        workbook.write(outputStream);
                        workbook.close();
                        outputStream.close();

                        // Guardar datos en base de datos
                        guardarEnBaseDatos();

                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(PanelImportarExcel.this,
                                    "Archivo guardado exitosamente en:\n" + fileToSave.getAbsolutePath()
                                    + "\nDatos guardados en base de datos",
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

    private void guardarEnBaseDatos() {
        List<ProductoExcel> productos = new ArrayList<>();

        // Obtener la hoja seleccionada
        String nombreHoja = (String) cmbHojas.getSelectedItem();
        if (nombreHoja == null) {
            return; // No hay hoja seleccionada
        }

        Sheet sheet = workbook.getSheet(nombreHoja);
        if (sheet == null) {
            return;
        }

        ColumnasHoja columnas = buscarColumnas(sheet);
        if (columnas == null) {
            return;
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            // Verificar si la fila tiene datos
            Cell nombreCell = row.getCell(columnas.colNombre);
            String nombre = obtenerValorCelda(nombreCell);

            // Saltar filas vacías
            if (nombre == null || nombre.trim().isEmpty()) {
                continue;
            }

            Cell codigoCell = row.getCell(columnas.colCodigo);
            String codigo = obtenerValorCelda(codigoCell);

            if (!nombre.isEmpty() && !codigo.isEmpty()) {
                productos.add(new ProductoExcel(nombre, codigo));
            }
        }

        // Importar en base de datos
        ImportResult resultado = importadorService.importarProductosDesdeExcel(nombreArchivoActual, productos);

        // Actualizar caché
        importadorService.actualizarCache();

        // Mostrar mensaje con errores si los hay
        if (!resultado.getErrores().isEmpty()) {
            StringBuilder errores = new StringBuilder();
            for (String error : resultado.getErrores()) {
                errores.append("• ").append(error).append("\n");
            }

            JOptionPane.showMessageDialog(this,
                    "Se importaron " + resultado.getProductosImportados() + " productos\n"
                    + "Errores:\n" + errores.toString(),
                    "Importación con errores", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Se importaron " + resultado.getProductosImportados() + " productos correctamente.",
                    "Importación exitosa", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
