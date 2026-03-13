package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.PartidaAbierta;
import com.deustosport.my_app.enums.EstadoPartidaAbierta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PartidaAbiertaRepository extends JpaRepository<PartidaAbierta, Long> {

    List<PartidaAbierta> findByEstado(EstadoPartidaAbierta estado);

    List<PartidaAbierta> findByCreadaPorId(Long usuarioId);

    @Query("SELECT p FROM PartidaAbierta p WHERE p.estado = 'ABIERTA' " +
           "AND p.fechaLimiteUnion > :ahora")
    List<PartidaAbierta> findOpenMatchesAvailable(@Param("ahora") LocalDateTime ahora);

    @Query("SELECT p FROM PartidaAbierta p WHERE p.reserva.pista.id = :pistaId " +
           "AND p.estado != 'CANCELADA'")
    List<PartidaAbierta> findByPistaId(@Param("pistaId") Long pistaId);
}
