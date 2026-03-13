package com.deustosport.my_app.entity;

import com.deustosport.my_app.enums.TipoDeporte;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "pistas",
        indexes = {
                @Index(name = "idx_pista_tipo_deporte", columnList = "tipoDeporte"),
                @Index(name = "idx_pista_instalacion", columnList = "instalacion_id")
        }
)
public class Pista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDeporte tipoDeporte;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "instalacion_id")
    private Instalacion instalacion;

    @Column(nullable = false)
    private Integer maxJugadores;

    @Column(nullable = false)
    private boolean activa = true;
}
