package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Polideportivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolideportivoRepository extends JpaRepository<Polideportivo, Long> {
    List<Polideportivo> findByAyuntamientoId(Long ayuntamientoId);
    long countByAyuntamientoId(Long ayuntamientoId);

    @Query("SELECT COUNT(p) > 0 FROM Polideportivo p " +
           "WHERE LOWER(p.nombre) = LOWER(:nombre) AND LOWER(p.direccion) = LOWER(:direccion)")
    boolean existsByNombreAndDireccionIgnoreCase(
            @Param("nombre") String nombre,
            @Param("direccion") String direccion);

    @Query("SELECT COUNT(p) > 0 FROM Polideportivo p " +
           "WHERE p.id <> :id " +
           "AND LOWER(p.nombre) = LOWER(:nombre) AND LOWER(p.direccion) = LOWER(:direccion)")
    boolean existsByNombreAndDireccionIgnoreCaseExcludingId(
            @Param("id") Long id,
            @Param("nombre") String nombre,
            @Param("direccion") String direccion);
}
