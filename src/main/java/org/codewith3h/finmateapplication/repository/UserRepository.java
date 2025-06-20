package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.List;
=======
>>>>>>> origin/authentication
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);
    Optional<User> findByPasswordResetToken(String passwordResetToken);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.id = ?1")
    void updateLastLoginAt(Integer userId);
<<<<<<< HEAD

    List<User> findAllByIsPremium(boolean check);
=======
>>>>>>> origin/authentication
}
