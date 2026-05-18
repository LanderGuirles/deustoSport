package com.deustosport.my_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

    private static final Logger log = LoggerFactory.getLogger(QRCodeServiceTest.class);

    @InjectMocks
    private QRCodeService qrCodeService;

    @Test
    void generateQRCode_debeGenerarArrayDeBytes() {
        log.info("[TEST] generateQRCode - debe generar array de bytes no vacío");
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