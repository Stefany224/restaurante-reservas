-- Version para pgAdmin, la base restaurante_db se crea antes a mano y esto se ejecuta en el Query Tool sobre esa base
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'restaurante') THEN
        CREATE USER restaurante WITH PASSWORD 'restaurante';
    ELSE
        ALTER USER restaurante WITH PASSWORD 'restaurante';
    END IF;
END
$$;

-- Permisos para que Flyway pueda crear las tablas
ALTER DATABASE restaurante_db OWNER TO restaurante;
GRANT ALL PRIVILEGES ON DATABASE restaurante_db TO restaurante;
GRANT ALL ON SCHEMA public TO restaurante;
