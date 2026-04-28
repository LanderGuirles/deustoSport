package com.deustosport.my_app.service;

import net.glxn.qrgen.javase.QRCode;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QRCodeService {

    /**
     * Genera un código QR con el texto proporcionado y lo devuelve como array de bytes en formato PNG.
     *
     * @param text El texto a codificar en el QR
     * @return Array de bytes de la imagen PNG del QR
     */
    public byte[] generateQRCode(String text) {
        ByteArrayOutputStream stream = QRCode.from(text).withSize(200, 200).stream();
        return stream.toByteArray();
    }

    /**
     * Genera un código QR con tamaño estándar (200x200) para una reserva.
     *
     * @param reservaId ID de la reserva
     * @param usuarioId ID del usuario
     * @param pistaNombre Nombre de la pista
     * @param fecha Fecha de la reserva
     * @param horaInicio Hora de inicio
     * @return Array de bytes del QR
     */
    public byte[] generateReservaQR(Long reservaId, Long usuarioId, String pistaNombre,
                                    String fecha, String horaInicio) {
        String qrText = String.format("RESERVA-%d-USUARIO-%d-PISTA-%s-FECHA-%s-HORA-%s",
                                      reservaId, usuarioId, pistaNombre, fecha, horaInicio);
        return generateQRCode(qrText);
    }
}