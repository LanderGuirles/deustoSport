package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Incidencia;
import com.deustosport.my_app.enums.EstadoIncidencia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findAllByOrderByFechaCreacionDesc();

    List<Incidencia> findByEstadoOrderByFechaCreacionDesc(EstadoIncidencia estado);

    List<Incidencia> findByPolideportivoId(Long polideportivoId);
}