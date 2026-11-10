package com.skillforge.opportunity.repository;

import com.skillforge.opportunity.model.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Bookmark> findByUserIdAndOpportunityId(Long userId, Long opportunityId);

    boolean existsByUserIdAndOpportunityId(Long userId, Long opportunityId);

    void deleteByUserIdAndOpportunityId(Long userId, Long opportunityId);
}
