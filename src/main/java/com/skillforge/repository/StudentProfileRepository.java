package com.skillforge.repository;

import com.skillforge.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUserEmail(String email);
    boolean existsByUsn(String usn);
    boolean existsByUsnAndIdNot(String usn, Long id);
}
