package com.calendarugr.user_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.calendarugr.user_service.entities.TemporaryToken;

public interface TemporaryTokenRepository extends JpaRepository<TemporaryToken, Long> {
    Optional<TemporaryToken> findByToken(String token);
    void deleteByToken(String token);
}
