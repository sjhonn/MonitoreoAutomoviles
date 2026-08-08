# AutoTrack

AutoTrack es una plataforma web desarrollada en Java para administrar y monitorear automoviles en tiempo real. El backend utiliza Spring Boot, seguridad JWT, JPA y WebSocket. El frontend se sirve desde la misma aplicacion, por lo que no necesita Node.js para ejecutarse.

El repositorio esta preparado para trabajar de tres formas:

- Local rapido con H2.
- Local con XAMPP / MariaDB / MySQL.
- Docker Compose con MySQL.

## Funcionalidades

- Inicio de sesion con JWT.
- Roles `ADMIN` y `USER`.
- CRUD de vehiculos.
- CRUD de conductores.
- Asignacion de conductor a vehiculo.
- Estados de vehiculo.
- Registro de coordenadas GPS.
- Historial de las ultimas ubicaciones.
- Velocidad, rumbo y ultima conexion.
- Actualizacion en tiempo real mediante WebSocket.
- Simulador GPS integrado.
- Alertas por exceso de velocidad.
- Dashboard con indicadores de flota.
- Swagger / OpenAPI.
- Health check con Spring Boot Actuator.
- H2 para desarrollo rapido.
- XAMPP / MariaDB para desarrollo local persistente.
- MySQL con Docker Compose.
- GitHub Actions para ejecutar pruebas en cada push o pull request.
- Configuracion de secretos mediante variables de entorno.

## Tecnologias

- Java 21
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA / Hibernate
- WebSocket
- H2
- MySQL Connector/J
- MariaDB Java Client
- Maven Wrapper
- Swagger / OpenAPI
- Docker
- GitHub Actions

## Estructura principal

```text
AutoTrack/
|-- .github/workflows/ci.yml
|-- .mvn/
|-- database/
|   `-- autotrack_xampp.sql
|-- src/
|   |-- main/
|   |   |-- java/com/autotrack/
|   |   |   |-- config/
|   |   |   |-- controller/
|   |   |   |-- dto/
|   |   |   |-- entity/
|   |   |   |-- exception/
|   |   |   |-- repository/
|   |   |   |-- security/
|   |   |   |-- service/
|   |   |   `-- websocket/
|   |   `-- resources/
|   |       |-- static/
|   |       |-- application.yml
|   |       |-- application-local.yml
|   |       |-- application-mysql.yml
|   |       `-- application-xampp.yml
|   `-- test/
|-- .env.example
|-- .gitignore
|-- compose.yaml
|-- Dockerfile
|-- mvnw
|-- mvnw.cmd
|-- pom.xml
|-- run-local.cmd
|-- run-xampp.cmd
`-- run-xampp.sh
```

## Requisitos

Para ejecutar AutoTrack necesitas:

- Java 21.
- XAMPP si deseas utilizar la base persistente local.
- Git si deseas publicar el proyecto en GitHub.
- Docker Desktop solamente si utilizaras el modo Docker.

No necesitas instalar Maven globalmente porque el proyecto incluye Maven Wrapper.

## Credenciales locales iniciales

Por defecto AutoTrack crea un administrador cuando la base de datos esta vacia:

```text
Correo: admin@autotrack.local
Contrasena: Admin123!
```

Estas credenciales son solo para desarrollo local. En un entorno compartido deben cambiarse con las variables `APP_ADMIN_EMAIL` y `APP_ADMIN_PASSWORD`.

## Opcion 1: ejecutar rapidamente con H2

Esta opcion no necesita XAMPP.

En Windows:

```bat
run-local.cmd
```

O directamente:

```bat
mvnw.cmd spring-boot:run
```

En Linux, macOS o Git Bash:

```bash
./mvnw spring-boot:run
```

Abrir:

```text
http://localhost:8080
```

La base H2 se guarda localmente dentro de `data/` y esta excluida de Git.

## Opcion 2: ejecutar con XAMPP

Esta es la configuracion recomendada si deseas revisar y administrar los datos desde phpMyAdmin.

### Paso 1. Iniciar XAMPP

Abre XAMPP Control Panel y enciende el servicio que aparece como `MySQL`.

Apache no es obligatorio para AutoTrack. Spring Boot utiliza su propio servidor HTTP en el puerto `8080`.

### Paso 2. Crear la base de datos

