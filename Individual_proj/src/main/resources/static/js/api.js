// api.js
import { navigateTo } from "./spa.js";

// 1️⃣ Save a new private recipe
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
        // Optionally refetch or do something else
    } catch (error) {
        console.error("🚨 ERROR: Saving recipe failed", error);
        alert("❌ Could not save recipe.");
    }
}

// 2️⃣ Update an existing recipe (if you have a PUT endpoint)
export async function updateRecipe(recipeId, updatedRecipe) {
    const token = localStorage.getItem("token");
    if (!token) {
        alert("❌ Not authenticated. Please log in.");
        return false;
    }

    try {
        const response = await fetch(`http://localhost:8081/api/recipes/${recipeId}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify(updatedRecipe),
        });

        if (!response.ok) {
            throw new Error(`❌ Failed to update recipe. Status: ${response.status}`);
        }

        console.log("✅ Recipe updated successfully!");
        return true;
    } catch (error) {
        console.error("🚨 ERROR: Updating recipe failed", error);
        return false;
    }
}

// 3️⃣ Fetch user's *private* recipes
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
        console.log("📥 Private recipes fetched:", data);

        // If response has _embedded.recipeList
        if (data._embedded && data._embedded.recipeList) {
            return data._embedded.recipeList;
        }
        // If it’s a direct array
        if (Array.isArray(data)) {
            return data;
        }

        console.error("🚨 Unexpected response format:", data);
        return [];

    } catch (error) {
        console.error("🚨 ERROR: Fetching recipes failed", error);
        alert("❌ Could not fetch private recipes.");
        return [];
    }
}

// 4️⃣ Fetch *public* recipes
export async function fetchPublicRecipes() {
    try {
        const response = await fetch("http://localhost:8081/api/recipes/public", {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });

        if (!response.ok) {
            throw new Error(`❌ Error fetching public recipes. Status: ${response.status}`);
        }

        const data = await response.json();
        console.log("📥 Public recipes fetched:", data);

        // If response has _embedded.recipeList
        if (data._embedded && data._embedded.recipeList) {
            return data._embedded.recipeList;
        }
        // If it’s a direct array
        if (Array.isArray(data)) {
            return data;
        }

        console.error("🚨 Unexpected public response format:", data);
        return [];

    } catch (error) {
        console.error("🚨 ERROR: Fetching public recipes failed", error);
        alert("❌ Could not fetch public recipes.");
        return [];
    }
}

// 5️⃣ Login user
export async function loginUser() {
    const email = document.getElementById("login-email")?.value.trim();
    const password = document.getElementById("login-password")?.value.trim();

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

// 6️⃣ Register user
export async function registerUser() {
    const email = document.getElementById("register-email")?.value.trim();
    const password = document.getElementById("register-password")?.value.trim();

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
                role: "USER", // 🔥 Ensure normal users get role USER
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
