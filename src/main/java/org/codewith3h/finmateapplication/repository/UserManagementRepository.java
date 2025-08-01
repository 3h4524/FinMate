package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserManagementRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
} 