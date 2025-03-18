import { fetchRecipes, fetchPublicRecipes, fetchRecipesByTag } from "./api.js";
import { showAddRecipeModal } from "./recipeForm.js";
import { renderRecipeChart } from "./recipeChart.js";
import { loadRecipeDropdown } from "./recipeDropdown.js";
import { navigateTo } from "./spa.js";

export async function loadDashboard() {
    const mainContent = document.getElementById("main-content");

    // 🔰 Main Dashboard Layout
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Welcome to Your Dashboard</h2>
            <p>Manage your recipes below:</p>

            <div class="d-flex gap-3">
                <button id="add-recipe-btn" class="btn btn-success">➕ Add Recipe</button>
            </div>

            <!-- Tag Scroll Section -->
            <h5 class="mt-4">Browse by Tag:</h5>
            <div class="d-flex align-items-center">
                <button id="scroll-left" class="btn btn-light">◀</button>
                <div id="tag-card-container" class="d-flex overflow-hidden" style="width: 80%; gap: 10px;"></div>
                <button id="scroll-right" class="btn btn-light">▶</button>
            </div>

            <!-- Recipe Selection -->
            <div class="mt-3">
                <h5>All Recipes:</h5>
                <select id="recipe-dropdown" class="form-select">
                    <option value="">-- Select a Recipe --</option>
                </select>
                <div id="selected-recipe" class="mt-3"></div>
                <div id="recipe-chart-container" class="mt-4"></div>
            </div>

            <!-- Tag-Based Recipe Section -->
            <div id="tag-recipe-section" class="hidden mt-4">
                <h3 id="tag-title"></h3>
                <div id="tag-recipe-list" class="mt-2"></div>
                <button id="close-tag-section" class="btn btn-secondary mt-2">Close</button>
            </div>
        </div>
    `;

    // ✅ Attach Event Listeners
    document.getElementById("add-recipe-btn").addEventListener("click", showAddRecipeModal);
    document.getElementById("logout-btn").addEventListener("click", handleLogout);
    document.getElementById("close-tag-section").addEventListener("click", () => {
        document.getElementById("tag-recipe-section").style.display = "none";
    });

	let [privateRecipes, publicRecipes] = await Promise.all([
	    fetchRecipes(),
	    fetchPublicRecipes()
	]);

	// ✅ Debugging API Responses
	console.log("📡 Raw API Response (Private Recipes):", privateRecipes);
	console.log("📡 Raw API Response (Public Recipes):", publicRecipes);

	// 🛠 **Extracting Correctly**
	privateRecipes = privateRecipes._embedded?.recipeDtoList || privateRecipes || [];
	publicRecipes = publicRecipes._embedded?.recipeDtoList || publicRecipes || [];


	console.log("✅ Final Private Recipes:", privateRecipes);
	console.log("✅ Final Public Recipes:", publicRecipes);

	// ✅ Load Recipes into Dropdown
	loadRecipeDropdown(privateRecipes, publicRecipes);




    // ✅ Load Tags into Scrollable Cards
    loadTagCards(privateRecipes, publicRecipes);
}

// 🔒 Logout Function
function handleLogout() {
    console.log("🚪 Logging out...");
    localStorage.removeItem("token"); // Remove authentication token
    navigateTo("login"); // Redirect to login page
}

// ✅ Load Tags into Scrollable Cards
async function loadTagCards(privateRecipes, publicRecipes) {
    const tagContainer = document.getElementById("tag-card-container");
    tagContainer.innerHTML = "";

    const allTags = new Set();
    [...privateRecipes, ...publicRecipes].forEach(recipe => {
        if (recipe.tags) {
            recipe.tags.forEach(tag => {
                if (typeof tag === "object" && tag.name) {
                    allTags.add(tag.name);
                } else if (typeof tag === "string") {
                    allTags.add(tag);
                }
            });
        }
    });

    const tagsArray = Array.from(allTags);
    console.log("📌 Processed Tags:", tagsArray);

    tagsArray.forEach(tagName => {
        const tagCard = document.createElement("div");
        tagCard.className = "tag-card";
        tagCard.textContent = tagName;
        tagCard.addEventListener("click", () => {
            console.log(`🖱️ Clicked on Tag: ${tagName}`);
            openTagSection(tagName);
        });
        tagContainer.appendChild(tagCard);
    });

    // Scroll Functionality
    document.getElementById("scroll-left").addEventListener("click", () => {
        tagContainer.scrollLeft -= 100;
    });
    document.getElementById("scroll-right").addEventListener("click", () => {
        tagContainer.scrollLeft += 100;
    });
}

async function openTagSection(tag) {
    console.log(`📥 Fetching recipes for tag: "${tag}"`);

    const tagSection = document.getElementById("tag-recipe-section");
    const tagTitle = document.getElementById("tag-title");
    const recipeList = document.getElementById("tag-recipe-list");

    tagSection.style.display = "block";
    tagTitle.textContent = `Recipes for: ${tag}`;
    recipeList.innerHTML = "";

    try {
        const recipes = await fetchRecipesByTag(tag);
        
        // ✅ Debugging log
        console.log(`✅ Found ${recipes?.length || 0} recipes for tag "${tag}"`, recipes);

        if (!Array.isArray(recipes) || recipes.length === 0) {
            console.warn(`⚠️ No recipes found for tag: ${tag}`);
            recipeList.innerHTML = `<p class="text-danger">❌ No recipes found.</p>`;
            return;
        }

        recipes.forEach(recipe => {
            const item = document.createElement("div");
            item.className = "recipe-tag-item card p-2 mb-2";
            item.innerHTML = `
                <strong>${recipe.name}</strong><br>
                <small>${recipe.description || "No description"}</small>
                <hr>
                <p><strong>Ingredients:</strong></p>
                <ul>
                    ${recipe.ingredients?.map(ing => `<li>${ing.name} - ${ing.cookingTime} mins (${ing.cookingMethod})</li>`).join("") || "<li>No ingredients listed</li>"}
                </ul>
            `;
            recipeList.appendChild(item);
        });

    } catch (error) {
        console.error("❌ Error fetching recipes by tag:", error);
        recipeList.innerHTML = `<p class="text-danger">❌ Failed to load recipes.</p>`;
    }
}


