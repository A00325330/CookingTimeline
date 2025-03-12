// recipeChart.js
import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import { updateRecipe } from "./api.js";  // If your API has updateRecipe()
import { attachCookingTimer } from "./cookingTimer.js";

/**
 * Renders a Gantt-style chart for a recipe’s ingredients in descending
 * order of cooking time, plus a "Start Cooking" button and an inline timer.
 *
 * @param {Object}   recipe         The full recipe object from backend
 * @param {Function} updateCallback A function to call (like reload the chart)
 *                                  after an ingredient is edited & saved.
 */
export function renderRecipeChart(recipe, updateCallback) {
  const container = document.getElementById("recipe-chart-container");
  container.innerHTML = ""; // Clear anything that was there before

  if (!recipe || !recipe.ingredients || recipe.ingredients.length === 0) {
    container.innerHTML = `<p class="text-danger">⚠️ No ingredients to display.</p>`;
    return;
  }

  // 1) Maximum cooking time
  const maxTime = Math.max(...recipe.ingredients.map(ing => ing.cookingTime));

  // 2) Sort ingredients descending by cookingTime
  const sortedIngredients = [...recipe.ingredients].sort(
    (a, b) => b.cookingTime - a.cookingTime
  );

  // 3) Build the data used by D3
  //    We interpret: “start” = (maxTime - cookingTime)
  //    so the left edge of each bar is that ingredient’s “start time”
  const processedData = sortedIngredients.map(ing => ({
    name:   ing.name,
    method: ing.cookingMethod,
    start:  maxTime - ing.cookingTime,
    duration: ing.cookingTime,
    originalIngredient: ing
  }));

  // ───────── Layout & margins ─────────
  const margin = { top: 40, right: 160, bottom: 30, left: 180 };
  const containerWidth = Math.max(container.clientWidth || 800, 600);
  const chartWidth  = containerWidth - margin.left - margin.right;
  const chartHeight = processedData.length * 50;

  const svgWidth  = chartWidth  + margin.left + margin.right;
  const svgHeight = chartHeight + margin.top  + margin.bottom;

  // Create the main SVG
  const svg = d3.select(container)
    .append("svg")
      .attr("width",  svgWidth)
      .attr("height", svgHeight);

  // A group offset by our margins
  const g = svg.append("g")
    .attr("transform", `translate(${margin.left},${margin.top})`);

  // We'll place a "Total Time Left" label up near top-left
  const totalTimeLabel = svg.append("text")
    .attr("x", margin.left)
    .attr("y", margin.top * 0.6)
    .attr("font-size", "16px")
    .attr("fill", "#333")
    .text(`Total Time Left: (not started)`);

  // SCALES
  const xScale = d3.scaleLinear()
    .domain([0, maxTime])
    .range([0, chartWidth]);

  const yScale = d3.scaleBand()
    .domain(processedData.map(d => d.name))  // each ingredient name
    .range([0, chartHeight])
    .padding(0.2);

  // Helper: random pastel color
  function getRandomPastelColor() {
    return `hsl(${Math.random() * 360}, 70%, 80%)`;
  }

  // ───────── Draw the bars ─────────
  g.selectAll(".bar")
    .data(processedData)
    .enter()
    .append("rect")
      .attr("class", "bar")
      .attr("x",      d => xScale(d.start))
      .attr("y",      d => yScale(d.name))
      .attr("width",  d => xScale(d.duration))
      .attr("height", yScale.bandwidth())
      .attr("fill",   getRandomPastelColor)
      .attr("stroke", "#555")
      .attr("rx",     5)
      .style("cursor","pointer")
      .on("click", (event, d) => openEditPanel(recipe, d, updateCallback));

  // Label “Start: Xm” to the left of each bar
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

  // “countdown-label” for each bar — updated by the cookingTimer
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

  // ───────── X & Y Axes ─────────
  const xAxis = d3.axisBottom(xScale).tickFormat(d => `${d}m`);
  g.append("g")
    .attr("transform", `translate(0,${chartHeight})`)
    .call(xAxis);

  const yAxis = d3.axisLeft(yScale);
  g.append("g").call(yAxis);

  // ───────── “Start Cooking” button ─────────
  const startButton = document.createElement("button");
  startButton.textContent = "▶️ Start Cooking";
  startButton.classList.add("btn", "btn-primary", "mt-3");
  container.appendChild(startButton);

  // Let a separate cookingTimer handle the countdown logic
  attachCookingTimer({
    startButton,
    maxTime,
    totalTimeLabel,
    countdownLabels, // D3 selection
    processedData,
  });
}

// ─────────────────────────────────────────────────────────────────────────────
// The small “edit ingredient” panel
function openEditPanel(recipe, ingredient, updateCallback) {
  const panel = document.getElementById("edit-panel");
  document.getElementById("edit-name").value   = ingredient.name;
  document.getElementById("edit-time").value   = ingredient.duration;
  document.getElementById("edit-method").value = ingredient.method;

  panel.classList.add("show");

  document.getElementById("save-edit").onclick = async function () {
    ingredient.originalIngredient.name = document.getElementById("edit-name").value;
    ingredient.originalIngredient.cookingTime = parseInt(document.getElementById("edit-time").value, 10);
    ingredient.originalIngredient.cookingMethod = document.getElementById("edit-method").value;

    // Attempt to update the entire recipe
    const updated = await updateRecipe(recipe.id, recipe);
    if (updated) {
      panel.classList.remove("show");
      updateCallback(); // e.g. re-render the chart or reload the dashboard
    } else {
      alert("❌ Failed to update recipe.");
    }
  };

  document.getElementById("cancel-edit").onclick = () => {
    panel.classList.remove("show");
  };
}
