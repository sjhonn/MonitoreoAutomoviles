package com.autotrack.config;

import com.autotrack.entity.AppUser;
import com.autotrack.entity.Driver;
import com.autotrack.entity.Role;
import com.autotrack.entity.Vehicle;
import com.autotrack.entity.VehicleStatus;
import com.autotrack.repository.AppUserRepository;
import com.autotrack.repository.DriverRepository;
import com.autotrack.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            AppUserRepository userRepository,
            DriverRepository driverRepository,
            VehicleRepository vehicleRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.name:AutoTrack Admin}") String adminName,
            @Value("${app.admin.email:admin@autotrack.local}") String adminEmail,
            @Value("${app.admin.password:Admin123!}") String adminPassword,
            @Value("${app.seed-demo:true}") boolean seedDemo) {
        return args -> {
            if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
                AppUser admin = new AppUser();
                admin.setName(adminName);
                admin.setEmail(adminEmail.trim().toLowerCase());
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                userRepository.save(admin);
            }

            if (seedDemo && vehicleRepository.count() == 0) {
                Driver driver = new Driver();
                driver.setFullName("Demo Driver");
                driver.setLicenseNumber("DEMO-A001");
                driver.setPhone("+51 900 000 000");
                driver.setActive(true);
                driver = driverRepository.save(driver);

                Vehicle vehicle = new Vehicle();
                vehicle.setPlate("ATR-001");
                vehicle.setBrand("Toyota");
                vehicle.setModel("Corolla");
                vehicle.setYear(2024);
                vehicle.setStatus(VehicleStatus.AVAILABLE);
                vehicle.setMaxSpeed(80.0);
                vehicle.setDriver(driver);
                vehicleRepository.save(vehicle);
            }
        };
    }
}
