package com.aube.eats.repository;

import com.aube.eats.domain.Entities.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MenuItemRepository extends JpaRepository<MenuItem, String>, JpaSpecificationExecutor<MenuItem> {
	long countByActiveTrue();
}