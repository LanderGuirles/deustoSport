package com.deustosport.my_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import com.deustosport.my_app.enums.DuracionAbonos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tarifa_abono")
public class TarifaAbono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_abono_id", nullable = false)
    private PlanAbono planAbono;

    @Column(nullable = false)
    private DuracionAbonos duracion;

    @Column(nullable = false)
    private Integer edadMinima;

    @Column(nullable = false)
    private Integer edadMax;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;    
}
