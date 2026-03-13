package com.deustosport.my_webapp.repository;

import com.deustosport.my_webapp.entity.HorarioPista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioPistaRepository extends JpaRepository<HorarioPista, Long> {

    List<HorarioPista> findByPistaId(Long pistaId);

    List<HorarioPista> findByPistaIdAndDiaSemana(Long pistaId, Integer diaSemana);

    List<HorarioPista> findByDisponibleTrue();
}
