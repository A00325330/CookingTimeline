import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";

export let totalCookingTime;
let startTime = null;

/**
 * Main function to visualize the recipe in the Gantt chart.
 */
export function graphRecipe(recipe) {
    if (!recipe || !recipe.ingredients || recipe.ingredients.length === 0) {
        console.error("Invalid recipe data:", recipe);
        document.getElementById("ganttChart").innerHTML = "<p>No ingredients to display.</p>";
        return;
    }

    totalCookingTime = Math.max(...recipe.ingredients.map(ing => ing.cookingTime));

    // ✅ Sort ingredients by longest cooking time first
    recipe.ingredients.sort((a, b) => b.cookingTime - a.cookingTime);

    // ✅ Remove any existing SVG before drawing a new one
    d3.select("#ganttChart").select("svg").remove();

    const margin = { top: 40, right: 200, bottom: 50, left: 160 };
    const width = 800 - margin.left - margin.right;
    const height = recipe.ingredients.length * 50;

    // ✅ Create SVG
    const svg = d3.select("#ganttChart")
        .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", `translate(${margin.left},${margin.top})`);

    // ✅ X Axis Scale
    const x = d3.scaleLinear()
        .domain([0, totalCookingTime])
        .range([0, width]);

    // ✅ Y Axis Scale
    const y = d3.scaleBand()
        .domain(recipe.ingredients.map(d => d.name))
        .range([0, height])
        .padding(0.3);

    // ✅ X Axis
    svg.append("g")
        .attr("transform", `translate(0,${height})`)
        .call(d3.axisBottom(x));

    // ✅ Y Axis
    svg.append("g")
        .call(d3.axisLeft(y));

    // ✅ Tooltip
    const tooltip = d3.select("#ganttChart")
        .append("div")
        .style("position", "absolute")
        .style("background", "#fff")
        .style("border", "1px solid #ddd")
        .style("padding", "5px")
        .style("border-radius", "5px")
        .style("pointer-events", "none")
        .style("opacity", 0);

    // ✅ Draw bars with tooltip
    svg.selectAll("rect")
        .data(recipe.ingredients)
        .enter()
        .append("rect")
        .attr("y", d => y(d.name))
        .attr("height", y.bandwidth())
        .attr("x", d => x(totalCookingTime - d.cookingTime))
        .attr("width", d => x(d.cookingTime))
        .attr("fill", d => getRandomColor())
        .attr("stroke", "black")
        .attr("stroke-width", 2)
        .on("mouseover", (event, d) => {
            tooltip.style("opacity", 1)
                .html(`<strong>${d.name}</strong><br>Cooking Method: ${d.cookingMethod}`)
                .style("left", (event.pageX + 10) + "px")
                .style("top", (event.pageY - 20) + "px");
        })
        .on("mousemove", (event) => {
            tooltip.style("left", (event.pageX + 10) + "px")
                .style("top", (event.pageY - 20) + "px");
        })
        .on("mouseout", () => {
            tooltip.style("opacity", 0);
        });

    // ✅ Update UI elements
    document.getElementById("timer-display").textContent = `Total Cooking Time: ${totalCookingTime} mins`;

    // ✅ Enable the Start Cooking Button
    const startButton = document.getElementById("start-timer-btn");
    startButton.disabled = false;
    startButton.onclick = () => startCookingTimer(recipe);
}

/**
 * Generates a random color for bars
 */
function getRandomColor() {
    return `rgba(${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)}, 0.7)`;
}

/**
 * Formats time in MM:SS format
 */
function formatTime(minutes) {
    const min = Math.floor(minutes);
    const sec = Math.floor((minutes % 1) * 60);
    return `${min}:${sec < 10 ? '0' : ''}${sec}`;
}

/**
 * Starts the cooking timer countdown
 */
function startCookingTimer(recipe) {
    startTime = new Date();
    console.log("Cooking started at:", startTime);

    const interval = setInterval(() => {
        const elapsedTime = (new Date() - startTime) / 60000;
        const remainingTime = totalCookingTime - elapsedTime;

        if (remainingTime <= 0) {
            clearInterval(interval);
            document.getElementById("time-left-display").textContent = "Cooking Done!";
            return;
        }

        document.getElementById("time-left-display").textContent = `Time Left: ${formatTime(remainingTime)}`;
    }, 1000);
}
