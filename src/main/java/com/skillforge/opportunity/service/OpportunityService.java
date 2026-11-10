package com.skillforge.opportunity.service;

import com.skillforge.entity.User;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.opportunity.dto.MatchResult;
import com.skillforge.opportunity.dto.OpportunityDtos.*;
import com.skillforge.opportunity.exception.UnauthorizedOpportunityAccessException;
import com.skillforge.opportunity.integration.CurrentUserProvider;
import com.skillforge.opportunity.integration.SkillCatalogGateway;
import com.skillforge.opportunity.integration.StudentProfileData;
import com.skillforge.opportunity.integration.StudentProfileGateway;
import com.skillforge.opportunity.integration.UserContext;
import com.skillforge.opportunity.model.Bookmark;
import com.skillforge.opportunity.model.Opportunity;
import com.skillforge.opportunity.model.OpportunitySkillRequirement;
import com.skillforge.opportunity.model.OpportunityStatus;
import com.skillforge.opportunity.model.OpportunityType;
import com.skillforge.opportunity.repository.ApplicationRepository;
import com.skillforge.opportunity.repository.BookmarkRepository;
import com.skillforge.opportunity.repository.OpportunityRepository;
import com.skillforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final StudentProfileGateway studentProfileGateway;
    private final SkillCatalogGateway skillCatalogGateway;
    private final MatchService matchService;

    @Transactional(readOnly = true)
    public List<OpportunitySummaryResponse> searchOpportunities(String q, OpportunityType type, String location, Long skillId) {
        UserContext current = tryGetCurrentUser();
        StudentProfileData profile = null;
        Set<Long> bookmarkedIds = Collections.emptySet();

        if (current != null && "STUDENT".equalsIgnoreCase(current.role())) {
            profile = studentProfileGateway.getStudentProfile(current.userId());
            bookmarkedIds = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(current.userId()).stream()
                    .map(b -> b.getOpportunity().getId())
                    .collect(Collectors.toSet());
        }

        // Students see only OPEN opportunities in the public search
        OpportunityStatus searchStatus = (current != null && "RECRUITER".equalsIgnoreCase(current.role())) ? null : OpportunityStatus.OPEN;

        List<Opportunity> opportunities = opportunityRepository.searchOpportunities(
                searchStatus,
                type,
                (location != null && !location.isBlank()) ? location.trim() : null,
                skillId,
                (q != null && !q.isBlank()) ? q.trim() : null
        );

        StudentProfileData finalProfile = profile;
        Set<Long> finalBookmarks = bookmarkedIds;

        return opportunities.stream()
                .map(opp -> mapToSummary(opp, finalProfile, finalBookmarks.contains(opp.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OpportunityDetailResponse getOpportunityDetails(Long id) {
        Opportunity opp = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));

        UserContext current = tryGetCurrentUser();
        StudentProfileData profile = null;
        boolean bookmarked = false;
        boolean applied = false;
        String applicationStatus = null;

        if (current != null && "STUDENT".equalsIgnoreCase(current.role())) {
            profile = studentProfileGateway.getStudentProfile(current.userId());
            bookmarked = bookmarkRepository.existsByUserIdAndOpportunityId(current.userId(), opp.getId());
            var appOpt = applicationRepository.findByOpportunityIdAndApplicantId(opp.getId(), current.userId());
            if (appOpt.isPresent()) {
                applied = true;
                applicationStatus = appOpt.get().getStatus().name();
            }
        }

        MatchResult match = matchService.computeMatch(opp, profile);
        long applicantCount = applicationRepository.countByOpportunityId(opp.getId());

        StudentProfileData finalProfile = profile;
        List<MatchResult.SkillRequirementDetail> allRequirements = opp.getRequiredSkills().stream()
                .map(req -> {
                    String skillName = skillCatalogGateway.getSkillName(req.getSkillId());
                    Integer studentLevel = (finalProfile != null && finalProfile.skillLevels() != null)
                            ? finalProfile.skillLevels().get(req.getSkillId())
                            : null;
                    boolean satisfied = studentLevel != null && studentLevel >= req.getRequiredLevel();
                    return new MatchResult.SkillRequirementDetail(
                            req.getSkillId(), skillName, req.getRequiredLevel(), studentLevel, satisfied);
                })
                .collect(Collectors.toList());

        return new OpportunityDetailResponse(
                opp.getId(),
                opp.getTitle(),
                opp.getCompany(),
                opp.getLocation(),
                opp.getType(),
                opp.getStatus(),
                opp.getDescription(),
                opp.getDeadline(),
                opp.getMinCgpa(),
                MatchService.parseCommaSeparatedStrings(opp.getEligibleBranches()),
                MatchService.parseCommaSeparatedIntegers(opp.getEligibleYears()),
                allRequirements,
                match.matchedSkills(),
                match.missingSkills(),
                match.matchPercentage(),
                match.eligible(),
                match.eligibilityReasons(),
                bookmarked,
                applied,
                applicationStatus,
                applicantCount,
                opp.getCreatedAt(),
                opp.getRecruiter().getId()
        );
    }

    @Transactional
    public void addBookmark(Long opportunityId) {
        UserContext current = currentUserProvider.getCurrentUser();
        Opportunity opp = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + opportunityId));

        if (!bookmarkRepository.existsByUserIdAndOpportunityId(current.userId(), opportunityId)) {
            User user = userRepository.getReferenceById(current.userId());
            bookmarkRepository.save(new Bookmark(user, opp));
        }
    }

    @Transactional
    public void removeBookmark(Long opportunityId) {
        UserContext current = currentUserProvider.getCurrentUser();
        bookmarkRepository.deleteByUserIdAndOpportunityId(current.userId(), opportunityId);
    }

    @Transactional(readOnly = true)
    public List<OpportunitySummaryResponse> getBookmarks() {
        UserContext current = currentUserProvider.getCurrentUser();
        StudentProfileData profile = studentProfileGateway.getStudentProfile(current.userId());

        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(current.userId());
        return bookmarks.stream()
                .map(b -> mapToSummary(b.getOpportunity(), profile, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OpportunitySummaryResponse> getMyPostings() {
        UserContext current = currentUserProvider.getCurrentUser();
        List<Opportunity> list = opportunityRepository.findByRecruiterIdOrderByCreatedAtDesc(current.userId());
        return list.stream()
                .map(opp -> mapToSummary(opp, null, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public OpportunityDetailResponse createOpportunity(OpportunityCreateRequest request) {
        UserContext current = currentUserProvider.getCurrentUser();
        User recruiter = userRepository.findById(current.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter account not found"));

        Opportunity opp = new Opportunity();
        opp.setRecruiter(recruiter);
        populateOpportunityFields(opp, request);

        Opportunity saved = opportunityRepository.save(opp);
        return getOpportunityDetails(saved.getId());
    }

    @Transactional
    public OpportunityDetailResponse updateOpportunity(Long id, OpportunityCreateRequest request) {
        Opportunity opp = getRecruiterOwnedOpportunity(id);
        populateOpportunityFields(opp, request);

        Opportunity saved = opportunityRepository.save(opp);
        return getOpportunityDetails(saved.getId());
    }

    @Transactional
    public void deleteOpportunity(Long id) {
        Opportunity opp = getRecruiterOwnedOpportunity(id);
        opportunityRepository.delete(opp);
    }

    @Transactional
    public OpportunitySummaryResponse setOpportunityStatus(Long id, OpportunityStatus status) {
        Opportunity opp = getRecruiterOwnedOpportunity(id);
        opp.setStatus(status);
        Opportunity saved = opportunityRepository.save(opp);
        return mapToSummary(saved, null, false);
    }

    public Opportunity getRecruiterOwnedOpportunity(Long id) {
        UserContext current = currentUserProvider.getCurrentUser();
        Opportunity opp = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));

        if (!opp.getRecruiter().getId().equals(current.userId()) && !"ADMIN".equalsIgnoreCase(current.role())) {
            throw new UnauthorizedOpportunityAccessException("You do not have permission to manage this opportunity");
        }
        return opp;
    }

    private void populateOpportunityFields(Opportunity opp, OpportunityCreateRequest req) {
        opp.setTitle(req.title().trim());
        opp.setCompany(req.company().trim());
        opp.setLocation(req.location().trim());
        opp.setType(req.type());
        opp.setDescription(req.description().trim());
        opp.setDeadline(req.deadline());
        opp.setMinCgpa(req.minCgpa());

        if (req.eligibleBranches() != null && !req.eligibleBranches().isEmpty()) {
            opp.setEligibleBranches(String.join(", ", req.eligibleBranches()));
        } else {
            opp.setEligibleBranches(null);
        }

        if (req.eligibleYears() != null && !req.eligibleYears().isEmpty()) {
            opp.setEligibleYears(req.eligibleYears().stream().map(String::valueOf).collect(Collectors.joining(", ")));
        } else {
            opp.setEligibleYears(null);
        }

        // Validate and update required skills
        opp.getRequiredSkills().clear();
        if (req.requiredSkills() != null) {
            for (SkillRequirementDto s : req.requiredSkills()) {
                if (!skillCatalogGateway.existsById(s.skillId())) {
                    throw new BadRequestException("Skill with ID " + s.skillId() + " does not exist in master catalog");
                }
                opp.getRequiredSkills().add(new OpportunitySkillRequirement(opp, s.skillId(), s.requiredLevel()));
            }
        }
    }

    private OpportunitySummaryResponse mapToSummary(Opportunity opp, StudentProfileData profile, boolean bookmarked) {
        MatchResult match = (profile != null) ? matchService.computeMatch(opp, profile) : null;
        long applicantCount = applicationRepository.countByOpportunityId(opp.getId());

        return new OpportunitySummaryResponse(
                opp.getId(),
                opp.getTitle(),
                opp.getCompany(),
                opp.getLocation(),
                opp.getType(),
                opp.getStatus(),
                opp.getDeadline(),
                opp.getMinCgpa(),
                MatchService.parseCommaSeparatedStrings(opp.getEligibleBranches()),
                MatchService.parseCommaSeparatedIntegers(opp.getEligibleYears()),
                match != null ? match.matchPercentage() : null,
                match != null ? match.eligible() : null,
                bookmarked,
                applicantCount,
                opp.getCreatedAt()
        );
    }

    private UserContext tryGetCurrentUser() {
        try {
            return currentUserProvider.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}
