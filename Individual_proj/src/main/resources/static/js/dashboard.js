// dashboard.js - Handles Dashboard Page UI

import { fetchRecipes } from "./api.js";

export function loadDashboard() {
    const mainContent = document.getElementById("main-content");
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Welcome to Your Dashboard</h2>
            <p>Manage your recipes below:</p>
            <div class="d-flex gap-3">
                <button id="view-recipes-btn" class="btn btn-info">📜 View Recipes</button>
                <button id="add-recipe-btn" class="btn btn-success">➕ Add Recipe</button>
                <button id="logout-btn" class="btn btn-danger">🚪 Logout</button>
            </div>
            <div id="recipes-container" class="mt-3"></div>
        </div>
    `;

    document.getElementById("view-recipes-btn").addEventListener("click", fetchRecipes);
    document.getElementById("add-recipe-btn").addEventListener("click", () => alert("Add Recipe Modal Here"));
    document.getElementById("logout-btn").addEventListener("click", () => {
        localStorage.removeItem("token");
        navigateTo("login");
    });
}
