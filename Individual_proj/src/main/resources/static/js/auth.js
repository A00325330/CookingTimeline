const API_URL = "http://localhost:8081/api/auth";

// ✅ Opens authentication modal (Login/Register)
function openAuthModal(type) {
    const authTitle = document.getElementById("auth-title");
    const authModal = document.getElementById("auth-modal");
    const authSubmit = document.getElementById("auth-submit");

    if (!authTitle || !authModal || !authSubmit) {
        console.warn("⚠️ Auth modal elements not found. Check your HTML structure.");
        return;
    }

    authTitle.textContent = type;
    authModal.style.display = "block";
    authSubmit.setAttribute("data-action", type);
}

// ✅ Closes authentication modal
function closeAuthModal() {
    const authModal = document.getElementById("auth-modal");
    if (authModal) {
        authModal.style.display = "none";
    } else {
        console.warn("⚠️ Auth modal not found. Cannot close.");
    }
}

// ✅ Handles login or registration
async function handleAuth() {
    const authSubmit = document.getElementById("auth-submit");
    const emailInput = document.getElementById("auth-email");
    const passwordInput = document.getElementById("auth-password");

    if (!authSubmit || !emailInput || !passwordInput) {
        console.error("❌ Required auth elements not found. Cannot proceed.");
        return;
    }

    const action = authSubmit.getAttribute("data-action").toLowerCase();
    const email = emailInput.value.trim();
    const password = passwordInput.value;

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
            alert("✅ Login successful!");

            // Redirect admin users
            if (getUserRole() === "ROLE_ADMIN") {
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

// ✅ Logs the user out
function logoutUser() {
    localStorage.removeItem("jwt");
    updateUI();
}

// ✅ Extracts user role from JWT
function getUserRole() {
    const token = localStorage.getItem("jwt");
    if (!token) return null;

    try {
        const tokenPayload = JSON.parse(atob(token.split(".")[1]));
        return tokenPayload.role || null;
    } catch (error) {
        console.error("❌ Error decoding JWT:", error);
        return null;
    }
}

// ✅ Updates UI based on authentication status
function updateUI() {
    const token = localStorage.getItem("jwt");
    const isAdmin = getUserRole() === "ROLE_ADMIN";

    function toggleVisibility(id, shouldShow) {
        const element = document.getElementById(id);
        if (element) {
            element.style.display = shouldShow ? "inline-block" : "none";
        }
    }

    toggleVisibility("login-btn", !token);
    toggleVisibility("register-btn", !token);
    toggleVisibility("logout-btn", !!token);
    toggleVisibility("admin-btn", isAdmin);

    // ✅ Attach admin page navigation
    const adminBtn = document.getElementById("admin-btn");
    if (adminBtn) {
        adminBtn.addEventListener("click", () => {
            window.location.href = "admin.html";
        });
    }
}

// ✅ Attach event listeners safely
document.addEventListener("DOMContentLoaded", () => {
    console.log("✅ DOM Loaded - Attaching Auth Event Listeners...");

    function safeAddListener(id, event, handler) {
        const element = document.getElementById(id);
        if (element) {
            element.addEventListener(event, handler);
            console.log(`✅ Attached ${event} listener to #${id}`);
        } else {
            console.warn(`⚠️ Element #${id} not found. Skipping event listener.`);
        }
    }

    safeAddListener("login-btn", "click", () => openAuthModal("Login"));
    safeAddListener("register-btn", "click", () => openAuthModal("Register"));
    safeAddListener("auth-submit", "click", handleAuth);
    safeAddListener("auth-close", "click", closeAuthModal);
    safeAddListener("logout-btn", "click", logoutUser);
});

// ✅ Initialize UI on page load
document.addEventListener("DOMContentLoaded", updateUI);
