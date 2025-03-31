package com.tus.group_project.controller;

import com.tus.group_project.dao.TagRepository;
import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.dto.RecipeDto;
import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.User;
import com.tus.group_project.service.IRecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipeControllerTest {

    private IRecipeService recipeService;
    private UserRepository userRepository;
    private TagRepository tagRepository;
    private RecipeController recipeController;

    @BeforeEach
    void setup() {
        recipeService = mock(IRecipeService.class);
        userRepository = mock(UserRepository.class);
        tagRepository = mock(TagRepository.class);
        recipeController = new RecipeController(recipeService, userRepository, tagRepository);
    }

    @Test
    void getPublicRecipes_shouldReturnOkAndIncludeSelfLink() {
        // Arrange
    	Recipe recipe = new Recipe();
    	recipe.setId(1L);
    	recipe.setName("Test Recipe");
    	recipe.setTags(new ArrayList<>());
    	recipe.setIngredients(new ArrayList<>());
    	recipe.setSteps(new ArrayList<>());


        when(recipeService.getPublicRecipes()).thenReturn(List.of(recipe));

        // Act
        ResponseEntity<CollectionModel<EntityModel<RecipeDto>>> response = recipeController.getPublicRecipes();

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getLinks().hasLink("self"));

        List<EntityModel<RecipeDto>> recipes = response.getBody().getContent().stream().toList();
        assertEquals(1, recipes.size());
        assertEquals("Test Recipe", recipes.get(0).getContent().getName());
    }
}
