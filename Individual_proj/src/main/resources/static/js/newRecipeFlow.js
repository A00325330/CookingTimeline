import { checkExistingTempRecipe, createTempRecipe, deleteTempRecipe, addIngredientToTemp } from "./api.js";
import { addTempRecipeToDropdown } from "./dropdown.js";
import { openRecipeModal, closeRecipeModal, setupModalForExistingRecipe, setupModalForNewRecipe } from "./modal.js";
import { renderIngredients } from "./utils.js";
import { fetchAndGraphRecipe } from "./recipes.js"; // ✅ Ensure it’s imported

let newRecipeId = null;
let existingTempRecipe = null;

document.addEventListener("DOMContentLoaded", async () => {
    console.log("✅ Checking for existing temp recipe...");
    existingTempRecipe = await checkExistingTempRecipe();
    
    document.getElementById("openNewRecipeModal").addEventListener("click", openRecipeModal);
    document.getElementById("closeRecipeModal").addEventListener("click", closeRecipeModal);
    document.getElementById("recipeNameNextBtn").addEventListener("click", handleCreateTempRecipe);
    document.getElementById("addIngredientBtn").addEventListener("click", handleAddIngredient);
    document.getElementById("finishRecipeBtn").addEventListener("click", handleFinishRecipe);
	document.getElementById("recipe-select").addEventListener("change", function () {
	    const selectedRecipeId = this.value;
	    console.log(`🔍 Fetching and graphing Recipe ID: ${selectedRecipeId}`);
	    if (selectedRecipeId) fetchAndGraphRecipe(selectedRecipeId);
	});
// ✅ Attach event listeners for ingredient editing & deletion
document.querySelectorAll(".edit-ingredient").forEach(button => {
    button.addEventListener("click", async (event) => {
        const ingredientId = event.target.getAttribute("data-id");
        const newName = prompt("Enter new ingredient name:");
        const newTime = prompt("Enter new cooking time (mins):");
        const newMethod = prompt("Enter new cooking method:");

        if (newName && newTime && newMethod) {
            await updateIngredientInTemp(existingTempRecipe.id, ingredientId, {
                name: newName,
                cookingTime: parseInt(newTime),
                cookingMethod: newMethod
            });

            // Refresh UI
            existingTempRecipe = await checkExistingTempRecipe();
            document.getElementById("ingredientList").innerHTML = renderIngredients(existingTempRecipe.ingredients, true);
        }
    });
});

document.querySelectorAll(".delete-ingredient").forEach(button => {
    button.addEventListener("click", async (event) => {
        const ingredientId = event.target.getAttribute("data-id");
        await deleteIngredientFromTemp(existingTempRecipe.id, ingredientId);

        // Refresh UI
        existingTempRecipe = await checkExistingTempRecipe();
        document.getElementById("ingredientList").innerHTML = renderIngredients(existingTempRecipe.ingredients, true);
    });
});


});

/**
 * ✅ Creates a new temporary recipe
 */
async function handleCreateTempRecipe() {
    const name = document.getElementById("recipeNameInput").value.trim();
    if (!name) {
        alert("Please enter a recipe name!");
        return;
    }

    const recipe = await createTempRecipe(name);
    if (recipe) {
        newRecipeId = recipe.id;
        existingTempRecipe = recipe;
        addTempRecipeToDropdown(recipe);
        document.getElementById("recipe-step-1").style.display = "none";
        document.getElementById("recipe-step-2").style.display = "block";
    }
}

/**
 * ✅ Adds an ingredient to the temporary recipe
 */
async function handleAddIngredient() {
    if (!existingTempRecipe) {
        alert("No recipe ID found. Please create a recipe first.");
        return;
    }

    const name = document.getElementById("ingredientName").value.trim();
    const time = parseInt(document.getElementById("ingredientTime").value) || 0;
    const method = document.getElementById("ingredientMethod").value.trim();

    if (!name || !method || !time) {
        alert("Enter all ingredient details!");
        return;
    }

    const updatedRecipe = await addIngredientToTemp(existingTempRecipe.id, { name, cookingTime: time, cookingMethod: method });
    if (updatedRecipe) {
        existingTempRecipe = updatedRecipe;
        document.getElementById("ingredientList").innerHTML = renderIngredients(updatedRecipe.ingredients);
    }
}

/**
 * ✅ Completes the temp recipe process and updates the dropdown
 */
function handleFinishRecipe() {
    console.log("🏁 Finishing recipe process...");
    if (existingTempRecipe) {
        addTempRecipeToDropdown(existingTempRecipe);
    }
    closeRecipeModal();
}
