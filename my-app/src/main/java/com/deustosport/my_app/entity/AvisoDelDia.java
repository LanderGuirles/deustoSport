package com.deustosport.my_app.entity;

import com.deustosport.my_app.enums.TipoAviso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa un aviso del día publicado por secretaría.
 *
 * Un aviso del día es un mensaje corto, visible en la pantalla de inicio
 * de todos los usuarios, cuyo propósito es informar de incidencias o
 * novedades que afecten al uso de las instalaciones deportivas.
 *
 * Reglas de negocio principales:
 * <ul>
 *   <li>Puede existir más de un aviso activo simultáneamente.</li>
 *   <li>La pantalla de inicio mostrará el aviso activo con mayor prioridad
 *       y, dentro de igual prioridad, el más reciente.</li>
 *   <li>Un aviso con {@code fechaExpiracion} anterior a la fecha actual
 *       se considera expirado aunque su campo {@code activo} sea true;
 *       el servicio lo filtra y puede desactivarlo automáticamente.</li>
 *   <li>Sólo usuarios con rol SECRETARIA pueden crear, modificar o
 *       eliminar avisos (validación delegada al frontend y documentada
 *       en el controlador).</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "avisos_del_dia",
    indexes = {
        @Index(name = "idx_aviso_activo_prioridad", columnList = "activo, prioridad DESC"),
        @Index(name = "idx_aviso_fecha_creacion",   columnList = "fecha_creacion DESC"),
        @Index(name = "idx_aviso_fecha_expiracion", columnList = "fecha_expiracion")
    }
)
public class AvisoDelDia {

    // ─────────────────────────────────────────────────────────────
    // Clave primaria
    // ─────────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─────────────────────────────────────────────────────────────
    // Contenido del aviso
    // ─────────────────────────────────────────────────────────────

    /**
     * Título corto del aviso, visible como cabecera en el banner.
     * Longitud máxima: 120 caracteres.
     * Ejemplo: "Aviso de mantenimiento"
     */
    @Column(nullable = false, length = 120)
    private String titulo;

    /**
     * Cuerpo del aviso. Mensaje descriptivo que detalla la incidencia
     * o novedad comunicada a los usuarios.
     * Longitud máxima: 1000 caracteres.
     * Ejemplo: "Las duchas de la planta baja estarán fuera de servicio
     *            hasta las 16:00 h. de hoy por una avería en la instalación."
     */
    @Column(nullable = false, length = 1000)
    private String mensaje;

    /**
     * Clasificación del aviso según su nivel de impacto.
     * Determina el color y el icono del banner en la pantalla de inicio.
     * Valores: INFO | AVISO | URGENTE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAviso tipo;

    // ─────────────────────────────────────────────────────────────
    // Estado y vigencia
    // ─────────────────────────────────────────────────────────────

    /**
     * Indica si el aviso está activo y debe mostrarse a los usuarios.
     * Un aviso desactivado no se muestra aunque no haya expirado.
     */
    @Column(nullable = false)
    private boolean activo;

    /**
     * Fecha y hora exacta en que se creó el aviso.
     * Se establece automáticamente en el servicio al persistir el aviso;
     * no debe ser modificada por peticiones externas.
     */
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Fecha límite de visibilidad del aviso (inclusive).
     * Si es null, el aviso permanece activo indefinidamente hasta que
     * sea desactivado o eliminado manualmente.
     * Si la fecha es anterior a hoy, el aviso se considera expirado
     * y el servicio lo excluirá de las consultas de avisos activos.
     */
    @Column(name = "fecha_expiracion")
    private LocalDate fechaExpiracion;

    /**
     * Número de orden para resolver empates en la visualización cuando
     * existen varios avisos activos simultáneos.
     * Mayor número = mayor prioridad = aparece primero.
     * Rango recomendado: 0 (normal) a 10 (máxima urgencia).
     */
    @Column(nullable = false)
    private Integer prioridad;

    // ─────────────────────────────────────────────────────────────
    // Auditoría: quién creó el aviso
    // ─────────────────────────────────────────────────────────────

    /**
     * Nombre completo del empleado de secretaría que publicó el aviso.
     * Se almacena de forma desnormalizada para no crear dependencia fuerte
     * con la entidad Usuario y simplificar las consultas de auditoría.
     */
    @Column(name = "creado_por_nombre", length = 120)
    private String creadoPorNombre;

    /**
     * ID del usuario (secretaría) que creó el aviso.
     * Puede ser null si el aviso fue creado por un proceso del sistema.
     */
    @Column(name = "creado_por_id")
    private Long creadoPorId;

    // ─────────────────────────────────────────────────────────────
    // Auditoría: modificaciones
    // ─────────────────────────────────────────────────────────────

    /**
     * Fecha y hora de la última modificación del aviso.
     * Null si nunca ha sido editado desde su creación.
     */
    @Column(name = "fecha_ultima_modificacion")
    private LocalDateTime fechaUltimaModificacion;

    /**
     * Nombre del empleado que realizó la última modificación.
     * Null si el aviso no ha sido editado.
     */
    @Column(name = "modificado_por_nombre", length = 120)
    private String modificadoPorNombre;
}
