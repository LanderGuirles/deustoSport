package com.deustosport.my_app.repository;

import com.deustosport.my_app.entity.PlanAbono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanAbonoRepository extends JpaRepository<PlanAbono, Long> {

}