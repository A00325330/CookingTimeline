// newRecipeFlow.js

let newRecipeId = null;

document.addEventListener("DOMContentLoaded", () => {
  // MODAL ELEMENTS
  const modal = document.getElementById("recipe-modal");
  const closeModalBtn = document.getElementById("closeRecipeModal");

  // STEP 1
  const step1 = document.getElementById("recipe-step-1");
  const recipeNameInput = document.getElementById("recipeNameInput");
  const recipeNameNextBtn = document.getElementById("recipeNameNextBtn");

  // STEP 2
  const step2 = document.getElementById("recipe-step-2");
  const ingredientName = document.getElementById("ingredientName");
  const ingredientTime = document.getElementById("ingredientTime");
  const ingredientMethod = document.getElementById("ingredientMethod");
  const addIngredientBtn = document.getElementById("addIngredientBtn");
  const ingredientListDiv = document.getElementById("ingredientList");
  const finishRecipeBtn = document.getElementById("finishRecipeBtn");

  // RECIPE DROPDOWN
  const recipeSelect = document.getElementById("recipe-select");

  // BUTTON TO SHOW MODAL
  const openModalBtn = document.getElementById("openNewRecipeModal");
  openModalBtn.addEventListener("click", () => {
    console.log("Opening New Recipe modal...");
    modal.style.display = "block";

    // Reset steps
    step1.style.display = "block";
    step2.style.display = "none";
    ingredientListDiv.innerHTML = "";
    newRecipeId = null;
    recipeNameInput.value = "";
  });

  // CLOSE MODAL
  closeModalBtn.addEventListener("click", () => {
    console.log("Closing modal...");
    modal.style.display = "none";
  });

  // If user clicks outside modal, also close
  window.addEventListener("click", (e) => {
    if (e.target === modal) {
      console.log("Clicked outside modal, closing...");
      modal.style.display = "none";
    }
  });

  /**
   * STEP 1: Save recipe name (Create TEMP recipe)
   */
  recipeNameNextBtn.addEventListener("click", async () => {
    const name = recipeNameInput.value.trim();
    if (!name) {
      alert("Please enter a recipe name!");
      return;
    }

    console.log("Creating temp recipe with name:", name);

    let response; // define outside try so we can log inside catch if needed
    try {
      response = await fetch("http://localhost:8081/api/recipes/temp", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: name,
          visibility: "PRIVATE",
          ingredients: [],
          steps: []
        })
      });

      console.log("Response status (create temp recipe):", response.status);

      if (!response.ok) {
        throw new Error(`Failed to create recipe. Status code: ${response.status}`);
      }

      const data = await response.json();
      console.log("New recipe created:", data);

      newRecipeId = data.id; // store ID

      // Hide step1, show step2
      step1.style.display = "none";
      step2.style.display = "block";

    } catch (err) {
      console.error("Error creating recipe:", err);
      alert(`Error creating recipe: ${err.message}`);
    }
  });

  /**
   * STEP 2: Add Ingredient
   */
  addIngredientBtn.addEventListener("click", async () => {
    if (!newRecipeId) {
      alert("No recipe ID found. Please create a recipe name first.");
      return;
    }

    const ingName = ingredientName.value.trim();
    const ingTime = parseInt(ingredientTime.value) || 0;
    const ingMethod = ingredientMethod.value.trim();

    if (!ingName || !ingMethod || !ingTime) {
      alert("Enter name, time, and method for ingredient!");
      return;
    }

    console.log("Adding ingredient:", { ingName, ingTime, ingMethod });

    let response;
    try {
      // If your backend expects "temp recipe" or "normal recipe" is different, adjust the URL accordingly:
      // e.g. POST /api/recipes/temp/{id}/ingredients or /api/recipes/{id}/ingredients
      response = await fetch(`http://localhost:8081/api/recipes/temp/${newRecipeId}/ingredients`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: ingName,
          cookingTime: ingTime,
          cookingMethod: ingMethod
        })
      });

      console.log("Response status (add ingredient):", response.status);

      if (!response.ok) {
        throw new Error(`Failed to add ingredient. Status code: ${response.status}`);
      }

      const updatedRecipe = await response.json();
      console.log("Updated recipe:", updatedRecipe);

      // Rerender the ingredient list
      renderIngredients(updatedRecipe.ingredients);

      // Clear input fields
      ingredientName.value = "";
      ingredientTime.value = "";
      ingredientMethod.value = "";

    } catch (err) {
      console.error("Error adding ingredient:", err);
      alert(`Error adding ingredient: ${err.message}`);
    }
  });

  /**
   * FINISH
   */
  finishRecipeBtn.addEventListener("click", () => {
    console.log("Finishing recipe creation... newRecipeId:", newRecipeId);

    if (newRecipeId) {
      // Add newly created recipe to the dropdown
      const name = recipeNameInput.value.trim() || `Recipe #${newRecipeId}`;
      const option = document.createElement("option");
      option.value = newRecipeId;
      option.textContent = name;
      recipeSelect.appendChild(option);

      // Optionally select it
      recipeSelect.value = newRecipeId;
    }

    // Close modal
    modal.style.display = "none";
  });
});

/**
 * Renders the list of ingredients in step2
 */
function renderIngredients(ingredients) {
  const ingredientListDiv = document.getElementById("ingredientList");
  if (!ingredients || ingredients.length === 0) {
    ingredientListDiv.innerHTML = "<p>No ingredients yet.</p>";
    return;
  }
  let html = "<ul>";
  ingredients.forEach((ing) => {
    html += `<li><strong>${ing.name}</strong> - ${ing.cookingTime} mins (${ing.cookingMethod})</li>`;
  });
  html += "</ul>";
  ingredientListDiv.innerHTML = html;
}
