export function handleLogout() {
    localStorage.removeItem("token");
    navigateTo("login");
}
