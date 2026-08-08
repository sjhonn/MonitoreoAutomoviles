-- AutoTrack - base de datos para XAMPP / MariaDB / MySQL
-- Importar desde phpMyAdmin o ejecutar desde la consola SQL.

CREATE DATABASE IF NOT EXISTS autotrack
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_spanish_ci;

USE autotrack;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    password VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BIT(1) NOT NULL DEFAULT b'1',
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS drivers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(140) NOT NULL,
    license_number VARCHAR(60) NOT NULL,
    phone VARCHAR(40) NULL,
    active BIT(1) NOT NULL DEFAULT b'1',
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_drivers_license_number (license_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    plate VARCHAR(20) NOT NULL,
    brand VARCHAR(80) NOT NULL,
    model VARCHAR(80) NOT NULL,
    model_year INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    max_speed DOUBLE NOT NULL DEFAULT 80,
    driver_id BIGINT NULL,
    last_latitude DOUBLE NULL,
    last_longitude DOUBLE NULL,
    last_speed DOUBLE NULL,
    last_heading DOUBLE NULL,
    last_seen TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vehicles_plate (plate),
    KEY idx_vehicles_driver_id (driver_id),
    CONSTRAINT fk_vehicles_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS vehicle_locations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    speed DOUBLE NOT NULL,
    heading DOUBLE NULL,
    recorded_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_vehicle_locations_vehicle_time (vehicle_id, recorded_at),
    CONSTRAINT fk_vehicle_locations_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

CREATE TABLE IF NOT EXISTS alerts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    message VARCHAR(300) NOT NULL,
    acknowledged BIT(1) NOT NULL DEFAULT b'0',
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_alerts_vehicle_created (vehicle_id, created_at),
    KEY idx_alerts_acknowledged (acknowledged),
    CONSTRAINT fk_alerts_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- No se inserta la contrasena del administrador en SQL.
-- Al iniciar AutoTrack, Spring Boot crea de forma segura el usuario administrador
-- usando BCrypt y las variables APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD.
-- Si la base esta vacia, tambien crea un conductor y un vehiculo de demostracion.
