import { checkExistingTempRecipe } from "./api.js"; 

import { renderIngredients } from "./utils.js";

import { deleteTempRecipe, updateIngredientInTemp, deleteIngredientFromTemp } from "./api.js";
export async function openRecipeModal() {
    console.log("Opening Recipe Modal...");

    // ✅ Always fetch the latest temp recipe before opening modal
    const tempRecipe = await checkExistingTempRecipe();

    document.getElementById("recipe-modal").style.display = "block";

    if (tempRecipe) {
        console.log("⚠️ Existing temp recipe found, loading it...");
        setupModalForExistingRecipe(tempRecipe);
    } else {
        console.log("🆕 No existing temp recipe, allowing new creation.");
        setupModalForNewRecipe();
    }
}


export function closeRecipeModal() {
    console.log("Closing Recipe Modal...");
    document.getElementById("recipe-modal").style.display = "none";
}


export function setupModalForExistingRecipe(recipe) {
    console.log("⚠️ Loading Existing Temp Recipe...");

    document.getElementById("recipe-step-1").style.display = "block";
    document.getElementById("recipe-step-2").style.display = "block"; // ✅ Show ingredients for editing

    document.getElementById("recipeNameInput").value = recipe.name;
    document.getElementById("recipeNameInput").disabled = true;

    document.getElementById("recipeNameNextBtn").textContent = "Edit Recipe";
    document.getElementById("recipeNameNextBtn").onclick = () => setupModalForNewRecipe(recipe);

    document.getElementById("addIngredientBtn").disabled = false; // ✅ Enable ingredient modification
    document.getElementById("addIngredientBtn").removeAttribute("title");

    document.getElementById("ingredientList").innerHTML = renderIngredients(recipe.ingredients, true); // ✅ Allow editing

    // ✅ Ensure Delete button exists
    if (!document.getElementById("deleteTempRecipeBtn")) {
        const deleteBtn = document.createElement("button");
        deleteBtn.id = "deleteTempRecipeBtn";
        deleteBtn.className = "btn btn-danger mt-2";
        deleteBtn.textContent = "Delete Recipe";
        deleteBtn.onclick = async () => {
            await deleteTempRecipe();
            setupModalForNewRecipe(); // Reset modal after deleting
        };
        document.getElementById("recipe-step-1").appendChild(deleteBtn);
    }
}



export function setupModalForNewRecipe() {
    console.log("🔄 Resetting Modal for New Recipe...");
    document.getElementById("recipeNameInput").value = "";
    document.getElementById("recipeNameInput").disabled = false;
    document.getElementById("recipeNameNextBtn").textContent = "Next →";
    document.getElementById("recipeNameNextBtn").onclick = handleCreateTempRecipe;
}
