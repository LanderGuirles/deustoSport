package com.deustosport.my_app.dto;

import com.deustosport.my_app.enums.TipoAviso;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de salida que representa un aviso del día serializado para el cliente.
 *
 * Devuelto por el backend en todas las respuestas GET, POST y PUT
 * relacionadas con el recurso /api/avisos.
 *
 * Incluye todos los campos de la entidad AvisoDelDia que son relevantes
 * para el frontend, más el campo calculado {@code expirado} que indica
 * si el aviso ha superado su fecha de expiración.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvisoDelDiaResponse {

    /** Identificador único del aviso en base de datos. */
    private Long id;

    /**
     * Título visible en la cabecera del banner.
     */
    private String titulo;

    /**
     * Cuerpo del aviso con la descripción de la incidencia o novedad.
     */
    private String mensaje;

    /**
     * Nivel de impacto: INFO | AVISO | URGENTE.
     * El frontend usa este valor para seleccionar el color y el icono
     * del banner (verde / naranja / rojo).
     */
    private TipoAviso tipo;

    /**
     * true si el aviso está marcado como activo y debe mostrarse
     * en la pantalla de inicio de los usuarios.
     */
    private boolean activo;

    /**
     * Fecha y hora exacta en que el aviso fue publicado.
     */
    private LocalDateTime fechaCreacion;

    /**
     * Fecha límite de visibilidad del aviso.
     * Null si el aviso no tiene caducidad automática.
     */
    private LocalDate fechaExpiracion;

    /**
     * Prioridad de visualización. Mayor número = aparece antes
     * cuando hay varios avisos activos simultáneos.
     */
    private Integer prioridad;

    /**
     * Nombre del empleado de secretaría que publicó el aviso.
     * Puede ser null si no se registró en el momento de la creación.
     */
    private String creadoPorNombre;

    /**
     * ID del empleado de secretaría que publicó el aviso.
     */
    private Long creadoPorId;

    /**
     * Fecha y hora de la última edición del aviso.
     * Null si el aviso no ha sido modificado desde su creación.
     */
    private LocalDateTime fechaUltimaModificacion;

    /**
     * Nombre del empleado que realizó la última edición.
     * Null si el aviso no ha sido modificado.
     */
    private String modificadoPorNombre;

    /**
     * Campo calculado: true si {@code fechaExpiracion} es no nula
     * y anterior a la fecha actual. El backend lo calcula en el
     * servicio antes de devolver la respuesta; el frontend lo usa
     * para mostrar visualmente los avisos caducados en el panel
     * de secretaría (con estilo atenuado).
     */
    private boolean expirado;
}
