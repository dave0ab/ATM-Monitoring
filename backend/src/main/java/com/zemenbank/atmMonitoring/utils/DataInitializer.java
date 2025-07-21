package com.zemenbank.atmMonitoring.utils;

import com.zemenbank.atmMonitoring.enums.Roles;
import com.zemenbank.atmMonitoring.model.atmMonitor.PingStatus;
import com.zemenbank.atmMonitoring.model.atmMonitor.User;
import com.zemenbank.atmMonitoring.repositories.atmMonitor.UserRepository;
import com.zemenbank.atmMonitoring.services.impl.scheduled.PingService;
import com.zemenbank.atmMonitoring.services.impl.scheduled.PollingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PollingService p;
    public DataInitializer(UserRepository userRepository , PollingService p) {
        this.userRepository = userRepository;
        this.p = p;
    }

    @Override
    public void run(String... args) {

        userRepository.findAll().forEach(user -> {
            if (user.getEmailSubscribe() == null) {
                user.setEmailSubscribe(true); // or true, if that's your default
                userRepository.save(user);
            }
        });

        String domainId = "dawit.gebrehiwot";
        Optional<User> existingUser = userRepository.findByDomainId(domainId);

        if (existingUser.isEmpty()) {
            User newUser = User.builder()
                    .domainId(domainId)
                    .status("ACTIVE")
                    .role(Roles.ADMIN) // Adjust the role as per your requirement
                    .createdAt(LocalDateTime.now())
                    .lastSignedOn(Instant.now())
                    .emailSubscribe(true)
                    .notificationLevel("FIFTH")
                    .build();

            userRepository.save(newUser);
            p.pingOfflineAtms();
            System.out.println("Admin user initialized.");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}
