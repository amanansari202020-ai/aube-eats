package com.aube.eats.service;

import com.aube.eats.domain.Entities.Category;
import com.aube.eats.domain.Entities.MenuItem;
import com.aube.eats.dto.MenuItemDto;
import com.aube.eats.mapper.MenuMapper;
import com.aube.eats.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {
    @Mock MenuItemRepository menuItems;
    @Mock MenuMapper mapper;
    private MenuService service;

    @BeforeEach
    void setUp() {
        service = new MenuService(menuItems, mapper);
    }

    @Test
    void searchMapsPagedActiveMenuItems() {
        Pageable pageable = PageRequest.of(1, 10);
        Category category = new Category();
        category.setName("Bagels");
        MenuItem item = new MenuItem();
        item.setId("sesame");
        item.setCategory(category);
        MenuItemDto dto = new MenuItemDto("sesame", "The Sesame", "Scallion cream cheese", new BigDecimal("229.00"), "Bagels", "image.jpg", true);
        when(menuItems.findAll(ArgumentMatchers.<Specification<MenuItem>>any(), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(item), pageable, 11));
        when(mapper.toDto(item)).thenReturn(dto);

        var result = service.search("Bagels", true, "sesame", pageable);

        assertEquals(1, result.getNumberOfElements());
            assertEquals(2, result.getTotalPages());
        assertEquals(dto, result.getContent().get(0));
        verify(menuItems).findAll(ArgumentMatchers.<Specification<MenuItem>>any(), eq(pageable));
    }
}