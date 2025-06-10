package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Integer> {

    Optional<Wallet> findByUserId(Integer userId);
}
