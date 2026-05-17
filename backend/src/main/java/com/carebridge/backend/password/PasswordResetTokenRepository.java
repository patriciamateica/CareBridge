package com.carebridge.backend.password;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.email = :email")
    void deleteAllByEmail(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt < :now")
    void deleteExpired(LocalDateTime now);
}
