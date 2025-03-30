package com.tus.group_project.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {

    @Test
    void testDefaultValues() {
        Recipe recipe = new Recipe();

        assertEquals(Visibility.PRIVATE, recipe.getVisibility(), "Expected default visibility to be PRIVATE");
        assertNotNull(recipe.getTags(), "Expected tags list to be initialized");
        assertTrue(recipe.getTags().isEmpty(), "Expected tags to be empty by default");
        assertFalse(recipe.isTemporary(), "Expected isTemporary to be false by default");
    }

    @Test
    void testSettersAndGetters() {
        Recipe recipe = new Recipe();

        recipe.setName("Pasta");
        recipe.setDescription("Creamy Alfredo");
        recipe.setCookingMethod(CookingMethod.BAKE);
        recipe.setCookingTime(30);
        recipe.setSteps(List.of("Boil water", "Add pasta"));
        recipe.setStartTime(LocalDateTime.of(2024, 3, 1, 12, 0));
        recipe.setFinishTime(LocalDateTime.of(2024, 3, 1, 12, 30));
        recipe.setTemporary(true);

        assertEquals("Pasta", recipe.getName());
        assertEquals("Creamy Alfredo", recipe.getDescription());
        assertEquals(CookingMethod.BAKE, recipe.getCookingMethod());
        assertEquals(30, recipe.getCookingTime());
        assertEquals(2, recipe.getSteps().size());
        assertEquals(LocalDateTime.of(2024, 3, 1, 12, 0), recipe.getStartTime());
        assertEquals(LocalDateTime.of(2024, 3, 1, 12, 30), recipe.getFinishTime());
        assertTrue(recipe.isTemporary());
    }

    @Test
    void testAddTagsAndIngredients() {
        Recipe recipe = new Recipe();

        Tag tag = new Tag();
        tag.setName("Vegan");
        recipe.getTags().add(tag);

        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setName("Tomato");
        recipe.setIngredients(List.of(ingredient));

        assertEquals(1, recipe.getTags().size(), "Expected 1 tag to be added");
        assertEquals("Vegan", recipe.getTags().get(0).getName());

        assertEquals(1, recipe.getIngredients().size(), "Expected 1 ingredient");
        assertEquals("Tomato", recipe.getIngredients().get(0).getName());
    }
}
