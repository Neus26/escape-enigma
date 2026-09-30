DROP DATABASE IF EXISTS escape_enigma;
CREATE DATABASE escape_enigma
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE escape_enigma;

-- =========================================================
-- Tabla: salas
-- =========================================================
CREATE TABLE salas (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80)   NOT NULL,
    tematica        VARCHAR(50)   NOT NULL,
    dificultad      TINYINT       NOT NULL,
    aforo_max       INT           NOT NULL,
    precio_persona  DECIMAL(5,2)  NOT NULL,
    activa          BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_dificultad CHECK (dificultad BETWEEN 1 AND 5),
    CONSTRAINT chk_aforo      CHECK (aforo_max  > 0),
    CONSTRAINT chk_precio     CHECK (precio_persona >= 0)
);

-- =========================================================
-- Tabla: reservas
-- =========================================================
CREATE TABLE reservas (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    sala_id         INT           NOT NULL,
    fecha_hora      DATETIME      NOT NULL,
    nombre_grupo    VARCHAR(80)   NOT NULL,
    num_jugadores   INT           NOT NULL,
    completada      BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_reserva_sala FOREIGN KEY (sala_id) REFERENCES salas(id),
    CONSTRAINT chk_jugadores  CHECK (num_jugadores > 0)
);

CREATE INDEX idx_reservas_fecha ON reservas(fecha_hora);
CREATE INDEX idx_reservas_sala  ON reservas(sala_id);

-- =========================================================
-- Datos de ejemplo: salas
-- =========================================================
INSERT INTO salas (nombre, tematica, dificultad, aforo_max, precio_persona, activa) VALUES
    ('La Cripta del Faraón', 'Histórica',       4, 6, 22.00, TRUE),
    ('Asylum',               'Terror',          5, 6, 25.00, TRUE),
    ('El Detective',         'Misterio',        3, 4, 20.00, TRUE),
    ('Estación Espacial',    'Ciencia Ficción', 4, 8, 24.00, TRUE),
    ('La Madriguera',        'Infantil',        2, 8, 15.00, TRUE),
    ('Wild West',            'Aventura',        3, 6, 22.00, TRUE),
    ('El Sótano (en obras)', 'Terror',          5, 6, 25.00, FALSE);

-- =========================================================
-- Datos de ejemplo: reservas
-- =========================================================
INSERT INTO reservas (sala_id, fecha_hora, nombre_grupo, num_jugadores, completada) VALUES
    (1, '2026-04-10 18:00:00', 'Los Aventureros',     5, TRUE),
    (2, '2026-04-12 17:00:00', 'Equipo Pesadilla',    4, TRUE),
    (3, '2026-04-15 19:30:00', 'Sherlocks',           3, TRUE),
    (5, '2026-04-18 11:00:00', 'Cumple de Marta',     8, TRUE),
    (1, '2026-04-20 20:00:00', 'Los Faraones',        6, TRUE),
    (4, '2026-04-22 17:00:00', 'Astronautas',         7, TRUE),
    (2, '2026-04-25 18:30:00', 'Sin Miedo',           5, TRUE),
    (6, '2026-04-28 16:00:00', 'Cowboys',             4, TRUE),
    (3, '2026-04-29 17:00:00', 'Mentes Brillantes',   4, TRUE),
    (1, '2026-05-02 18:00:00', 'Familia García',      4, TRUE),
    (5, '2026-05-03 12:00:00', 'Cumple de Lucía',     6, TRUE),
    (4, '2026-05-04 19:00:00', 'Trekkies Córdoba',    8, TRUE),
    (2, '2026-05-05 21:00:00', 'Halloween Crew',      6, FALSE),
    (3, '2026-05-05 19:00:00', 'Detectives Junior',   4, TRUE),
    (1, '2026-05-10 18:00:00', 'Tutankamon Team',     5, FALSE),
    (4, '2026-05-12 17:30:00', 'Voyager',             5, FALSE),
    (5, '2026-05-15 11:00:00', 'Cumple de Pablo',     7, FALSE),
    (6, '2026-05-16 20:00:00', 'Forajidos',           6, FALSE);