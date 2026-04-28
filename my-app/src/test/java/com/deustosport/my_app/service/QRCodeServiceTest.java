package com.deustosport.my_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

    @InjectMocks
    private QRCodeService qrCodeService;

    @Test
    void generateQRCode_debeGenerarArrayDeBytes() {
        // Given
        String text = "TEST-QR-CODE";

        // When
        byte[] result = qrCodeService.generateQRCode(text);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Un QR básico debería tener al menos algunos bytes
        assertTrue(result.length > 100);
    }

    @Test
    void generateReservaQR_debeGenerarQRConDatosReserva() {
        // Given
        Long reservaId = 123L;
        Long usuarioId = 456L;
        String pistaNombre = "Pista Fútbol 1";
        String fecha = "2024-12-25";
        String horaInicio = "10:00";

        // When
        byte[] result = qrCodeService.generateReservaQR(reservaId, usuarioId, pistaNombre, fecha, horaInicio);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}