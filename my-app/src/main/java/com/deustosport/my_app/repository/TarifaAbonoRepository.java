package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.TarifaAbono;
import com.deustosport.my_app.enums.DuracionAbonos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaAbonoRepository extends JpaRepository<TarifaAbono, Long> {

    List<TarifaAbono> findByPlanAbonoId(Long planId);

    @Query("SELECT t FROM TarifaAbono t WHERE t.planAbono.id = :planId AND t.duracion = :duracion AND :edad BETWEEN t.edadMinima AND t.edadMax")
    Optional<TarifaAbono> findTarifaExactaParaUsuario(
            @Param("planId") Long planId, 
            @Param("duracion") DuracionAbonos duracion, 
            @Param("edad") int edad
    );
}