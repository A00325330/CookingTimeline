package com.tus.group_project.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tus.group_project.model.Recipe;
import com.tus.group_project.model.Tag;
import com.tus.group_project.model.Visibility;
import com.tus.group_project.model.RecipeIngredient; 

@Data
public class RecipeDto {
    private String name;
    private String description;
    private List<String> steps;
    private int cookingTime;
    private Visibility visibility;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private List<String> tags;  
    private List<IngredientDto> ingredients;  

    public static RecipeDto fromEntity(Recipe recipe) {
        RecipeDto dto = new RecipeDto();
        dto.setName(recipe.getName());
        dto.setDescription(recipe.getDescription());
        dto.setSteps(recipe.getSteps());
        dto.setCookingTime(recipe.getCookingTime());
        dto.setVisibility(recipe.getVisibility());
        dto.setStartTime(recipe.getStartTime());
        dto.setFinishTime(recipe.getFinishTime());

        // ✅ Defensive Copy of `Set<Tag>` before processing
        dto.setTags(new ArrayList<>(recipe.getTags()).stream().map(Tag::getName).toList());

        // ✅ Defensive Copy of `Set<RecipeIngredient>` before processing
        dto.setIngredients(new ArrayList<>(recipe.getIngredients()).stream()
            .map(IngredientDto::fromEntity)
            .toList());

        return dto;
    }


    @Data
    public static class IngredientDto {
        private String name;
        private int cookingTime;
        private String cookingMethod;

        public static IngredientDto fromEntity(RecipeIngredient ingredient) {
            IngredientDto dto = new IngredientDto();
            dto.setName(ingredient.getName());
            dto.setCookingTime(ingredient.getCookingTime());
            dto.setCookingMethod(ingredient.getCookingMethod());
            return dto;
        }
    }
}
