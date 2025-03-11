document.addEventListener("DOMContentLoaded", function () {
    // Buttons
    const viewRecipesBtn = document.getElementById("view-recipes-btn");
    const recipeList = document.getElementById("recipe-list");

    // ✅ View Recipes Function
    viewRecipesBtn.addEventListener("click", function () {
        fetch("http://localhost:8081/api/recipes/mine", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("token")}`
            }
        })
        .then(response => {
            if (!response.ok) throw new Error("Failed to fetch recipes.");
            return response.json();
        })
        .then(data => {
            recipeList.innerHTML = ""; // Clear existing list

            if (data._embedded && data._embedded.recipeList.length > 0) {
                data._embedded.recipeList.forEach(recipe => {
                    const listItem = document.createElement("li");
                    listItem.classList.add("list-group-item");
                    listItem.innerHTML = `
                        <strong>${recipe.name}</strong> - ${recipe.description}
                        <br><small>Cooking Time: ${recipe.cookingTime} mins</small>
                    `;

                    recipeList.appendChild(listItem);
                });
            } else {
                recipeList.innerHTML = "<li class='list-group-item text-muted'>No recipes found.</li>";
            }

            // Show the modal
            new bootstrap.Modal(document.getElementById("viewRecipesModal")).show();
        })
        .catch(error => {
            console.error("Error:", error);
            alert("Error fetching recipes.");
        });
    });
});
