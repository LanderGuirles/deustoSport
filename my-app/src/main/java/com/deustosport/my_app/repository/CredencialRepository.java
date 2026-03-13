package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredencialRepository extends JpaRepository<Credencial, Long> {

    Optional<Credencial> findByUsuarioId(Long usuarioId);

    Optional<Credencial> findByTokenRecuperacion(String token);
}
