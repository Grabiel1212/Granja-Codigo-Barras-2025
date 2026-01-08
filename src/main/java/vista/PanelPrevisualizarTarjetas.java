package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import model.Producto;

public class PanelPrevisualizarTarjetas extends JDialog {

    private GeneradorGU generadorGU;
    private List<Producto> productos;
    private List<Integer> cantidades; // Lista para almacenar las cantidades
    private JPanel cardsPanel;

    // Colores para la interfaz
    private Color colorPrimario = new Color(41, 128, 185);
    private Color colorFondo = new Color(245, 245, 245);
    private Color colorBorde = new Color(220, 220, 220);

    public PanelPrevisualizarTarjetas(GeneradorGU parent, List<Producto> productos, List<Integer> cantidades) {
        super(parent, "Vista Previa de Tarjetas", true);
        this.generadorGU = parent;
        this.productos = productos;
        this.cantidades = cantidades; // Guardamos las cantidades
        
        // Calcular total de tarjetas sumando todas las cantidades
        int totalTarjetas = 0;
        for (Integer cantidad : cantidades) {
            totalTarjetas += cantidad;
        }
        
        setSize(1000, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        getContentPane().setBackground(colorFondo);

        // Panel de título
        JPanel titlePanel = crearTitlePanel(totalTarjetas);
        add(titlePanel, BorderLayout.NORTH);

        // Panel de tarjetas
        crearCardsPanel();

        // Panel de botones inferiores
        JPanel buttonPanel = crearButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel crearTitlePanel(int totalTarjetas) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(colorPrimario);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel(
            "<html><center>Vista Previa de Tarjetas<br>" +
            "<font size='3'>" + productos.size() + " productos únicos | " + totalTarjetas + " tarjetas a generar</font></center></html>",
            JLabel.CENTER
        );
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Botón para regresar (cerrar)
        JButton btnRegresar = crearBotonEstilizado("← Regresar", new Color(149, 165, 166));
        btnRegresar.setPreferredSize(new Dimension(120, 35));
        btnRegresar.addActionListener(e -> dispose());
        titlePanel.add(btnRegresar, BorderLayout.WEST);

        return titlePanel;
    }

    private void crearCardsPanel() {
        cardsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        cardsPanel.setBackground(colorFondo);

        // Crear tarjetas para cada producto (una por producto, mostrando la cantidad)
        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            int cantidad = cantidades.get(i);
            JPanel card = crearTarjetaProducto(producto, cantidad);
            cardsPanel.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.getViewport().setBackground(colorFondo);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel crearTarjetaProducto(Producto producto, int cantidad) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        card.setPreferredSize(new Dimension(250, 150));

        // Nombre del producto
        JLabel nombreLabel = new JLabel("<html><center>" + producto.nombre + "</center></html>");
        nombreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nombreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nombreLabel.setForeground(new Color(60, 60, 60));

        // Precio
        JLabel precioLabel = new JLabel("<html><center><b>" + producto.precio + "</b></center></html>");
        precioLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        precioLabel.setHorizontalAlignment(SwingConstants.CENTER);
        precioLabel.setForeground(new Color(231, 76, 60));

        // Código
        JLabel codigoLabel = new JLabel(
                "<html><center><font size='5'><b>" + producto.codigo + "</b></font></center></html>");
        codigoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        codigoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        codigoLabel.setForeground(new Color(41, 128, 185));

        // Etiqueta de cantidad
        JLabel cantidadLabel = new JLabel(
                "<html><center>Cantidad: <b>" + cantidad + "</b></center></html>");
        cantidadLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cantidadLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cantidadLabel.setForeground(new Color(39, 174, 96));

        // Panel sur para código y cantidad
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.add(codigoLabel, BorderLayout.CENTER);
        southPanel.add(cantidadLabel, BorderLayout.SOUTH);

        card.add(nombreLabel, BorderLayout.NORTH);
        card.add(precioLabel, BorderLayout.CENTER);
        card.add(southPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel crearButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        buttonPanel.setBackground(colorFondo);

        // Botón Exportar PDF
        JButton btnExportarPDF = crearBotonEstilizado("📄 Exportar a PDF", new Color(52, 152, 219));
        btnExportarPDF.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExportarPDF.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnExportarPDF.addActionListener(e -> exportarAPDF());
        buttonPanel.add(btnExportarPDF);

        buttonPanel.add(Box.createHorizontalStrut(50));

        // Botón Cerrar
        JButton btnCerrar = crearBotonEstilizado("✖ Cerrar", new Color(231, 76, 60));
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnCerrar.addActionListener(e -> dispose());
        buttonPanel.add(btnCerrar);

        return buttonPanel;
    }

    private void exportarAPDF() {
        if (productos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay productos para exportar",
                    "Exportar PDF", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar PDF");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String fileName = "tarjetas_productos_" + sdf.format(new Date()) + ".pdf";
        fileChooser.setSelectedFile(new File(fileName));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            File finalFile = file;

            // Mostrar indicador de progreso
            JOptionPane.showMessageDialog(this,
                    "Generando PDF... Esto puede tomar unos momentos.",
                    "Generando PDF",
                    JOptionPane.INFORMATION_MESSAGE);

            new Thread(() -> {
                try {
                    generarPDF(finalFile);
                    SwingUtilities.invokeLater(() -> {
                        // Calcular total de tarjetas generadas
                        int totalGeneradas = 0;
                        for (Integer cantidad : cantidades) {
                            totalGeneradas += cantidad;
                        }
                        
                        JOptionPane.showMessageDialog(this,
                                "PDF generado exitosamente:\n" +
                                "• Productos únicos: " + productos.size() + "\n" +
                                "• Tarjetas generadas: " + totalGeneradas + "\n" +
                                "• Ubicación: " + finalFile.getAbsolutePath(),
                                "PDF Generado", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                                "Error al generar PDF: " + e.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }

    private void generarPDF(java.io.File file) throws Exception {
        com.itextpdf.text.Document document = null;
        java.io.FileOutputStream fos = null;

        try {
            document = new com.itextpdf.text.Document(
                    com.itextpdf.text.PageSize.A4, 12, 12, 12, 12);
            fos = new java.io.FileOutputStream(file);
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, fos);
            document.open();

            int numColumnas = 4;
            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(numColumnas);
            table.setWidthPercentage(100);
            table.setSpacingBefore(5);
            table.setSpacingAfter(5);
            table.setWidths(new float[] { 1f, 1f, 1f, 1f });

            // Generar tarjetas según la cantidad de cada producto
            for (int i = 0; i < productos.size(); i++) {
                Producto producto = productos.get(i);
                int cantidad = cantidades.get(i);
                
                for (int j = 0; j < cantidad; j++) {
                    com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell();
                    cell.setBorder(com.itextpdf.text.Rectangle.BOX);
                    cell.setBorderWidth(0.4f);

                    cell.setPaddingTop(6f);
                    cell.setPaddingBottom(6f);
                    cell.setPaddingLeft(5f);
                    cell.setPaddingRight(5f);

                    cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(com.itextpdf.text.Element.ALIGN_MIDDLE);
                    cell.setMinimumHeight(70f);

                    // Fuentes
                    com.itextpdf.text.Font nombreFont = com.itextpdf.text.FontFactory.getFont(
                            com.itextpdf.text.FontFactory.HELVETICA,
                            6f,
                            com.itextpdf.text.Font.NORMAL);

                    com.itextpdf.text.Font precioFont = com.itextpdf.text.FontFactory.getFont(
                            com.itextpdf.text.FontFactory.HELVETICA,
                            6.5f,
                            com.itextpdf.text.Font.NORMAL);

                    com.itextpdf.text.Font codigoFont = com.itextpdf.text.FontFactory.getFont(
                            com.itextpdf.text.FontFactory.HELVETICA_BOLD,
                            13f,
                            com.itextpdf.text.Font.BOLD);

                    com.itextpdf.text.Font codigoLabelFont = com.itextpdf.text.FontFactory.getFont(
                            com.itextpdf.text.FontFactory.HELVETICA,
                            4f,
                            com.itextpdf.text.Font.NORMAL);

                    // Contenido
                    com.itextpdf.text.Paragraph nombre = new com.itextpdf.text.Paragraph(producto.nombre, nombreFont);
                    nombre.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    nombre.setLeading(6f);
                    nombre.setSpacingAfter(2f);

                    com.itextpdf.text.Paragraph precio = new com.itextpdf.text.Paragraph(producto.precio, precioFont);
                    precio.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    precio.setLeading(6f);
                    precio.setSpacingAfter(8f);

                    com.itextpdf.text.Paragraph codigoParrafo = new com.itextpdf.text.Paragraph();
                    codigoParrafo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                    codigoParrafo.setLeading(9f);

                    com.itextpdf.text.Chunk label = new com.itextpdf.text.Chunk("COD: ", codigoLabelFont);
                    com.itextpdf.text.Chunk codigo = new com.itextpdf.text.Chunk(producto.codigo, codigoFont);

                    codigoParrafo.add(label);
                    codigoParrafo.add(codigo);

                    cell.addElement(nombre);
                    cell.addElement(precio);
                    cell.addElement(codigoParrafo);

                    table.addCell(cell);
                }
            }

            // Rellenar fila final si es necesario
            int totalCeldas = 0;
            for (Integer cantidad : cantidades) {
                totalCeldas += cantidad;
            }
            
            int resto = totalCeldas % numColumnas;
            if (resto != 0) {
                for (int i = resto; i < numColumnas; i++) {
                    com.itextpdf.text.pdf.PdfPCell empty = new com.itextpdf.text.pdf.PdfPCell();
                    empty.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                    empty.setMinimumHeight(70f);
                    table.addCell(empty);
                }
            }

            document.add(table);

        } finally {
            if (document != null && document.isOpen())
                document.close();
            if (fos != null)
                fos.close();
        }
    }

    private JButton crearBotonEstilizado(String text, Color bgColor) {
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
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }
}