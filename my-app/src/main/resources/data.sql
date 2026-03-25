-- ============================================================
-- USUARIOS
-- ============================================================
INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol)
VALUES ('Juan García',       'juan@deustosport.com',   '11111111A', '666111222', true, false, 'CLIENTE');

INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol)
VALUES ('María López',       'maria@deustosport.com',  '22222222B', '666333444', true, true, 'SECRETARIA');

INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol)
VALUES ('Carlos Rodríguez',  'carlos@deustosport.com', '33333333C', '666555666', true, false, 'COORDINADOR');

INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol)
VALUES ('Laura Fernández',  'laura@deustosport.com',  '44444444D', '666777888', true, false, 'CLIENTE');

INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol)
VALUES ('Iker Martínez',    'iker@deustosport.com',   '55555555E', '666999000', true, true,  'CLIENTE');

INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol)
VALUES ('Nerea Sánchez',    'nerea@deustosport.com',  '66666666F', '666123789', true, false, 'CLIENTE');

INSERT INTO usuarios (nombre_completo, email, dni, telefono, activo, es_socio, rol)
VALUES ('Ayuntamiento de Bilbao', 'ayuntamiento.bilbao@deustosport.com', '77777777G', '944204200', true, false, 'AYUNTAMIENTO');

-- ============================================================
-- CREDENCIALES  (password: password123)
-- Hash BCrypt de "password123":
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e
-- ============================================================
INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion)
VALUES (1, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());

INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion)
VALUES (2, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());

INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion)
VALUES (3, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());

INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion)
VALUES (4, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());

INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion)
VALUES (5, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());

INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion)
VALUES (6, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());

INSERT INTO credenciales (usuario_id, password_hash, activo, fecha_creacion)
VALUES (7, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DxYJ3e', true, NOW());

-- ============================================================
-- INSTALACIONES
-- ============================================================
INSERT INTO instalaciones (id, nombre, direccion, hora_apertura, hora_cierre)
VALUES (1, 'Polideportivo Deusto', 'Calle Agirre, 1, Bilbao', '08:00:00', '22:00:00');

ALTER TABLE instalaciones ALTER COLUMN id RESTART WITH 2;

-- ============================================================
-- PISTAS
-- ============================================================
INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa)
VALUES ('Pista Pádel 1',  'PADEL',  1, 4,  true);

INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa)
VALUES ('Pista Pádel 2',  'PADEL',  1, 4,  true);

INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa)
VALUES ('Pista Tenis 1',  'TENIS',  1, 4,  true);

INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa)
VALUES ('Pista Tenis 2',  'TENIS',  1, 4,  true);

INSERT INTO pistas (nombre, tipo_deporte, instalacion_id, max_jugadores, activa)
VALUES ('Pista Fútbol 1', 'FUTBOL', 1, 22, true);

-- ============================================================
-- TARIFAS
-- Estructura:
--   Lunes-Viernes (1-5)  hora valle 08:00-17:00
--   Lunes-Viernes (1-5)  hora punta 17:00-22:00
--   Sábado-Domingo (6-7) todo el día 08:00-22:00
--
-- Precios por deporte:
--   PADEL  : valle  8 €/h  | punta 15 €/h | finde 18 €/h
--   TENIS  : valle  7 €/h  | punta 13 €/h | finde 16 €/h
--   FUTBOL : valle 12 €/h  | punta 20 €/h | finde 25 €/h
-- ============================================================

-- ---------- PÁDEL ----------
-- Valle Lunes-Viernes
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 1, '08:00:00', '17:00:00', 8.00,  '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 2, '08:00:00', '17:00:00', 8.00,  '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 3, '08:00:00', '17:00:00', 8.00,  '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 4, '08:00:00', '17:00:00', 8.00,  '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 5, '08:00:00', '17:00:00', 8.00,  '2026-01-01', true);
-- Punta Lunes-Viernes
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 1, '17:00:00', '22:00:00', 15.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 2, '17:00:00', '22:00:00', 15.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 3, '17:00:00', '22:00:00', 15.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 4, '17:00:00', '22:00:00', 15.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 5, '17:00:00', '22:00:00', 15.00, '2026-01-01', true);
-- Finde
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 6, '08:00:00', '22:00:00', 18.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('PADEL', 7, '08:00:00', '22:00:00', 18.00, '2026-01-01', true);

-- ---------- TENIS ----------
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 1, '08:00:00', '17:00:00', 7.00,  '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 2, '08:00:00', '17:00:00', 7.00,  '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 3, '08:00:00', '17:00:00', 7.00,  '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 4, '08:00:00', '17:00:00', 7.00,  '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 5, '08:00:00', '17:00:00', 7.00,  '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 1, '17:00:00', '22:00:00', 13.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 2, '17:00:00', '22:00:00', 13.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 3, '17:00:00', '22:00:00', 13.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 4, '17:00:00', '22:00:00', 13.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 5, '17:00:00', '22:00:00', 13.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 6, '08:00:00', '22:00:00', 16.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('TENIS', 7, '08:00:00', '22:00:00', 16.00, '2026-01-01', true);

-- ---------- FÚTBOL ----------
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 1, '08:00:00', '17:00:00', 12.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 2, '08:00:00', '17:00:00', 12.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 3, '08:00:00', '17:00:00', 12.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 4, '08:00:00', '17:00:00', 12.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 5, '08:00:00', '17:00:00', 12.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 1, '17:00:00', '22:00:00', 20.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 2, '17:00:00', '22:00:00', 20.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 3, '17:00:00', '22:00:00', 20.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 4, '17:00:00', '22:00:00', 20.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 5, '17:00:00', '22:00:00', 20.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 6, '08:00:00', '22:00:00', 25.00, '2026-01-01', true);
INSERT INTO tarifas (tipo_deporte, dia_semana, hora_inicio, hora_fin, precio_por_hora, vigente_desde, activa)
VALUES ('FUTBOL', 7, '08:00:00', '22:00:00', 25.00, '2026-01-01', true);