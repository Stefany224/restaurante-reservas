-- V4: el teléfono ya no tiene que empezar con 9: se acepta cualquier número de 9 dígitos con formato "X XXXX XXXX".
-- La regla de V3 ya está aplicada en la base, así que se reemplaza con una migración nueva (V3 no se edita).
ALTER TABLE cliente DROP CONSTRAINT ck_cliente_telefono;

ALTER TABLE cliente
    ADD CONSTRAINT ck_cliente_telefono CHECK (telefono ~ '^[0-9] [0-9]{4} [0-9]{4}$');
