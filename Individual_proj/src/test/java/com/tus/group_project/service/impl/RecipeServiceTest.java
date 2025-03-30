package com.tus.group_project.service.impl;

import com.tus.group_project.dao.RecipeRepository;
import com.tus.group_project.dao.TagRepository;
import com.tus.group_project.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private TagRepository tagRepository;

    @InjectMocks private RecipeService recipeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRecipe_NewTag() {
        Recipe recipe = new Recipe();
        recipe.setTags(List.of(new Tag(null, "Dinner", new HashSet<>())));
        User user = new User();

        when(tagRepository.findByName("Dinner")).thenReturn(Optional.empty());
        when(tagRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(recipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Recipe saved = recipeService.createRecipe(recipe, user);

        assertEquals(user, saved.getUser());
        assertEquals(1, saved.getTags().size());
        verify(tagRepository).save(any());
        verify(recipeRepository).save(any());
    }

    @Test
    void testGetPublicRecipes() {
        List<Recipe> dummyList = List.of(new Recipe());
        when(recipeRepository.findByVisibility(Visibility.PUBLIC)).thenReturn(dummyList);

        List<Recipe> result = recipeService.getPublicRecipes();

        assertEquals(1, result.size());
        verify(recipeRepository).findByVisibility(Visibility.PUBLIC);
    }

    @Test
    void testGetUserRecipes() {
        User user = new User();
        List<Recipe> dummyList = List.of(new Recipe());
        when(recipeRepository.findByUser(user)).thenReturn(dummyList);

        List<Recipe> result = recipeService.getUserRecipes(user);

        assertEquals(1, result.size());
        verify(recipeRepository).findByUser(user);
    }

    @Test
    void testGetRecipeById_Public() {
        Recipe recipe = new Recipe();
        recipe.setVisibility(Visibility.PUBLIC);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        Optional<Recipe> result = recipeService.getRecipeById(1L, null);

        assertTrue(result.isPresent());
    }

    @Test
    void testGetRecipeById_PrivateOwned() {
        User user = new User();
        Recipe recipe = new Recipe();
        recipe.setVisibility(Visibility.PRIVATE);
        recipe.setUser(user);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        Optional<Recipe> result = recipeService.getRecipeById(1L, user);

        assertTrue(result.isPresent());
    }

    @Test
    void testUpdateRecipe_Success() {
        User user = new User();
        Recipe existing = new Recipe();
        existing.setVisibility(Visibility.PRIVATE);
        existing.setUser(user);
        existing.setName("Old");

        Recipe update = new Recipe();
        update.setName("New");
        update.setIngredients(new ArrayList<>());
        update.setSteps(List.of("Step1"));
        update.setCookingTime(20);
        update.setVisibility(Visibility.PUBLIC);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(recipeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Recipe result = recipeService.updateRecipe(1L, update, user);

        assertEquals("New", result.getName());
        assertEquals(20, result.getCookingTime());
        assertEquals(Visibility.PUBLIC, result.getVisibility());
    }

    @Test
    void testDeleteRecipe_Found() {
        Recipe recipe = new Recipe();
        User user = new User();
        recipe.setUser(user);
        recipe.setVisibility(Visibility.PRIVATE);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe(1L, user);

        verify(recipeRepository).delete(recipe);
    }

    @Test
    void testCalculateRecipeCookTime() {
        RecipeIngredient ingredient = new RecipeIngredient(null, null, "Chicken", 30, "Grill");
        Recipe recipe = new Recipe();
        recipe.setIngredients(List.of(ingredient));

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));

        int time = recipeService.calculateRecipeCookTime(1L);

        assertEquals(30, time);
    }

    @Test
    void testGetRecipesByTag_PublicAndOwned() {
        Tag tag = new Tag();
        User user = new User();
        Recipe publicRecipe = new Recipe();
        publicRecipe.setVisibility(Visibility.PUBLIC);

        Recipe privateRecipe = new Recipe();
        privateRecipe.setVisibility(Visibility.PRIVATE);
        privateRecipe.setUser(user);

        tag.setRecipes(Set.of(publicRecipe, privateRecipe));
        when(tagRepository.findByName("Quick")).thenReturn(Optional.of(tag));

        List<Recipe> result = recipeService.getRecipesByTag("Quick", user);

        assertEquals(2, result.size());
    }

    @Test
    void testGetAllTags() {
        when(tagRepository.findAll()).thenReturn(List.of(new Tag()));

        List<Tag> result = recipeService.getAllTags();

        assertEquals(1, result.size());
        verify(tagRepository).findAll();
    }
}
