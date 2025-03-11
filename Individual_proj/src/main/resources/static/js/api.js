window.loginUser = async function (email, password) {
    try {
        const response = await fetch("http://localhost:8081/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) throw new Error("Login failed");

        const data = await response.json();
        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);
        alert("Login successful!");
        window.location.reload();
    } catch (error) {
        alert(error.message);
    }
};

window.registerUser = async function (email, password) {
    try {
        const response = await fetch("http://localhost:8081/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
        });

        if (!response.ok) throw new Error("Registration failed");

        alert("Registration successful! Please login.");
    } catch (error) {
        alert(error.message);
    }
};
