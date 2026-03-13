package com.deustosport.my_webapp.repository;

import com.deustosport.my_webapp.entity.Pista;
import com.deustosport.my_webapp.enums.TipoDeporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PistaRepository extends JpaRepository<Pista, Long> {

    List<Pista> findByTipoDeporte(TipoDeporte tipoDeporte);

    List<Pista> findByInstalacionId(Long instalacionId);

    List<Pista> findByActivaTrue();
}
