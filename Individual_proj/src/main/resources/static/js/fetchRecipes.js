export async function fetchRecipesForDropdown() {
    try {
        const response = await fetch("http://localhost:8081/api/recipes/public");
        if (!response.ok) {
            throw new Error("Failed to fetch recipes!");
        }

        const data = await response.json();
        const recipes = data._embedded?.recipeList || [];
        const dropdown = document.getElementById("recipe-select");

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
    } catch (error) {
        alert(error.message);
    }
}

export async function loadSelectedRecipe(graphRecipe) {
    const recipeId = document.getElementById("recipe-select").value;
    const startButton = document.getElementById("start-timer-btn");

    if (!recipeId) {
        startButton.disabled = true;
        return;
    }

    try {
        const response = await fetch(`http://localhost:8081/api/recipes/public/${recipeId}`);
        if (!response.ok) {
            throw new Error("Failed to fetch the selected recipe!");
        }

        const recipe = await response.json();
        graphRecipe(recipe);
        startButton.disabled = false; // ✅ Enable start button once a recipe is selected
    } catch (error) {
        alert(error.message);
    }
}
