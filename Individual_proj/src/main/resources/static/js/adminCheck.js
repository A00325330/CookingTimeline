// adminCheck.js
export function isAdmin() {
  const token = localStorage.getItem("token");
  if (!token) return false;

  try {
    const payloadBase64 = token.split(".")[1];
    if (!payloadBase64) return false;

    const payloadString = atob(payloadBase64);
    const payload = JSON.parse(payloadString);
    
    const role = payload.role || ""; // e.g. "ROLE_ADMIN"
    return role.includes("ADMIN");
  } catch (error) {
    console.error("❌ Error decoding token for admin check:", error);
    return false;
  }
}
