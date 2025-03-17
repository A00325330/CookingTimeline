import { navigateTo } from "./spa.js";

const API_BASE_URL = "http://localhost:8081/api";

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
    return apiRequest("/recipes/mine");
}

export async function fetchPublicRecipes() {
    return apiRequest("/recipes/public", "GET", null, false);
}

// 🔹 Tags Actions
export async function fetchTags() {
    const token = localStorage.getItem("token");
    if (!token) {
        console.error("🚨 ERROR: No auth token. Cannot fetch tags.");
        return [];
    }

    try {
        const response = await fetch("http://localhost:8081/api/tags", {
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


export async function fetchRecipesByTag(tagName) {
    if (typeof tagName !== "string") {
        console.error("❌ fetchRecipesByTag received invalid input:", tagName);
        return [];
    }

    try {
        console.log(`📥 Fetching recipes for tag: "${tagName}"`);
        return await apiRequest(`/recipes/by-tag/${encodeURIComponent(tagName)}`);
    } catch (error) {
        console.error("❌ Error fetching recipes by tag:", error);
        return [];
    }
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

