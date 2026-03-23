package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Pista;
import com.deustosport.my_app.enums.TipoDeporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PistaRepository extends JpaRepository<Pista, Long> {

    boolean existsByNombreIgnoreCase(String nombre); 

    List<Pista> findByTipoDeporte(TipoDeporte tipoDeporte);

    List<Pista> findByInstalacionId(Long instalacionId);

    List<Pista> findByActivaTrue();
}
