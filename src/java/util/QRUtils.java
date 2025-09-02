/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import Modelo.Usuarios;
import Modelo.Visitante;

public class QRUtils {

    public static byte[] generarBytes(String contenido, int ancho, int alto) throws Exception {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix matrix = new QRCodeWriter()
            .encode(contenido, BarcodeFormat.QR_CODE, ancho, alto, hints);

        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                img.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "PNG", baos);
            return baos.toByteArray();
        }
    }

 public static byte[] generarBytes(Usuarios usuario, int ancho, int alto) throws Exception {
    int id = usuario.getId();
    if (id <= 0) {
        throw new IllegalArgumentException(
            "No se puede generar QR: id de usuario no válido");
    }
    String contenido = String.valueOf(id);
    return generarBytes(contenido, ancho, alto);
}
 
   // QR para Usuarios
  /*  public static byte[] generarBytes(Usuarios usuario, int ancho, int alto) throws Exception {
        int id = usuario.getId();
        if (id <= 0) {
            throw new IllegalArgumentException("No se puede generar QR: id de usuario no válido");
        }
        // Sugerencia: usa prefijo para distinguir tipos en el lector.
        String contenido = "USUARIO:" + id;
        return generarBytes(contenido, ancho, alto);
    }*/

    // QR para Visitantes
    public static byte[] generarBytes(Visitante visitante, int ancho, int alto) throws Exception {
        int id = visitante.getId();
        if (id <= 0) {
            throw new IllegalArgumentException("No se puede generar QR: id de visitante no válido");
        }
        String contenido = "VISITANTE:" + id;
        return generarBytes(contenido, ancho, alto);
    }
 
 
}
