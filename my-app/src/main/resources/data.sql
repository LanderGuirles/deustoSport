-- Insertar usuarios de prueba
INSERT INTO usuarios (nombre_completo, email, telefono, activo) VALUES ('Juan García', 'juan@deustosport.com', '666111222', true);
INSERT INTO usuarios (nombre_completo, email, telefono, activo) VALUES ('María López', 'maria@deustosport.com', '666333444', true);
INSERT INTO usuarios (nombre_completo, email, telefono, activo) VALUES ('Carlos Rodríguez', 'carlos@deustosport.com', '666555666', true);

-- Insertar credenciales de prueba
-- Password: password123 (hash BCrypt)
-- Password: password456 (hash BCrypt)
-- Password: password789 (hash BCrypt)
INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (1, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());
INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (2, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());
INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion) VALUES (3, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());


-- Insertar polideportivo base
INSERT INTO instalaciones (id, nombre, direccion, hora_apertura, hora_cierre) 
VALUES (1, 'Polideportivo Deusto', 'Calle Agirre, 1, Bilbao', '08:00:00', '22:00:00');

-- Si usas H2 con modo PostgreSQL:
ALTER TABLE instalaciones ALTER COLUMN id RESTART WITH 2;