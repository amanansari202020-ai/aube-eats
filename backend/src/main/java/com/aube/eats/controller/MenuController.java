package com.aube.eats.controller;

import com.aube.eats.dto.MenuItemDto;
import com.aube.eats.service.MenuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {
    private final MenuService menuService;
    public MenuController(MenuService menuService) { this.menuService = menuService; }

    @GetMapping
    public Page<MenuItemDto> search(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Boolean vegetarian,
        @RequestParam(required = false, name = "q") String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size
    ) {
        return menuService.search(category, vegetarian, query, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("name").ascending()));
    }
}