document.addEventListener("DOMContentLoaded", function () {
    checkAdminAccess();
    loadAdminRecipes();

    document.getElementById("recipeForm").addEventListener("submit", handleRecipeForm);
    document.getElementById("logout-btn").addEventListener("click", logoutUser);
});

const API_URL = "http://localhost:8081/api/recipes/public";

function checkAdminAccess() {
    const token = localStorage.getItem("jwt");
    if (!token) {
        alert("Unauthorized access. Redirecting to login...");
        window.location.href = "index.html";
        return;
    }

    // Decode JWT to check if user is an admin
    const tokenPayload = JSON.parse(atob(token.split(".")[1]));
    if (tokenPayload.role !== "ROLE_ADMIN") {
        alert("Access denied! Only admins can view this page.");
        window.location.href = "index.html";
    }
}

async function loadAdminRecipes() {
    const token = localStorage.getItem("jwt");
    const response = await fetch(API_URL, {
        method: "GET",
        headers: { "Authorization": `Bearer ${token}` }
    });

    const data = await response.json();
    const recipes = data._embedded?.recipeList || [];
    displayAdminRecipes(recipes);
}

function displayAdminRecipes(recipes) {
    const container = document.getElementById("adminRecipeList");
    container.innerHTML = "";

    recipes.forEach(recipe => {
        const recipeCard = document.createElement("div");
        recipeCard.classList.add("recipe-card");

        recipeCard.innerHTML = `
            <div class="card">
                <div class="card-body">
                    <h5 class="card-title">${recipe.name}</h5>
                    <p class="card-text">${recipe.description}</p>
                    <p><strong>Cooking Method:</strong> ${recipe.cookingMethod}</p>
                    <p><strong>Cooking Time:</strong> ${recipe.cookingTime} mins</p>
                    <button class="btn btn-warning btn-sm" onclick="editRecipe(${recipe.id}, '${recipe.name}', '${recipe.description}', '${recipe.cookingMethod}', ${recipe.cookingTime})">Edit</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteRecipe(${recipe.id})">Delete</button>
                </div>
            </div>
        `;
        container.appendChild(recipeCard);
    });
}

function editRecipe(id, name, description, method, time) {
    document.getElementById("recipeId").value = id;
    document.getElementById("recipeName").value = name;
    document.getElementById("recipeDesc").value = description;
    document.getElementById("cookingMethod").value = method;
    document.getElementById("cookingTime").value = time;
}

async function handleRecipeForm(event) {
    event.preventDefault();
    const token = localStorage.getItem("jwt");

    const id = document.getElementById("recipeId").value;
    const name = document.getElementById("recipeName").value;
    const description = document.getElementById("recipeDesc").value;
    const method = document.getElementById("cookingMethod").value;
    const time = document.getElementById("cookingTime").value;

    const recipeData = { name, description, cookingMethod: method, cookingTime: time };

    if (id) {
        // Update recipe
        await fetch(`${API_URL}/${id}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(recipeData)
        });
    } else {
        // Create recipe
        await fetch(API_URL, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(recipeData)
        });
    }

    alert("Recipe saved successfully!");
    document.getElementById("recipeForm").reset();
    loadAdminRecipes();
}

async function deleteRecipe(id) {
    const token = localStorage.getItem("jwt");

    if (!confirm("Are you sure you want to delete this recipe?")) return;

    await fetch(`${API_URL}/${id}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${token}` }
    });

    alert("Recipe deleted successfully!");
    loadAdminRecipes();
}

function logoutUser() {
    localStorage.removeItem("jwt");
    window.location.href = "index.html";
}
