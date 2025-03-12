import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import { updateRecipe } from "./api.js"; // Ensure your API function exists

export function renderRecipeChart(recipe, updateCallback) {
    const container = document.getElementById("recipe-chart-container");
    container.innerHTML = ""; // Clear previous chart

    if (!recipe || !recipe.ingredients || recipe.ingredients.length === 0) {
        console.error("❌ No ingredients found for Gantt chart.");
        container.innerHTML = `<p class="text-danger">⚠️ No ingredients to display.</p>`;
        return;
    }

    // 1) Find the ingredient with the maximum cookingTime
    const maxTime = Math.max(...recipe.ingredients.map(ing => ing.cookingTime));

    // 2) Sort the ingredients in descending order of cookingTime
    const sortedIngredients = [...recipe.ingredients].sort(
      (a, b) => b.cookingTime - a.cookingTime
    );

    // 3) Build processedData from the *sorted* array
    const processedData = sortedIngredients.map(ing => ({
        id: ing.id,  // optional if you need IDs
        name: ing.name,
        method: ing.cookingMethod,
        start: maxTime - ing.cookingTime,
        duration: ing.cookingTime,
        end: maxTime,
        originalIngredient: ing
    }));

    // Set up your SVG dimensions
    const width = container.clientWidth || 800;
    const height = processedData.length * 50 + 40;

    const svg = d3.select(container)
        .append("svg")
        .attr("width", width)
        .attr("height", height);

    // Scales
    const xScale = d3.scaleLinear()
        .domain([0, maxTime])
        .range([100, width - 20]);

    const yScale = d3.scaleBand()
        .domain(processedData.map(d => d.name)) // use the sorted names
        .range([20, height - 20])
        .padding(0.2);

    function getRandomPastelColor() {
        return `hsl(${Math.random() * 360}, 70%, 80%)`;
    }

    // Bars
    svg.selectAll(".bar")
        .data(processedData)
        .enter()
        .append("rect")
        .attr("class", "bar")
        .attr("x", d => xScale(d.start))
        .attr("y", d => yScale(d.name))
        .attr("width", d => xScale(d.end) - xScale(d.start))
        .attr("height", yScale.bandwidth())
        .attr("fill", getRandomPastelColor)
        .attr("stroke", "#555")
        .attr("rx", 5)
        .style("cursor", "pointer")
        .on("click", (event, d) => openEditPanel(recipe, d, updateCallback));

    // Label “Start: X mins”
    svg.selectAll(".label")
        .data(processedData)
        .enter()
        .append("text")
        .attr("x", d => xScale(d.start) + 5)
        .attr("y", d => yScale(d.name) + yScale.bandwidth() / 1.5)
        .text(d => `Start: ${d.start} mins`)
        .attr("fill", "#333")
        .attr("font-size", "12px");

    // Axes
    const xAxis = d3.axisBottom(xScale).tickFormat(d => `${d}m`);
    const yAxis = d3.axisLeft(yScale);

    svg.append("g")
       .attr("transform", `translate(0,${height - 20})`)
       .call(xAxis);

    svg.append("g")
       .attr("transform", `translate(100,0)`)
       .call(yAxis);
}

// Same as before, the edit panel function
function openEditPanel(recipe, ingredient, updateCallback) {
    const panel = document.getElementById("edit-panel");
    document.getElementById("edit-name").value   = ingredient.name;
    document.getElementById("edit-time").value   = ingredient.duration;
    document.getElementById("edit-method").value = ingredient.method;

    panel.classList.add("show");

    document.getElementById("save-edit").onclick = async function () {
        ingredient.originalIngredient.name = document.getElementById("edit-name").value;
        ingredient.originalIngredient.cookingTime = parseInt(document.getElementById("edit-time").value);
        ingredient.originalIngredient.cookingMethod = document.getElementById("edit-method").value;

        // Attempt an update request to the backend
        const updated = await updateRecipe(recipe.id, recipe);
        if (updated) {
            panel.classList.remove("show");
            updateCallback(); // Reload the chart or dashboard
        } else {
            alert("❌ Failed to update recipe.");
        }
    };

    document.getElementById("cancel-edit").onclick = function () {
        panel.classList.remove("show");
    };
}
