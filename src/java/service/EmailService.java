/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import Modelo.Visitante;
import java.text.SimpleDateFormat;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.util.Date;
import java.util.Properties;

public class EmailService {
    private final Session session;
    private final String remitente;

    /**
     * @param host servidor SMTP (p.ej. "smtp.gmail.com")
     * @param port puerto SMTP (p.ej. "587")
     * @param user usuario SMTP (se usará como remitente)
     * @param pass contraseña o app-password SMTP
     * @param tls  true para habilitar STARTTLS
     */
    public EmailService(String host, String port,
                        final String user, final String pass,
                        boolean tls) {
        this.remitente = user;
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(tls));

        this.session = Session.getInstance(props,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            }
        );
    }

    /**
     * Envía un correo con un adjunto (el QR).
     *
     * @param destino      dirección de email del residente
     * @param asunto       asunto del correo
     * @param cuerpo       texto plano del mensaje
     * @param qrBytes      bytes PNG del QR generado
     * @param nombreArchivo nombre de archivo para el adjunto (p.ej. "qr.png")
     * @throws MessagingException si hay un error en JavaMail
     */
    public void enviarQR(String destino,
                         String asunto,
                         String cuerpo,
                         byte[] qrBytes,
                         String nombreArchivo) throws MessagingException {

        // 1) Crea el mensaje
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(remitente));
        msg.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(destino, false)
        );
        msg.setSubject(asunto, "UTF-8");
        msg.setSentDate(new Date());

        // 2) Parte de texto
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(cuerpo, "UTF-8");

        // 3) Parte de adjunto (QR en memoria)
        MimeBodyPart qrPart = new MimeBodyPart();
        DataSource ds = new ByteArrayDataSource(qrBytes, "image/png");
        qrPart.setDataHandler(new DataHandler(ds));
        qrPart.setFileName(nombreArchivo);

        // 4) Combina ambas partes
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(qrPart);
        msg.setContent(multipart);

        // 5) Envía el correo
        Transport.send(msg);
    }
    
    // Envío especializado para Visitantes (correo al visitante)
public void enviarQRVisitante(Visitante visitante, byte[] qrBytes) throws MessagingException {
    if (visitante == null) throw new IllegalArgumentException("Visitante es null");
    if (qrBytes == null || qrBytes.length == 0) throw new IllegalArgumentException("QR vacío");
    if (visitante.getCorreo() == null || visitante.getCorreo().isEmpty())
        throw new IllegalArgumentException("Correo del visitante vacío");

    String asunto = "Notificación de accesos creados";
    String cuerpo = String.format(
        "¡Hola!\nSe ha generado exitosamente tu código QR de acceso al residencial.\n" +
        "Nombre del visitante: %s\nValidez del código QR: %s\n\n" +
        "Guarda este correo o el código QR adjunto.\n" +
        "Preséntalo al llegar al residencial para que el personal de seguridad lo escanee y valide tu acceso.\n" +
        "¡Gracias por coordinar tu visita con anticipación!",
        nvl(visitante.getNombre(), "Visitante"),
        nvl(visitante.getValidezTexto(), "según indicaciones")
    );

    String nombreArchivo = "qr_visitante_" + visitante.getId() + ".png";
    enviarQR(visitante.getCorreo(), asunto, cuerpo, qrBytes, nombreArchivo);
}

// Envío al residente que registró la visita
public void enviarQRResidente(Visitante visitante, String correoResidente, byte[] qrBytes) throws MessagingException {
    if (visitante == null) throw new IllegalArgumentException("Visitante es null");
    if (qrBytes == null || qrBytes.length == 0) throw new IllegalArgumentException("QR vacío");
    if (correoResidente == null || correoResidente.isEmpty())
        throw new IllegalArgumentException("Correo del residente vacío");

    String asunto = "Notificación de accesos creados";
    String cuerpo = String.format(
        "El código QR fue generado exitosamente para la persona %s el día %s a las %s para acceder al condominio.\n" +
        "Este código tiene una validez de %s.\n" +
        "En caso de cualquier irregularidad, por favor contacte al administrador del sistema.",
        nvl(visitante.getNombre(), "Visitante"),
        nvl(visitante.getFechaGeneracionTextoDia(), "hoy"),
        nvl(visitante.getFechaGeneracionTextoHora(), "ahora"),
        nvl(visitante.getValidezTexto(), "según indicaciones")
    );

    String nombreArchivo = "qr_visitante_" + visitante.getId() + ".png";
    enviarQR(correoResidente, asunto, cuerpo, qrBytes, nombreArchivo);
}

