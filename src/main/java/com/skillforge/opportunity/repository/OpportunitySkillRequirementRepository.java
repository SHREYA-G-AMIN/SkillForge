package com.skillforge.opportunity.repository;

import com.skillforge.opportunity.model.OpportunitySkillRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpportunitySkillRequirementRepository extends JpaRepository<OpportunitySkillRequirement, Long> {
    List<OpportunitySkillRequirement> findByOpportunityId(Long opportunityId);
    void deleteByOpportunityId(Long opportunityId);
}
