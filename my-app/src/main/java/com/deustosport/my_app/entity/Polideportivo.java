package com.deustosport.my_app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "polideportivos")
public class Polideportivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime horaApertura = LocalTime.of(8, 0);

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime horaCierre = LocalTime.of(22, 0);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ayuntamiento_id", nullable = false)
    private Ayuntamiento ayuntamiento;

    @OneToMany(mappedBy = "polideportivo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pista> pistas = new ArrayList<>();
}
