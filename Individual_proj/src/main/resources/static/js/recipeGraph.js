import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";

/**
 * Global variables
 */
export let totalCookingTime;
let startTime = null;

/**
 * Fetch and graph a recipe, ensuring proper handling of temporary/public recipes.
 */
/**
 * ✅ Fetches the selected recipe details and graphs it.
 */
async function fetchAndGraphRecipe(recipeId) {
    try {
        console.log(`🔍 Fetching details for Recipe ID: ${recipeId}...`);

        let response = await fetch(`http://localhost:8081/api/recipes/public/${recipeId}`);

        // ✅ If not found, try fetching it as a TEMP recipe
        if (response.status === 404) {
            console.warn(`⚠️ Recipe ID ${recipeId} not found as public, checking temporary storage...`);
            response = await fetch(`http://localhost:8081/api/recipes/temp/${recipeId}`);
        }

        // ❌ If still not found, return error
        if (!response.ok) {
            throw new Error(`Failed to fetch recipe! Status: ${response.status}`);
        }

        // ✅ Convert to JSON
        const recipe = await response.json();
        console.log("✅ Successfully fetched Recipe Data:", recipe);

        // ✅ Ensure recipe has ingredients before graphing
        if (!recipe.ingredients || recipe.ingredients.length === 0) {
            console.warn("⚠️ Recipe has no ingredients. Skipping graphing...");
            return;
        }

        // ✅ Graph the fetched recipe
        graphRecipe(recipe);
    } catch (error) {
        console.error("❌ Error fetching recipe details:", error.message);
        alert(`Error fetching recipe: ${error.message}`);
    }
}


/**
 * Generates the cooking timeline graph using D3.js
 */

/**
 * ✅ Graphs a recipe timeline in a Gantt-style chart.
 */
export function graphRecipe(recipe) {
    if (!recipe || !recipe.ingredients || recipe.ingredients.length === 0) {
        console.warn("⚠️ No ingredients found for recipe:", recipe);
        document.getElementById("ganttChart").innerHTML = "<p>No ingredients to display.</p>";
        return;
    }

    console.log("📊 Graphing recipe:", recipe.name);

    // ✅ Remove previous graph
    d3.select("#ganttChart").select("svg").remove();

    const totalCookingTime = Math.max(...recipe.ingredients.map(ing => ing.cookingTime));

    const margin = { top: 40, right: 200, bottom: 50, left: 160 };
    const width = 800 - margin.left - margin.right;
    const height = recipe.ingredients.length * 50;

    const svg = d3.select("#ganttChart")
        .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", `translate(${margin.left},${margin.top})`);

    // ✅ X Axis
    const x = d3.scaleLinear()
        .domain([0, totalCookingTime])
        .range([0, width]);

    svg.append("g")
        .attr("transform", `translate(0,${height})`)
        .call(d3.axisBottom(x));

    // ✅ Y Axis
    const y = d3.scaleBand()
        .domain(recipe.ingredients.map(d => d.name))
        .range([0, height])
        .padding(0.3);

    svg.append("g").call(d3.axisLeft(y));

    // ✅ Draw bars
    svg.selectAll("rect")
        .data(recipe.ingredients)
        .enter()
        .append("rect")
        .attr("y", d => y(d.name))
        .attr("height", y.bandwidth())
        .attr("x", d => x(totalCookingTime - d.cookingTime))
        .attr("width", d => x(d.cookingTime))
        .attr("fill", d => `rgba(${Math.random() * 255}, ${Math.random() * 255}, ${Math.random() * 255}, 0.7)`)
        .attr("stroke", "black")
        .attr("stroke-width", 2);

    document.getElementById("timer-display").textContent = `Total Cooking Time: ${totalCookingTime} mins`;
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
    console.log("⏳ Cooking started at:", startTime);

    const interval = setInterval(() => {
        const elapsedTime = (new Date() - startTime) / 60000;
        const remainingTime = totalCookingTime - elapsedTime;

        if (remainingTime <= 0) {
            clearInterval(interval);
            document.getElementById("time-left-display").textContent = "🍽 Cooking Done!";
            return;
        }

        document.getElementById("time-left-display").textContent = `⏳ Time Left: ${formatTime(remainingTime)}`;

        // ✅ Update ingredient start times dynamically
        d3.selectAll(".start-time-text")
            .text(d => {
                const countdown = totalCookingTime - d.cookingTime - elapsedTime;
                return countdown > 0 ? formatTime(countdown) : "Start Now!";
            });

    }, 1000);
}
