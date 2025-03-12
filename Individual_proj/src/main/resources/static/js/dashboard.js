import { fetchRecipes } from "./api.js";
import * as RecipeForm from "./recipeForm.js";
import { loadRecipeDropdown } from "./recipeDropdown.js";
import { showAddRecipeModal } from "./recipeForm.js";

export async function loadDashboard() {
    const mainContent = document.getElementById("main-content");
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Welcome to Your Dashboard</h2>
            <p>Manage your recipes below:</p>
			<div class="d-flex gap-3">
			    <button id="add-recipe-btn" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#addRecipeModal">
			        ➕ Add Recipe
			    </button>
			    <button id="logout-btn" class="btn btn-danger">🚪 Logout</button>
			</div>

            <!-- 📌 Recipe Dropdown -->
            <div class="mt-3">
                <h5>Select a Recipe:</h5>
                <select id="recipe-dropdown" class="form-select">
                    <option value="">-- Select a Recipe --</option>
                </select>
                <div id="selected-recipe" class="mt-3"></div>
            </div>
        </div>
    `;

    document.getElementById("add-recipe-btn").addEventListener("click", RecipeForm.showAddRecipeModal);
    document.getElementById("logout-btn").addEventListener("click", handleLogout);

    // ✅ Fetch recipes and send to recipeDropdown.js
    const recipes = await fetchRecipes();
    loadRecipeDropdown(recipes);
}

// ✅ Logout Function
function handleLogout() {
    localStorage.removeItem("token");
    navigateTo("login");
}
