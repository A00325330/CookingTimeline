import { fetchRecipes, fetchPublicRecipes, fetchTags, fetchRecipesByTag } from "./api.js";
import { showAddRecipeModal } from "./recipeForm.js";
import { renderRecipeChart } from "./recipeChart.js";
import { loadRecipeDropdown } from "./recipeDropdown.js";

export async function loadDashboard() {
    const mainContent = document.getElementById("main-content");

    // 🔰 Main Dashboard Layout
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Welcome to Your Dashboard</h2>
            <p>Manage your recipes below:</p>

            <div class="d-flex gap-3">
                <button id="add-recipe-btn" class="btn btn-success">➕ Add Recipe</button>
                <button id="logout-btn" class="btn btn-danger">🚪 Logout</button>
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
        </div>

        <!-- Tag Pop-Up Panel -->
        <div id="tag-popup" class="popup hidden">
            <div class="popup-content">
                <span id="close-popup" class="close">&times;</span>
                <h3 id="popup-title">Recipes with this Tag</h3>
                <select id="popup-recipe-dropdown" class="form-select mt-2"></select>
            </div>
        </div>

        <style>
            .popup {
                display: none;
                position: fixed;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                width: 50%;
                background: white;
                padding: 20px;
                box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);
                border-radius: 8px;
                z-index: 1000;
            }
            .popup-content {
                display: flex;
                flex-direction: column;
                align-items: center;
            }
            .close {
                position: absolute;
                top: 10px;
                right: 20px;
                font-size: 24px;
                cursor: pointer;
            }
            .hidden { display: none; }
            .tag-card {
                background: #f8f9fa;
                padding: 10px 15px;
                border-radius: 8px;
                cursor: pointer;
                flex-shrink: 0;
                text-align: center;
                min-width: 150px;
                max-width: 150px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
        </style>
    `;

    // ✅ Attach Event Listeners
    document.getElementById("add-recipe-btn").addEventListener("click", showAddRecipeModal);
    document.getElementById("logout-btn").addEventListener("click", handleLogout);
    document.getElementById("close-popup").addEventListener("click", () => {
        document.getElementById("tag-popup").classList.add("hidden");
    });

    // ✅ Fetch private & public recipes
    let [privateRecipes, publicRecipes] = await Promise.all([
        fetchRecipes(),
        fetchPublicRecipes()
    ]);

    // ✅ Extract HATEOAS `_embedded.recipeList`
    privateRecipes = privateRecipes._embedded?.recipeList || [];
    publicRecipes = publicRecipes._embedded?.recipeList || [];

    console.log("✅ Private Recipes:", privateRecipes);
    console.log("✅ Public Recipes:", publicRecipes);

    // ✅ Load Recipes into Dropdown
    loadRecipeDropdown(privateRecipes, publicRecipes);

    // ✅ Load Tags into Scrollable Cards
    loadTagCards(privateRecipes, publicRecipes);
}

// 🔒 Logout
function handleLogout() {
    localStorage.removeItem("token");
    navigateTo("login");
}

async function loadTagCards(privateRecipes, publicRecipes) {
    const tagContainer = document.getElementById("tag-card-container");
    tagContainer.innerHTML = "";

    const allTags = new Set();
    [...privateRecipes, ...publicRecipes].forEach(recipe => {
        if (recipe.tags) {
            recipe.tags.forEach(tag => {
                // ✅ Fix: Ensure we're storing only tag names, not full objects
                if (typeof tag === "object" && tag.name) {
                    allTags.add(tag.name);
                } else if (typeof tag === "string") {
                    allTags.add(tag);
                }
            });
        }
    });

    const tagsArray = Array.from(allTags);
    console.log("📌 Processed Tags:", tagsArray); // ✅ Debugging

    tagsArray.forEach(tagName => {
        const tagCard = document.createElement("div");
        tagCard.className = "tag-card";
        tagCard.textContent = tagName; // ✅ Fix: Ensure only the name is displayed
        tagCard.addEventListener("click", () => {
            console.log(`🖱️ Clicked on Tag: ${tagName}`);
            openTagPopup(tagName);
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

async function openTagPopup(tag) {
    if (typeof tag !== "string") {
        console.error("❌ Invalid tag passed:", tag);
        return;
    }

    console.log(`📥 Fetching recipes for tag: "${tag}"`);

    const tagSection = document.getElementById("tag-recipe-section");
    const tagTitle = document.getElementById("tag-title");
    const recipeList = document.getElementById("tag-recipe-list");

    // ✅ Show Section & Update Title
    tagSection.style.display = "block";
    tagTitle.textContent = `Recipes for: ${tag}`;
    recipeList.innerHTML = ""; // Clear previous results

    try {
        // ✅ Fetch Recipes for the Tag
        const recipes = await fetchRecipesByTag(tag);

        if (!recipes || recipes.length === 0) {
            console.warn(`⚠️ No recipes found for tag: ${tag}`);
            recipeList.innerHTML = `<p class="text-danger">❌ No recipes found.</p>`;
            return;
        }

        // ✅ Display Recipes in a Simple List
        recipes.forEach(recipe => {
            const item = document.createElement("div");
            item.className = "recipe-tag-item";
            item.innerHTML = `<strong>${recipe.name}</strong><br><small>${recipe.description || "No description"}</small>`;
            recipeList.appendChild(item);
        });

    } catch (error) {
        console.error("❌ Error fetching recipes by tag:", err)}
		document.getElementById("close-tag-section").addEventListener("click", () => {
		    document.getElementById("tag-recipe-section").style.display = "none";
		});
}



