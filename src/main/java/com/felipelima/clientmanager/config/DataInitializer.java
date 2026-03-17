package com.felipelima.clientmanager.config;

import com.felipelima.clientmanager.entity.User;
import com.felipelima.clientmanager.entity.enums.RoleEnum;
import com.felipelima.clientmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123qwel@#"));
            admin.setRole(RoleEnum.ADMIN);
            userRepository.save(admin);

            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("123qwe123"));
            user.setRole(RoleEnum.USER);
            userRepository.save(user);

            log.info("Default users created: admin and user");
        }
    }
}