package com.tus.group_project.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import com.tus.group_project.model.Visibility;

@Data
public class RecipeDto {
    private String name;
    private String description; // ✅ Added description field
    private List<IngredientDto> ingredients; // ✅ Uses a list of objects for ingredients
    private List<String> steps;
    private int cookingTime;
    private Visibility visibility;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    // ✅ Nested DTO class for Ingredients (supports cooking methods)
    @Data
    public static class IngredientDto {
        private String name;
        private int cookingTime;
        private String cookingMethod;
    }
}
