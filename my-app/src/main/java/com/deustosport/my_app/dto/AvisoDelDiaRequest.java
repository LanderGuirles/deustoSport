package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.TipoAviso;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para crear o actualizar un aviso del día.
 *
 * Enviado por el cliente (panel de secretaría) en el cuerpo JSON
 * de las peticiones POST y PUT al endpoint /api/avisos.
 *
 * Campos obligatorios: titulo, mensaje, tipo.
 * Campos opcionales:   fechaExpiracion (null = sin caducidad),
 *                      prioridad       (null = 0 por defecto),
 *                      creadoPorId     (null admitido),
 *                      creadoPorNombre (null admitido).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvisoDelDiaRequest {

    /**
     * Título visible en la cabecera del banner de aviso.
     * Máximo 120 caracteres. No debe estar vacío.
     */
    private String titulo;

    /**
     * Cuerpo del aviso con la descripción detallada de la incidencia
     * o novedad que se comunica a los usuarios.
     * Máximo 1000 caracteres. No debe estar vacío.
     */
    private String mensaje;

    /**
     * Nivel de impacto del aviso: INFO, AVISO o URGENTE.
     * Determina el color y el icono del banner en home.html.
     */
    private TipoAviso tipo;

    /**
     * Fecha hasta la cual el aviso es relevante (inclusive).
     * Si se omite o se envía null, el aviso no tiene caducidad
     * automática y deberá desactivarse manualmente.
     */
    private LocalDate fechaExpiracion;

    /**
     * Nivel de prioridad para resolver empates de visibilidad.
     * Si se omite o se envía null, el servicio asignará 0 por defecto.
     * Rango recomendado: 0 (normal) – 10 (máxima urgencia).
     */
    private Integer prioridad;

    /**
     * ID del usuario de secretaría que publica el aviso.
     * Se obtiene de localStorage en el frontend y se envía
     * para trazabilidad. Puede ser null.
     */
    private Long creadoPorId;

    /**
     * Nombre completo del usuario de secretaría que publica el aviso.
     * Se obtiene de localStorage en el frontend y se almacena
     * de forma desnormalizada para auditoría. Puede ser null.
     */
    private String creadoPorNombre;
}
