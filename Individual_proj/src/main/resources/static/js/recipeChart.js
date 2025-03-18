import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import { updateRecipe } from "./api.js";  
import { attachCookingTimer } from "./cookingTimer.js";

/**
 * Renders a Gantt-style chart for a recipe’s ingredients.
 *
 * @param {Object} recipe The full recipe object from backend
 */
export function renderRecipeChart(recipe) {
    console.log("📊 Rendering chart for:", recipe.name, recipe);

    const container = document.getElementById("recipe-chart-container");
    container.innerHTML = ""; // Clear anything that was there before

    if (!recipe || !recipe.ingredients || recipe.ingredients.length === 0) {
        container.innerHTML = `<p class="text-danger">⚠️ No ingredients to display.</p>`;
        return;
    }

    const maxTime = Math.max(...recipe.ingredients.map(ing => ing.cookingTime));

    const sortedIngredients = [...recipe.ingredients].sort(
        (a, b) => b.cookingTime - a.cookingTime
    );

    const processedData = sortedIngredients.map(ing => ({
        name: ing.name,
        method: ing.cookingMethod,
        start: maxTime - ing.cookingTime,
        duration: ing.cookingTime,
        originalIngredient: ing
    }));

	const margin = { top: 40, right: 150, bottom: 50, left: 200 }; 
	const chartWidth = Math.min(window.innerWidth * 0.7, 900) - margin.left - margin.right; 
	const chartHeight = processedData.length * 50;

	const svg = d3.select(container)
	    .append("svg")
	    .attr("width", chartWidth + margin.left + margin.right)
	    .attr("height", chartHeight + margin.top + margin.bottom);

	const g = svg.append("g")
	    .attr("transform", `translate(${margin.left},${margin.top})`);

	// **Timer for Total Cooking Time** (Dynamically positioned)
	const totalTimeLabel = svg.append("text")
	    .attr("x", chartWidth + margin.left - 20) // Moved to fit inside chart
	    .attr("y", margin.top * 0.6)
	    .attr("text-anchor", "end")  // Align text to the right
	    .attr("font-size", "18px")
	    .attr("font-weight", "bold")
	    .attr("fill", "#333")
	    .text(`Total Time Left: ${maxTime}m`);


    const xScale = d3.scaleLinear()
        .domain([0, maxTime])
        .range([0, chartWidth]);

    const yScale = d3.scaleBand()
        .domain(processedData.map(d => d.name))
        .range([0, chartHeight])
        .padding(0.2);

    function getRandomPastelColor() {
        return `hsl(${Math.random() * 360}, 70%, 80%)`;
    }

    g.selectAll(".bar")
        .data(processedData)
        .enter()
        .append("rect")
        .attr("class", "bar")
        .attr("x", d => xScale(d.start))
        .attr("y", d => yScale(d.name))
        .attr("width", d => xScale(d.duration))
        .attr("height", yScale.bandwidth())
        .attr("fill", getRandomPastelColor)
        .attr("stroke", "#555")
        .attr("rx", 5)
        .style("cursor", "pointer")
        .on("click", (event, d) => openEditPanel(recipe, d));

    g.selectAll(".start-label")
        .data(processedData)
        .enter()
        .append("text")
        .attr("class", "start-label")
        .attr("x", d => xScale(d.start) - 45)
        .attr("y", d => yScale(d.name) + yScale.bandwidth() * 0.65)
        .attr("text-anchor", "end")
        .attr("font-size", "13px")
        .attr("fill", "#333")
        .text(d => `Start: ${d.start}m`);

    const countdownLabels = g.selectAll(".countdown-label")
        .data(processedData)
        .enter()
        .append("text")
        .attr("class", "countdown-label")
        .attr("x", d => xScale(d.start) + xScale(d.duration) + 10)
        .attr("y", d => yScale(d.name) + yScale.bandwidth() * 0.65)
        .attr("font-size", "12px")
        .attr("fill", "#333")
        .text("");

    const xAxis = d3.axisBottom(xScale).tickFormat(d => `${d}m`);
    g.append("g")
        .attr("transform", `translate(0,${chartHeight})`)
        .call(xAxis);

    const yAxis = d3.axisLeft(yScale);
    g.append("g").call(yAxis);

    // 🔥 **Positioned "Start Cooking" Button Below Chart**
    const startButton = document.createElement("button");
    startButton.textContent = "▶️ Start Cooking";
    startButton.classList.add("btn", "btn-primary", "mt-3");
    container.appendChild(startButton);

    // 🔥 **Start Total Cooking Timer**
    attachCookingTimer({
        startButton,
        maxTime,
        totalTimeLabel,
        countdownLabels, 
        processedData,
    });

    console.log("✅ Chart rendered successfully!");
}


// ─────────────────────────────────────────────────────────────────────────────
// 🔥 **Restored: Ingredient Edit Panel**
function openEditPanel(recipe, ingredient) {
    const panel = document.getElementById("edit-panel");
    document.getElementById("edit-name").value = ingredient.name;
    document.getElementById("edit-time").value = ingredient.duration;
    document.getElementById("edit-method").value = ingredient.method;

    panel.classList.add("show");

    document.getElementById("save-edit").onclick = async function () {
        ingredient.originalIngredient.name = document.getElementById("edit-name").value;
        ingredient.originalIngredient.cookingTime = parseInt(document.getElementById("edit-time").value, 10);
        ingredient.originalIngredient.cookingMethod = document.getElementById("edit-method").value;

        const updated = await updateRecipe(recipe.id, recipe);
        if (updated) {
            panel.classList.remove("show");
            renderRecipeChart(recipe);  // ✅ Re-render after edit
        } else {
            alert("❌ Failed to update recipe.");
        }
    };

    document.getElementById("cancel-edit").onclick = () => {
        panel.classList.remove("show");
    };
}
