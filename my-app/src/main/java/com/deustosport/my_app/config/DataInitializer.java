package com.deustosport.my_app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Verificar si ya existen usuarios
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuarios", Integer.class);
        if (count != null && count > 0) {
            System.out.println("✅ Datos de prueba ya existen, saltando inicialización");
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("password123");
        System.out.println("🔐 Hash generado: " + hashedPassword);

        // Insertar usuarios
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, telefono, activo) VALUES (?, ?, ?, ?)",
            "Juan García", "juan@deustosport.com", "666111222", true
        );
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, telefono, activo) VALUES (?, ?, ?, ?)",
            "María López", "maria@deustosport.com", "666333444", true
        );
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, telefono, activo) VALUES (?, ?, ?, ?)",
            "Carlos Rodríguez", "carlos@deustosport.com", "666555666", true
        );

        // Insertar credenciales con hash generado dinámicamente
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

        System.out.println("✅ Datos de prueba inicializados correctamente");
        System.out.println("Usuario: juan@deustosport.com / Contraseña: password123");
    }
}
