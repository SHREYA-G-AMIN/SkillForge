package com.skillforge.opportunity.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opportunity_skill_requirements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"opportunity_id", "skill_id"}))
@Getter
@Setter
@NoArgsConstructor
public class OpportunitySkillRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(name = "skill_id", nullable = false)
    private Long skillId;

    @Column(nullable = false)
    private Integer requiredLevel; // 1 to 5 scale

    public OpportunitySkillRequirement(Opportunity opportunity, Long skillId, Integer requiredLevel) {
        this.opportunity = opportunity;
        this.skillId = skillId;
        this.requiredLevel = requiredLevel;
    }
}
