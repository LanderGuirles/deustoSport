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
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
        if (count != null && count > 0) {
            System.out.println("✅ Datos de prueba ya existen, saltando inicialización");
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("password123");
        System.out.println("🔐 Hash generado: " + hashedPassword);

        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo) VALUES (?, ?, ?, ?, ?)",
            "Juan García", "juan@deustosport.com", "11111111A", "666111222", true
        );
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo) VALUES (?, ?, ?, ?, ?)",
            "María López", "maria@deustosport.com", "22222222B", "666333444", true
        );
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo) VALUES (?, ?, ?, ?, ?)",
            "Carlos Rodríguez", "carlos@deustosport.com", "33333333C", "666555666", true
        );

        jdbcTemplate.update(
            "INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (?, ?, ?, ?)",
            1, hashedPassword, true, LocalDateTime.now()
        );
        jdbcTemplate.update(
            "INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (?, ?, ?, ?)",
            2, hashedPassword, true, LocalDateTime.now()
        );
        jdbcTemplate.update(
            "INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (?, ?, ?, ?)",
            3, hashedPassword, true, LocalDateTime.now()
        );

        // --- Tarifas por deporte, día y franja horaria ---
        TipoDeporte[] deportes = {TipoDeporte.PADEL, TipoDeporte.TENIS, TipoDeporte.FUTBOL};
        BigDecimal[] precioValle = {new BigDecimal("8.00"), new BigDecimal("7.00"), new BigDecimal("12.00")};
        BigDecimal[] precioPunta = {new BigDecimal("15.00"), new BigDecimal("13.00"), new BigDecimal("20.00")};
        BigDecimal[] precioFinde = {new BigDecimal("18.00"), new BigDecimal("16.00"), new BigDecimal("25.00")};

        for (int i = 0; i < deportes.length; i++) {
            TipoDeporte deporte = deportes[i];

            // Lunes a viernes — hora valle (8:00 - 17:00)
            for (int dia = 1; dia <= 5; dia++) {
                Tarifa valle = new Tarifa();
                valle.setTipoDeporte(deporte);
                valle.setDiaSemana(dia);
                valle.setHoraInicio(LocalTime.of(8, 0));
                valle.setHoraFin(LocalTime.of(17, 0));
                valle.setPrecioPorHora(precioValle[i]);
                valle.setVigenteDesde(LocalDate.of(2026, 1, 1));
                valle.setActiva(true);
                tarifaRepository.save(valle);
            }

            // Lunes a viernes — hora punta (17:00 - 22:00)
            for (int dia = 1; dia <= 5; dia++) {
                Tarifa punta = new Tarifa();
                punta.setTipoDeporte(deporte);
                punta.setDiaSemana(dia);
                punta.setHoraInicio(LocalTime.of(17, 0));
                punta.setHoraFin(LocalTime.of(22, 0));
                punta.setPrecioPorHora(precioPunta[i]);
                punta.setVigenteDesde(LocalDate.of(2026, 1, 1));
                punta.setActiva(true);
                tarifaRepository.save(punta);
            }

            // Sábado (6) y domingo (7) — precio especial todo el día
            for (int dia = 6; dia <= 7; dia++) {
                Tarifa finde = new Tarifa();
                finde.setTipoDeporte(deporte);
                finde.setDiaSemana(dia);
                finde.setHoraInicio(LocalTime.of(8, 0));
                finde.setHoraFin(LocalTime.of(22, 0));
                finde.setPrecioPorHora(precioFinde[i]);
                finde.setVigenteDesde(LocalDate.of(2026, 1, 1));
                finde.setActiva(true);
                tarifaRepository.save(finde);
            }
        }

        System.out.println("✅ Datos de prueba inicializados correctamente");
        System.out.println("Usuario: juan@deustosport.com / Contraseña: password123");
        System.out.println("Tarifas cargadas: " + tarifaRepository.count());
    }
}