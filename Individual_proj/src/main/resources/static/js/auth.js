// auth.js - Handles login & registration page rendering

import { loginUser, registerUser } from "./api.js";

export function loadLoginPage() {
    const mainContent = document.getElementById("main-content");
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Login</h2>
            <form id="login-form">
                <input type="email" id="login-email" class="form-control mb-2" placeholder="Email" required />
                <input type="password" id="login-password" class="form-control mb-2" placeholder="Password" required />
                <button type="submit" id="login-button"class="btn btn-primary">Login</button>
            </form>
        </div>
    `;
    document.getElementById("login-form").addEventListener("submit", (e) => {
        e.preventDefault();
        loginUser();
    });
}

export function loadRegisterPage() {
    const mainContent = document.getElementById("main-content");
    mainContent.innerHTML = `
        <div class="card p-4">
            <h2>Register</h2>
            <form id="register-form">
                <input type="email" id="register-email" class="form-control mb-2" placeholder="Email" required />
                <input type="password" id="register-password" class="form-control mb-2" placeholder="Password" required />
                <button type="submit" class="btn btn-secondary">Register</button>
            </form>
        </div>
    `;
    document.getElementById("register-form").addEventListener("submit", (e) => {
        e.preventDefault();
        registerUser();
    });
}
