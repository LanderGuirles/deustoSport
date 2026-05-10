package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Festivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FestivoRepository extends JpaRepository<Festivo, Long> {

    @Query("SELECT f FROM Festivo f WHERE :fecha BETWEEN f.fechaInicio AND f.fechaFin")
    List<Festivo> findFestivosEnFecha(@Param("fecha") LocalDate fecha);

    @Query("SELECT f FROM Festivo f WHERE (f.fechaInicio BETWEEN :inicio AND :fin) OR (f.fechaFin BETWEEN :inicio AND :fin)")
    List<Festivo> findFestivosEnRango(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);
}
