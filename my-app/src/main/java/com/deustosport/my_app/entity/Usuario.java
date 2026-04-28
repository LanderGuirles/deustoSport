package com.deustosport.my_app.entity;

import com.deustosport.my_app.enums.Rol;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(unique = true, length = 20)
    private String dni;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "es_socio", nullable = false)
    private boolean esSocio = false;

    @Column(nullable = false)
    private BigDecimal billetera = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    // ACTUALIZADO: mappedBy apunta al nuevo campo "titular"
    @OneToMany(mappedBy = "titular", cascade = CascadeType.ALL)
    private List<AbonoUsuario> abonos = new ArrayList<>();

    // Compatibility helpers for tests that used separate nombre/apellidos
    @jakarta.persistence.Transient
    private String password;

    public String getNombre() {
        if (this.nombreCompleto == null) return null;
        String[] parts = this.nombreCompleto.split(" ", 2);
        return parts.length > 0 ? parts[0] : this.nombreCompleto;
    }

    public String getApellidos() {
        if (this.nombreCompleto == null) return null;
        String[] parts = this.nombreCompleto.split(" ", 2);
        return parts.length > 1 ? parts[1] : "";
    }

    public void setNombre(String nombre) {
        String ap = getApellidos();
        if (ap == null || ap.isEmpty()) {
            this.nombreCompleto = nombre;
        } else {
            this.nombreCompleto = nombre + " " + ap;
        }
    }

    public void setApellidos(String apellidos) {
        String nom = getNombre();
        if (nom == null || nom.isEmpty()) {
            this.nombreCompleto = apellidos;
        } else {
            this.nombreCompleto = nom + " " + apellidos;
        }
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}