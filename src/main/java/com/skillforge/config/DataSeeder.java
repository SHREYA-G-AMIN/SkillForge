package com.skillforge.config;

import com.skillforge.entity.Role;
import com.skillforge.entity.Skill;
import com.skillforge.entity.User;
import com.skillforge.repository.SkillRepository;
import com.skillforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/** Seeds an admin account and a starter master skill list on first run. */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!adminEmail.isBlank() && !adminPassword.isBlank() && !userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        if (skillRepository.count() == 0) {
            skillRepository.saveAll(List.of(
                    new Skill("Java", "Programming"), new Skill("Python", "Programming"),
                    new Skill("C++", "Programming"), new Skill("JavaScript", "Programming"),
                    new Skill("TypeScript", "Programming"),
                    new Skill("Spring Boot", "Backend"), new Skill("Node.js", "Backend"),
                    new Skill("Django", "Backend"), new Skill("REST APIs", "Backend"),
                    new Skill("HTML", "Frontend"), new Skill("CSS", "Frontend"),
                    new Skill("React", "Frontend"), new Skill("Flutter", "Mobile"),
                    new Skill("SQL", "Database"), new Skill("MySQL", "Database"),
                    new Skill("MongoDB", "Database"),
                    new Skill("Git", "Tools"), new Skill("Docker", "DevOps"),
                    new Skill("AWS", "Cloud"), new Skill("Linux", "Tools"),
                    new Skill("Data Structures", "Core CS"), new Skill("Algorithms", "Core CS"),
                    new Skill("Machine Learning", "AI/ML"),
                    new Skill("Communication", "Soft Skills"), new Skill("Problem Solving", "Soft Skills")
            ));
        }
    }
}
