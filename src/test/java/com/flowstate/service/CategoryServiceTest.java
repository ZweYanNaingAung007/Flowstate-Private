package com.flowstate.service;

import com.flowstate.entity.Category;
import com.flowstate.repository.CategoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldCreateCategorySuccessfully() {

        Category category = new Category();
        category.setName("Programming");
        category.setDescription("Programming-related tasks");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Programming");
        savedCategory.setDescription("Programming-related tasks");

        when(categoryRepository.save(category)).thenReturn(savedCategory);

        Category result = categoryService.create(category);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Programming", result.getName());
        assertEquals("Programming-related tasks", result.getDescription());

        verify(categoryRepository).save(category);
    }

    @Test
    void shouldFindCategoryById() {

        Category category = new Category();
        category.setId(1L);
        category.setName("Programming");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        Category result = categoryService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Programming", result.getName());

        verify(categoryRepository).findById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenCategoryDoesNotExist() {

        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoryService.findById(999L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        assertEquals(
                "Category not found",
                exception.getReason()
        );

        verify(categoryRepository).findById(999L);
    }
}