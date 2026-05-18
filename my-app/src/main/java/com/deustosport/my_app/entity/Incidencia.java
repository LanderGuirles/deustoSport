package com.deustosport.my_app.entity;

import com.deustosport.my_app.enums.EstadoIncidencia;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "incidencias")
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reportada_por_id")
    private Usuario reportadaPor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "polideportivo_id")
    private Polideportivo polideportivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pista_id")
    private Pista pista;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoIncidencia estado = EstadoIncidencia.ABIERTA;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(length = 120)
    private String ubicacionEspecifica;

    @PrePersist
    public void onCreate() {
        LocalDateTime ahora = LocalDateTime.now();
        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = ahora;
        }
    }

    @PreUpdate
    public void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}