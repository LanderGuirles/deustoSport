package com.deustosport.my_app.entity;

import com.deustosport.my_app.enums.AmbitoAbono;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "abonos_usuario")
public class AbonoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Titular del abono (el que paga)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_id", nullable = false)
    private Usuario titular;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanAbono plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmbitoAbono ambito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "polideportivo_id")
    private Polideportivo polideportivo; // Solo si ambito == LOCAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ayuntamiento_id")
    private Ayuntamiento ayuntamiento; // Necesario si ambito == CIUDAD (para saber de qué ciudad son todos los polis)

    // Lista de beneficiarios (familiares/amigos incluidos en el plan)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "abono_beneficiarios",
        joinColumns = @JoinColumn(name = "abono_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<Usuario> beneficiarios = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private boolean activo = true;
}