package com.skillforge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Master skill list shared by all modules (profiles, role requirements, opportunities). */
@Entity
@Table(name = "skills")
@Getter @Setter @NoArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String category;

    public Skill(String name, String category) {
        this.name = name;
        this.category = category;
    }
}
