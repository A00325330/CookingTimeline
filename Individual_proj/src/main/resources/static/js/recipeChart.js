let recipeChartInstance = null;

import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";

export function renderRecipeChart(recipe) {
    const container = document.getElementById("recipe-chart-container");
    container.innerHTML = ""; // Clear previous chart

    if (!recipe || !recipe.ingredients || recipe.ingredients.length === 0) {
        console.error("❌ No ingredients found for Gantt chart.");
        container.innerHTML = `<p class="text-danger">⚠️ No ingredients to display.</p>`;
        return;
    }

    // ✅ Step 1: Calculate the latest finish time
    const maxTime = Math.max(...recipe.ingredients.map(ing => ing.cookingTime));

    // ✅ Step 2: Calculate start & end times for each ingredient
    const processedData = recipe.ingredients.map(ing => ({
        name: `${ing.name} (${ing.cookingMethod})`,
        start: maxTime - ing.cookingTime,
        duration: ing.cookingTime,
        end: maxTime
    }));

    // ✅ Step 3: Set up SVG dimensions
    const width = container.clientWidth || 800;
    const height = processedData.length * 50 + 40; // 50px per bar

    const svg = d3.select(container)
        .append("svg")
        .attr("width", width)
        .attr("height", height);

    // ✅ Step 4: Scales
    const xScale = d3.scaleLinear()
        .domain([0, maxTime])
        .range([100, width - 20]);

    const yScale = d3.scaleBand()
        .domain(processedData.map(d => d.name))
        .range([20, height - 20])
        .padding(0.2);

    // ✅ Step 5: Draw bars
    svg.selectAll(".bar")
        .data(processedData)
        .enter()
        .append("rect")
        .attr("class", "bar")
        .attr("x", d => xScale(d.start))
        .attr("y", d => yScale(d.name))
        .attr("width", d => xScale(d.end) - xScale(d.start))
        .attr("height", yScale.bandwidth())
        .attr("fill", "rgba(75, 192, 192, 0.6)")
        .attr("stroke", "rgba(75, 192, 192, 1)")
        .attr("rx", 5);

    // ✅ Step 6: Add labels
    svg.selectAll(".label")
        .data(processedData)
        .enter()
        .append("text")
        .attr("x", d => xScale(d.start) + 5)
        .attr("y", d => yScale(d.name) + yScale.bandwidth() / 1.5)
        .text(d => `Start: ${d.start} mins`)
        .attr("fill", "#333")
        .attr("font-size", "12px");

    // ✅ Step 7: Add axes
    const xAxis = d3.axisBottom(xScale).tickFormat(d => `${d}m`);
    const yAxis = d3.axisLeft(yScale);

    svg.append("g")
        .attr("transform", `translate(0,${height - 20})`)
        .call(xAxis);

    svg.append("g")
        .attr("transform", `translate(100,0)`)
        .call(yAxis);
}
