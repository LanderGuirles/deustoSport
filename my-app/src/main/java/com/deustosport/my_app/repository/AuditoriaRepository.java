package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByOrderByFechaHoraDesc();

    List<Auditoria> findByUsuarioOrderByFechaHoraDesc(String usuario);

    List<Auditoria> findByEntidadOrderByFechaHoraDesc(String entidad);

    List<Auditoria> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Query("SELECT a FROM Auditoria a ORDER BY a.fechaHora DESC")
    List<Auditoria> findTopByOrderByFechaHoraDesc(int limit);

    long countByAccion(String accion);

    @Query("SELECT a.usuario, COUNT(a) as total FROM Auditoria a GROUP BY a.usuario ORDER BY total DESC")
    List<Object[]> findTopUsuariosByActividad(int limit);

    void deleteByFechaHoraBefore(LocalDateTime fechaLimite);

    @Query("SELECT a FROM Auditoria a WHERE a.entidad = :entidad AND a.entidadId = :entidadId ORDER BY a.fechaHora DESC")
    List<Auditoria> findByEntidadAndEntidadIdOrderByFechaHoraDesc(@Param("entidad") String entidad, @Param("entidadId") Long entidadId);

    @Query("SELECT a FROM Auditoria a WHERE a.accion = :accion AND a.fechaHora >= :fechaInicio ORDER BY a.fechaHora DESC")
    List<Auditoria> findByAccionAndFechaHoraAfterOrderByFechaHoraDesc(@Param("accion") String accion, @Param("fechaInicio") LocalDateTime fechaInicio);

    @Query("SELECT COUNT(a) FROM Auditoria a WHERE a.usuario = :usuario AND a.fechaHora >= :fechaInicio")
    long countByUsuarioAndFechaHoraAfter(@Param("usuario") String usuario, @Param("fechaInicio") LocalDateTime fechaInicio);

    @Query("SELECT DISTINCT a.entidad FROM Auditoria a")
    List<String> findDistinctEntidades();

    @Query("SELECT DISTINCT a.accion FROM Auditoria a")
    List<String> findDistinctAcciones();

    @Query("SELECT a FROM Auditoria a WHERE LOWER(a.detalles) LIKE LOWER(CONCAT('%', :busqueda, '%')) ORDER BY a.fechaHora DESC")
    List<Auditoria> findByDetallesContainingIgnoreCase(@Param("busqueda") String busqueda);
}