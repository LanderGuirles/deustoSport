package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstalacionRepository extends JpaRepository<Instalacion, Long> {
}
