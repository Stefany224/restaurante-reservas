# Sabor Gourmet · Sistema de reservas

Aplicación web para que los clientes del restaurante **reserven, modifiquen y cancelen** mesas en línea, y para que el
personal **administre las mesas, las reservas y los clientes**.

Hecha con **Spring Boot 3.5.6 (Java 21)**, **Spring MVC**, **Spring Data JPA**, **Thymeleaf**, **Spring Security**,
**PostgreSQL** y **Flyway**.

---

## Paso 1 · Clonar el proyecto

```powershell
git clone https://github.com/Stefany224/restaurante_reservas.git
cd restaurante_reservas
```

O descargar el ZIP desde GitHub (*Code > Download ZIP*) y descomprimirlo.

---

## Paso 2 · Crear la base de datos

No hay que importar ningún respaldo. Solo se crea, una sola vez, la base vacía **`restaurante_db`** en PostgreSQL
y el usuario **`restaurante`** con clave **`restaurante`**, que es con el que se conecta la aplicación. Las tablas y los datos de ejemplo los carga la aplicación sola al
iniciar (Paso 4).

1. Abrir pgAdmin y conectarse al servidor PostgreSQL (usuario `postgres`).
2. Clic derecho en **Databases > Create > Database...**, nombre **`restaurante_db`** y guardar.
3. Clic derecho sobre **`restaurante_db`** > **Query Tool**.
4. Abrir el archivo [`crear-bd-pgadmin.sql`](crear-bd-pgadmin.sql) (ícono de carpeta en el Query Tool) o copiar su
   contenido, y ejecutarlo completo con **F5**.
5. Debe terminar sin errores. Esto crea el usuario `restaurante` y le da permisos sobre la base.

---

## Paso 3 · Abrir el proyecto en Eclipse

1. **File > Import... > Maven > Existing Maven Projects > Next**.
2. En *Root Directory* elegir la carpeta del proyecto y presionar **Finish**.
3. Esperar a que Maven descargue las dependencias (barra de progreso abajo a la derecha). La primera vez puede tardar
   unos minutos.
4. Verificar que el proyecto use Java 21: clic derecho en el proyecto > **Properties > Java Build Path > Libraries**
   debe decir *JavaSE-21*. Si no, cambiarlo en **Project Facets** o en el JRE del proyecto.

---

## Paso 4 · Ejecutar la aplicación

**Desde Eclipse:** abrir `src/main/java/cl/ipss/restaurante/RestauranteApplication.java`, clic derecho >
**Run As > Java Application** (o **Spring Boot App** si tienes Spring Tools).

**Desde la consola**, en la carpeta del proyecto:

```powershell
.\mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run          # Linux / macOS
```

Cuando en la consola aparezca `Started RestauranteApplication`, abrir en el navegador:

**<http://localhost:8085>**

La primera vez **Flyway** ejecuta los scripts de `src/main/resources/db/migration` y crea las tablas con mesas,
clientes y reservas de ejemplo, por eso la base no se importa a mano.

---

## Paso 5 · Probar el sistema

### Como cliente (sin cuenta)

1. En el inicio, en **Reserva tu mesa**, elegir día, turno y personas y presionar **Buscar**.
2. Elegir una de las mesas libres y completar los datos para **confirmar** la reserva.
3. En **Gestiona tu reserva**, escribir el correo para ver, **modificar** o **cancelar** las reservas.

Correos de prueba que ya tienen reservas: `ana.perez@correo.cl`, `luis.rojas@correo.cl`, `camila.soto@correo.cl`.

### Como personal

1. Entrar a **Personal** en la barra superior (o <http://localhost:8085/login>).
2. Usuario **`admin`** · clave **`admin123`**.
3. Desde ahí se revisan las **Reservas** (filtros por día y por tipo), se administran las **Mesas** y se ven los
   **Clientes**.

| Página | URL | Acceso |
|---|---|---|
| Inicio | `/` | público |
| Disponibilidad | `/reservas/disponibilidad` | público |
| Mis reservas | `/reservas/mis-reservas` | público, con el correo |
| Ingreso del personal | `/login` | público |
| Reservas | `/admin/reservas` | personal |
| Mesas | `/admin/mesas` | personal |
| Clientes | `/admin/clientes` | personal |
