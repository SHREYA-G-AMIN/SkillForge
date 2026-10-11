package com.skillforge.repository;

import com.skillforge.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    boolean existsByJti(String jti);
    void deleteByExpiresAtBefore(Instant cutoff);
}
