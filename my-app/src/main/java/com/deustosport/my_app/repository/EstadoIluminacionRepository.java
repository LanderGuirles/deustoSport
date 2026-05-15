package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.EstadoIluminacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoIluminacionRepository extends JpaRepository<EstadoIluminacion, Long> {

    Optional<EstadoIluminacion> findByPistaId(Long pistaId);
}
