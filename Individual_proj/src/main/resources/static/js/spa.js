// spa.js - Handles SPA navigation

import { loadLoginPage, loadRegisterPage } from "./auth.js";
import { loadDashboard } from "./dashboard.js";

export function navigateTo(page) {
    const logoutBtn = document.getElementById("logout-btn");

    if (page === "login") {
        loadLoginPage();
        logoutBtn.style.display = "none"; // Hide logout on login
    } else if (page === "register") {
        loadRegisterPage();
        logoutBtn.style.display = "none"; // Hide logout on register
    } else if (localStorage.getItem("token")) {
        loadDashboard();
        logoutBtn.style.display = "block"; // Show logout when logged in
    } else {
        loadLoginPage();
        logoutBtn.style.display = "none"; // Default state
    }
}


document.addEventListener("DOMContentLoaded", () => {
    const loginBtn = document.getElementById("login-btn");
    const registerBtn = document.getElementById("register-btn");
    const logoutBtn = document.getElementById("logout-btn");

    loginBtn.addEventListener("click", () => navigateTo("login"));
    registerBtn.addEventListener("click", () => navigateTo("register"));
    logoutBtn.addEventListener("click", () => {
        localStorage.removeItem("token");
        navigateTo("login");
    });

    navigateTo("login");
});
