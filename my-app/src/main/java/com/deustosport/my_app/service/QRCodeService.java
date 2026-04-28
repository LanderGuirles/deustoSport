package com.deustosport.my_app.service;

import net.glxn.qrgen.javase.QRCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class QRCodeService {

    @Value("${qr.storage.path:qrcodes}")
    private String qrStoragePath;

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

    /**
     * Genera y guarda el QR como PNG en disco. Devuelve la ruta relativa del archivo.
     *
     * @param reservaId ID de la reserva
     * @param usuarioId ID del usuario
     * @param pistaNombre Nombre de la pista
     * @param fecha Fecha de la reserva
     * @param horaInicio Hora de inicio
     * @return Ruta relativa del archivo PNG generado
     * @throws IOException Si hay error al guardar el archivo
     */
    public String generateAndSaveReservaQR(Long reservaId, Long usuarioId, String pistaNombre,
                                           String fecha, String horaInicio) throws IOException {
        // Crear directorio si no existe
        Path directory = Paths.get(qrStoragePath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // Nombre del archivo basado en ID de reserva
        String filename = "reserva_" + reservaId + ".png";
        Path filePath = directory.resolve(filename);

        // Generar QR
        byte[] qrBytes = generateReservaQR(reservaId, usuarioId, pistaNombre, fecha, horaInicio);

        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(qrBytes);
        }

        // Devolver ruta relativa para la URL
        return "/api/qr/" + filename;
    }

    /**
     * Lee el archivo PNG del QR desde disco.
     *
     * @param filename Nombre del archivo (ej: reserva_123.png)
     * @return Array de bytes del PNG
     * @throws IOException Si el archivo no existe o hay error de lectura
     */
    public byte[] readQRFile(String filename) throws IOException {
        Path filePath = Paths.get(qrStoragePath, filename);
        if (!Files.exists(filePath)) {
            throw new IOException("Archivo QR no encontrado: " + filename);
        }
        return Files.readAllBytes(filePath);
    }
}