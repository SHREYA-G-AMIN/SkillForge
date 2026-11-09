package com.skillforge.config;

import com.skillforge.entity.CareerRole;
import com.skillforge.entity.Skill;
import com.skillforge.repository.CareerRoleRepository;
import com.skillforge.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CareerDataSeeder implements CommandLineRunner {
    private final CareerRoleRepository careerRoleRepository;
    private final SkillRepository skillRepository;

    @Override
    public void run(String... args) {
        seedSkills();
        seedRoles();
    }

    private void seedSkills() {
        List<String> skills = List.of("Java","Spring Boot","SQL","REST API","Git","Docker","JavaScript","React","HTML","CSS","Python","Machine Learning","Statistics","Pandas","Linux","Kubernetes","CI/CD","AWS","Node.js","TypeScript");
        for (String s : skills) {
            if (!skillRepository.existsByNameIgnoreCase(s)) {
                skillRepository.save(new Skill(s, null));
            }
        }
    }

    private void seedRoles() {
        add("Backend Developer","Server-side apps", List.of("Java","Spring Boot","SQL","REST API","Git","Docker"));
        add("Frontend Developer","Client-side apps", List.of("JavaScript","React","HTML","CSS","Git","REST API"));
        add("Full Stack Developer","End-to-end apps", List.of("JavaScript","React","Java","Spring Boot","SQL","Git"));
        add("Data Scientist","Data/ML", List.of("Python","Machine Learning","SQL","Statistics","Pandas"));
        add("DevOps Engineer","Infra/Delivery", List.of("Linux","Docker","Kubernetes","CI/CD","AWS","Git"));
    }

    private void add(String name, String desc, List<String> skills) {
        if (careerRoleRepository.findByNameIgnoreCase(name).isEmpty()) {
            careerRoleRepository.save(CareerRole.builder().name(name).description(desc).requiredSkills(skills).build());
        }
    }
}