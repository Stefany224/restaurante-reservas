-- Crea el usuario y la base restaurante_db desde psql, se ejecuta una sola vez y se puede repetir sin problema
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'restaurante') THEN
        CREATE USER restaurante WITH PASSWORD 'restaurante';
    ELSE
        ALTER USER restaurante WITH PASSWORD 'restaurante';
    END IF;
END
$$;

-- CREATE DATABASE no se puede usar dentro de DO, por eso se arma con gexec
SELECT 'CREATE DATABASE restaurante_db OWNER restaurante'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'restaurante_db')\gexec

\connect restaurante_db

-- Permisos para que Flyway pueda crear las tablas
ALTER DATABASE restaurante_db OWNER TO restaurante;
GRANT ALL PRIVILEGES ON DATABASE restaurante_db TO restaurante;
GRANT ALL ON SCHEMA public TO restaurante;
