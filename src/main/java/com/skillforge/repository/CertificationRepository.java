package com.skillforge.repository;

import com.skillforge.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    Optional<Certification> findByIdAndProfileId(Long id, Long profileId);
}
