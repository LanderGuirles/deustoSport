package com.deustosport.my_app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@deustosport.com}")
    private String remitente;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    /**
     * Envía un email de recuperación de contraseña
     * @param destinatario Email del usuario
     * @param token Token único para restablecer la contraseña
     */
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

    /**
     * Simula el envío de email en consola (para desarrollo)
     */
    private void simularEnvioEmail(String destinatario, String token) {
        System.out.println("=========================================================");
        System.out.println("📧 SIMULACIÓN DE ENVÍO DE EMAIL (Desarrollo)");
        System.out.println("Destinatario: " + destinatario);
        System.out.println("Asunto: DeustoSport - Recuperación de Contraseña");
        System.out.println("Contenido: ");
        System.out.println(generarContenidoRecuperacion(token));
        System.out.println("=========================================================");
        logger.warn("Email simulado: modo desarrollo activado. Para enviar emails reales, configure spring.mail.* y app.email.enabled=true");
    }
}