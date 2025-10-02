package vista;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import model.Producto;
import service.GeneradorService;
import helpers.DottedLineSeparator;
import helpers.RoundedBorder;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import com.itextpdf.text.Rectangle;
import java.awt.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Image;
import java.io.ByteArrayOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class GeneradorGU extends JFrame {

    // Paleta de colores moderna
    public static final Color COLOR_FONDO = new Color(245, 247, 250); // ejemplo

    public static final Color COLOR_PRIMARIO = new Color(41, 128, 185);
    public static final Color COLOR_SECUNDARIO = new Color(52, 152, 219);
    public static final Color COLOR_ACENTO = new Color(231, 76, 60);
    private static final Color COLOR_TEXTO = new Color(44, 62, 80);
    public static final Color COLOR_BORDE = new Color(189, 195, 199);
    private static final Color COLOR_CARD = new Color(255, 255, 255);

    private JTextField txtCodigo;
    private JTextField txtNombreProducto;
    private JComboBox<String> cmbFormato;
    private JLabel lblImagen;
    private JLabel lblNumeroCodigo;
    private JLabel lblNombrePreview;
    private BufferedImage imagenCodigo;
    private String ultimoCodigoGenerado;
    private JProgressBar progressBar;
    private JButton btnGuardarProducto;
    private JButton btnVerLista;
    private JButton btnImportarExcel;
    private JButton btnImportarCodigo;
    private JTextField txtPrecio; // Agregar esta variable
    private JLabel lblPrecioPreview; // Agregar esta variable

    private List<Producto> listaProductos = new ArrayList<>();

    // Configuración de impresión para impresora térmica
    private static final float PAPEL_ANCHO_MM = 72f; // Área imprimible de 72mm
    private static final float MARGEN_IZQ_MM = 4f;
    private static final float MARGEN_DER_MM = 4f;
    private static final float MARGEN_SUP_MM = 5f;
    private static final float MARGEN_INF_MM = 5f;

    public GeneradorGU() {
        super("Generador de Códigos de Barras");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 800);
        setMinimumSize(new Dimension(750, 700));
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);
        setLocationRelativeTo(null);

        // Configuración de fuente global
        Font fuentePrincipal = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("Label.font", fuentePrincipal);
        UIManager.put("Button.font", fuentePrincipal);
        UIManager.put("ComboBox.font", fuentePrincipal);
        UIManager.put("TextField.font", fuentePrincipal);

        // Panel principal con borde redondeado
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(COLOR_CARD);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(COLOR_FONDO);
        add(mainPanel, BorderLayout.CENTER);

        // Encabezado moderno con gradiente
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo con gradiente
                GradientPaint gradient = new GradientPaint(
                        0, 0, COLOR_PRIMARIO,
                        getWidth(), 0, COLOR_SECUNDARIO
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Borde sutil
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        headerPanel.setPreferredSize(new Dimension(100, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        headerPanel.setOpaque(false);

        // Título con sombra
        JLabel lblTitulo = new JLabel("Generador de Códigos de Barras", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("UTILMARQUET S.A.C", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitulo.setForeground(new Color(240, 240, 240));

        headerPanel.add(lblTitulo, BorderLayout.CENTER);
        headerPanel.add(lblSubtitulo, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel de contenido principal
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);

        // Panel de controles con borde redondeado
        JPanel panelControles = crearPanelControles();
        contentPanel.add(panelControles, BorderLayout.NORTH);

        // Panel de visualización con borde redondeado
        JPanel panelVisualizacion = crearPanelVisualizacion();
        contentPanel.add(panelVisualizacion, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = crearPanelBotones();
        contentPanel.add(panelBotones, BorderLayout.SOUTH);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Barra de estado en la parte inferior
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDE));
        statusPanel.setBackground(COLOR_CARD);
        statusPanel.setPreferredSize(new Dimension(100, 30));

        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);

        JLabel lblStatus = new JLabel("Listo");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        statusPanel.add(lblStatus, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Hacer la ventana redimensionable
        setResizable(true);

        // Centrar ventana después de empaquetar
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel crearPanelControles() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Título de sección
        JLabel lblSeccion = new JLabel("Configuración del Código");
        lblSeccion.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSeccion.setForeground(COLOR_TEXTO);
        lblSeccion.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(lblSeccion, gbc);

        // Configuración de GridBagConstraints
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Campo de nombre del producto
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Nombre Producto:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtNombreProducto = new JTextField();
        txtNombreProducto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNombreProducto.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.add(txtNombreProducto, gbc);

        // Campo de código
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Código:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtCodigo = new JTextField();
        txtCodigo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtCodigo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.add(txtCodigo, gbc);

        // Botón generar nuevo
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton btnGenerarCodigo = crearBoton("Generar Nuevo", e -> generarNuevoCodigo());
        btnGenerarCodigo.setBackground(COLOR_ACENTO);
        panel.add(btnGenerarCodigo, gbc);

        // Campo de precio (NUEVO)
        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Precio (S/):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPrecio = new JTextField();
        txtPrecio.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPrecio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        // Placeholder para indicar el formato
        txtPrecio.setToolTipText("Ejemplo: 25.50");
        panel.add(txtPrecio, gbc);

        // Combo formato (se mueve a la siguiente fila)
        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(new JLabel("Formato:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbFormato = new JComboBox<>(new String[]{"CODE_128", "QR_CODE", "EAN_13"});
        cmbFormato.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbFormato.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
        panel.add(cmbFormato, gbc);

        // Botón de ayuda (se mueve a la siguiente fila)
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        JButton btnAyuda = crearBoton("Ayuda", e -> mostrarAyuda());
        btnAyuda.setBackground(new Color(149, 165, 166));
        panel.add(btnAyuda, gbc);
        // Botón para guardar producto (se mueve a la siguiente fila)
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        btnGuardarProducto = crearBoton("Guardar Producto", e -> guardarProducto());
        btnGuardarProducto.setBackground(new Color(46, 204, 113)); // Verde
        btnGuardarProducto.setEnabled(false);
        panel.add(btnGuardarProducto, gbc);
        return panel;
    }

    private JPanel crearPanelVisualizacion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(15, COLOR_BORDE),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Título de sección
        JLabel lblSeccion = new JLabel("Vista Previa del Código");
        lblSeccion.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSeccion.setForeground(COLOR_TEXTO);
        lblSeccion.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(lblSeccion, BorderLayout.NORTH);

        // Panel para imagen con desplazamiento
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        // Panel para imagen
        JPanel panelImagen = new JPanel(new BorderLayout());
        panelImagen.setBackground(Color.WHITE);
        panelImagen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Etiqueta para nombre del producto
        lblNombrePreview = new JLabel(" ", JLabel.CENTER);
        lblNombrePreview.setFont(new Font("Arial", Font.BOLD, 18));
        lblNombrePreview.setForeground(COLOR_PRIMARIO);
        lblNombrePreview.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panelImagen.add(lblNombrePreview, BorderLayout.NORTH);

        // Etiqueta para imagen
        lblImagen = new JLabel("", SwingConstants.CENTER);
        lblImagen.setVerticalAlignment(SwingConstants.CENTER);

        // Mensaje inicial integrado en la misma etiqueta
        lblImagen.setText("<html><div style='text-align: center;'>"
                + "<b>¡Bienvenido al Generador de Códigos de Barras!</b><br><br>"
                + "Para comenzar, ingresa un código o genera uno nuevo<br>"
                + "y luego haz clic en 'Generar Imagen'</div></html>");
        lblImagen.setHorizontalTextPosition(JLabel.CENTER);
        lblImagen.setVerticalTextPosition(JLabel.CENTER);
        panelImagen.add(lblImagen, BorderLayout.CENTER);

        // Panel sur para precio y código
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);

        // Label para mostrar el precio (NUEVO)
        lblPrecioPreview = new JLabel(" ", JLabel.CENTER);
        lblPrecioPreview.setFont(new Font("Arial", Font.BOLD, 20));
        lblPrecioPreview.setForeground(new Color(231, 76, 60)); // Color rojo llamativo
        lblPrecioPreview.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        southPanel.add(lblPrecioPreview, BorderLayout.NORTH);

        // Label para mostrar el número del código
        lblNumeroCodigo = new JLabel(" ", JLabel.CENTER);
        lblNumeroCodigo.setFont(new Font("Arial", Font.BOLD, 18));
        lblNumeroCodigo.setForeground(COLOR_TEXTO);
        lblNumeroCodigo.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        southPanel.add(lblNumeroCodigo, BorderLayout.SOUTH);

        panelImagen.add(southPanel, BorderLayout.SOUTH);

        scrollPane.setViewportView(panelImagen);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Botón para importar Excel
        btnImportarExcel = crearBoton("Importar Excel", e -> abrirPanelImportarExcel());
        btnImportarExcel.setBackground(new Color(241, 196, 15)); // Amarillo
        panel.add(btnImportarExcel);

        btnImportarCodigo = crearBoton("Importar Codigo", e -> abrirPanelImportarCodigo());
        btnImportarCodigo.setBackground(new Color(241, 196, 15));
        panel.add(btnImportarCodigo);

        // Botón generar imagen
        JButton btnGenerarImagen = crearBoton("Generar Imagen", e -> generarImagen());
        btnGenerarImagen.setBackground(COLOR_PRIMARIO);
        panel.add(btnGenerarImagen);

        // Botón para guardar la imagen
        JButton btnGuardar = crearBoton("Descargar Imagen", e -> guardarImagen());
        btnGuardar.setBackground(COLOR_SECUNDARIO);
        panel.add(btnGuardar);

        // Botón para ver lista de productos
        btnVerLista = crearBoton("Ver Lista (" + listaProductos.size() + ")", e -> mostrarListaProductos());
        btnVerLista.setBackground(new Color(155, 89, 182)); // Morado
        panel.add(btnVerLista);

        // Botón para salir
        JButton btnSalir = crearBoton("Salir", e -> System.exit(0));
        btnSalir.setBackground(new Color(149, 165, 166));
        panel.add(btnSalir);

        return panel;
    }

    private JButton crearBoton(String texto, ActionListener action) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color baseColor = getBackground();
                if (getModel().isPressed()) {
                    g2.setColor(baseColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        boton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        boton.setForeground(Color.WHITE);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setOpaque(false);
        boton.setPreferredSize(new Dimension(170, 45));

        // Sombra de texto para mejorar visibilidad
        boton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                g2.setFont(b.getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = b.getText();
                int x = (b.getWidth() - fm.stringWidth(text)) / 2;
                int y = (b.getHeight() + fm.getAscent()) / 2 - 3;

                // Sombra
                g2.setColor(new Color(0, 0, 0, 80));
                g2.drawString(text, x + 1, y + 1);

                // Texto principal
                g2.setColor(b.getForeground());
                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        boton.addActionListener(action);

        // Efecto cursor hover
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boton.setCursor(Cursor.getDefaultCursor());
            }
        });

        return boton;
    }

    private void abrirPanelImportarExcel() {
        new PanelImportarExcel(this).setVisible(true);
    }
    // Nuevos métodos

    private void abrirPanelImportarCodigo() {
        new PanelImportarCodigo(this).setVisible(true);
    }

    private void mostrarAyuda() {
        String mensaje = "<html><div style='width: 300px; text-align: center;'>"
                + "<h2>Ayuda Rápida</h2>"
                + "<p><b>1. Nombre Producto</b><br>"
                + "Ingrese el nombre del producto</p>"
                + "<p><b>2. Generar Nuevo</b><br>"
                + "Crea un nuevo código único o EAN-13</p>"
                + "<p><b>3. Generar Imagen</b><br>"
                + "Muestra una vista previa del código</p>"
                + "<p><b>4. Guardar Producto</b><br>"
                + "Almacena el producto para posterior impresión</p>"
                + "<p><b>5. Ver Lista</b><br>"
                + "Muestra todos los productos guardados</p>"
                + "<p><b>6. Descargar Imagen</b><br>"
                + "Guarda el código como imagen PNG</p>"
                + "<p><b>Formatos:</b><br>"
                + "- <b>CODE_128</b>: Códigos alfanuméricos<br>"
                + "- <b>QR_CODE</b>: Códigos QR<br>"
                + "- <b>EAN_13</b>: Códigos de producto</div></html>";

        JOptionPane.showMessageDialog(this, mensaje, "Ayuda del Sistema",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void generarNuevoCodigo() {
        String formato = cmbFormato.getSelectedItem().toString();

        if (formato.equals("EAN_13")) {
            ultimoCodigoGenerado = GeneradorService.generarEAN13();
        } else {
            ultimoCodigoGenerado = GeneradorService.generarCodigoUnico();
        }

        txtCodigo.setText(ultimoCodigoGenerado);
    }

    private void generarImagen() {
        try {
            String nombre = txtNombreProducto.getText();
            String codigo = txtCodigo.getText();
            String precio = txtPrecio.getText().trim(); // Obtener precio

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "<html><div style='text-align:center;'>Por favor, ingrese un nombre<br>para el producto</div></html>",
                        "Nombre vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (codigo.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "<html><div style='text-align:center;'>Por favor, ingrese un código<br>o genere uno nuevo</div></html>",
                        "Código vacío", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ultimoCodigoGenerado = codigo;

            BarcodeFormat formato;
            switch (cmbFormato.getSelectedItem().toString()) {
                case "QR_CODE":
                    formato = BarcodeFormat.QR_CODE;
                    break;
                case "EAN_13":
                    formato = BarcodeFormat.EAN_13;
                    break;
                default:
                    formato = BarcodeFormat.CODE_128;
            }

            // Simular progreso
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Generando imagen...");

            // Usar un hilo separado para la generación
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    GeneradorService service = new GeneradorService();
                    imagenCodigo = service.generarImagenCodigo(
                            codigo,
                            formato,
                            350,
                            formato == BarcodeFormat.QR_CODE ? 350 : 120
                    );

                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        // Mostrar el nombre del producto
                        lblNombrePreview.setText(nombre);

                        // Mostrar el precio formateado (NUEVO)
                        if (!precio.isEmpty()) {
                            lblPrecioPreview.setText("S/ " + precio);
                        } else {
                            lblPrecioPreview.setText("");
                        }

                        // Mostrar la imagen en el JLabel
                        lblImagen.setIcon(new ImageIcon(imagenCodigo));
                        // Limpiar el mensaje inicial
                        lblImagen.setText("");
                        // Mostrar el número debajo del código
                        lblNumeroCodigo.setText(codigo);

                        // Habilitar botón de guardar producto
                        btnGuardarProducto.setEnabled(true);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(GeneradorGU.this,
                                "<html><div style='text-align:center;'>Error: " + e.getMessage() + "</div></html>",
                                "Error de generación", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        progressBar.setVisible(false);
                    }
                }
            }.execute();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "<html><div style='text-align:center;'>Error: " + e.getMessage() + "</div></html>",
                    "Error de generación", JOptionPane.ERROR_MESSAGE);
            progressBar.setVisible(false);
        }
    }

    public void agregarProductosImportados(List<Producto> productos) {
        listaProductos.addAll(productos);
        btnVerLista.setText("Ver Lista (" + listaProductos.size() + ")");

        JOptionPane.showMessageDialog(this,
                "Se importaron " + productos.size() + " productos exitosamente!",
                "Importación Exitosa", JOptionPane.INFORMATION_MESSAGE);
    }

    private void guardarProducto() {
        String nombre = txtNombreProducto.getText().trim();
        String codigo = txtCodigo.getText().trim();
        String precio = txtPrecio.getText().trim(); // Obtener el precio

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un nombre para el producto", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (imagenCodigo == null) {
            JOptionPane.showMessageDialog(this, "Primero genere una imagen del código de barras", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BarcodeFormat formato;
        switch (cmbFormato.getSelectedItem().toString()) {
            case "QR_CODE":
                formato = BarcodeFormat.QR_CODE;
                break;
            case "EAN_13":
                formato = BarcodeFormat.EAN_13;
                break;
            default:
                formato = BarcodeFormat.CODE_128;
        }

        // Formatear el precio para mostrar S/
        String precioFormateado = precio.isEmpty() ? "" : "S/ " + precio;

        // Agregar producto a la lista con el precio
        Producto nuevoProducto = new Producto(nombre, codigo, imagenCodigo, formato, precioFormateado);
        listaProductos.add(nuevoProducto);

        // Actualizar contador en botón de lista
        btnVerLista.setText("Ver Lista (" + listaProductos.size() + ")");

        // Mostrar mensaje de éxito
        JOptionPane.showMessageDialog(this,
                "<html><div style='text-align:center;'>Producto guardado exitosamente!<br>Total: " + listaProductos.size() + "</div></html>",
                "Producto Guardado", JOptionPane.INFORMATION_MESSAGE);

        // Limpiar campos para el siguiente producto
        txtNombreProducto.setText("");
        txtCodigo.setText("");
        txtPrecio.setText(""); // Limpiar campo de precio
        lblNombrePreview.setText("");
        lblPrecioPreview.setText(""); // Limpiar preview de precio
        lblImagen.setIcon(null);
        lblImagen.setText("<html><div style='text-align: center;'>Producto guardado!<br>Ingrese un nuevo producto</div></html>");
        lblNumeroCodigo.setText("");
        btnGuardarProducto.setEnabled(false);
    }

    public void mostrarListaProductos() {
        if (listaProductos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "<html><div style='text-align:center;'>No hay productos guardados<br>Guarde al menos un producto</div></html>",
                    "Lista Vacía", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Crear un diálogo modal
        JDialog dialog = new JDialog(this, "Productos Guardados", true);
        dialog.setSize(800, 600);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        // Panel de título
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_PRIMARIO);
        JLabel titleLabel = new JLabel("Productos Guardados (" + listaProductos.size() + ")", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        dialog.add(headerPanel, BorderLayout.NORTH);

        // Método para renderizar productos
        Runnable renderProductos = () -> {
            JPanel contentPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 2 columnas
            contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            contentPanel.setBackground(COLOR_FONDO);

            for (Producto producto : listaProductos) {
                JPanel card = new JPanel(new BorderLayout(10, 10));
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_BORDE),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10))
                );

// Panel superior con nombre y precio
                JPanel topPanel = new JPanel(new BorderLayout());
                topPanel.setOpaque(false);

                JLabel nameLabel = new JLabel(producto.nombre, JLabel.CENTER);
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                nameLabel.setForeground(COLOR_TEXTO);

// Mostrar precio si existe (NUEVO)
                if (producto.precio != null && !producto.precio.isEmpty()) {
                    JLabel priceLabel = new JLabel(producto.precio, JLabel.RIGHT);
                    priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    priceLabel.setForeground(new Color(231, 76, 60)); // Rojo llamativo
                    topPanel.add(priceLabel, BorderLayout.EAST);
                }

                topPanel.add(nameLabel, BorderLayout.CENTER);
                card.add(topPanel, BorderLayout.NORTH);

                // Imagen
                JLabel imageLabel = new JLabel(new ImageIcon(producto.imagen), JLabel.CENTER);
                card.add(imageLabel, BorderLayout.CENTER);

                // Panel inferior con código y botón eliminar
                JPanel bottomPanel = new JPanel(new BorderLayout());
                bottomPanel.setOpaque(false);

                JLabel codeLabel = new JLabel(producto.codigo, JLabel.CENTER);
                codeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                codeLabel.setForeground(COLOR_TEXTO);
                bottomPanel.add(codeLabel, BorderLayout.CENTER);

                JButton btnEliminar = new JButton("Eliminar");
                btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btnEliminar.setBackground(new Color(231, 76, 60));
                btnEliminar.setFocusPainted(false);
                btnEliminar.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(dialog,
                            "¿Seguro que quieres eliminar este producto?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        listaProductos.remove(producto);
                        dialog.dispose(); // cerrar para volver a generar
                        mostrarListaProductos(); // recargar
                    }
                });

                bottomPanel.add(btnEliminar, BorderLayout.EAST);
                card.add(bottomPanel, BorderLayout.SOUTH);

                contentPanel.add(card);
            }

            JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.getViewport().setBackground(COLOR_FONDO);
            dialog.add(scrollPane, BorderLayout.CENTER);
        };

        // Cargar productos la primera vez
        renderProductos.run();

        // Panel de botones inferiores
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        buttonPanel.setBackground(COLOR_FONDO);

        // Botón imprimir
        JButton btnImprimir = new JButton("🖨 Imprimir Todos");
        btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnImprimir.setBackground(new Color(40, 180, 99)); // Verde brillante
        btnImprimir.setFocusPainted(false);
        btnImprimir.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        buttonPanel.add(btnImprimir);
        btnImprimir.addActionListener(e -> imprimirProductos());

        // Botón exportar PDF
        JButton btnExportarPDF = new JButton("📄 Exportar a PDF");
        btnExportarPDF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExportarPDF.setBackground(new Color(52, 152, 219)); // Azul cielo
        btnExportarPDF.setFocusPainted(false);
        btnExportarPDF.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        buttonPanel.add(btnExportarPDF);
        btnExportarPDF.addActionListener(e -> exportarAPDF());

        // Botón eliminar todos
        JButton btnEliminarTodos = new JButton("🗑 Eliminar Todos");
        btnEliminarTodos.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEliminarTodos.setBackground(new Color(192, 57, 43)); // Rojo más fuerte
        btnEliminarTodos.setFocusPainted(false);
        btnEliminarTodos.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        buttonPanel.add(btnEliminarTodos);
        btnEliminarTodos.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "¿Seguro que quieres eliminar TODOS los productos?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                listaProductos.clear();
                dialog.dispose();
                mostrarListaProductos(); // recargar vacío
            }
        });

        // Botón cerrar
        JButton btnCerrar = new JButton("✖ Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.setBackground(new Color(231, 76, 60)); // Rojo claro
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        buttonPanel.add(btnCerrar);
        btnCerrar.addActionListener(e -> dialog.dispose());

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void imprimirProductos() {
        if (listaProductos.isEmpty()) {
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();

        // Configurar página A4
        PageFormat pageFormat = job.defaultPage();
        Paper paper = new Paper();
        double paperWidth = 210; // Ancho A4 en mm
        double paperHeight = 297; // Alto A4 en mm
        double margin = 10; // Márgenes de 10mm

        paper.setSize(paperWidth * 72 / 25.4, paperHeight * 72 / 25.4);
        paper.setImageableArea(
                margin * 72 / 25.4,
                margin * 72 / 25.4,
                (paperWidth - 2 * margin) * 72 / 25.4,
                (paperHeight - 2 * margin) * 72 / 25.4
        );
        pageFormat.setPaper(paper);
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                double usableWidth = pageFormat.getImageableWidth();
                double usableHeight = pageFormat.getImageableHeight();

                // Configurar 4 columnas y 8 filas (32 códigos por página)
                int cols = 4;
                int rows = 8;
                int itemsPerPage = cols * rows;
                int startIndex = pageIndex * itemsPerPage;

                if (startIndex >= listaProductos.size()) {
                    return Printable.NO_SUCH_PAGE;
                }

                double cellWidth = usableWidth / cols;
                double cellHeight = usableHeight / rows;

                // Reducir tamaño para dejar espacio entre códigos
                double padding = 5;
                double contentWidth = cellWidth - 2 * padding;
                double contentHeight = cellHeight - 2 * padding;

                int currentIndex = startIndex;
                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {
                        if (currentIndex >= listaProductos.size()) {
                            break;
                        }

                        Producto producto = listaProductos.get(currentIndex);

                        // Calcular posición
                        double x = col * cellWidth + padding;
                        double y = row * cellHeight + padding;

                        // Dibujar código de barras reducido
                        BufferedImage img = resizeImage(producto.imagen, (int) contentWidth, (int) (contentHeight * 0.7));
                        g2d.drawImage(img, (int) x, (int) y, null);

                        // Dibujar texto reducido
                        g2d.setFont(new Font("Arial", Font.PLAIN, 6));
                        String text = producto.codigo;
                        int textWidth = g2d.getFontMetrics().stringWidth(text);
                        g2d.drawString(text, (int) (x + (contentWidth - textWidth) / 2), (int) (y + contentHeight * 0.7 + 8));

                        currentIndex++;
                    }
                }
                return Printable.PAGE_EXISTS;
            }
        }, pageFormat);

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al imprimir: " + ex.getMessage(),
                        "Error de Impresión", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Método auxiliar para redimensionar imágenes
    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, original.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private void exportarAPDF() {
        if (listaProductos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay productos para exportar",
                    "Exportar PDF", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Filtrar productos con imágenes válidas
        List<Producto> productosValidos = listaProductos.stream()
                .filter(p -> p.imagen != null)
                .toList();

        if (productosValidos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay productos con imágenes válidas para exportar",
                    "Exportar PDF", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar PDF");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fileName = "codigos_barras_" + sdf.format(new Date()) + ".pdf";
        fileChooser.setSelectedFile(new File(fileName));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            File finalFile = file;

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            progressBar.setString("Generando PDF...");

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    Document document = null;
                    FileOutputStream fos = null;
                    try {
                        document = new Document(PageSize.A4);
                        fos = new FileOutputStream(finalFile);
                        PdfWriter writer = PdfWriter.getInstance(document, fos);
                        document.open();

                        int numColumnas = Math.min(4, Math.max(1, productosValidos.size()));
                        PdfPTable table = new PdfPTable(numColumnas);
                        table.setWidthPercentage(100);
                        table.setSpacingBefore(10);
                        table.setSpacingAfter(10);

                        // Mantener altura fija para consistencia
                        float padding = 5;
                        float cellHeight = 55; // Altura consistente

                        for (Producto producto : productosValidos) {
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                PdfPCell cell = new PdfPCell();
                                cell.setBorder(Rectangle.BOX);
                                cell.setBorderWidth(0.5f);
                                cell.setPadding(padding);
                                cell.setFixedHeight(cellHeight);

                                ImageIO.write(producto.imagen, "PNG", baos);
                                byte[] imageBytes = baos.toByteArray();

                                Image img = Image.getInstance(imageBytes);
                                img.scaleToFit(50, 20); // Tamaño fijo de imagen
                                img.setAlignment(Element.ALIGN_CENTER);

                                // Fuente para el nombre - tamaño reducido para que quepa todo
                                com.itextpdf.text.Font nombreFont = com.itextpdf.text.FontFactory.getFont(
                                        com.itextpdf.text.FontFactory.HELVETICA, 5f, com.itextpdf.text.Font.NORMAL
                                );
                                String nombreReducido = resumirTexto(producto.nombre, 2); // Reducir a 2 palabras
                                com.itextpdf.text.Paragraph nombre = new com.itextpdf.text.Paragraph(nombreReducido, nombreFont);
                                nombre.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);

                                // PRECIO - en negrita negra
                                com.itextpdf.text.Paragraph precioParrafo = null;
                                if (producto.precio != null && !producto.precio.isEmpty()) {
                                    com.itextpdf.text.Font precioFont = com.itextpdf.text.FontFactory.getFont(
                                            com.itextpdf.text.FontFactory.HELVETICA_BOLD, 6f, com.itextpdf.text.Font.NORMAL
                                    );
                                    precioParrafo = new com.itextpdf.text.Paragraph(producto.precio, precioFont);
                                    precioParrafo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                                }

                                // Fuente para el código - tamaño consistente
                                com.itextpdf.text.Font codigoFont = com.itextpdf.text.FontFactory.getFont(
                                        com.itextpdf.text.FontFactory.HELVETICA, 5f
                                );
                                com.itextpdf.text.Paragraph codigo = new com.itextpdf.text.Paragraph(producto.codigo, codigoFont);
                                codigo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);

                                // Agregar elementos a la celda en orden correcto
                                cell.addElement(nombre);

                                // Agregar precio si existe
                                if (precioParrafo != null) {
                                    cell.addElement(precioParrafo);
                                }

                                cell.addElement(img);
                                cell.addElement(codigo);
                                table.addCell(cell);

                            } catch (Exception e) {
                                System.err.println("Error procesando producto: " + producto.nombre);
                                e.printStackTrace();
                            }
                        }

                        int celdasFaltantes = numColumnas - (productosValidos.size() % numColumnas);
                        if (celdasFaltantes < numColumnas && celdasFaltantes > 0) {
                            for (int i = 0; i < celdasFaltantes; i++) {
                                PdfPCell emptyCell = new PdfPCell();
                                emptyCell.setBorder(Rectangle.BOX);
                                emptyCell.setBorderWidth(0.5f);
                                table.addCell(emptyCell);
                            }
                        }

                        document.add(table);
                        document.close();

                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(GeneradorGU.this,
                                    "PDF generado exitosamente en:\n" + finalFile.getAbsolutePath(),
                                    "PDF Generado", JOptionPane.INFORMATION_MESSAGE);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(GeneradorGU.this,
                                    "Error al generar PDF: " + e.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        });
                    } finally {
                        try {
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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

    public String resumirTexto(String texto, int maxPalabras) {
        String[] palabras = texto.trim().split("\\s+");
        if (palabras.length <= maxPalabras) {
            return texto;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxPalabras; i++) {
            sb.append(palabras[i]).append(" ");
        }
        sb.append("...");
        return sb.toString().trim();
    }

    private void guardarImagen() {
        if (imagenCodigo == null) {
            JOptionPane.showMessageDialog(this,
                    "<html><div style='text-align:center;'>Primero genere una imagen<br>haciendo clic en 'Generar Imagen'</div></html>",
                    "Imagen no disponible", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Simular progreso
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        progressBar.setString("Guardando imagen...");

        // Usar un hilo separado para guardar la imagen
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Crear una imagen que combine el código de barras y el número
                int margenInferior = 40;
                int ancho = imagenCodigo.getWidth();
                int alto = imagenCodigo.getHeight() + margenInferior;

                BufferedImage imagenCompleta = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = imagenCompleta.createGraphics();

                // Fondo blanco
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, ancho, alto);

                // Dibujar el código de barras
                g2d.drawImage(imagenCodigo, 0, 0, null);

                // Dibujar el texto (centrado en la parte inferior)
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                String texto = ultimoCodigoGenerado;
                int x = (ancho - g2d.getFontMetrics().stringWidth(texto)) / 2;
                int y = imagenCodigo.getHeight() + 30;
                g2d.drawString(texto, x, y);

                g2d.dispose();

                // Guardar la imagen combinada
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Guardar código de barras");

                // Sugerir nombre de archivo
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String fileName = "codigo_barras_" + sdf.format(new Date()) + ".png";
                fileChooser.setSelectedFile(new File(fileName));

                // Filtro para archivos PNG
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos PNG", "png");
                fileChooser.setFileFilter(filter);

                int userSelection = fileChooser.showSaveDialog(GeneradorGU.this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    try {
                        ImageIO.write(imagenCompleta, "PNG", fileToSave);
                        JOptionPane.showMessageDialog(GeneradorGU.this,
                                "<html><div style='text-align:center;'>Imagen guardada exitosamente en:<br>"
                                + fileToSave.getAbsolutePath() + "</div></html>",
                                "Guardado exitoso", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(GeneradorGU.this,
                                "<html><div style='text-align:center;'>Error al guardar:<br>" + e.getMessage() + "</div></html>",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
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
