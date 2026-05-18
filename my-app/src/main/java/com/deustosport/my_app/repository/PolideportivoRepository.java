package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Polideportivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolideportivoRepository extends JpaRepository<Polideportivo, Long> {
    List<Polideportivo> findByAyuntamientoId(Long ayuntamientoId);
}
