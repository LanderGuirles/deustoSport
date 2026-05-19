package com.deustosport.my_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ayuntamientos")
public class Ayuntamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, unique = true, length = 20)
    private String cif;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "ayuntamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Polideportivo> polideportivos = new ArrayList<>();
}
