package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByIsSystemTrue();
}