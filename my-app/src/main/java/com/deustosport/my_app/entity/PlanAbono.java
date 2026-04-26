package com.deustosport.my_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import com.deustosport.my_app.enums.DuracionAbonos;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plan_abono")
public class PlanAbono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @Column(nullable = false)
    private Integer cantidadPersonas;

    @Column(nullable = false)
    private Integer edadMinima;

    @Column(nullable = false)
    private Integer edadMax;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DuracionAbonos duracion;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoPistasPorcentaje;

    @Column(nullable = false)
    private boolean activo = true;
}