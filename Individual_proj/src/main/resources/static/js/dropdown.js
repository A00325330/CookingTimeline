import { fetchAndGraphRecipe } from "./recipes.js";

export function setupRecipeDropdown() {
    const dropdown = document.getElementById("recipe-select");

    dropdown.addEventListener("change", function () {
        const selectedRecipeId = this.value;
        if (selectedRecipeId) {
            console.log(`🔍 Fetching and graphing Recipe ID: ${selectedRecipeId}`);
            fetchAndGraphRecipe(selectedRecipeId);
        }
    });

    fetchPublicRecipes(); // Ensure dropdown is populated on load
}

export function addTempRecipeToDropdown(recipe) {
    const dropdown = document.getElementById("recipe-select");
    let optionExists = Array.from(dropdown.options).some(option => option.value === String(recipe.id));

    if (!optionExists) {
        let option = document.createElement("option");
        option.value = recipe.id;
        option.textContent = recipe.name;
        dropdown.appendChild(option);
    }

    dropdown.value = recipe.id;
}
