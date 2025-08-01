package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCategoryRepository extends JpaRepository<UserCategory, Integer> {
    List<UserCategory> findByUserId(Integer userId);

    Integer countByUserId(Integer userId);
}
