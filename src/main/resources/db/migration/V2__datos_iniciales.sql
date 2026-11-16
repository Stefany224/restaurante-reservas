-- V2: datos de ejemplo para ver algo al levantar el proyecto.
INSERT INTO mesa (numero, capacidad, ubicacion, activa) VALUES
    (1,  2, 'SALON',   TRUE),
    (2,  2, 'SALON',   TRUE),
    (3,  4, 'SALON',   TRUE),
    (4,  4, 'SALON',   TRUE),
    (5,  6, 'SALON',   TRUE),
    (6,  4, 'TERRAZA', TRUE),
    (7,  6, 'TERRAZA', TRUE),
    (8, 10, 'PRIVADO', TRUE),
    (9,  2, 'TERRAZA', FALSE);   -- mesa en mantención: no aparece para reservar

INSERT INTO cliente (nombre, email, telefono) VALUES
    ('Ana Pérez',   'ana.perez@correo.cl',   '+56 9 1111 2222'),
    ('Luis Rojas',  'luis.rojas@correo.cl',  '+56 9 3333 4444'),
    ('Camila Soto', 'camila.soto@correo.cl', '+56 9 5555 6666');

-- Reservas a partir de mañana para que se puedan modificar o cancelar desde la aplicación
INSERT INTO reserva (mesa_id, cliente_id, fecha, hora, personas, comentario, estado) VALUES
    (3, 1, CURRENT_DATE + 1, '20:00', 4, 'Cumpleaños, si se puede cerca de la ventana', 'ACTIVA'),
    (6, 2, CURRENT_DATE + 1, '13:00', 3, NULL,                                          'ACTIVA'),
    (8, 3, CURRENT_DATE + 2, '21:00', 8, 'Cena de la empresa',                          'ACTIVA'),
    (1, 1, CURRENT_DATE + 3, '14:00', 2, NULL,                                          'CANCELADA');
