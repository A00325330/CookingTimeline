let recipeChartInstance = null;

export function renderRecipeChart(recipe) {
    if (typeof Chart === "undefined") {
        console.error("❌ Chart.js is not loaded.");
        return;
    }

    const chartContainer = document.getElementById("recipe-chart-container");
    chartContainer.innerHTML = `<canvas id="recipe-chart"></canvas>`; // Reset canvas

    const ctx = document.getElementById("recipe-chart").getContext("2d");

    if (!recipe || !recipe.ingredients || recipe.ingredients.length === 0) {
        chartContainer.innerHTML = `<p class="text-danger">⚠️ No ingredients to display.</p>`;
        return;
    }

    // Sort ingredients by cooking time (longest to shortest)
    const sortedIngredients = [...recipe.ingredients].sort((a, b) => b.cookingTime - a.cookingTime);

    // Extract names and times for chart
    const labels = sortedIngredients.map(ing => `${ing.name} (${ing.cookingMethod})`);
    const times = sortedIngredients.map(ing => ing.cookingTime);

    // ✅ Destroy previous chart instance if it exists
    if (recipeChartInstance) {
        recipeChartInstance.destroy();
    }

    // ✅ Create new Chart
    recipeChartInstance = new Chart(ctx, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [{
                label: "Cooking Time (mins)",
                data: times,
                backgroundColor: "rgba(54, 162, 235, 0.6)", // Blue bars
                borderColor: "rgba(54, 162, 235, 1)",
                borderWidth: 1
            }]
        },
        options: {
            indexAxis: "y", // Horizontal bar chart
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: {
                    title: { display: true, text: "Time (minutes)" }
                },
                y: {
                    title: { display: true, text: "Ingredient" }
                }
            }
        }
    });
}
