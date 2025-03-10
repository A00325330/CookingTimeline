export async function checkExistingTempRecipe() {
    try {
        const response = await fetch("http://localhost:8081/api/recipes/temp/2"); // Adjust ID logic as needed
        return response.ok ? await response.json() : null;
    } catch (error) {
        console.error("❌ Error checking for temp recipe:", error);
        return null;
    }
}

export async function createTempRecipe(name) {
    try {
        const response = await fetch("http://localhost:8081/api/recipes/temp", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, visibility: "PRIVATE", ingredients: [], steps: [] })
        });

        if (!response.ok) throw new Error("Failed to create recipe.");
        return await response.json();
    } catch (error) {
        console.error("❌ Error creating recipe:", error);
        return null;
    }
}

import { fetchPublicRecipes } from "./recipes.js";

export async function deleteTempRecipe() {
    if (!existingTempRecipe) {
        alert("No temporary recipe to delete.");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8081/api/recipes/temp/${existingTempRecipe.id}`, {
            method: "DELETE"
        });

        if (!response.ok) throw new Error("Failed to delete recipe.");

        console.log("✅ Temporary recipe deleted.");
        existingTempRecipe = null;

        // ✅ Remove from UI & enable new recipe creation
        document.getElementById("temp-recipe-warning")?.remove();
        document.getElementById("deleteTempRecipeBtn")?.remove();

        // ✅ Refresh dropdown after deleting temp recipe
        await fetchPublicRecipes();
    } catch (error) {
        console.error("❌ Error deleting recipe:", error);
        alert(error.message);
    }
}


export async function addIngredientToTemp(recipeId, ingredient) {
    try {
        const response = await fetch(`http://localhost:8081/api/recipes/temp/${recipeId}/ingredients`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(ingredient)
        });

        if (!response.ok) throw new Error("Failed to add ingredient.");
        return await response.json();
    } catch (error) {
        console.error("❌ Error adding ingredient:", error);
        return null;
    }
}
export async function updateIngredientInTemp(recipeId, ingredientId, updatedData) {
    try {
        const response = await fetch(`http://localhost:8081/api/recipes/temp/${recipeId}/ingredients/${ingredientId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(updatedData)
        });

        if (!response.ok) throw new Error("Failed to update ingredient.");
        return await response.json();
    } catch (error) {
        console.error("❌ Error updating ingredient:", error);
        alert(error.message);
    }
}

export async function deleteIngredientFromTemp(recipeId, ingredientId) {
    try {
        const response = await fetch(`http://localhost:8081/api/recipes/temp/${recipeId}/ingredients/${ingredientId}`, {
            method: "DELETE"
        });

        if (!response.ok) throw new Error("Failed to delete ingredient.");
    } catch (error) {
        console.error("❌ Error deleting ingredient:", error);
        alert(error.message);
    }
}

