package com.deustosport.my_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

import com.deustosport.my_app.enums.TipoPlanAbono;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "planAbono")
public class PlanAbono {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private TipoPlanAbono tipo ;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoPistasPorcentaje = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean permitePiscina = true;
}