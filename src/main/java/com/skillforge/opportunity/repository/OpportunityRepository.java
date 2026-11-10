package com.skillforge.opportunity.repository;

import com.skillforge.opportunity.model.Opportunity;
import com.skillforge.opportunity.model.OpportunityStatus;
import com.skillforge.opportunity.model.OpportunityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    List<Opportunity> findByRecruiterIdOrderByCreatedAtDesc(Long recruiterId);

    Optional<Opportunity> findByIdAndRecruiterId(Long id, Long recruiterId);

    @Query("""
        SELECT DISTINCT o FROM Opportunity o
        LEFT JOIN o.requiredSkills rs
        WHERE (:status IS NULL OR o.status = :status)
          AND (:type IS NULL OR o.type = :type)
          AND (:location IS NULL OR LOWER(o.location) LIKE LOWER(CONCAT('%', :location, '%')))
          AND (:skillId IS NULL OR rs.skillId = :skillId)
          AND (:q IS NULL OR (
              LOWER(o.title) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(o.company) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(o.location) LIKE LOWER(CONCAT('%', :q, '%'))
          ))
        ORDER BY o.createdAt DESC
    """)
    List<Opportunity> searchOpportunities(
            @Param("status") OpportunityStatus status,
            @Param("type") OpportunityType type,
            @Param("location") String location,
            @Param("skillId") Long skillId,
            @Param("q") String q
    );
}
