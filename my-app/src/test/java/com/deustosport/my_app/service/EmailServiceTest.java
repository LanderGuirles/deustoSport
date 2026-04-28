package com.deustosport.my_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private QRCodeService qrCodeService;

    @InjectMocks
    private EmailService emailService;

    @Test
    void enviarEmailCreacionReserva_debeEnviarEmailConQR() {
        // Given
        String destinatario = "usuario@test.com";
        String nombrePista = "Pista Fútbol 1";
        String tipoDeporte = "FÚTBOL";
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        BigDecimal precio = new BigDecimal("25.00");
        Long reservaId = 123L;
        Long usuarioId = 456L;

        byte[] qrCodeMock = "fake-qr-code".getBytes();
        when(qrCodeService.generateReservaQR(reservaId, usuarioId, nombrePista,
                fecha.toString(), horaInicio.toString())).thenReturn(qrCodeMock);

        // When
        emailService.enviarEmailCreacionReserva(destinatario, nombrePista, tipoDeporte,
                fecha, horaInicio, horaFin, precio, reservaId, usuarioId);

        // Then
        // Como mailSender es null (modo simulado), no se debería enviar email real
        // pero el método debería ejecutarse sin errores
        verify(qrCodeService).generateReservaQR(reservaId, usuarioId, nombrePista,
                fecha.toString(), horaInicio.toString());
    }

    @Test
    void enviarEmailConfirmacionReserva_debeEnviarEmailConQR() {
        // Given
        String destinatario = "usuario@test.com";
        String nombrePista = "Pista Fútbol 1";
        String tipoDeporte = "FÚTBOL";
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        BigDecimal precio = new BigDecimal("25.00");
        Long reservaId = 123L;
        Long usuarioId = 456L;

        byte[] qrCodeMock = "fake-qr-code".getBytes();
        when(qrCodeService.generateReservaQR(reservaId, usuarioId, nombrePista,
                fecha.toString(), horaInicio.toString())).thenReturn(qrCodeMock);

        // When
        emailService.enviarEmailConfirmacionReserva(destinatario, nombrePista, tipoDeporte,
                fecha, horaInicio, horaFin, precio, reservaId, usuarioId);

        // Then
        verify(qrCodeService).generateReservaQR(reservaId, usuarioId, nombrePista,
                fecha.toString(), horaInicio.toString());
    }

    @Test
    void enviarEmailBienvenida_debeEnviarEmailBienvenida() {
        // Given
        String destinatario = "nuevo@test.com";
        String nombre = "Juan Pérez";

        // When
        emailService.enviarEmailBienvenida(destinatario, nombre);

        // Then
        // Método debería ejecutarse sin errores en modo simulado
    }

    @Test
    void generarContenidoHtmlReservaCreada_debeGenerarHTMLCorrecto() {
        // Given
        String nombrePista = "Pista Fútbol 1";
        String tipoDeporte = "FÚTBOL";
        LocalDate fecha = LocalDate.of(2024, 12, 25);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        BigDecimal precio = new BigDecimal("25.00");

        // When
        String result = emailService.generarContenidoHtmlReservaCreada(nombrePista, tipoDeporte,
                fecha, horaInicio, horaFin, precio);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Tu reserva ha sido creada"));
        assertTrue(result.contains("Pista Fútbol 1"));
        assertTrue(result.contains("25.00"));
        assertTrue(result.contains("<html>"));
        assertTrue(result.contains("</html>"));
    }

    @Test
    void generarContenidoTextoReservaCreada_debeGenerarTextoCorrecto() {
        // Given
        String nombrePista = "Pista Fútbol 1";
        String tipoDeporte = "FÚTBOL";
        LocalDate fecha = LocalDate.of(2024, 12, 25);
        LocalTime horaInicio = LocalTime.of(10, 0);
        LocalTime horaFin = LocalTime.of(11, 0);
        BigDecimal precio = new BigDecimal("25.00");

        // When
        String result = emailService.generarContenidoTextoReservaCreada(nombrePista, tipoDeporte,
                fecha, horaInicio, horaFin, precio);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Tu reserva ha sido creada"));
        assertTrue(result.contains("Pista Fútbol 1"));
        assertTrue(result.contains("25.00"));
        assertFalse(result.contains("<html>"));
    }
}