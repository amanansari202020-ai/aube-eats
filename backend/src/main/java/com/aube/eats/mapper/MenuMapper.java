package com.aube.eats.mapper;

import com.aube.eats.domain.Entities.MenuItem;
import com.aube.eats.dto.MenuItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MenuMapper {
    @Mapping(target = "category", source = "category.name")
    MenuItemDto toDto(MenuItem item);
}