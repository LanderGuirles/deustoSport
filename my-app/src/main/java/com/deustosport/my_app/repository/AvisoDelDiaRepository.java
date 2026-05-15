package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.AvisoDelDia;
import com.deustosport.my_app.enums.TipoAviso;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la gestión de persistencia de AvisoDelDia.
 *
 * Extiende JpaRepository para disponer de las operaciones CRUD básicas
 * (save, findById, findAll, delete…) y añade consultas específicas del
 * dominio mediante JPQL y métodos derivados de Spring Data.
 */
@Repository
public interface AvisoDelDiaRepository extends JpaRepository<AvisoDelDia, Long> {

    // ─────────────────────────────────────────────────────────────
    // Consultas de avisos activos (los que se muestran a usuarios)
    // ─────────────────────────────────────────────────────────────

    /**
     * Devuelve todos los avisos cuyo campo {@code activo} es true Y cuya
     * fecha de expiración es nula o posterior o igual a {@code hoy},
     * ordenados por prioridad descendente y, en caso de empate, por
     * fecha de creación descendente (el más reciente primero).
     *
     * Esta consulta es la principal para obtener qué avisos mostrar en
     * la pantalla de inicio de los usuarios.
     *
     * @param hoy fecha actual; se usa para filtrar avisos no expirados.
     * @return lista ordenada de avisos activos y vigentes.
     */
    @Query("""
        SELECT a FROM AvisoDelDia a
        WHERE a.activo = true
          AND (a.fechaExpiracion IS NULL OR a.fechaExpiracion >= :hoy)
        ORDER BY a.prioridad DESC, a.fechaCreacion DESC
        """)
    List<AvisoDelDia> findAvisosActivosVigentes(@Param("hoy") LocalDate hoy);

    /**
     * Devuelve el aviso de mayor prioridad (y más reciente en caso de
     * empate) que esté activo y vigente. Es el aviso principal que se
     * muestra de forma prominente en el banner de home.html.
     *
     * @param hoy fecha actual para filtrar expirados.
     * @return Optional con el aviso principal, vacío si no hay ninguno.
     */
    @Query("""
        SELECT a FROM AvisoDelDia a
        WHERE a.activo = true
          AND (a.fechaExpiracion IS NULL OR a.fechaExpiracion >= :hoy)
        ORDER BY a.prioridad DESC, a.fechaCreacion DESC
        LIMIT 1
        """)
    Optional<AvisoDelDia> findAvisoPrincipal(@Param("hoy") LocalDate hoy);

    // ─────────────────────────────────────────────────────────────
    // Consultas de listado para el panel de secretaría
    // ─────────────────────────────────────────────────────────────

    /**
     * Devuelve todos los avisos (activos e inactivos) ordenados por
     * fecha de creación descendente. Se usa en el panel de secretaría
     * para mostrar el historial completo de avisos publicados.
     *
     * @return lista completa de avisos, del más reciente al más antiguo.
     */
    List<AvisoDelDia> findAllByOrderByFechaCreacionDesc();

    /**
     * Devuelve únicamente los avisos marcados como activos, ordenados
     * por prioridad descendente y fecha de creación descendente.
     *
     * @return lista de avisos activos (pueden incluir expirados).
     */
    List<AvisoDelDia> findByActivoTrueOrderByPrioridadDescFechaCreacionDesc();

    /**
     * Devuelve únicamente los avisos marcados como inactivos (archivados),
     * ordenados por fecha de creación descendente.
     *
     * @return lista de avisos inactivos/archivados.
     */
    List<AvisoDelDia> findByActivoFalseOrderByFechaCreacionDesc();

    // ─────────────────────────────────────────────────────────────
    // Consultas por tipo de aviso
    // ─────────────────────────────────────────────────────────────

    /**
     * Devuelve todos los avisos de un tipo concreto (INFO, AVISO, URGENTE),
     * ordenados por fecha de creación descendente.
     *
     * @param tipo clasificación del aviso a filtrar.
     * @return lista de avisos del tipo indicado.
     */
    List<AvisoDelDia> findByTipoOrderByFechaCreacionDesc(TipoAviso tipo);

    /**
     * Devuelve los avisos activos de un tipo concreto, vigentes a la fecha
     * indicada, ordenados por prioridad y fecha de creación.
     *
     * @param tipo clasificación del aviso.
     * @param hoy  fecha actual para filtrar expirados.
     * @return lista de avisos activos del tipo indicado.
     */
    @Query("""
        SELECT a FROM AvisoDelDia a
        WHERE a.activo = true
          AND a.tipo = :tipo
          AND (a.fechaExpiracion IS NULL OR a.fechaExpiracion >= :hoy)
        ORDER BY a.prioridad DESC, a.fechaCreacion DESC
        """)
    List<AvisoDelDia> findActivosByTipo(@Param("tipo") TipoAviso tipo,
                                         @Param("hoy") LocalDate hoy);

    // ─────────────────────────────────────────────────────────────
    // Consultas de mantenimiento / limpieza automática
    // ─────────────────────────────────────────────────────────────

    /**
     * Devuelve los avisos que han superado su fecha de expiración pero
     * aún tienen el campo {@code activo} a true. Se usa para detectar
     * avisos "zombies" que deberían haberse desactivado automáticamente.
     *
     * @param hoy fecha actual; se buscan avisos expirados antes de hoy.
     * @return lista de avisos activos pero ya expirados.
     */
    @Query("""
        SELECT a FROM AvisoDelDia a
        WHERE a.activo = true
          AND a.fechaExpiracion IS NOT NULL
          AND a.fechaExpiracion < :hoy
        """)
    List<AvisoDelDia> findAvisosActivosExpirados(@Param("hoy") LocalDate hoy);

    /**
     * Desactiva masivamente todos los avisos cuya fecha de expiración
     * sea no nula y anterior a la fecha indicada. Operación de
     * mantenimiento que puede ejecutarse al arrancar la aplicación o
     * de forma programada para limpiar avisos caducados.
     *
     * @param hoy fecha actual de referencia.
     * @return número de filas actualizadas.
     */
    @Modifying
    @Query("""
        UPDATE AvisoDelDia a
        SET a.activo = false
        WHERE a.activo = true
          AND a.fechaExpiracion IS NOT NULL
          AND a.fechaExpiracion < :hoy
        """)
    int desactivarAvisosExpirados(@Param("hoy") LocalDate hoy);

    // ─────────────────────────────────────────────────────────────
    // Consultas de auditoría
    // ─────────────────────────────────────────────────────────────

    /**
     * Devuelve todos los avisos creados por un usuario concreto de
     * secretaría, identificado por su ID, ordenados del más reciente
     * al más antiguo.
     *
     * @param usuarioId ID del usuario de secretaría.
     * @return lista de avisos publicados por ese usuario.
     */
    List<AvisoDelDia> findByCreadoPorIdOrderByFechaCreacionDesc(Long usuarioId);

    /**
     * Cuenta cuántos avisos activos y vigentes hay en este momento.
     * Útil para mostrar un indicador/badge en el panel de secretaría.
     *
     * @param hoy fecha actual para filtrar expirados.
     * @return número de avisos activos vigentes.
     */
    @Query("""
        SELECT COUNT(a) FROM AvisoDelDia a
        WHERE a.activo = true
          AND (a.fechaExpiracion IS NULL OR a.fechaExpiracion >= :hoy)
        """)
    long countAvisosActivosVigentes(@Param("hoy") LocalDate hoy);
}
