package com.tus.group_project.dto;

import com.tus.group_project.dao.TagRepository;
import com.tus.group_project.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipeDtoTest {

    private TagRepository tagRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        tagRepository = Mockito.mock(TagRepository.class);
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
    }

    @Test
    void testFromEntity() {
        Tag tag = new Tag(1L, "Easy", new HashSet<>());
        RecipeIngredient ingredient = new RecipeIngredient(null, null, "Chicken", 25, "Bake");

        Recipe recipe = new Recipe();
        recipe.setName("Roast Chicken");
        recipe.setDescription("Tasty");
        recipe.setSteps(List.of("Prep", "Cook"));
        recipe.setVisibility(Visibility.PUBLIC);
        recipe.setStartTime(LocalDateTime.now());
        recipe.setFinishTime(LocalDateTime.now().plusHours(1));
        recipe.setTags(List.of(tag));
        recipe.setIngredients(List.of(ingredient));
        recipe.setCookingTime(25);

        RecipeDto dto = RecipeDto.fromEntity(recipe);

        assertEquals("Roast Chicken", dto.getName());
        assertEquals("Tasty", dto.getDescription());
        assertEquals(List.of("Easy"), dto.getTags());
        assertEquals(1, dto.getIngredients().size());
        assertEquals("Chicken", dto.getIngredients().get(0).getName());
    }

    @Test
    void testToEntity_WithNewTagsAndIngredients() {
        RecipeDto dto = new RecipeDto();
        dto.setName("Soup");
        dto.setDescription("Warm soup");
        dto.setSteps(List.of("Boil", "Serve"));
        dto.setVisibility(Visibility.PRIVATE);
        dto.setStartTime(LocalDateTime.now());
        dto.setFinishTime(LocalDateTime.now().plusMinutes(30));
        dto.setTags(List.of("Hearty", "Lunch"));

        RecipeDto.IngredientDto ing = new RecipeDto.IngredientDto();
        ing.setName("Water");
        ing.setCookingTime(10);
        ing.setCookingMethod("Fry");

        dto.setIngredients(List.of(ing));

        when(tagRepository.findByName("Hearty")).thenReturn(Optional.empty());
        when(tagRepository.findByName("Lunch")).thenReturn(Optional.empty());
        when(tagRepository.save(any())).thenAnswer(inv -> {
            Tag t = inv.getArgument(0);
            t.setId(new Random().nextLong());
            return t;
        });

        Recipe recipe = dto.toEntity(testUser, tagRepository);

        assertEquals("Soup", recipe.getName());
        assertEquals(1, recipe.getIngredients().size());
        assertEquals(10, recipe.getCookingTime());
        assertEquals(testUser, recipe.getUser());
        assertEquals(2, recipe.getTags().size());
    }

    @Test
    void testUpdateEntity_OverwritesExistingRecipe() {
        Tag existingTag = new Tag(1L, "Dinner", new HashSet<>());

        Recipe existing = new Recipe();
        existing.setIngredients(new ArrayList<>());
        existing.setTags(new ArrayList<>());
        existing.setUser(testUser);

        RecipeDto dto = new RecipeDto();
        dto.setName("Updated");
        dto.setDescription("Updated Desc");
        dto.setSteps(List.of("New Step"));
        dto.setVisibility(Visibility.PUBLIC);
        dto.setStartTime(LocalDateTime.now());
        dto.setFinishTime(LocalDateTime.now().plusMinutes(20));
        dto.setTags(List.of("Dinner"));

        RecipeDto.IngredientDto ing = new RecipeDto.IngredientDto();
        ing.setName("Rice");
        ing.setCookingTime(15);
        ing.setCookingMethod("Boil");
        dto.setIngredients(List.of(ing));

        when(tagRepository.findByName("Dinner")).thenReturn(Optional.of(existingTag));

        dto.updateEntity(existing, tagRepository);

        assertEquals("Updated", existing.getName());
        assertEquals(1, existing.getIngredients().size());
        assertEquals("Rice", existing.getIngredients().get(0).getName());
        assertEquals(15, existing.getCookingTime());
    }
}
