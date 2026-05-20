package com.deustosport.my_app.config;

import com.deustosport.my_app.entity.Credencial;
import com.deustosport.my_app.entity.Usuario;
import com.deustosport.my_app.enums.Rol;
import com.deustosport.my_app.repository.CredencialRepository;
import com.deustosport.my_app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Component
public class DataInitializer implements CommandLineRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CredencialRepository credencialRepository;

    @Override
    public void run(String... args) throws Exception {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuarios", Integer.class);

        if (count != null && count > 0) {
            log.info("Datos de prueba ya existen, saltando inicialización");
            asegurarCuentaMantenimiento();
            return;
        }

        log.info("Primera ejecución: inicializando datos de prueba...");

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("password123");
        String hashMantenimiento = encoder.encode("mantenimiento");
        String hashBilbao = encoder.encode("bilbao");

        // ── Ayuntamiento ─────────────────────────────────────────────
        jdbcTemplate.update(
            "INSERT INTO ayuntamientos (nombre, cif) VALUES (?,?)",
            "Ayuntamiento de Bilbao", "P4802000D"
        );

        // ── Polideportivo (ayuntamiento_id = 1) ───────────────────────
        jdbcTemplate.update(
            "INSERT INTO polideportivos (nombre, direccion, hora_apertura, hora_cierre, ayuntamiento_id) VALUES (?,?,?,?,?)",
            "Polideportivo Deusto", "Calle Agirre, 1, Bilbao", "08:00:00", "22:00:00", 1
        );

        // ── Pistas (polideportivo_id = 1) ──────────────────────────────
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, polideportivo_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Pádel 1", "PADEL", 1, 4, true);
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, polideportivo_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Pádel 2", "PADEL", 1, 4, true);
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, polideportivo_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Tenis 1", "TENIS", 1, 4, true);
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, polideportivo_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Tenis 2", "TENIS", 1, 4, true);
        jdbcTemplate.update(
            "INSERT INTO pistas (nombre, tipo_deporte, polideportivo_id, max_jugadores, activa) VALUES (?,?,?,?,?)",
            "Pista Fútbol 1", "FUTBOL", 1, 22, true);

        // ── Usuarios ─────────────────────────────────────────────────
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, fecha_nacimiento, telefono, activo, es_socio, billetera, rol) VALUES (?,?,?,?,?,?,?,?,?)",
            "Juan García", "juan@deustosport.com", "11111111A", LocalDate.of(1995, 10, 17), "666111222", true, false, 50.00, "CLIENTE");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, billetera, rol, polideportivo_id) VALUES (?,?,?,?,?,?,?,?,?)",
            "María López", "maria@deustosport.com", "22222222B", "666333444", true, true, 100.00, "SECRETARIA", 1);
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, billetera, rol, polideportivo_id) VALUES (?,?,?,?,?,?,?,?,?)",
            "Carlos Rodríguez", "carlos@deustosport.com", "33333333C", "666555666", true, false, 75.00, "COORDINADOR", 1);
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, billetera, rol) VALUES (?,?,?,?,?,?,?,?)",
            "Laura Fernández", "laura@deustosport.com", "44444444D", "666777888", true, false, 30.00, "CLIENTE");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, billetera, rol) VALUES (?,?,?,?,?,?,?,?)",
            "Iker Martínez", "iker@deustosport.com", "55555555E", "666999000", true, true, 120.00, "CLIENTE");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, billetera, rol) VALUES (?,?,?,?,?,?,?,?)",
            "Nerea Sánchez", "nerea@deustosport.com", "66666666F", "666123789", true, false, 5.00, "CLIENTE");
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, billetera, rol, polideportivo_id) VALUES (?,?,?,?,?,?,?,?,?)",
            "Iker Martín", "iker.mantenimiento@gmail.com",
            "88888888H", "944204250", true, false, 0.00, "MANTENIMIENTO", 1);
        jdbcTemplate.update(
            "INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, billetera, rol, ayuntamiento_id) VALUES (?,?,?,?,?,?,?,?,?)",
            "Ayuntamiento de Bilbao", "ayuntamiento.bilbao@deustosport.com",
            "77777777G", "944204200", true, false, 0.00, "AYUNTAMIENTO", 1);

        // ── Credenciales ─────────────────────────────────────────────
        LocalDateTime ahora = LocalDateTime.now();
        for (int i = 1; i <= 6; i++) {
            jdbcTemplate.update(
                "INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (?,?,?,?)",
                i, hash, true, ahora);
        }
        jdbcTemplate.update(
            "INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (?,?,?,?)",
            7, hashMantenimiento, true, ahora);
        jdbcTemplate.update(
            "INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (?,?,?,?)",
            8, hashBilbao, true, ahora);

        log.info("Datos de prueba inicializados correctamente");
        log.info("Usuarios: juan@deustosport.com / password123 | Pistas: 5");

        // ── Bonos ──────────────────────────────────────────────────
        jdbcTemplate.update(
            "INSERT INTO bonos (nombre, creditos_incluidos, precio, validez_dias, activo) VALUES (?,?,?,?,?)",
            "Bono Básico", 5, 20.00, 30, true);
        jdbcTemplate.update(
            "INSERT INTO bonos (nombre, creditos_incluidos, precio, validez_dias, activo) VALUES (?,?,?,?,?)",
            "Bono Familiar", 15, 50.00, 90, true);
        jdbcTemplate.update(
            "INSERT INTO bonos (nombre, creditos_incluidos, precio, validez_dias, activo) VALUES (?,?,?,?,?)",
            "Bono Pro", 30, 90.00, 180, true);
    }

    private void asegurarCuentaMantenimiento() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String email = "iker.mantenimiento@gmail.com";

        com.deustosport.my_app.entity.Polideportivo poli = jdbcTemplate.query("SELECT id FROM polideportivos LIMIT 1", (rs, rowNum) -> {
            com.deustosport.my_app.entity.Polideportivo p = new com.deustosport.my_app.entity.Polideportivo();
            p.setId(rs.getLong("id"));
            return p;
        }).stream().findFirst().orElse(null);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setNombreCompleto("Iker Martín");
                    nuevo.setEmail(email);
                    nuevo.setDni("88888888H");
                    nuevo.setTelefono("944204250");
                    nuevo.setActivo(true);
                    nuevo.setEsSocio(false);
                    nuevo.setBilletera(BigDecimal.ZERO);
                    nuevo.setRol(Rol.MANTENIMIENTO);
                    nuevo.setPolideportivo(poli);
                    return usuarioRepository.save(nuevo);
                });

        usuario.setNombreCompleto("Iker Martín");
        usuario.setDni("88888888H");
        usuario.setTelefono("944204250");
        usuario.setActivo(true);
        usuario.setEsSocio(false);
        usuario.setBilletera(BigDecimal.ZERO);
        usuario.setRol(Rol.MANTENIMIENTO);
        usuario.setPolideportivo(poli);
        usuario = usuarioRepository.save(usuario);

        Credencial credencial = credencialRepository.findByUsuarioId(usuario.getId())
                .orElseGet(Credencial::new);
        credencial.setUsuario(usuario);
        credencial.setPasswordHash(encoder.encode("mantenimiento"));
        credencial.setActivo(true);
        credencial.setFechaCreacion(credencial.getFechaCreacion() != null ? credencial.getFechaCreacion() : LocalDateTime.now());
        credencialRepository.save(credencial);
    }
}