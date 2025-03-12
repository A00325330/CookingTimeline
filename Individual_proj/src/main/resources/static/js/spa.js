// spa.js - Handles SPA navigation

import { loadLoginPage, loadRegisterPage } from "./auth.js";
import { loadDashboard } from "./dashboard.js";

export function navigateTo(page) {
    if (page === "login") {
        loadLoginPage();
    } else if (page === "register") {
        loadRegisterPage();
    } else if (localStorage.getItem("token")) {
        loadDashboard();
    } else {
        loadLoginPage();
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
