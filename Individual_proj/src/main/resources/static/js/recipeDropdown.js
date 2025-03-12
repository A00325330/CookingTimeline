import { renderRecipeChart } from "./recipeChart.js";

export function loadRecipeDropdown(recipes) {
    const dropdown = document.getElementById("recipe-dropdown");
    
    if (!recipes || !Array.isArray(recipes)) {
        console.error("❌ ERROR: Expected an array but got:", recipes);
        dropdown.innerHTML = `<option value="">❌ Failed to load recipes</option>`;
        return;
    }

    dropdown.innerHTML = `<option value="">-- Select a Recipe --</option>`; // Reset dropdown

    recipes.forEach(recipe => {
        const option = document.createElement("option");
        option.value = recipe.id;
        option.textContent = recipe.name;
        dropdown.appendChild(option);
    });

    dropdown.addEventListener("change", () => displaySelectedRecipe(recipes));
}

export function displaySelectedRecipe(recipes) {
    const selectedId = document.getElementById("recipe-dropdown").value;
    const recipe = recipes.find(r => r.id == selectedId);
    const display = document.getElementById("selected-recipe");

    if (!recipe) {
        display.innerHTML = "";
        return;
    }

    display.innerHTML = `
        <div class="card p-3 mt-2">
            <h4>${recipe.name}</h4>
            <p>${recipe.description}</p>
            <div id="recipe-chart-container" class="mt-4">
                <canvas id="recipe-chart"></canvas>
            </div>
        </div>
    `;

    renderRecipeChart(recipe);  // ✅ Call function to draw Gantt-style bar chart
}
