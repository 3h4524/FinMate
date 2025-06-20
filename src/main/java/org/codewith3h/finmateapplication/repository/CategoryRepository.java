package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> origin/authentication
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByNameAndType(String categoryName, String type);
<<<<<<< HEAD
        List<Category> findAllByNameNot(String name);
=======
>>>>>>> origin/authentication
}
