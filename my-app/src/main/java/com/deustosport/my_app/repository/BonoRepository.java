package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Bono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonoRepository extends JpaRepository<Bono, Long> {

    List<Bono> findByActivoTrue();
}
