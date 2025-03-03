const API_URL = "http://localhost:8081/api/auth";
const RECIPES_URL = "http://localhost:8081/api/recipes";

// Open modal
function openAuthModal(type) {
    document.getElementById("auth-title").textContent = type;
    document.getElementById("auth-modal").style.display = "block";
    document.getElementById("auth-submit").setAttribute("data-action", type);
}

// Close modal
function closeAuthModal() {
    document.getElementById("auth-modal").style.display = "none";
}

// Handle login or register
async function handleAuth() {
    const action = document.getElementById("auth-submit").getAttribute("data-action").toLowerCase();
    const email = document.getElementById("auth-email").value.trim();
    const password = document.getElementById("auth-password").value;

    if (!email || !password) {
        alert("Please enter both email and password.");
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${action}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || "Authentication failed.");
        }

        if (action === "login" && data.token) {
            localStorage.setItem("jwt", data.token);
            updateUI();
            alert("Login successful!");

            // Redirect only if login is required
            const userRole = getUserRole();
            if (userRole === "ROLE_ADMIN") {
                window.location.href = "admin.html";
            }
        } else {
            alert("Something went wrong.");
        }
    } catch (error) {
        alert(error.message);
    }

    closeAuthModal();
}

// Logout function
function logoutUser() {
    localStorage.removeItem("jwt");
    updateUI();
}

// Extract user role from JWT
function getUserRole() {
    const token = localStorage.getItem("jwt");
    if (!token) return null;

    try {
        const tokenPayload = JSON.parse(atob(token.split(".")[1]));
        return tokenPayload.role || null;
    } catch (error) {
        console.error("Error decoding JWT:", error);
        return null;
    }
}

// Update UI state
function updateUI() {
    const token = localStorage.getItem("jwt");
    const isAdmin = getUserRole() === "ROLE_ADMIN";

    document.getElementById("login-btn").style.display = token ? "none" : "inline-block";
    document.getElementById("register-btn").style.display = token ? "none" : "inline-block";
    document.getElementById("logout-btn").style.display = token ? "inline-block" : "none";

    const adminBtn = document.getElementById("admin-btn");
    if (adminBtn) {
        adminBtn.style.display = isAdmin ? "inline-block" : "none";
        adminBtn.addEventListener("click", () => {
            window.location.href = "admin.html";
        });
    }
}

// Initialize UI
document.addEventListener("DOMContentLoaded", updateUI);

// Event listeners
document.getElementById("login-btn").addEventListener("click", () => openAuthModal("Login"));
document.getElementById("register-btn").addEventListener("click", () => openAuthModal("Register"));
document.getElementById("auth-submit").addEventListener("click", handleAuth);
document.getElementById("auth-close").addEventListener("click", closeAuthModal);
document.getElementById("logout-btn").addEventListener("click", logoutUser);
