export async function fetchRecipesForDropdown() {
    console.log("✅ fetchRecipesForDropdown() is running...");

    try {
        const response = await fetch("http://localhost:8081/api/recipes/public");
        console.log("API Response Status:", response.status); // Debug

        if (!response.ok) {
            throw new Error(`Failed to fetch recipes! Status: ${response.status}`);
        }

        const data = await response.json();
        console.log("Fetched Data:", data); // Debugging API response

        const recipes = data._embedded?.recipeList || [];
        console.log("Recipes Array:", recipes); // Debugging recipes array

        const dropdown = document.getElementById("recipe-select");
        if (!dropdown) {
            console.error("❌ Dropdown not found in DOM!");
            return;
        }

        if (recipes.length === 0) {
            dropdown.innerHTML = `<option value="">No recipes available</option>`;
            return;
        }

        dropdown.innerHTML = `<option value="">-- Select a Recipe --</option>`; // Reset dropdown
        recipes.forEach(recipe => {
            const option = document.createElement("option");
            option.value = recipe.id;
            option.textContent = recipe.name;
            dropdown.appendChild(option);
        });

        dropdown.disabled = false; // ✅ Enable dropdown once recipes load
        console.log("✅ Dropdown successfully populated.");
    } catch (error) {
        console.error("❌ Fetch Error:", error.message);
        alert(error.message);
    }
}
