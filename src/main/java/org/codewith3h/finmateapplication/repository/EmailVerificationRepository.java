package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmailAndVerificationCode(String email, String verificationCode);
    Optional<EmailVerification> findFirstByEmailOrderByCreatedAtDesc(String email);
} 