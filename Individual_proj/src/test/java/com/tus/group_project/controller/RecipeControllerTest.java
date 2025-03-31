package com.tus.group_project.controller;

import com.tus.group_project.dao.TagRepository;
import com.tus.group_project.dao.UserRepository;
import com.tus.group_project.dto.RecipeDto;
import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.User;
import com.tus.group_project.model.Visibility;
import com.tus.group_project.service.IRecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipeControllerTest {

    private IRecipeService recipeService;
    private UserRepository userRepository;
    private TagRepository tagRepository;
    private RecipeController recipeController;

    private User testUser;

    @BeforeEach
    void setup() {
        recipeService = mock(IRecipeService.class);
        userRepository = mock(UserRepository.class);
        tagRepository = mock(TagRepository.class);
        recipeController = new RecipeController(recipeService, userRepository, tagRepository);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");

        // Setup mock auth context
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            testUser.getEmail(), "password", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
            new TestingAuthenticationToken(userDetails, null));
    }

    @Test
    void getPublicRecipes_shouldReturnOkAndIncludeSelfLink() {
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("Test Recipe");
        recipe.setTags(new ArrayList<>());
        recipe.setIngredients(new ArrayList<>());
        recipe.setSteps(new ArrayList<>());

        when(recipeService.getPublicRecipes()).thenReturn(List.of(recipe));

        ResponseEntity<CollectionModel<EntityModel<RecipeDto>>> response = recipeController.getPublicRecipes();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getLinks().hasLink("self"));
    }

    @Test
    void getMyRecipes_shouldReturnUserRecipes() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setUser(testUser);
        recipe.setTags(new ArrayList<>());
        recipe.setIngredients(new ArrayList<>());
        recipe.setSteps(new ArrayList<>());
        when(recipeService.getUserRecipes(testUser)).thenReturn(List.of(recipe));

        ResponseEntity<CollectionModel<EntityModel<RecipeDto>>> response = recipeController.getMyRecipes();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getRecipesByTag_shouldReturnTaggedRecipes() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTags(new ArrayList<>());
        recipe.setUser(testUser);
        recipe.setIngredients(new ArrayList<>());
        recipe.setSteps(new ArrayList<>());
        when(recipeService.getRecipesByTag("tag", testUser)).thenReturn(List.of(recipe));

        ResponseEntity<CollectionModel<EntityModel<RecipeDto>>> response = recipeController.getRecipesByTag("tag");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getRecipeById_shouldReturnRecipeIfOwner() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setUser(testUser);
        recipe.setVisibility(Visibility.PRIVATE);
        recipe.setTags(new ArrayList<>());
        recipe.setIngredients(new ArrayList<>());
        recipe.setSteps(new ArrayList<>());
        when(recipeService.getRecipeById(1L)).thenReturn(Optional.of(recipe));

        ResponseEntity<EntityModel<RecipeDto>> response = recipeController.getRecipeById(1L);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void createRecipe_shouldReturnCreated() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        RecipeDto dto = new RecipeDto();
        Recipe entity = new Recipe();
        entity.setId(1L);
        entity.setUser(testUser);
        entity.setTags(new ArrayList<>());
        entity.setIngredients(new ArrayList<>());
        entity.setSteps(new ArrayList<>());
        when(tagRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(recipeService.createRecipe(any(), eq(testUser))).thenReturn(entity);

        ResponseEntity<EntityModel<RecipeDto>> response = recipeController.createRecipe(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void updateRecipe_shouldReturnUpdatedRecipe() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setUser(testUser);
        recipe.setTags(new ArrayList<>());
        recipe.setIngredients(new ArrayList<>());
        recipe.setSteps(new ArrayList<>());

        when(recipeService.getRecipeById(1L)).thenReturn(Optional.of(recipe));
        when(recipeService.updateRecipe(eq(1L), any(), eq(testUser))).thenReturn(recipe);

        RecipeDto dto = new RecipeDto();
        ResponseEntity<EntityModel<RecipeDto>> response = recipeController.updateRecipe(1L, dto);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void deleteRecipe_shouldReturnOk() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        doNothing().when(recipeService).deleteRecipe(1L, testUser);

        ResponseEntity<RepresentationModel<?>> response = recipeController.deleteRecipe(1L);

        assertEquals(200, response.getStatusCode().value());
    }
}