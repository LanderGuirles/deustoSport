package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Ayuntamiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AyuntamientoRepository extends JpaRepository<Ayuntamiento, Long> {
}
