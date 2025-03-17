// 📂 tags.js (New Module)
import { fetchAPI } from "./api.js";
import { loadRecipesByTag } from "./recipes.js"; // Import function from recipes.js

const tagsContainer = document.getElementById("tags-container");
const recipeDropdown = document.getElementById("recipe-dropdown"); // Ensure this is the correct ID

export async function loadTags() {
    try {
        const tags = await fetchAPI("/api/tags"); // ✅ Modular API call

        tagsContainer.innerHTML = ""; // Clear existing tags
        tags.forEach(tag => {
            const tagCard = document.createElement("div");
            tagCard.className = "card p-2 m-2 shadow-sm";
            tagCard.style.cursor = "pointer";
            tagCard.innerHTML = `<div class="card-body text-center">${tag.name}</div>`;

            // Click event to filter recipes
            tagCard.addEventListener("click", () => {
                document.querySelectorAll(".card").forEach(c => c.classList.remove("border-primary"));
                tagCard.classList.add("border-primary"); // Highlight selected tag
                loadRecipesByTag(tag.name);
            });

            tagsContainer.appendChild(tagCard);
        });
    } catch (error) {
        console.error("❌ Error loading tags:", error);
    }
}
