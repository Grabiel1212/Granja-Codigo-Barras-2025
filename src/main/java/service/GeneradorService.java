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

    private static final Set<String> CODIGOS_GENERADOS = new HashSet<>();
    private static final Random random = new Random();
    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    static {
        CODIGOS_GENERADOS.addAll(ImportadorService.getCodigosGlobales());
    }

    public static String generarCodigoUnico() {
        String codigo;
        do {
            codigo = generarCodigo(12);
        } while (!esCodigoUnico(codigo));
        CODIGOS_GENERADOS.add(codigo);
        return codigo;
    }

    public static String generarEAN13() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder("775");
            for (int i = 0; i < 9; i++) {
                sb.append(random.nextInt(10));
            }
            int checksum = calcularDigitoControlEAN13(sb.toString());
            codigo = sb.toString() + checksum;
        } while (!esCodigoUnico(codigo));
        CODIGOS_GENERADOS.add(codigo);
        return codigo;
    }

    private static String generarCodigo(int longitud) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < longitud; i++) {
            sb.append(CARACTERES.charAt(random.nextInt(CARACTERES.length())));
        }
        return sb.toString();
    }

    private static boolean esCodigoUnico(String codigo) {
        return !CODIGOS_GENERADOS.contains(codigo)
                && !ImportadorService.existeEnBD(codigo);
    }

    private static int calcularDigitoControlEAN13(String base) {
        int sum = 0;
        for (int i = 0; i < base.length(); i++) {
            int digit = Character.getNumericValue(base.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int checksum = 10 - (sum % 10);
        return (checksum == 10) ? 0 : checksum;
    }

    public static BufferedImage generarImagenCodigo(String codigo, BarcodeFormat formato, int ancho, int alto) {
        try {
            if (formato == BarcodeFormat.EAN_13 && !codigo.matches("\\d{13}")) {
                throw new IllegalArgumentException("EAN-13 requiere exactamente 13 dígitos numéricos");
            }
            BitMatrix bitMatrix = new MultiFormatWriter().encode(codigo, formato, ancho, alto);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            throw new RuntimeException("Error al generar imagen del código", e);
        }
    }

    // 🔄 Método para actualizar la caché de códigos generados
    public static void actualizarCache(Set<String> codigosExistentes) {
        CODIGOS_GENERADOS.clear();
        CODIGOS_GENERADOS.addAll(codigosExistentes);
    }
}
