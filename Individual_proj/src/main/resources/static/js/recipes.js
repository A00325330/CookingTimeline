import { fetchRecipesForDropdown, loadSelectedRecipe } from "./fetchRecipes.js";  // ✅ From fetchRecipes.js
import { graphRecipe } from "./recipeGraph.js";  // ✅ From recipeGraph.js
import { startCookingTimer } from "./cookingTimer.js"; // ✅ From cookingTimer.js

document.addEventListener("DOMContentLoaded", function () {
    console.log("Fetching recipes..."); // Debugging step
    fetchRecipesForDropdown();

    document.getElementById("recipe-select").addEventListener("change", () => loadSelectedRecipe(graphRecipe));
    document.getElementById("start-timer-btn").addEventListener("click", startCookingTimer);
});
