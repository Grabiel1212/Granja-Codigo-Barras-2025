package service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GeneradorService {
    private static final Set<String> codigosGenerados = new HashSet<>();
    private static final Random random = new Random();

    // Genera un código único de 12 dígitos (no EAN-13)
  public static String generarCodigoUnico() {
    String codigo;
    String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    do {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        codigo = sb.toString();
    } while (codigosGenerados.contains(codigo));

    codigosGenerados.add(codigo);
    return codigo;
}


    // Genera un código EAN-13 válido
    public static String generarEAN13() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder();

            // Prefijo de país ficticio
            sb.append("775");

            // 9 dígitos aleatorios restantes
            for (int i = 0; i < 9; i++) {
                sb.append(random.nextInt(10));
            }

            String base = sb.toString();
            int checksum = calcularDigitoControlEAN13(base);
            codigo = base + checksum;

        } while (codigosGenerados.contains(codigo));

        codigosGenerados.add(codigo);
        return codigo;
    }

    // Calcula el dígito de control (checksum) EAN-13
    private static int calcularDigitoControlEAN13(String base) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            int digit = Character.getNumericValue(base.charAt(i));
            sum += (i % 2 == 0) ? digit * 1 : digit * 3;
        }
        int checksum = 10 - (sum % 10);
        return (checksum == 10) ? 0 : checksum;
    }

    // Genera la imagen del código de barras
    public static BufferedImage generarImagenCodigo(String codigo, BarcodeFormat formato, int ancho, int alto) {
        try {
            // Validación para EAN-13
            if (formato == BarcodeFormat.EAN_13 && !codigo.matches("\\d{13}")) {
                throw new IllegalArgumentException("EAN-13 requiere exactamente 13 dígitos numéricos");
            }

            BitMatrix bitMatrix = new MultiFormatWriter().encode(codigo, formato, ancho, alto);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            throw new RuntimeException("Error al generar imagen del código", e);
        }
    }
}
