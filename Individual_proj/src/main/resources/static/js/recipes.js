async function loadRecipesPage() {
    const mainContent = document.getElementById("main-content");
    mainContent.innerHTML = `<h2>Your Recipes</h2><ul id="recipe-list" class="list-group"></ul>`;
    
    const recipes = await fetchRecipes();
    
    const recipeList = document.getElementById("recipe-list");
	if (recipes._embedded && recipes._embedded.recipeDtoList) {
	    recipes._embedded.recipeDtoList.forEach(recipe => {
            const li = document.createElement("li");
            li.className = "list-group-item";
            li.textContent = recipe.name;
            recipeList.appendChild(li);
        });
    } else {
        recipeList.innerHTML = "<li class='list-group-item'>No recipes found.</li>";
    }
}

async function loadAddRecipePage() {
    const mainContent = document.getElementById("main-content");
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Add New Recipe</h2>
            <input type="text" id="recipe-name" class="form-control mb-2" placeholder="Recipe Name" />
            <textarea id="recipe-description" class="form-control mb-2" placeholder="Description"></textarea>
            <button id="submit-recipe" class="btn btn-primary">Save Recipe</button>
        </div>
    `;

    document.getElementById("submit-recipe").addEventListener("click", saveRecipe);
}

async function saveRecipe() {
    const name = document.getElementById("recipe-name").value;
    const description = document.getElementById("recipe-description").value;
    
    const token = localStorage.getItem("token");
    const response = await fetch(`${API_BASE_URL}/recipes`, {
        method: "POST",
        headers: { "Authorization": `Bearer ${token}`, "Content-Type": "application/json" },
        body: JSON.stringify({ name, description, visibility: "PRIVATE" })
    });
    
    if (response.ok) {
        alert("Recipe saved!");
        loadRecipesPage();
    } else {
        alert("Failed to save recipe.");
    }
}
