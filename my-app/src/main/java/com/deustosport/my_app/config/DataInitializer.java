package com.deustosport.my_app.config;

import com.deustosport.my_app.entity.Tarifa;
import com.deustosport.my_app.enums.TipoDeporte;
import com.deustosport.my_app.repository.TarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Override
    public void run(String... args) throws Exception {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuarios", Integer.class);

        if (count != null && count > 0) {
            System.out.println("✅ Datos de prueba ya existen, saltando inicialización");
            return;
        }

        System.out.println("🚀 Primera ejecución: inicializando datos de prueba...");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("password123");
        String hashBilbao = encoder.encode("bilbao");

        // ── Instalación ──────────────────────────────────────────────
        jdbcTemplate.update(
            "INSERT INTO instalaciones (nombre, direccion, hora_apertura, hora_cierre) VALUES (?,?,?,?)",
            "Polideportivo Deusto", "Calle Agirre, 1, Bilbao", "08:00:00", "22:00:00"
        );

        // ── Pistas (instalacion_id = 1) ───────────────────────────────
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Pádel 1", "PADEL", 1, 4, true);
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Pádel 2", "PADEL", 1, 4, true);
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Tenis 1", "TENIS", 1, 4, true);
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Tenis 2", "TENIS", 1, 4, true);
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Fútbol 1", "FUTBOL", 1, 22, true);

        // ── Usuarios ─────────────────────────────────────────────────
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol) VALUES (?,?,?,?,?,?,?)",
            "Juan García", "juan@deustosport.com", "11111111A", "666111222", true, false, "CLIENTE");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol) VALUES (?,?,?,?,?,?,?)",
            "María López", "maria@deustosport.com", "22222222B", "666333444", true, true, "SECRETARIA");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol) VALUES (?,?,?,?,?,?,?)",
            "Carlos Rodríguez", "carlos@deustosport.com", "33333333C", "666555666", true, false, "COORDINADOR");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol) VALUES (?,?,?,?,?,?,?)",
            "Laura Fernández", "laura@deustosport.com", "44444444D", "666777888", true, false, "CLIENTE");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol) VALUES (?,?,?,?,?,?,?)",
            "Iker Martínez", "iker@deustosport.com", "55555555E", "666999000", true, true, "CLIENTE");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol) VALUES (?,?,?,?,?,?,?)",
            "Nerea Sánchez", "nerea@deustosport.com", "66666666F", "666123789", true, false, "CLIENTE");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol) VALUES (?,?,?,?,?,?,?)",
            "Ayuntamiento de Bilbao", "ayuntamiento.bilbao@deustosport.com",
            "77777777G", "944204200", true, false, "AYUNTAMIENTO");

        // ── Credenciales ─────────────────────────────────────────────
        LocalDateTime ahora = LocalDateTime.now();
        for (int i = 1; i <= 6; i++) {
            jdbcTemplate.update(
                "INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (?,?,?,?)",
                i, hash, true, ahora);
        }
        jdbcTemplate.update(
            "INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (?,?,?,?)",
            7, hashBilbao, true, ahora);

        // ── Tarifas ──────────────────────────────────────────────────
        TipoDeporte[] deportes  = {TipoDeporte.PADEL, TipoDeporte.TENIS, TipoDeporte.FUTBOL};
        BigDecimal[]  valle     = {new BigDecimal("8.00"),  new BigDecimal("7.00"),  new BigDecimal("12.00")};
        BigDecimal[]  punta     = {new BigDecimal("15.00"), new BigDecimal("13.00"), new BigDecimal("20.00")};
        BigDecimal[]  finde     = {new BigDecimal("18.00"), new BigDecimal("16.00"), new BigDecimal("25.00")};
        LocalDate     desde     = LocalDate.of(2026, 1, 1);

        for (int i = 0; i < deportes.length; i++) {
            // Lunes–Viernes hora valle (8-17)
            for (int dia = 1; dia <= 5; dia++) {
                Tarifa t = new Tarifa();
                t.setTipoDeporte(deportes[i]);
                t.setDiaSemana(dia);
                t.setHoraInicio(LocalTime.of(8, 0));
                t.setHoraFin(LocalTime.of(17, 0));
                t.setPrecioPorHora(valle[i]);
                t.setVigenteDesde(desde);
                t.setActiva(true);
                tarifaRepository.save(t);
            }
            // Lunes–Viernes hora punta (17-22)
            for (int dia = 1; dia <= 5; dia++) {
                Tarifa t = new Tarifa();
                t.setTipoDeporte(deportes[i]);
                t.setDiaSemana(dia);
                t.setHoraInicio(LocalTime.of(17, 0));
                t.setHoraFin(LocalTime.of(22, 0));
                t.setPrecioPorHora(punta[i]);
                t.setVigenteDesde(desde);
                t.setActiva(true);
                tarifaRepository.save(t);
            }
            // Sábado y domingo (8-22)
            for (int dia = 6; dia <= 7; dia++) {
                Tarifa t = new Tarifa();
                t.setTipoDeporte(deportes[i]);
                t.setDiaSemana(dia);
                t.setHoraInicio(LocalTime.of(8, 0));
                t.setHoraFin(LocalTime.of(22, 0));
                t.setPrecioPorHora(finde[i]);
                t.setVigenteDesde(desde);
                t.setActiva(true);
                tarifaRepository.save(t);
            }
        }

        System.out.println("✅ Datos de prueba inicializados correctamente");
        System.out.println("   Usuarios: juan@deustosport.com / password123");
        System.out.println("   Pistas: 5 | Tarifas: " + tarifaRepository.count());
    }
}