package com.skillforge.opportunity.config;

import com.skillforge.entity.Role;
import com.skillforge.entity.Skill;
import com.skillforge.entity.User;
import com.skillforge.opportunity.model.Opportunity;
import com.skillforge.opportunity.model.OpportunitySkillRequirement;
import com.skillforge.opportunity.model.OpportunityStatus;
import com.skillforge.opportunity.model.OpportunityType;
import com.skillforge.opportunity.repository.OpportunityRepository;
import com.skillforge.repository.SkillRepository;
import com.skillforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Order(10) // Run after DataSeeder (which seeds master skills)
@RequiredArgsConstructor
public class OpportunityDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final OpportunityRepository opportunityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed default recruiter account
        User recruiter = userRepository.findByEmail("recruiter@skillforge.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail("recruiter@skillforge.com");
                    u.setPasswordHash(passwordEncoder.encode("Recruiter123!"));
                    u.setRole(Role.RECRUITER);
                    return userRepository.save(u);
                });

        if (opportunityRepository.count() == 0) {
            // Helper to find skill by name
            Optional<Skill> javaSkill = skillRepository.findByName("Java");
            Optional<Skill> springSkill = skillRepository.findByName("Spring Boot");
            Optional<Skill> reactSkill = skillRepository.findByName("React");
            Optional<Skill> sqlSkill = skillRepository.findByName("SQL");
            Optional<Skill> gitSkill = skillRepository.findByName("Git");
            Optional<Skill> pythonSkill = skillRepository.findByName("Python");

            // 1. Internship
            Opportunity internship = new Opportunity();
            internship.setRecruiter(recruiter);
            internship.setTitle("Backend Engineering Intern");
            internship.setCompany("Apex Cloud Systems");
            internship.setLocation("Bangalore (Hybrid)");
            internship.setType(OpportunityType.INTERNSHIP);
            internship.setDescription("Join our core backend infrastructure team to design scalable microservices and RESTful APIs using Spring Boot and relational databases.");
            internship.setDeadline(LocalDate.now().plusMonths(2));
            internship.setMinCgpa(7.5);
            internship.setEligibleBranches("Computer Science, Information Science, Electronics");
            internship.setEligibleYears("3, 4");
            internship.setStatus(OpportunityStatus.OPEN);

            javaSkill.ifPresent(s -> internship.getRequiredSkills().add(new OpportunitySkillRequirement(internship, s.getId(), 3)));
            springSkill.ifPresent(s -> internship.getRequiredSkills().add(new OpportunitySkillRequirement(internship, s.getId(), 3)));
            sqlSkill.ifPresent(s -> internship.getRequiredSkills().add(new OpportunitySkillRequirement(internship, s.getId(), 2)));

            opportunityRepository.save(internship);

            // 2. Full-time Job
            Opportunity job = new Opportunity();
            job.setRecruiter(recruiter);
            job.setTitle("Full Stack Software Engineer");
            job.setCompany("Nexus Digital Technologies");
            job.setLocation("Hyderabad (On-site)");
            job.setType(OpportunityType.JOB);
            job.setDescription("We are looking for passionate graduate software engineers to craft high-performance client web applications with React and robust backend services.");
            job.setDeadline(LocalDate.now().plusMonths(3));
            job.setMinCgpa(7.0);
            job.setEligibleBranches("Computer Science, Information Science, Electrical");
            job.setEligibleYears("4");
            job.setStatus(OpportunityStatus.OPEN);

            reactSkill.ifPresent(s -> job.getRequiredSkills().add(new OpportunitySkillRequirement(job, s.getId(), 3)));
            javaSkill.ifPresent(s -> job.getRequiredSkills().add(new OpportunitySkillRequirement(job, s.getId(), 4)));
            gitSkill.ifPresent(s -> job.getRequiredSkills().add(new OpportunitySkillRequirement(job, s.getId(), 3)));

            opportunityRepository.save(job);

            // 3. Hackathon
            Opportunity hackathon = new Opportunity();
            hackathon.setRecruiter(recruiter);
            hackathon.setTitle("National AI & Web Innovation Hackathon 2026");
            hackathon.setCompany("SkillForge Foundation");
            hackathon.setLocation("Virtual / Remote");
            hackathon.setType(OpportunityType.HACKATHON);
            hackathon.setDescription("A 48-hour competitive innovation hackathon for engineering students across India. Build AI-driven solutions to real-world challenges.");
            hackathon.setDeadline(LocalDate.now().plusWeeks(6));
            hackathon.setMinCgpa(6.0);
            hackathon.setEligibleBranches(""); // All branches eligible
            hackathon.setEligibleYears("1, 2, 3, 4");
            hackathon.setStatus(OpportunityStatus.OPEN);

            pythonSkill.ifPresent(s -> hackathon.getRequiredSkills().add(new OpportunitySkillRequirement(hackathon, s.getId(), 2)));
            gitSkill.ifPresent(s -> hackathon.getRequiredSkills().add(new OpportunitySkillRequirement(hackathon, s.getId(), 2)));

            opportunityRepository.save(hackathon);
        }
    }
}
