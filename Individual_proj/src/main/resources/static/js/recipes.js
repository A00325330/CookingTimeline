import { graphRecipe } from "./recipeGraph.js"; // ✅ Import graphing function

document.addEventListener("DOMContentLoaded", function () {
    console.log("✅ DOM Loaded - Fetching Public Recipes...");
    fetchPublicRecipes();
});

/**
 * Fetches public recipes from the API and populates the dropdown.
 */
async function fetchPublicRecipes() {
    try {
        const response = await fetch("http://localhost:8081/api/recipes/public");
        console.log("API Response Status:", response.status); // ✅ Debugging

        if (!response.ok) {
            throw new Error(`Failed to fetch recipes! Status: ${response.status}`);
        }

        const data = await response.json();
        console.log("✅ Fetched Public Recipes:", data); // ✅ Debugging API response

        const recipes = data._embedded?.recipeList || [];
        const dropdown = document.getElementById("recipe-select");

        if (!dropdown) {
            console.error("❌ Dropdown not found in DOM!");
            return;
        }

        if (recipes.length === 0) {
            dropdown.innerHTML = `<option value="">No recipes available</option>`;
            return;
        }

        dropdown.innerHTML = `<option value="">-- Select a Recipe --</option>`; // Reset dropdown
        recipes.forEach(recipe => {
            const option = document.createElement("option");
            option.value = recipe.id;
            option.textContent = recipe.name;
            dropdown.appendChild(option);
        });

        dropdown.disabled = false; // ✅ Enable dropdown once recipes load

        // ✅ Add event listener for selection
        dropdown.addEventListener("change", function () {
            const selectedRecipeId = dropdown.value;
            if (selectedRecipeId) {
                fetchAndGraphRecipe(selectedRecipeId);
            }
        });

        console.log("✅ Dropdown successfully populated.");
    } catch (error) {
        console.error("❌ Fetch Error:", error.message);
        alert(error.message);
    }
}

/**
 * Fetches the selected recipe details and graphs it.
 */
async function fetchAndGraphRecipe(recipeId) {
    try {
        console.log(`Fetching details for Recipe ID: ${recipeId}...`);
        const response = await fetch(`http://localhost:8081/api/recipes/public/${recipeId}`);
        
        if (!response.ok) {
            throw new Error(`Failed to fetch recipe! Status: ${response.status}`);
        }

        const recipe = await response.json();
        console.log("✅ Fetched Recipe Data:", recipe);

        graphRecipe(recipe); // ✅ Call graphing function
    } catch (error) {
        console.error("❌ Error fetching recipe details:", error.message);
        alert(error.message);
    }
}
