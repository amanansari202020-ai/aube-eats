package com.aube.eats.service;

import com.aube.eats.domain.Entities.MenuItem;
import com.aube.eats.dto.MenuItemDto;
import com.aube.eats.mapper.MenuMapper;
import com.aube.eats.repository.MenuItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {
    private final MenuItemRepository menuItems;
    private final MenuMapper mapper;

    public MenuService(MenuItemRepository menuItems, MenuMapper mapper) { this.menuItems = menuItems; this.mapper = mapper; }

    @Transactional(readOnly = true)
    public Page<MenuItemDto> search(String category, Boolean vegetarian, String query, Pageable pageable) {
        Specification<MenuItem> spec = (root, criteria, builder) -> builder.isTrue(root.get("active"));
        if (category != null && !category.isBlank()) spec = spec.and((root, criteria, builder) -> builder.equal(builder.lower(root.get("category").get("name")), category.toLowerCase()));
        if (Boolean.TRUE.equals(vegetarian)) spec = spec.and((root, criteria, builder) -> builder.isTrue(root.get("vegetarian")));
        if (query != null && !query.isBlank()) {
            String pattern = "%" + query.trim().toLowerCase() + "%";
            spec = spec.and((root, criteria, builder) -> builder.or(builder.like(builder.lower(root.get("name")), pattern), builder.like(builder.lower(root.get("description")), pattern)));
        }
        return menuItems.findAll(spec, pageable).map(mapper::toDto);
    }
}