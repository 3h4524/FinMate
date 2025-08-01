package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Feature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Integer> {
    Optional<Feature> findByCode(String featureCode);

    List<Feature> findByIsActiveTrue();

    Feature findByIsActiveTrueAndCode(String code);

    Page<Feature> findByIsActive(boolean active, Pageable pageable);

    Page<Feature> findAll(Specification<Feature> specification, Pageable pageable);
}
