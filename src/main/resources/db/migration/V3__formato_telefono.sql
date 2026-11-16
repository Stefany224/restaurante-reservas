-- V3: los teléfonos pasan al formato de celular chileno "9 1234 5678" (nueva validación del formulario).
-- No se edita V2 porque Flyway ya la aplicó (cambiarla rompería su checksum): los cambios van en una migración nueva.

-- 1) Teléfonos con al menos 8 dígitos: se toman los últimos 8 y se arma "9 XXXX XXXX"
--    (ej.: "+56 9 1111 2222" -> "9 1111 2222")
UPDATE cliente
SET telefono = '9 '
    || substr(regexp_replace(telefono, '[^0-9]', '', 'g'), length(regexp_replace(telefono, '[^0-9]', '', 'g')) - 7, 4)
    || ' '
    || right(regexp_replace(telefono, '[^0-9]', '', 'g'), 4)
WHERE length(regexp_replace(telefono, '[^0-9]', '', 'g')) >= 8;

-- 2) Los que no alcanzan 8 dígitos no se pueden recuperar: quedan con un valor de relleno válido
UPDATE cliente
SET telefono = '9 0000 0000'
WHERE length(regexp_replace(telefono, '[^0-9]', '', 'g')) < 8;

-- 3) Desde ahora la base también exige el formato (defensa en profundidad, igual que en el formulario)
ALTER TABLE cliente
    ADD CONSTRAINT ck_cliente_telefono CHECK (telefono ~ '^9 [0-9]{4} [0-9]{4}$');
