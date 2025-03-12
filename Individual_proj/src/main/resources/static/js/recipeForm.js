// recipeForm.js
import { saveRecipe } from "./api.js";
import { loadDashboard } from "./dashboard.js";
import { isAdmin } from "./adminCheck.js";  // <== We'll check roles here

let ingredients = [];

export function showAddRecipeModal() {
  const mainContent = document.getElementById("main-content");
  mainContent.innerHTML = `
    <div class="card p-4">
      <h2>Add New Recipe</h2>
      <p>(It will be ${
        isAdmin() ? "PUBLIC" : "PRIVATE"
      } by default.)</p>

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

  // Reset any leftover
  ingredients = [];

  document
    .getElementById("add-ingredient-btn")
    .addEventListener("click", addIngredient);

  document
    .getElementById("submit-recipe-btn")
    .addEventListener("click", submitRecipe);
}

function addIngredient() {
  const name = document.getElementById("ingredient-name").value.trim();
  const time = +document.getElementById("ingredient-time").value.trim();
  const method = document.getElementById("ingredient-method").value.trim();

  if (!name || !time || !method) {
    alert("⚠️ Please fill all ingredient fields.");
    return;
  }

  ingredients.push({ name, cookingTime: time, cookingMethod: method });
  renderIngredientList();
}

function renderIngredientList() {
  const container = document.getElementById("ingredient-list");
  container.innerHTML = "";

  ingredients.forEach((ing, index) => {
    const item = document.createElement("div");
    item.className = "alert alert-secondary d-flex justify-content-between align-items-center mb-1";
    item.innerHTML = `
      <span>${ing.name} - ${ing.cookingTime} mins (${ing.cookingMethod})</span>
      <button class="btn btn-sm btn-danger">❌</button>
    `;

    item.querySelector("button").addEventListener("click", () => {
      ingredients.splice(index, 1);
      renderIngredientList();
    });

    container.appendChild(item);
  });
}

async function submitRecipe() {
  const name = document.getElementById("recipe-name").value.trim();
  const description = document.getElementById("recipe-description").value.trim();

  if (!name || !description) {
    alert("⚠️ Please enter a recipe name and description.");
    return;
  }

  // 🔑 Key difference: set visibility based on isAdmin()
  const visibility = isAdmin() ? "PUBLIC" : "PRIVATE";

  const recipeData = {
    name,
    description,
    ingredients,
    visibility
  };

  // Save
  await saveRecipe(recipeData);
  alert(`✅ Recipe added successfully as ${visibility}!`);

  // Return to dashboard
  loadDashboard();
}
