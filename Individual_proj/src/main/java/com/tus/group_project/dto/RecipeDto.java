package com.tus.group_project.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import com.tus.group_project.model.Visibility;

import com.tus.group_project.model.CookingMethod;

@Data
public class RecipeDto {
    private String name;
    private List<String> ingredients;
    private List<String> steps;
    private CookingMethod cookingMethod;
    private int cookingTime;
    private Visibility visibility;  // ✅ Add this field


    private LocalDateTime startTime;  // ✅ Ensure this is added
    private LocalDateTime finishTime; // ✅ Ensure this is added
}
