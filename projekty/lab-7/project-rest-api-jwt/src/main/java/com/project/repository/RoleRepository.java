package com.project.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.model.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	Optional<Role> findByName(String name);
}