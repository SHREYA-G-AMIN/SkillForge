package com.skillforge.opportunity.model;

import com.skillforge.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OpportunityType type;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private LocalDate deadline;

    private Double minCgpa;

    /** Comma-separated list of eligible branches, e.g. "Computer Science, Information Science", or empty for any. */
    @Column(length = 500)
    private String eligibleBranches;

    /** Comma-separated list of eligible academic years, e.g. "3, 4", or empty for any. */
    @Column(length = 50)
    private String eligibleYears;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpportunityStatus status = OpportunityStatus.OPEN;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpportunitySkillRequirement> requiredSkills = new ArrayList<>();

    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Application> applications = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = OpportunityStatus.OPEN;
        }
    }
}
