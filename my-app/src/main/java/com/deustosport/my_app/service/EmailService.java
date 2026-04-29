package com.deustosport.my_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private QRCodeService qrCodeService;

    @Value("${spring.mail.username:noreply@deustosport.com}")
    private String remitente;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public void enviarEmailBienvenida(String destinatario, String nombre) {
        String contenido = "¡Bienvenido a DeustoSport, " + nombre + "!\n\n" +
                "Tu cuenta ha sido creada exitosamente.\n\n" +
                "Ya puedes empezar a reservar pistas deportivas.\n\n" +
                "Si eres socio, disfruta de descuentos exclusivos.\n\n" +
                "¡Que disfrutes del deporte!\n\n" +
                "Equipo DeustoSport";

        if (!emailEnabled || mailSender == null) {
            logger.info("=== [EMAIL SIMULADO] Bienvenida ===");
            logger.info("Destinatario: {} | Nombre: {}", destinatario, nombre);
            logger.debug("Contenido: {}", contenido);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("Bienvenido a DeustoSport");
            mensaje.setText(contenido);
            mailSender.send(mensaje);
            logger.info("Email de bienvenida enviado a: {}", destinatario);
        } catch (Exception e) {
            logger.error("Error al enviar email de bienvenida a {}: {}", destinatario, e.getMessage());
        }
    }
    public void enviarEmailRecuperacion(String destinatario, String token) {
        // Si el envío de email está deshabilitado, simular en consola
        if (!emailEnabled || mailSender == null) {
            simularEnvioEmail(destinatario, token);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("DeustoSport - Recuperación de Contraseña");

            String contenido = generarContenidoRecuperacion(token);
            mensaje.setText(contenido);

            mailSender.send(mensaje);
            logger.info("Email de recuperación enviado a: {}", destinatario);

        } catch (Exception e) {
            logger.error("Error al enviar email de recuperación a {}: {}", destinatario, e.getMessage());
            // Opcional: lanzar excepción o manejarla según el flujo
        }
    }

    /**
     * Envía un email de confirmación de reserva de pista con enlace al código QR.
     */
    public void enviarEmailConfirmacionReserva(String destinatario,
                                               String nombrePista,
                                               String tipoDeporte,
                                               LocalDate fecha,
                                               LocalTime horaInicio,
                                               LocalTime horaFin,
                                               BigDecimal precio,
                                               Long reservaId,
                                               Long usuarioId) {
        String contenidoTexto = generarContenidoTextoReservaConfirmada(nombrePista, tipoDeporte, fecha, horaInicio, horaFin, precio);

        // Generar y guardar QR
        try {
            qrCodeService.generateAndSaveReservaQR(reservaId, usuarioId, nombrePista, fecha.toString(), horaInicio.toString());
            String qrUrl = "/api/qr/reserva_" + reservaId + ".png";  // Enlace relativo al QR
            contenidoTexto += "\n\nCódigo QR para acceso: " + qrUrl;
        } catch (Exception e) {
            logger.warn("No se pudo generar QR para reserva {}: {}", reservaId, e.getMessage());
        }

        if (!emailEnabled || mailSender == null) {
            logger.info("=== [EMAIL SIMULADO] Reserva confirmada ===");
            logger.info("Destinatario: {}", destinatario);
            logger.info("Pista: {} ({}) | {} {} - {} | {}€ | Reserva ID: {}", nombrePista, tipoDeporte, fecha, horaInicio, horaFin, precio, reservaId);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("DeustoSport - Reserva de Pista Confirmada");
            mensaje.setText(contenidoTexto);
            mailSender.send(mensaje);
            logger.info("Email de confirmación de reserva enviado a: {}", destinatario);
        } catch (Exception e) {
            logger.error("Error al enviar email de confirmación de reserva a {}: {}", destinatario, e.getMessage());
        }
    }

    /**
     * Envía un email de creación de reserva pendiente de pago con enlace al código QR.
     */
    public void enviarEmailCreacionReserva(String destinatario,
                                           String nombrePista,
                                           String tipoDeporte,
                                           LocalDate fecha,
                                           LocalTime horaInicio,
                                           LocalTime horaFin,
                                           BigDecimal precio,
                                           Long reservaId,
                                           Long usuarioId) {
        String contenidoTexto = generarContenidoTextoReservaCreada(nombrePista, tipoDeporte, fecha, horaInicio, horaFin, precio);

        // Generar y guardar QR
        try {
            qrCodeService.generateAndSaveReservaQR(reservaId, usuarioId, nombrePista, fecha.toString(), horaInicio.toString());
            String qrUrl = "/api/qr/reserva_" + reservaId + ".png";  // Enlace relativo al QR
            contenidoTexto += "\n\nCódigo QR de tu reserva: " + qrUrl;
        } catch (Exception e) {
            logger.warn("No se pudo generar QR para reserva {}: {}", reservaId, e.getMessage());
        }

        if (!emailEnabled || mailSender == null) {
            logger.info("=== [EMAIL SIMULADO] Reserva creada ===");
            logger.info("Destinatario: {}", destinatario);
            logger.info("Pista: {} ({}) | {} {} - {} | {}€ | Reserva ID: {}", nombrePista, tipoDeporte, fecha, horaInicio, horaFin, precio, reservaId);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("DeustoSport - Reserva Creada - Pendiente de Pago");
            mensaje.setText(contenidoTexto);
            mailSender.send(mensaje);
            logger.info("Email de creación de reserva enviado a: {}", destinatario);
        } catch (Exception e) {
            logger.error("Error al enviar email de creación de reserva a {}: {}", destinatario, e.getMessage());
        }
    }

    /**
     * Envia una notificacion generica por correo (incidencias o comunicados).
     */
    public void enviarEmailNotificacion(String destinatario, String titulo, String mensajeTexto) {
        if (titulo == null || titulo.isBlank() || mensajeTexto == null || mensajeTexto.isBlank()) {
            logger.warn("Se intento enviar un email de notificacion sin titulo o mensaje.");
            return;
        }

        String contenido = "DeustoSport - Notificacion\n\n" + mensajeTexto;

        if (!emailEnabled || mailSender == null) {
            logger.info("=== [EMAIL SIMULADO] Notificación ===");
            logger.info("Destinatario: {} | Asunto: {}", destinatario, titulo);
            logger.debug("Contenido: {}", contenido);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("DeustoSport - " + titulo);
            mensaje.setText(contenido);
            mailSender.send(mensaje);
            logger.info("Email de notificacion enviado a: {}", destinatario);
        } catch (Exception e) {
            logger.error("Error al enviar email de notificacion a {}: {}", destinatario, e.getMessage());
        }
    }

    /**
     * Genera el contenido HTML del email de confirmación de reserva
     */
    public String generarContenidoHtmlReservaConfirmada(String nombrePista, String tipoDeporte, LocalDate fecha,
                                               LocalTime horaInicio, LocalTime horaFin, BigDecimal precio) {
        return "<html><body>" +
                "<h2 style='color: #4CAF50;'>✨ ¡Tu reserva ha sido confirmada! ✨</h2>" +
            "<p>A continuación tienes tu código QR para mostrar en la entrada:</p>" +
            "<div style='margin: 20px 0; text-align: center;'>" +
            "<img src='cid:qrCodeImage' alt='Código QR de reserva' style='width:200px;height:200px;border:1px solid #ddd;padding:10px;'/>" +
            "</div>" +
                "<p>Presenta el código QR en la entrada para acceder a las instalaciones.</p>" +
                "<table style='border-collapse: collapse; width: 100%; max-width: 400px;'>" +
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>🏟️ Pista:</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>" + nombrePista + " (" + tipoDeporte + ")</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>📅 Fecha:</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>" + fecha + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>⏰ Hora:</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>" + horaInicio + " - " + horaFin + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>💰 Precio:</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>" + precio + " €</td></tr>" +
                "</table>" +
                "<p style='margin-top: 20px;'>🎉 ¡Que disfrutes del partido y que los astros estén contigo!</p>" +
                "<p><small>Si tienes alguna duda, contacta con nosotros.</small></p>" +
                "</body></html>";
    }

    /**
     * Genera el contenido de texto plano del email de confirmación de reserva
     */
    public String generarContenidoTextoReservaConfirmada(String nombrePista, String tipoDeporte, LocalDate fecha,
                                                LocalTime horaInicio, LocalTime horaFin, BigDecimal precio) {
        return "✨ Tu reserva ha sido confirmada! ✨\n\n" +
                "Presenta el código QR en la entrada para acceder a las instalaciones.\n\n" +
                "🏟️ Pista: " + nombrePista + " (" + tipoDeporte + ")\n" +
                "📅 Fecha: " + fecha + "\n" +
                "⏰ Hora: " + horaInicio + " - " + horaFin + "\n" +
                "💰 Precio: " + precio + " €\n\n" +
                "🎉 ¡Que disfrutes del partido y que los astros estén contigo!\n\n" +
                "Si tienes alguna duda, contacta con nosotros.";
    }

    /**
     * Genera el contenido HTML del email de creación de reserva
     */
    public String generarContenidoHtmlReservaCreada(String nombrePista, String tipoDeporte, LocalDate fecha,
                                               LocalTime horaInicio, LocalTime horaFin, BigDecimal precio) {
        return "<html><body>" +
                "<h2 style='color: #FF9800;'>⏳ Tu reserva ha sido creada ⏳</h2>" +
            "<p>Aquí tienes el código QR de tu reserva pendiente de pago:</p>" +
            "<div style='margin: 20px 0; text-align: center;'>" +
            "<img src='cid:qrCodeImage' alt='Código QR de reserva' style='width:200px;height:200px;border:1px solid #ddd;padding:10px;'/>" +
            "</div>" +
                "<p>Tu reserva está pendiente de pago. Una vez pagada, recibirás el código QR para acceder.</p>" +
                "<table style='border-collapse: collapse; width: 100%; max-width: 400px;'>" +
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>🏟️ Pista:</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>" + nombrePista + " (" + tipoDeporte + ")</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>📅 Fecha:</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>" + fecha + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>⏰ Hora:</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>" + horaInicio + " - " + horaFin + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #ddd;'><strong>💰 Precio:</strong></td><td style='padding: 8px; border: 1px solid #ddd;'>" + precio + " €</td></tr>" +
                "</table>" +
                "<p style='margin-top: 20px;'>Por favor, completa el pago para confirmar tu reserva.</p>" +
                "<p><small>Si tienes alguna duda, contacta con nosotros.</small></p>" +
                "</body></html>";
    }

    /**
     * Genera el contenido de texto plano del email de creación de reserva
     */
    public String generarContenidoTextoReservaCreada(String nombrePista, String tipoDeporte, LocalDate fecha,
                                                LocalTime horaInicio, LocalTime horaFin, BigDecimal precio) {
        return "⏳ Tu reserva ha sido creada ⏳\n\n" +
                "Tu reserva está pendiente de pago. Una vez pagada, recibirás el código QR para acceder.\n\n" +
                "🏟️ Pista: " + nombrePista + " (" + tipoDeporte + ")\n" +
                "📅 Fecha: " + fecha + "\n" +
                "⏰ Hora: " + horaInicio + " - " + horaFin + "\n" +
                "💰 Precio: " + precio + " €\n\n" +
                "Por favor, completa el pago para confirmar tu reserva.\n\n" +
                "Si tienes alguna duda, contacta con nosotros.";
    }

    /**
     * Simula el envío de email en consola (para desarrollo)
     */
    private void simularEnvioEmail(String destinatario, String token) {
        logger.info("=== [EMAIL SIMULADO] Recuperación de contraseña ===");
        logger.info("Destinatario: {}", destinatario);
        logger.debug("Token de recuperación: {}", token);
        logger.warn("Modo desarrollo activo. Para enviar emails reales configure spring.mail.* y app.email.enabled=true");
    }

    /**
     * Genera el contenido del email de recuperación
     */
    private String generarContenidoRecuperacion(String token) {
        return "Hola,\n\n" +
                "Hemos recibido una solicitud para restablecer tu contraseña en DeustoSport.\n\n" +
                "Tu código de seguridad de un solo uso es:\n" +
                "[" + token + "]\n\n" +
                "Este código caducará en 24 horas.\n\n" +
                "Si no solicitaste esta recuperación, puedes ignorar este email.\n\n" +
                "Saludos,\nEl equipo de DeustoSport";
    }
}