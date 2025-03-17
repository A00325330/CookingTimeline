import { renderRecipeChart } from "./recipeChart.js";

/**
 * Populates the recipe dropdown with private and public recipes.
 */
export function loadRecipeDropdown(privateRecipes, publicRecipes) {
    const dropdown = document.getElementById("recipe-dropdown");

    if (!dropdown) {
        console.error("❌ ERROR: Recipe dropdown not found.");
        return;
    }

    console.log("📥 Private Recipes:", privateRecipes);
    console.log("📥 Public Recipes:", publicRecipes);

    dropdown.innerHTML = `<option value="">-- Select a Recipe --</option>`; // Reset dropdown

    // 1️⃣ Private Recipes
    if (privateRecipes && privateRecipes.length > 0) {
        console.log("✅ Adding Private Recipes to Dropdown...");
        const privateGroup = document.createElement("optgroup");
        privateGroup.label = "Your Recipes";
        privateRecipes.forEach(recipe => {
            console.log(`🔹 Adding Private Recipe: ${recipe.name}`);
            const option = document.createElement("option");
            option.value = `private-${recipe.id}`;
            option.textContent = recipe.name;
            privateGroup.appendChild(option);
        });
        dropdown.appendChild(privateGroup);
    } else {
        console.warn("⚠️ No Private Recipes Found.");
    }

    // 2️⃣ Public Recipes
    if (publicRecipes && publicRecipes.length > 0) {
        console.log("✅ Adding Public Recipes to Dropdown...");
        const publicGroup = document.createElement("optgroup");
        publicGroup.label = "Public Recipes";
        publicRecipes.forEach(recipe => {
            console.log(`🔹 Adding Public Recipe: ${recipe.name}`);
            const option = document.createElement("option");
            option.value = `public-${recipe.id}`;
            option.textContent = recipe.name;
            publicGroup.appendChild(option);
        });
        dropdown.appendChild(publicGroup);
    } else {
        console.warn("⚠️ No Public Recipes Found.");
    }

    // ✅ Handle selection event
    dropdown.addEventListener("change", () => displaySelectedRecipe(privateRecipes, publicRecipes));
}

/**
 * Displays the selected recipe details and renders its chart.
 */
export function displaySelectedRecipe(privateRecipes, publicRecipes) {
    const selectedId = document.getElementById("recipe-dropdown").value;
    const display = document.getElementById("selected-recipe");

    if (!selectedId) {
        display.innerHTML = "";
        return;
    }

    const [type, idStr] = selectedId.split("-");
    const id = parseInt(idStr, 10);

    const recipe = type === "private"
        ? privateRecipes.find(r => r.id === id)
        : publicRecipes.find(r => r.id === id);

    if (!recipe) {
        display.innerHTML = `<p class="text-danger">⚠️ Recipe not found.</p>`;
        return;
    }

    display.innerHTML = `
        <div class="card p-3 mt-2">
            <h4>${recipe.name}</h4>
            <p>${recipe.description || "No description provided."}</p>
            <div id="recipe-chart-container" class="mt-4"></div>
        </div>
    `;

    renderRecipeChart(recipe);
}