Tienes dos alternativas.

#### Alternativa A: importar el archivo incluido

En phpMyAdmin importa:

```text
database/autotrack_xampp.sql
```

El script crea la base de datos `autotrack`, las tablas, indices y relaciones necesarias.

#### Alternativa B: crear solamente la base

En phpMyAdmin, pestaña SQL, ejecuta:

```sql
CREATE DATABASE IF NOT EXISTS autotrack
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

AutoTrack tiene `spring.jpa.hibernate.ddl-auto=update`, por lo que puede crear y actualizar las tablas al iniciar.

### Paso 3. Configuracion de conexion de XAMPP

AutoTrack incluye el perfil:

```text
src/main/resources/application-xampp.yml
```

Configuracion predeterminada:

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/autotrack
    username: root
    password:
    driver-class-name: org.mariadb.jdbc.Driver
```

Los valores corresponden a una instalacion local habitual de XAMPP:

```text
Host: localhost
Puerto: 3306
Base de datos: autotrack
Usuario: root
Contrasena: vacia
```

Si cambiaste la contrasena de `root`, no necesitas editar el codigo. En CMD puedes ejecutar:

```bat
set XAMPP_DB_PASSWORD=TU_PASSWORD
run-xampp.cmd
```

Tambien puedes cambiar toda la URL:

```bat
set XAMPP_DB_URL=jdbc:mariadb://localhost:3307/autotrack
set XAMPP_DB_USERNAME=root
set XAMPP_DB_PASSWORD=TU_PASSWORD
run-xampp.cmd
```

### Paso 4. Ejecutar AutoTrack con XAMPP

En Windows:

```bat
run-xampp.cmd
```

O con Maven Wrapper:

```bat
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=xampp
```

En Git Bash/Linux/macOS:

```bash
./run-xampp.sh
```

Luego abre:

```text
http://localhost:8080
```

### Que datos se deben copiar a MySQL

No debes copiar manualmente usuarios, contrasenas ni coordenadas para que la aplicacion funcione.

Lo unico obligatorio es que exista la base:

```sql
CREATE DATABASE IF NOT EXISTS autotrack
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

Si importas `database/autotrack_xampp.sql`, las tablas se crean por adelantado.

Al iniciar AutoTrack por primera vez:

1. Se crea el administrador con una contrasena BCrypt.
2. Si no existen vehiculos, se crea un conductor de demostracion.
3. Se crea un vehiculo de demostracion.
4. El simulador comienza a registrar posiciones GPS.
5. Las posiciones aparecen en `vehicle_locations`.
6. Las alertas aparecen en `alerts`.

Por seguridad, el script SQL no contiene una contrasena de administrador en texto plano ni un hash fijo.

## Tablas de la base de datos

AutoTrack utiliza:

```text
app_users
    Usuarios, roles y credenciales cifradas.

drivers
    Conductores.

vehicles
    Automoviles, estado, velocidad maxima, conductor y ultima posicion.

vehicle_locations
    Historial de posiciones GPS.

alerts
    Alertas asociadas a los vehiculos.
```

Relaciones principales:

```text
drivers 1 ---- N vehicles
vehicles 1 --- N vehicle_locations
vehicles 1 --- N alerts
```

## Perfil generico MySQL

Tambien existe `application-mysql.yml` para servidores MySQL que no sean XAMPP.

Variables disponibles:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Ejemplo:

```text
DB_URL=jdbc:mysql://localhost:3306/autotrack?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
DB_USERNAME=autotrack
DB_PASSWORD=tu_password
```

## Docker Compose

Copia el archivo de ejemplo:

```bash
cp .env.example .env
```

En Windows CMD puedes hacer:

```bat
copy .env.example .env
```

Edita las contrasenas y ejecuta:

```bash
docker compose up --build
```

Servicios:

```text
AutoTrack: http://localhost:8080
MySQL:    localhost:3306
```

No ejecutes al mismo tiempo MySQL de XAMPP y el MySQL de Docker en el mismo puerto `3306`.

## Swagger

Con la aplicacion iniciada:

```text
http://localhost:8080/swagger-ui.html
```

Para probar endpoints protegidos:

1. Ejecuta `POST /api/auth/login`.
2. Copia el token JWT.
3. Presiona `Authorize` en Swagger.
4. Ingresa el token.

## API principal

```text
POST   /api/auth/login
GET    /api/users/me

