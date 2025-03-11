document.addEventListener("DOMContentLoaded", () => {
    const loginBtn = document.getElementById("login-btn");
    const registerBtn = document.getElementById("register-btn");
    const logoutBtn = document.getElementById("logout-btn");
    const dashboardSection = document.getElementById("dashboard-section");

    // ✅ Check if user is already logged in
    const token = localStorage.getItem("token");

    if (token) {
        showDashboard();
    } else {
        showAuthButtons();
    }

    // ✅ Handle Login
    loginBtn.addEventListener("click", async () => {
        const email = prompt("Enter Email:");
        const password = prompt("Enter Password:");

        if (!email || !password) return alert("Email and password required!");

        try {
            const response = await fetch("http://localhost:8081/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            if (!response.ok) throw new Error("Login failed");

            const data = await response.json();
            localStorage.setItem("token", data.token); // ✅ Store JWT token

            showDashboard();
        } catch (error) {
            console.error("Login error:", error);
            alert("Invalid credentials!");
        }
    });

    // ✅ Handle Register
    registerBtn.addEventListener("click", async () => {
        const email = prompt("Enter Email:");
        const password = prompt("Enter Password:");

        if (!email || !password) return alert("Email and password required!");

        try {
            const response = await fetch("http://localhost:8081/api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            if (!response.ok) throw new Error("Registration failed");

            alert("Registration successful! Please login.");
        } catch (error) {
            console.error("Registration error:", error);
            alert("Registration failed!");
        }
    });

    // ✅ Handle Logout
    logoutBtn.addEventListener("click", () => {
        localStorage.removeItem("token"); // ❌ Remove token
        showAuthButtons();
    });

    // ✅ Function to show the dashboard after login
    function showDashboard() {
        loginBtn.style.display = "none";
        registerBtn.style.display = "none";
        logoutBtn.style.display = "inline-block";
        dashboardSection.style.display = "block";
    }

    // ✅ Function to show login/register if user is not logged in
    function showAuthButtons() {
        loginBtn.style.display = "inline-block";
        registerBtn.style.display = "inline-block";
        logoutBtn.style.display = "none";
        dashboardSection.style.display = "none";
    }
});
