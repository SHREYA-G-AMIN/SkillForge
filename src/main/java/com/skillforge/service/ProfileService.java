package com.skillforge.service;

import com.skillforge.dto.ProfileDtos.*;
import com.skillforge.entity.*;
import com.skillforge.exception.DuplicateResourceException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final StudentProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final ProfileSkillRepository profileSkillRepository;
    private final ProjectRepository projectRepository;
    private final CertificationRepository certificationRepository;
    private final ExperienceRepository experienceRepository;

    // ---------------- profile ----------------

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String email) {
        return toResponse(profileOf(email));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileById(Long id) {
        return toResponse(profileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found")));
    }

    public ProfileResponse updateMyProfile(String email, ProfileUpdateRequest req) {
        StudentProfile p = profileOf(email);
        String usn = req.usn().trim().toUpperCase();
        if (profileRepository.existsByUsnAndIdNot(usn, p.getId())) {
            throw new DuplicateResourceException("USN is already registered to another student");
        }
        p.setName(req.name().trim());
        p.setUsn(usn);
        p.setCollege(blankToNull(req.college()));
        p.setDegree(blankToNull(req.degree()));
        p.setBranch(blankToNull(req.branch()));
        p.setSemester(req.semester());
        p.setCgpa(req.cgpa());
        p.setPhone(blankToNull(req.phone()));
        p.setCareerGoal(blankToNull(req.careerGoal()));
        p.setCareerInterests(blankToNull(req.careerInterests()));
        p.setResumeUrl(blankToNull(req.resumeUrl()));
        return toResponse(profileRepository.saveAndFlush(p));
    }

    // ---------------- skills ----------------

    public SkillItem addSkill(String email, SkillRequest req) {
        StudentProfile p = profileOf(email);
        Skill skill = skillRepository.findById(req.skillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        if (profileSkillRepository.existsByProfileIdAndSkillId(p.getId(), skill.getId())) {
            throw new DuplicateResourceException("Skill already added to your profile");
        }
        ProfileSkill ps = new ProfileSkill();
        ps.setProfile(p);
        ps.setSkill(skill);
        ps.setLevel(req.level());
        return toSkillItem(profileSkillRepository.save(ps));
    }

    public SkillItem updateSkill(String email, Long id, SkillLevelRequest req) {
        ProfileSkill ps = profileSkillRepository.findByIdAndProfileId(id, profileOf(email).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill entry not found"));
        ps.setLevel(req.level());
        return toSkillItem(profileSkillRepository.save(ps));
    }

    public void deleteSkill(String email, Long id) {
        ProfileSkill ps = profileSkillRepository.findByIdAndProfileId(id, profileOf(email).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill entry not found"));
        profileSkillRepository.delete(ps);
    }

    // ---------------- projects ----------------

    public ProjectItem addProject(String email, ProjectRequest req) {
        Project pr = new Project();
        pr.setProfile(profileOf(email));
        applyProject(pr, req);
        return toProjectItem(projectRepository.save(pr));
    }

    public ProjectItem updateProject(String email, Long id, ProjectRequest req) {
        Project pr = projectRepository.findByIdAndProfileId(id, profileOf(email).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        applyProject(pr, req);
        return toProjectItem(projectRepository.save(pr));
    }

    public void deleteProject(String email, Long id) {
        Project pr = projectRepository.findByIdAndProfileId(id, profileOf(email).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        projectRepository.delete(pr);
    }

    // ---------------- certifications ----------------

    public CertificationItem addCertification(String email, CertificationRequest req) {
        Certification c = new Certification();
        c.setProfile(profileOf(email));
        applyCertification(c, req);
        return toCertificationItem(certificationRepository.save(c));
    }

    public CertificationItem updateCertification(String email, Long id, CertificationRequest req) {
        Certification c = certificationRepository.findByIdAndProfileId(id, profileOf(email).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));
        applyCertification(c, req);
        return toCertificationItem(certificationRepository.save(c));
    }

    public void deleteCertification(String email, Long id) {
        Certification c = certificationRepository.findByIdAndProfileId(id, profileOf(email).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));
        certificationRepository.delete(c);
    }

    // ---------------- experience ----------------

    public ExperienceItem addExperience(String email, ExperienceRequest req) {
        Experience e = new Experience();
        e.setProfile(profileOf(email));
        applyExperience(e, req);
        return toExperienceItem(experienceRepository.save(e));
    }

    public ExperienceItem updateExperience(String email, Long id, ExperienceRequest req) {
        Experience e = experienceRepository.findByIdAndProfileId(id, profileOf(email).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        applyExperience(e, req);
        return toExperienceItem(experienceRepository.save(e));
    }

    public void deleteExperience(String email, Long id) {
        Experience e = experienceRepository.findByIdAndProfileId(id, profileOf(email).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Experience not found"));
        experienceRepository.delete(e);
    }

    // ---------------- helpers ----------------

    private StudentProfile profileOf(String email) {
        return profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    private void applyProject(Project pr, ProjectRequest r) {
        pr.setTitle(r.title().trim());
        pr.setDescription(r.description());
        pr.setTechUsed(r.techUsed());
        pr.setLink(r.link());
    }

    private void applyCertification(Certification c, CertificationRequest r) {
        c.setName(r.name().trim());
        c.setIssuer(r.issuer());
        c.setIssueDate(r.issueDate());
    }

    private void applyExperience(Experience e, ExperienceRequest r) {
        e.setCompany(r.company().trim());
        e.setRole(r.role().trim());
        e.setDuration(r.duration());
        e.setDescription(r.description());
    }

    private SkillItem toSkillItem(ProfileSkill ps) {
        return new SkillItem(ps.getId(), ps.getSkill().getId(), ps.getSkill().getName(),
                ps.getSkill().getCategory(), ps.getLevel());
    }

    private ProjectItem toProjectItem(Project p) {
        return new ProjectItem(p.getId(), p.getTitle(), p.getDescription(), p.getTechUsed(), p.getLink());
    }

    private CertificationItem toCertificationItem(Certification c) {
        return new CertificationItem(c.getId(), c.getName(), c.getIssuer(), c.getIssueDate());
    }

    private ExperienceItem toExperienceItem(Experience e) {
        return new ExperienceItem(e.getId(), e.getCompany(), e.getRole(), e.getDuration(), e.getDescription());
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private ProfileResponse toResponse(StudentProfile p) {
        return new ProfileResponse(
                p.getId(), p.getUser().getEmail(), p.getName(), p.getUsn(), p.getCollege(), p.getDegree(),
                p.getBranch(), p.getSemester(), p.getCgpa(), p.getPhone(),
                p.getCareerGoal(), p.getCareerInterests(), p.getResumeUrl(),
                p.getSkills().stream().map(this::toSkillItem).toList(),
                p.getProjects().stream().map(this::toProjectItem).toList(),
                p.getCertifications().stream().map(this::toCertificationItem).toList(),
                p.getExperiences().stream().map(this::toExperienceItem).toList());
    }
}