GET    /api/vehicles
GET    /api/vehicles/{id}
POST   /api/vehicles
PUT    /api/vehicles/{id}
DELETE /api/vehicles/{id}

GET    /api/drivers
GET    /api/drivers/{id}
POST   /api/drivers
PUT    /api/drivers/{id}
DELETE /api/drivers/{id}

POST   /api/tracking/vehicles/{id}/locations
GET    /api/tracking/vehicles/{id}/locations

GET    /api/alerts
PATCH  /api/alerts/{id}/acknowledge

GET    /api/dashboard
```

## WebSocket

El dashboard utiliza:

```text
/ws/locations?token=JWT
```

Cuando se registra una posicion, el backend envia un evento como:

```json
{
  "type": "vehicle.location",
  "data": {
    "vehicleId": 1,
    "plate": "ATR-001",
    "latitude": -12.046374,
    "longitude": -77.042793,
    "speed": 64.2,
    "heading": 145.0
  }
}
```

## Simulador GPS

El simulador se activa por defecto para desarrollo.

Para desactivarlo:

```text
APP_SIMULATOR_ENABLED=false
```

Intervalo predeterminado:

```text
2000 ms
```

Puede cambiarse mediante:

```text
APP_SIMULATOR_INTERVAL_MS=5000
```

## Compilar

Windows:

```bat
mvnw.cmd clean package
```

Linux/macOS/Git Bash:

```bash
./mvnw clean package
```

El resultado es:

```text
target/AutoTrack.jar
```

Ejecutar el JAR:

```bash
java -jar target/AutoTrack.jar
```

Para usar XAMPP con el JAR:

Windows CMD:

```bat
set SPRING_PROFILES_ACTIVE=xampp
java -jar target/AutoTrack.jar
```

## Pruebas

```bash
./mvnw verify
```

En Windows:

```bat
mvnw.cmd verify
```

## Subir a GitHub

Crea un repositorio vacio en GitHub llamado, por ejemplo, `AutoTrack`.

Dentro de la carpeta del proyecto:

```bash
git init
git add .
git commit -m "feat: initialize AutoTrack real-time vehicle platform"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/AutoTrack.git
git push -u origin main
```

El archivo `.gitignore` evita subir:

- `target/`
- `.env`
- base H2 local
- configuracion de IDE
- archivos temporales

Nunca debes subir contrasenas reales, tokens, claves JWT ni archivos `.env`.

## GitHub Actions

El workflow:

```text
.github/workflows/ci.yml
```

Ejecuta Maven Verify con Java 21 en `push` y `pull_request`.

## Variables de entorno importantes

```text
JWT_SECRET
JWT_TTL_MINUTES
CORS_ALLOWED_ORIGINS
APP_ADMIN_NAME
APP_ADMIN_EMAIL
APP_ADMIN_PASSWORD
APP_SEED_DEMO
APP_SIMULATOR_ENABLED
APP_SIMULATOR_INTERVAL_MS
APP_SIMULATOR_INITIAL_DELAY_MS
XAMPP_DB_URL
XAMPP_DB_USERNAME
XAMPP_DB_PASSWORD
DB_URL
DB_USERNAME
DB_PASSWORD
```

## Seguridad

La configuracion local esta pensada para desarrollo. Para despliegue real:

- Cambia `JWT_SECRET`.
- Cambia la contrasena del administrador.
- No habilites credenciales de demostracion.
- Desactiva el simulador si recibiras GPS real.
- Configura CORS con el dominio permitido.
- Utiliza HTTPS.
- Utiliza un usuario de base de datos dedicado en lugar de `root`.
- Mantiene `.env` fuera del repositorio.

## Resultado esperado con XAMPP

Cuando XAMPP este iniciado, la base `autotrack` exista y ejecutes `run-xampp.cmd`, deberias ver:

```text
XAMPP MySQL/MariaDB
        |
        | localhost:3306
        v
    autotrack
        |
        v
Spring Boot / JPA
        |
        +--> app_users
        +--> drivers
        +--> vehicles
        +--> vehicle_locations
        `--> alerts
        |
        v
http://localhost:8080
```

AutoTrack no necesita copiar archivos dentro de `htdocs`, porque el frontend y el backend son servidos directamente por Spring Boot.
