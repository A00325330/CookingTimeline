import { graphRecipe } from "./recipeGraph.js"; // ✅ Import graphing function


document.addEventListener("DOMContentLoaded", function () {
    console.log("✅ DOM Loaded - Fetching Public Recipes...");
    fetchPublicRecipes();
});

/**
 * Fetches public recipes from the API and populates the dropdown.
 */
export async function fetchPublicRecipes() {
    try {
        const response = await fetch("http://localhost:8081/api/recipes/public");
        console.log("API Response Status:", response.status);

        if (!response.ok) {
            throw new Error(`Failed to fetch recipes! Status: ${response.status}`);
        }

        const data = await response.json();
        console.log("✅ Fetched Public Recipes:", data);

        const recipes = data._embedded?.recipeList || [];
        const dropdown = document.getElementById("recipe-select");

        if (!dropdown) {
            console.error("❌ Dropdown not found in DOM!");
            return;
        }

        dropdown.innerHTML = `<option value="">-- Select a Recipe --</option>`; // Reset dropdown

        recipes.forEach(recipe => {
            const option = document.createElement("option");
            option.value = recipe.id;
            option.textContent = recipe.name;
            dropdown.appendChild(option);
        });

        dropdown.disabled = false; // Enable dropdown once recipes load

        console.log("✅ Dropdown successfully populated.");
    } catch (error) {
        console.error("❌ Fetch Error:", error.message);
        alert(error.message);
    }
}


/**
 * Fetches the selected recipe details and graphs it.
 */
export async function fetchAndGraphRecipe(recipeId) {
    try {
        console.log(`🔍 Fetching details for Recipe ID: ${recipeId}...`);

        let response = await fetch(`http://localhost:8081/api/recipes/public/${recipeId}`);

        // ✅ If it's NOT found, try fetching as a TEMP recipe
        if (response.status === 404) {
            console.warn(`⚠️ Recipe ID ${recipeId} not found as public, checking temporary storage...`);
            response = await fetch(`http://localhost:8081/api/recipes/temp/${recipeId}`);
        }

        // ❌ If it's still not found, return an error
        if (!response.ok) {
            throw new Error(`Failed to fetch recipe! Status: ${response.status}`);
        }

        // ✅ Convert to JSON
        const recipe = await response.json();
        console.log("✅ Fetched Recipe Data:", recipe);

        // ✅ Graph the fetched recipe
        graphRecipe(recipe);
    } catch (error) {
        console.error("❌ Error fetching recipe details:", error.message);
        alert(`Error fetching recipe: ${error.message}`);
    }
}

