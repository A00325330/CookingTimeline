import { saveRecipe } from "./api.js";
import { loadDashboard } from "./dashboard.js"; // ✅ Ensure dashboard reloads after saving

let ingredients = [];

export function showAddRecipeModal() {
    const mainContent = document.getElementById("main-content");
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Add New Recipe</h2>
            <input type="text" id="recipe-name" class="form-control mb-2" placeholder="Recipe Name" required />
            <textarea id="recipe-description" class="form-control mb-2" placeholder="Description" required></textarea>

            <!-- Ingredients Section -->
            <h5>Ingredients</h5>
            <div id="ingredient-list" class="mb-3"></div>

            <div class="input-group mb-2">
                <input type="text" id="ingredient-name" class="form-control" placeholder="Ingredient Name" />
                <input type="number" id="ingredient-time" class="form-control" placeholder="Cooking Time (mins)" />
                <input type="text" id="ingredient-method" class="form-control" placeholder="Method (e.g. Boil, Fry)" />
                <button id="add-ingredient-btn" class="btn btn-outline-primary">➕</button>
            </div>

            <button id="submit-recipe-btn" class="btn btn-success">✅ Save Recipe</button>
        </div>
    `;

    document.getElementById("add-ingredient-btn").addEventListener("click", addIngredient);
    document.getElementById("submit-recipe-btn").addEventListener("click", submitRecipe);
}

function addIngredient() {
    const name = document.getElementById("ingredient-name").value.trim();
    const time = document.getElementById("ingredient-time").value.trim();
    const method = document.getElementById("ingredient-method").value.trim();

    if (!name || !time || !method) {
        alert("⚠️ Please fill all ingredient fields.");
        return;
    }

    const ingredientItem = document.createElement("div");
    ingredientItem.classList.add("alert", "alert-secondary", "d-flex", "justify-content-between", "align-items-center");
    ingredientItem.innerHTML = `
        <span>${name} - ${time} mins (${method})</span>
        <button class="btn btn-sm btn-danger remove-ingredient">❌</button>
    `;

    ingredients.push({ name, cookingTime: parseInt(time), cookingMethod: method });
    document.getElementById("ingredient-list").appendChild(ingredientItem);

    ingredientItem.querySelector(".remove-ingredient").addEventListener("click", () => {
        ingredients.splice(ingredients.findIndex(i => i.name === name), 1);
        ingredientItem.remove();
    });

    document.getElementById("ingredient-name").value = "";
    document.getElementById("ingredient-time").value = "";
    document.getElementById("ingredient-method").value = "";
}

async function submitRecipe() {
    const name = document.getElementById("recipe-name").value.trim();
    const description = document.getElementById("recipe-description").value.trim();

    if (!name || !description) {
        alert("⚠️ Please enter a recipe name and description.");
        return;
    }

    await saveRecipe({ name, description, ingredients });

    alert("✅ Recipe added successfully!");

    // ✅ Redirect user back to the dashboard
    loadDashboard();
}