// Helper opcional para evitar NPE en String.format
private static String nvl(String s, String def) {
    return (s == null || s.isEmpty()) ? def : s;
}


/**
 * Envía solo texto de confirmación al residente,
 * informándole que el código QR fue enviado al visitante.
 */
public void enviarConfirmacionResidente(Visitante v, String correoResidente)
        throws MessagingException {
    if (v == null) 
        throw new IllegalArgumentException("Visitante es null");
    if (correoResidente == null || correoResidente.isEmpty())
        throw new IllegalArgumentException("Correo del residente vacío");

    String asunto = "Notificación de accesos creados";
    String cuerpo = String.format(
    "El código QR fue generado exitosamente para la persona %s el día %s a las %s para acceder al condominio. "
  + "Este código tiene una validez de %s. "
  + "En caso de cualquier irregularidad, por favor contacte al administrador del sistema.",
    nvl(v.getNombre(), "Visitante"),
    new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
    new SimpleDateFormat("HH:mm:ss").format(new Date()),
    ("Por intentos".equalsIgnoreCase(v.getTipoVisita()) && v.getIntentos() > 0)
        ? v.getIntentos() + " intentos"
        : new SimpleDateFormat("yyyy-MM-dd").format(v.getFechaVisita())
);

    // Crea y envía un correo simple (sin adjuntos)
    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(remitente));
    msg.setRecipients(
        Message.RecipientType.TO,
        InternetAddress.parse(correoResidente, false)
    );
    msg.setSubject(asunto, "UTF-8");
    msg.setSentDate(new Date());
    msg.setText(cuerpo, "UTF-8");

    Transport.send(msg);
}


    /**
 * Envía una notificación al residente informando que el código QR del visitante
 * fue utilizado exitosamente para acceder.
 *
 * @param visitante      objeto Visitante utilizado
 * @param correoResidente dirección de correo del residente
 * @throws MessagingException si ocurre un error al enviar el correo
 */
public void enviarNotificacionUsoQR(Visitante visitante, String correoResidente) throws MessagingException {
    if (visitante == null) 
        throw new IllegalArgumentException("Visitante es null");
    if (correoResidente == null || correoResidente.isEmpty())
        throw new IllegalArgumentException("Correo del residente vacío");

    String asunto = "Notificación de uso de código QR";
    
    // Fecha y hora actuales
    String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    String hora  = new SimpleDateFormat("HH:mm:ss").format(new Date());

    // Determinar validez: por intentos o fecha
    String validez;
    if ("Por intentos".equalsIgnoreCase(visitante.getTipoVisita()) && visitante.getIntentos() != null) {
        validez = visitante.getIntentos() + " intentos restantes";
    } else if (visitante.getFechaVisita() != null) {
        validez = "hasta " + new SimpleDateFormat("yyyy-MM-dd").format(visitante.getFechaVisita());
    } else {
        validez = "según indicaciones";
    }

    String cuerpo = String.format(
        "El código QR generado para la persona %s fue utilizado exitosamente el día %s a las %s para acceder al condominio.\n" +
        "Este código tiene una validez de %s.\n" +
        "En caso de cualquier irregularidad, por favor contacte al administrador del sistema.",
        nvl(visitante.getNombre(), "Visitante"),
        fecha,
        hora,
        validez
    );

    // Crear mensaje simple (solo texto)
    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(remitente));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correoResidente, false));
    msg.setSubject(asunto, "UTF-8");
    msg.setSentDate(new Date());
    msg.setText(cuerpo, "UTF-8");

    Transport.send(msg);
}




}
