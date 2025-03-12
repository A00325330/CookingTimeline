import { fetchRecipes, saveRecipe } from "./api.js";

export function loadDashboard() {
    const mainContent = document.getElementById("main-content");
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Welcome to Your Dashboard</h2>
            <p>Manage your recipes below:</p>
            <div class="d-flex gap-3">
                <button id="add-recipe-btn" class="btn btn-success">➕ Add Recipe</button>
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

        <!-- 📌 Add Recipe Modal -->
        <div class="modal fade" id="addRecipeModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Add New Recipe</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
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

                        <button id="submit-recipe-btn" class="btn btn-primary">Save Recipe</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    // ✅ Attach event listeners
    document.getElementById("add-recipe-btn").addEventListener("click", showAddRecipeModal);
    document.getElementById("logout-btn").addEventListener("click", handleLogout);
    document.getElementById("add-ingredient-btn").addEventListener("click", addIngredient);
    document.getElementById("submit-recipe-btn").addEventListener("click", submitRecipe);

    // ✅ Fetch recipes and populate dropdown
    loadRecipeDropdown();
}

// ✅ Load recipes into the dropdown
async function loadRecipeDropdown() {
    const dropdown = document.getElementById("recipe-dropdown");
    const recipes = await fetchRecipes();

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


// ✅ Display selected recipe details
function displaySelectedRecipe(recipes) {
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
            <h5>Ingredients:</h5>
            <ul>
                ${recipe.ingredients.map(ing => `<li>${ing.name} - ${ing.cookingTime} mins (${ing.cookingMethod})</li>`).join("")}
            </ul>
        </div>
    `;
}

// ✅ Show Add Recipe Modal
function showAddRecipeModal() {
    const addRecipeModal = new bootstrap.Modal(document.getElementById("addRecipeModal"));
    addRecipeModal.show();
}

// ✅ Logout Function
function handleLogout() {
    localStorage.removeItem("token");
    navigateTo("login");
}

// ✅ Ingredient Handling
const ingredients = [];

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

    // ✅ Remove ingredient when clicking delete button
    ingredientItem.querySelector(".remove-ingredient").addEventListener("click", () => {
        ingredients.splice(ingredients.findIndex(i => i.name === name), 1);
        ingredientItem.remove();
    });

    // ✅ Clear input fields after adding ingredient
    document.getElementById("ingredient-name").value = "";
    document.getElementById("ingredient-time").value = "";
    document.getElementById("ingredient-method").value = "";
}

// ✅ Handle Recipe Submission
async function submitRecipe() {
    const name = document.getElementById("recipe-name").value.trim();
    const description = document.getElementById("recipe-description").value.trim();

    if (!name || !description) {
        alert("⚠️ Please enter a recipe name and description.");
        return;
    }

    await saveRecipe({ name, description, ingredients });

    // ✅ Clear modal after submission
    document.getElementById("recipe-name").value = "";
    document.getElementById("recipe-description").value = "";
    document.getElementById("ingredient-list").innerHTML = "";

    // ✅ Close the modal
    const addRecipeModal = bootstrap.Modal.getInstance(document.getElementById("addRecipeModal"));
    addRecipeModal.hide();

    // ✅ Reload recipe dropdown
    loadRecipeDropdown();

    alert("✅ Recipe added successfully!");
}
