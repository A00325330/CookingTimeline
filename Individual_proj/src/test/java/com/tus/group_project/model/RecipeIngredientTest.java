package com.tus.group_project.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipeIngredientTest {

    @Test
    void testDefaultConstructorAndSetters() {
        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setId(1L);
        ingredient.setName("Onion");
        ingredient.setCookingTime(10);
        ingredient.setCookingMethod("Chop");

        assertEquals(1L, ingredient.getId());
        assertEquals("Onion", ingredient.getName());
        assertEquals(10, ingredient.getCookingTime());
        assertEquals("Chop", ingredient.getCookingMethod());
        assertNull(ingredient.getRecipe(), "Expected recipe to be null by default");
    }

    @Test
    void testAllArgsConstructor() {
        Recipe recipe = new Recipe();
        RecipeIngredient ingredient = new RecipeIngredient(2L, recipe, "Garlic", 5, "Fry");

        assertEquals(2L, ingredient.getId());
        assertEquals(recipe, ingredient.getRecipe());
        assertEquals("Garlic", ingredient.getName());
        assertEquals(5, ingredient.getCookingTime());
        assertEquals("Fry", ingredient.getCookingMethod());
    }

    @Test
    void testToStringIncludesFields() {
        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setId(3L);
        ingredient.setName("Pepper");
        ingredient.setCookingTime(7);
        ingredient.setCookingMethod("Grill");

        String output = ingredient.toString();

        assertTrue(output.contains("Pepper"), "toString should include name");
        assertTrue(output.contains("7"), "toString should include cooking time");
        assertTrue(output.contains("Grill"), "toString should include cooking method");
    }
}
