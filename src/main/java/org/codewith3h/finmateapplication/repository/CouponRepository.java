package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon,Integer> {

    Page<Coupon> findAll(Specification<Coupon> spec, Pageable pageable);
    Optional<Coupon> findByCode(String code);
}
