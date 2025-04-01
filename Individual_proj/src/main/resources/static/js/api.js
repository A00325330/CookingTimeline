import { navigateTo } from "./spa.js";

// src/main/resources/static/js/api.js
const API_BASE_URL =
  window.location.port === "8081"
    ? "http://localhost:8081/api" // dev
    : "/api";                     // prod/tests

/**
 * Helper function to make API requests.
 * @param {string} endpoint - API endpoint.
 * @param {string} method - HTTP method (GET, POST, PUT, etc.).
 * @param {Object} [body] - Request body (optional).
 * @param {boolean} [authRequired] - Whether authentication is needed (default: true).
 * @returns {Promise<Object|Array|null>} - JSON response or null on error.
 */
async function apiRequest(endpoint, method = "GET", body = null, authRequired = true) {
    const headers = { "Content-Type": "application/json" };
    const token = localStorage.getItem("token");

    if (authRequired && !token) {
        alert("❌ Not authenticated. Please log in.");
        navigateTo("login");
        return null;
    }

    if (authRequired) headers["Authorization"] = `Bearer ${token}`;

    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method,
            headers,
            body: body ? JSON.stringify(body) : null,
        });

        if (response.status === 401) {
            alert("Session expired. Logging out...");
            localStorage.removeItem("token");
            navigateTo("login");
            return null;
        }

        if (!response.ok) {
            throw new Error(`❌ API Error: ${response.statusText} (Status: ${response.status})`);
        }

        return await response.json();
    } catch (error) {
        console.error(`🚨 ERROR: ${method} ${endpoint} failed`, error);
        alert(`❌ Request failed: ${error.message}`);
        return null;
    }
}

// 🔹 Recipe Actions
export async function saveRecipe(recipe) {
    return apiRequest("/recipes", "POST", recipe);
}

export async function updateRecipe(recipeId, updatedRecipe) {
    return apiRequest(`/recipes/${recipeId}`, "PUT", updatedRecipe);
}

export async function fetchRecipes() {
    const response = await apiRequest("/recipes/mine", "GET", null, true);
    console.log("📡 API Response for Private Recipes:", response);
    return response?._embedded?.recipeDtoList || [];
}

export async function fetchPublicRecipes() {
    const response = await apiRequest("/recipes/public", "GET", null, false);
    console.log("📡 API Response for Public Recipes:", response);
    return response?._embedded?.recipeDtoList || [];
}




// 🔹 Tags Actions
export async function fetchTags() {
    const token = localStorage.getItem("token");
    if (!token) {
        console.error("🚨 ERROR: No auth token. Cannot fetch tags.");
        return [];
    }

    try {
		const response = await fetch("/api/tags", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,  // 🔥 Include token
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            throw new Error(`❌ Error fetching tags. Status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error("🚨 ERROR: Fetching tags failed", error);
        return [];
    }
}



export async function fetchRecipesByTag(tag) {
    console.log(`📡 Fetching recipes for tag: ${tag}`);

    // ✅ Fix URL concatenation
    const url = `/recipes/by-tag/${encodeURIComponent(tag)}`;
    console.log("🔍 Requesting:", url);

    const response = await apiRequest(url, "GET");

    console.log("✅ Raw API Response:", response);

    // ✅ Ensure correct extraction
    const recipes = response?._embedded?.recipeDtoList || [];

    console.log(`✅ Extracted ${recipes.length} recipes for tag "${tag}"`, recipes);
    return recipes;
}






// 🔹 Authentication
export async function loginUser() {
    const email = document.getElementById("login-email")?.value.trim();
    const password = document.getElementById("login-password")?.value.trim();

    if (!email || !password) {
        alert("❌ Please enter both email and password.");
        return;
    }

    const data = await apiRequest("/auth/login", "POST", { email, password }, false);
    if (data) {
        localStorage.setItem("token", data.token);
        navigateTo("dashboard");
    }
}

export async function registerUser() {
    const email = document.getElementById("register-email")?.value.trim();
    const password = document.getElementById("register-password")?.value.trim();

    if (!email || !password) {
        alert("❌ Please enter both email and password.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password, role: "USER" }),
        });

        if (response.status === 400) {
            alert("❌ Registration failed: User already exists or invalid data.");
            return;
        }

        if (!response.ok) {
            throw new Error(`Unexpected error: ${response.statusText}`);
        }

        alert("✅ Registration successful! Please log in.");
        navigateTo("login");
    } catch (error) {
        console.error(error);
        alert("❌ An error occurred. Please try again later.");
    }
}

