package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Tarifa;
import com.deustosport.my_app.enums.TipoDeporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

    List<Tarifa> findByTipoDeporte(TipoDeporte tipoDeporte);

    List<Tarifa> findByTipoDeporteAndDiaSemana(TipoDeporte tipoDeporte, Integer diaSemana);

    List<Tarifa> findByActivaTrue();

    @Query("SELECT t FROM Tarifa t WHERE t.tipoDeporte = :tipoDeporte " +
           "AND t.diaSemana = :diaSemana " +
           "AND t.vigenteDesde <= :fecha " +
           "AND (t.vigenteHasta IS NULL OR t.vigenteHasta >= :fecha) " +
           "AND t.activa = true")
    List<Tarifa> findActiveByDeporteDiaAndFecha(
            @Param("tipoDeporte") TipoDeporte tipoDeporte,
            @Param("diaSemana") Integer diaSemana,
            @Param("fecha") LocalDate fecha
    );
}
