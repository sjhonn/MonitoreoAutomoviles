package com.autotrack.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String plate;

    @Column(nullable = false, length = 80)
    private String brand;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(name = "model_year", nullable = false)
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(nullable = false)
    private Double maxSpeed = 80.0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    private Double lastLatitude;
    private Double lastLongitude;
    private Double lastSpeed;
    private Double lastHeading;
    private Instant lastSeen;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
    public Double getLastLatitude() { return lastLatitude; }
    public void setLastLatitude(Double lastLatitude) { this.lastLatitude = lastLatitude; }
    public Double getLastLongitude() { return lastLongitude; }
    public void setLastLongitude(Double lastLongitude) { this.lastLongitude = lastLongitude; }
    public Double getLastSpeed() { return lastSpeed; }
    public void setLastSpeed(Double lastSpeed) { this.lastSpeed = lastSpeed; }
    public Double getLastHeading() { return lastHeading; }
    public void setLastHeading(Double lastHeading) { this.lastHeading = lastHeading; }
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
