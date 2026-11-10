package com.skillforge.opportunity.repository;

import com.skillforge.opportunity.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByApplicantIdOrderByAppliedAtDesc(Long applicantId);

    List<Application> findByOpportunityIdOrderByAppliedAtDesc(Long opportunityId);

    Optional<Application> findByOpportunityIdAndApplicantId(Long opportunityId, Long applicantId);

    boolean existsByOpportunityIdAndApplicantId(Long opportunityId, Long applicantId);

    long countByOpportunityId(Long opportunityId);
}
