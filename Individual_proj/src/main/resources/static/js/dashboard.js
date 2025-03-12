// dashboard.js
import { fetchRecipes, fetchPublicRecipes } from "./api.js";
import { showAddRecipeModal } from "./recipeForm.js";
import { renderRecipeChart } from "./recipeChart.js";

export async function loadDashboard() {
    const mainContent = document.getElementById("main-content");

    // 🔰 HTML structure
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Welcome to Your Dashboard</h2>
            <p>Manage your recipes below:</p>

            <div class="d-flex gap-3">
                <!-- Add Private Recipe Button -->
                <button id="add-recipe-btn" class="btn btn-success">
                    ➕ Add Recipe
                </button>
                <!-- Logout Button -->
                <button id="logout-btn" class="btn btn-danger">
                    🚪 Logout
                </button>
            </div>

            <!-- Single Combined Dropdown -->
            <div class="mt-3">
                <h5>All Recipes:</h5>
                <select id="all-recipes-dropdown" class="form-select">
                    <option value="">-- Select a Recipe --</option>
                </select>
                <div id="selected-recipe" class="mt-3"></div>
                <div id="recipe-chart-container" class="mt-4"></div>
            </div>

            <!-- 🔧 (Optional) Edit Ingredient Panel -->
            <div id="edit-panel" class="edit-panel">
                <h4>Edit Ingredient</h4>
                <label>Name: 
                    <input id="edit-name" type="text" class="form-control" />
                </label>
                <label>Cooking Time (mins):
                    <input id="edit-time" type="number" class="form-control" />
                </label>
                <label>Method:
                    <input id="edit-method" type="text" class="form-control" />
                </label>
                <div class="d-flex gap-2 mt-2">
                    <button id="save-edit" class="btn btn-primary">Save</button>
                    <button id="cancel-edit" class="btn btn-secondary">Cancel</button>
                </div>
            </div>
        </div>
    `;

    // ✅ Attach button listeners
    document.getElementById("add-recipe-btn").addEventListener("click", showAddRecipeModal);
    document.getElementById("logout-btn").addEventListener("click", handleLogout);

    // ✅ Fetch both private & public recipes
    const [privateRecipes, publicRecipes] = await Promise.all([
        fetchRecipes(),
        fetchPublicRecipes()
    ]);

    // ✅ In a single dropdown, we use <optgroup> to separate them
    const dropdown = document.getElementById("all-recipes-dropdown");

    // 1) Your Private Recipes
    if (privateRecipes && privateRecipes.length > 0) {
        const privateGroup = document.createElement("optgroup");
        privateGroup.label = "Your Recipes";
        privateRecipes.forEach(recipe => {
            const option = document.createElement("option");
            option.value = `private-${recipe.id}`; // e.g. \"private-2\"
            option.textContent = recipe.name;
            privateGroup.appendChild(option);
        });
        dropdown.appendChild(privateGroup);
    }

    // 2) Public Recipes
    if (publicRecipes && publicRecipes.length > 0) {
        const publicGroup = document.createElement("optgroup");
        publicGroup.label = "Public Recipes";
        publicRecipes.forEach(recipe => {
            const option = document.createElement("option");
            option.value = `public-${recipe.id}`; // e.g. \"public-5\"
            option.textContent = recipe.name;
            publicGroup.appendChild(option);
        });
        dropdown.appendChild(publicGroup);
    }

    // ✅ Single event listener for entire dropdown
    dropdown.addEventListener("change", () => {
        const selected = dropdown.value; // e.g. \"private-2\" or \"public-5\"
        if (!selected) {
            document.getElementById("selected-recipe").innerHTML = "";
            // Clear chart if you'd like
            document.getElementById("recipe-chart-container").innerHTML = "";
            return;
        }

        const [type, idStr] = selected.split("-");
        const id = parseInt(idStr, 10);

        // Find that recipe from whichever array
        let chosenRecipe;
        if (type === "private") {
            chosenRecipe = privateRecipes.find(r => r.id === id);
        } else {
            chosenRecipe = publicRecipes.find(r => r.id === id);
        }

        // ✅ Display the chosen recipe data (optional)
        displayChosenRecipe(chosenRecipe);

        // ✅ Render chart
        if (chosenRecipe) {
            renderRecipeChart(chosenRecipe, () => loadDashboard());
        }
    });
}

// 🔒 Logout
function handleLogout() {
    localStorage.removeItem("token");
    navigateTo("login");
}

// (Optional) Display the chosen recipe details in #selected-recipe
function displayChosenRecipe(recipe) {
  const container = document.getElementById("selected-recipe");
  if (!recipe) {
    container.innerHTML = "";
    return;
  }

  // 🔑 Sort ingredients descending by cookingTime
  const sortedIngredients = [...recipe.ingredients].sort(
    (a, b) => b.cookingTime - a.cookingTime
  );

  container.innerHTML = `
    <div class="card p-3">
      <h4>${recipe.name}</h4>
      <p>${recipe.description || ""}</p>
      <h5>Ingredients (Longest to Shortest):</h5>
      <ul>
        ${sortedIngredients
          .map(
            (ing) => `
            <li>
              ${ing.name} - ${ing.cookingTime} mins (${ing.cookingMethod})
            </li>
          `
          )
          .join("")}
      </ul>
    </div>
  `;
}

