package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    @Query("SELECT u FROM Usuario u " +
           "WHERE (:dni IS NULL OR LOWER(u.dni) LIKE LOWER(CONCAT('%', :dni, '%'))) " +
           "ORDER BY u.nombreCompleto ASC")
    List<Usuario> buscarParaSecretaria(@Param("dni") String dni);
}
