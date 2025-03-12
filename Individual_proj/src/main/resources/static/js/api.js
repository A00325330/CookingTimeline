// api.js - Handles authentication API calls

import { navigateTo } from "./spa.js";
// api.js - Handles API Calls


export async function fetchRecipes() {
    const token = localStorage.getItem("token");
    if (!token) {
        alert("❌ Not authenticated. Please log in.");
        return;
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

        const recipesContainer = document.getElementById("recipes-container");
        if (!recipesContainer) {
            console.error("❌ ERROR: #recipes-container not found.");
            return;
        }

        recipesContainer.innerHTML = data.length > 0
            ? data.map(recipe => `
                <div class="card mb-2 p-3">
                    <h4>${recipe.name}</h4>
                    <p>${recipe.description}</p>
                </div>
            `).join("")
            : `<p>No recipes found.</p>`;

    } catch (error) {
        console.error("🚨 ERROR: Fetching recipes failed", error);
        alert("❌ Could not fetch recipes.");
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
