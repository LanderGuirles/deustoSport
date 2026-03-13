package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.BonoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BonoUsuarioRepository extends JpaRepository<BonoUsuario, Long> {

    List<BonoUsuario> findByUsuarioId(Long usuarioId);

    List<BonoUsuario> findByUsuarioIdAndActivoTrue(Long usuarioId);

    @Query("SELECT b FROM BonoUsuario b WHERE b.usuario.id = :usuarioId " +
           "AND b.activo = true " +
           "AND b.fechaExpiracion >= :fechaHoy")
    List<BonoUsuario> findValidosByUsuarioIdAndFecha(
            @Param("usuarioId") Long usuarioId,
            @Param("fechaHoy") LocalDate fechaHoy
    );

    Optional<BonoUsuario> findByIdAndActivoTrue(Long bonoUsuarioId);
}
