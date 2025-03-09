package com.tus.group_project.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipe_ingredients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    @JsonIgnore  // ✅ Prevents infinite recursion
    private Recipe recipe;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int cookingTime;

    @Column(nullable = false) // ✅ Ensure cookingMethod is always set
    private String cookingMethod;

    @Override
    public String toString() {
        return "RecipeIngredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", cookingTime=" + cookingTime +
                ", cookingMethod='" + cookingMethod + '\'' +
                '}';
    }
}
