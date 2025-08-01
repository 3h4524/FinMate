package org.codewith3h.finmateapplication.repository;

import org.codewith3h.finmateapplication.entity.Sysdiagram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysdiagramRepository extends JpaRepository<Sysdiagram, Long> {
}
