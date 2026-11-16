-- V1: mesas del restaurante, clientes y reservas
CREATE TABLE mesa (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero         INTEGER     NOT NULL,
    capacidad      INTEGER     NOT NULL,
    ubicacion      VARCHAR(10) NOT NULL,
    activa         BOOLEAN     NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_mesa_numero UNIQUE (numero),
    CONSTRAINT ck_mesa_numero CHECK (numero >= 1),
    CONSTRAINT ck_mesa_capacidad CHECK (capacidad BETWEEN 1 AND 20),
    CONSTRAINT ck_mesa_ubicacion CHECK (ubicacion IN ('SALON', 'TERRAZA', 'PRIVADO'))
);

CREATE TABLE cliente (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    email     VARCHAR(120) NOT NULL,
    telefono  VARCHAR(20)  NOT NULL,
    creado_en TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_cliente_email UNIQUE (email)
);

CREATE TABLE reserva (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mesa_id        BIGINT       NOT NULL REFERENCES mesa (id),
    cliente_id     BIGINT       NOT NULL REFERENCES cliente (id),
    fecha          DATE         NOT NULL,
    hora           TIME         NOT NULL,
    personas       INTEGER      NOT NULL,
    comentario     VARCHAR(250),
    estado         VARCHAR(10)  NOT NULL DEFAULT 'ACTIVA',
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_reserva_personas CHECK (personas BETWEEN 1 AND 20),
    CONSTRAINT ck_reserva_estado CHECK (estado IN ('ACTIVA', 'CANCELADA'))
);

CREATE INDEX idx_reserva_fecha_hora ON reserva (fecha, hora);
CREATE INDEX idx_reserva_cliente ON reserva (cliente_id);
-- Defensa en profundidad: una mesa no puede tener dos reservas ACTIVAS el mismo día a la misma hora
CREATE UNIQUE INDEX uq_reserva_mesa_turno ON reserva (mesa_id, fecha, hora) WHERE estado = 'ACTIVA';
