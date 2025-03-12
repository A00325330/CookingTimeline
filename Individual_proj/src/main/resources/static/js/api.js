// api.js - Handles authentication API calls

import { navigateTo } from "./spa.js";
// api.js - Handles API Calls

export async function saveRecipe(recipe) {
    const token = localStorage.getItem("token");
    if (!token) {
        alert("❌ Not authenticated. Please log in.");
        return;
    }

    try {
        console.log("📤 Sending Recipe Data:", recipe);

        const response = await fetch("http://localhost:8081/api/recipes", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify(recipe),
        });

        if (!response.ok) {
            throw new Error(`❌ Failed to save recipe. Status: ${response.status}`);
        }

        alert("✅ Recipe saved successfully!");
        fetchRecipes(); // Refresh recipes
    } catch (error) {
        console.error("🚨 ERROR: Saving recipe failed", error);
        alert("❌ Could not save recipe.");
    }
}

export async function fetchRecipes() {
    const token = localStorage.getItem("token");
    if (!token) {
        alert("❌ Not authenticated. Please log in.");
        return [];
    }

    try {
        const response = await fetch("http://localhost:8081/api/recipes/mine", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            throw new Error(`❌ Error fetching recipes. Status: ${response.status}`);
        }

        const data = await response.json();
        console.log("📥 Recipes fetched:", data);

        // Ensure `recipeList` exists in `_embedded`
        if (data._embedded && data._embedded.recipeList) {
            return data._embedded.recipeList;
        }

        // If API returns a direct array, return it
        if (Array.isArray(data)) {
            return data;
        }

        console.error("🚨 Unexpected response format:", data);
        return []; // Return empty array to prevent crashes

    } catch (error) {
        console.error("🚨 ERROR: Fetching recipes failed", error);
        alert("❌ Could not fetch recipes.");
        return [];
    }
}


export async function loginUser() {
    const email = document.getElementById("login-email").value.trim();
    const password = document.getElementById("login-password").value.trim();

    if (!email || !password) {
        alert("❌ Please enter both email and password.");
        return;
    }

    try {
        const response = await fetch("http://localhost:8081/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) {
            alert("❌ Login failed. Please check your credentials.");
            return;
        }

        const data = await response.json();
        localStorage.setItem("token", data.token);
        navigateTo("dashboard");  // 🔥 Redirects to Dashboard
    } catch (error) {
        console.error("🚨 ERROR: Login request failed", error);
        alert("❌ Login request failed.");
    }
}


export async function registerUser() {
    const email = document.getElementById("register-email").value.trim();
    const password = document.getElementById("register-password").value.trim();

    if (!email || !password) {
        alert("❌ Please enter both email and password.");
        return;
    }

    try {
        const response = await fetch("http://localhost:8081/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                email: email,
                password: password,
                role: "USER", // 🔥 Ensure users are registered as "USER"
            }),
        });

        if (!response.ok) {
            const errorData = await response.json();
            alert("❌ Registration failed: " + (errorData.message || "Try a different email."));
            return;
        }

        alert("✅ Registration successful! Please log in.");
        navigateTo("login");
    } catch (error) {
        console.error("🚨 ERROR: Registration request failed", error);
        alert("❌ Registration request failed.");
    }
}
