package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import com.deustosport.my_app.enums.EstadoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByReservaId(Long reservaId);

    @Query("SELECT COALESCE(SUM(p.importe), 0) FROM Pago p " +
        "WHERE p.estadoPago = :estado " +
        "AND p.fechaPago >= :inicio " +
        "AND p.fechaPago < :fin")
    BigDecimal sumImporteByEstadoAndRangoFechas(
         @Param("estado") EstadoPago estado,
         @Param("inicio") LocalDateTime inicio,
         @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(p) FROM Pago p " +
        "WHERE p.estadoPago = :estado " +
        "AND p.fechaPago >= :inicio " +
        "AND p.fechaPago < :fin")
    long countByEstadoAndRangoFechas(
         @Param("estado") EstadoPago estado,
         @Param("inicio") LocalDateTime inicio,
         @Param("fin") LocalDateTime fin);
}