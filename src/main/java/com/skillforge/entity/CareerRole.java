package com.skillforge.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "career_roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CareerRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "career_role_skills", joinColumns = @JoinColumn(name = "career_role_id"))
    @Column(name = "skill_name")
    @Builder.Default
    private List<String> requiredSkills = new ArrayList<>();
}