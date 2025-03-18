import { renderRecipeChart } from "./recipeChart.js";

/**
 * Populates the recipe dropdown with private and public recipes.
 */
export function loadRecipeDropdown(privateRecipes, publicRecipes) {
    console.log("🔄 loadRecipeDropdown called!");
    console.log("📥 Received Private Recipes:", privateRecipes);
    console.log("📥 Received Public Recipes:", publicRecipes);

    const dropdown = document.getElementById("recipe-dropdown");
    if (!dropdown) {
        console.error("❌ ERROR: Recipe dropdown not found.");
        return;
    }

    dropdown.innerHTML = `<option value="">-- Select a Recipe --</option>`; // Reset dropdown

    if (privateRecipes.length > 0) {
        console.log("✅ Adding Private Recipes...");
        const privateGroup = document.createElement("optgroup");
        privateGroup.label = "Your Recipes";

        privateRecipes.forEach(recipe => {
            console.log(`🔹 Adding: ${recipe.name}`);
            const option = document.createElement("option");
            option.value = `private-${recipe.id || recipe.name}`; // Allow name fallback
            option.textContent = recipe.name;
            privateGroup.appendChild(option);
        });

        dropdown.appendChild(privateGroup);
    } else {
        console.warn("⚠️ No Private Recipes Found.");
    }

    if (publicRecipes.length > 0) {
        console.log("✅ Adding Public Recipes...");
        const publicGroup = document.createElement("optgroup");
        publicGroup.label = "Public Recipes";

        publicRecipes.forEach(recipe => {
            console.log(`🔹 Adding: ${recipe.name}`);
            const option = document.createElement("option");
            option.value = `public-${recipe.id || recipe.name}`; // Allow name fallback
            option.textContent = recipe.name;
            publicGroup.appendChild(option);
        });

        dropdown.appendChild(publicGroup);
    } else {
        console.warn("⚠️ No Public Recipes Found.");
    }

    // ✅ Ensure `displaySelectedRecipe` runs when selecting a recipe
    dropdown.addEventListener("change", () => displaySelectedRecipe(privateRecipes, publicRecipes));
}

/**
 * Displays the selected recipe details and renders its chart.
 */
export function displaySelectedRecipe(privateRecipes, publicRecipes) {
    const selectedId = document.getElementById("recipe-dropdown").value;
    const display = document.getElementById("selected-recipe");

    console.log("🔍 Selected Recipe ID:", selectedId);

    if (!selectedId) {
        display.innerHTML = "";
        return;
    }

    const [type, idStr] = selectedId.split("-");
    const id = parseInt(idStr, 10) || idStr; // Handle both numeric and string IDs

    console.log("📋 Searching in Private Recipes:", privateRecipes);
    console.log("📋 Searching in Public Recipes:", publicRecipes);

    const recipe = type === "private"
        ? privateRecipes.find(r => r.id == id || r.name === idStr)
        : publicRecipes.find(r => r.id == id || r.name === idStr);

    if (!recipe) {
        display.innerHTML = `<p class="text-danger">⚠️ Recipe not found.</p>`;
        console.error("❌ No matching recipe found for:", selectedId);
        return;
    }

    console.log("✅ Selected Recipe:", recipe);

    display.innerHTML = `
        <div class="card p-3 mt-2">
            <h4>${recipe.name}</h4>
            <p>${recipe.description || "No description provided."}</p>
            <div id="recipe-chart-container" class="mt-4"></div>
        </div>
    `;

    // ✅ **Fix: Ensure graph updates**
    renderRecipeChart(recipe);
}
