package com.deustosport.my_webapp.repository;

import com.deustosport.my_webapp.entity.ParticipantePartidaAbierta;
import com.deustosport.my_webapp.enums.EstadoParticipacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantePartidaAbiertaRepository extends JpaRepository<ParticipantePartidaAbierta, Long> {

    List<ParticipantePartidaAbierta> findByPartidaAbiertaId(Long partidaAbiertaId);

    List<ParticipantePartidaAbierta> findByUsuarioId(Long usuarioId);

    List<ParticipantePartidaAbierta> findByEstado(EstadoParticipacion estado);

    @Query("SELECT p FROM ParticipantePartidaAbierta p WHERE p.partidaAbierta.id = :partidaAbiertaId " +
           "AND p.estado != 'EXPULSADO'")
    List<ParticipantePartidaAbierta> findActiveParticipantsByMatchId(@Param("partidaAbiertaId") Long partidaAbiertaId);

    Optional<ParticipantePartidaAbierta> findByPartidaAbiertaIdAndUsuarioId(Long partidaAbiertaId, Long usuarioId);

    boolean existsByPartidaAbiertaIdAndUsuarioIdAndEstadoNot(
            Long partidaAbiertaId,
            Long usuarioId,
            EstadoParticipacion estado
    );
}
