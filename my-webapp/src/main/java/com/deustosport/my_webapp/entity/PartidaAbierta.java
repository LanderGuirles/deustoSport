package com.deustosport.my_webapp.entity;

import com.deustosport.my_webapp.enums.EstadoPartidaAbierta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "partidas_abiertas")
public class PartidaAbierta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", unique = true)
    private Reserva reserva;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creada_por_usuario_id")
    private Usuario creadaPor;

    @Column(nullable = false, length = 80)
    private String titulo;

    @Column(nullable = false)
    private Integer maxJugadores;

    @Column(nullable = false)
    private Integer jugadoresActuales = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPartidaAbierta estado = EstadoPartidaAbierta.ABIERTA;

    @Column(nullable = false)
    private LocalDateTime fechaLimiteUnion;
}
