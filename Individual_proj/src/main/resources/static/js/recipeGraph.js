import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";

export let totalCookingTime;
let startTime = null;

export function graphRecipe(recipe) {
    totalCookingTime = Math.max(...recipe.ingredients.map(ing => ing.cookingTime));

    // ✅ Sort ingredients by longest cooking time first
    recipe.ingredients.sort((a, b) => b.cookingTime - a.cookingTime);

    // ✅ Remove any existing SVG before drawing a new one
    d3.select("#ganttChart").select("svg").remove();

    const margin = { top: 40, right: 200, bottom: 50, left: 160 }; // Adjusted for extra column
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

    // ✅ Add "Time Until Preparation" Title
    svg.append("text")
        .attr("x", width + 50)
        .attr("y", -10) // Above the first ingredient
        .attr("text-anchor", "middle")
        .style("font-size", "14px")
        .style("font-weight", "bold")
        .text("Time Until Preparation");

    // ✅ Add Start Time Column (Initially Empty)
    const startTimes = svg.selectAll(".start-time-text")
        .data(recipe.ingredients)
        .enter()
        .append("text")
        .attr("class", "start-time-text")
        .attr("x", width + 50) // Position to the right of bars
        .attr("y", d => y(d.name) + y.bandwidth() / 2)
        .attr("dy", ".35em")
        .attr("text-anchor", "middle")
        .style("fill", "black")
        .style("font-size", "14px")
        .text(d => formatTime(totalCookingTime - d.cookingTime));

    document.getElementById("timer-display").textContent = `Total Cooking Time: ${totalCookingTime} mins`;

    // ✅ Enable the Start Cooking Button
    const startButton = document.getElementById("start-timer-btn");
    startButton.disabled = false;
    startButton.onclick = () => startCookingTimer(recipe, startTimes);
}

// ✅ Function to generate random colors for bars
function getRandomColor() {
    return `rgba(${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)}, 0.7)`;
}

// ✅ Function to format time (MM:SS)
function formatTime(minutes) {
    const min = Math.floor(minutes);
    const sec = Math.floor((minutes % 1) * 60);
    return `${min}:${sec < 10 ? '0' : ''}${sec}`;
}

// ✅ Function to start the cooking timer with a 3-second delay
function startCookingTimer(recipe, startTimes) {
    const firstIngredient = recipe.ingredients[0]; // The ingredient that starts first
    const firstIngredientSelection = d3.selectAll("rect").filter(d => d.name === firstIngredient.name);

    let countdown = 3; // 3-second delay
    document.getElementById("time-left-display").textContent = `Starting in: ${countdown}`;

    // ✅ Apply Shake Animation to First Ingredient
    firstIngredientSelection
        .transition()
        .duration(300)
        .style("transform", "translateX(-5px)")
        .transition()
        .duration(300)
        .style("transform", "translateX(5px)")
        .transition()
        .duration(300)
        .style("transform", "translateX(-5px)")
        .transition()
        .duration(300)
        .style("transform", "translateX(5px)");

    // ✅ 3...2...1 Countdown Before Starting
    const countdownInterval = setInterval(() => {
        countdown--;
        document.getElementById("time-left-display").textContent = `Starting in: ${countdown}`;

        // ✅ Shake animation continues during countdown
        firstIngredientSelection
            .transition()
            .duration(300)
            .style("transform", "translateX(-5px)")
            .transition()
            .duration(300)
            .style("transform", "translateX(5px)");

        if (countdown === 0) {
            clearInterval(countdownInterval);

            // ✅ Cooking officially starts here!
            startRealCookingTimer(recipe, startTimes);
        }
    }, 1000);
}

// ✅ Function to start the real countdown
function startRealCookingTimer(recipe, startTimes) {
    startTime = new Date();
    console.log("Cooking started at:", startTime);

    // ✅ Set finish time exactly **1 second after cooking starts**
    setTimeout(() => {
        const finishTime = new Date(startTime.getTime() + totalCookingTime * 60000);
        d3.selectAll(".finish-time-text")
            .data(recipe.ingredients)
            .text(finishTime.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }));
    }, 1000);

    // ✅ Actual countdown loop
    const interval = setInterval(() => {
        const elapsedTime = (new Date() - startTime) / 60000; // Convert ms to minutes
        const remainingTime = totalCookingTime - elapsedTime;

        if (remainingTime <= 0) {
            clearInterval(interval);
            document.getElementById("time-left-display").textContent = "Cooking Done!";
            return;
        }

        document.getElementById("time-left-display").textContent = `Time Left: ${formatTime(remainingTime)}`;

        startTimes.text(d => {
            const countdown = totalCookingTime - d.cookingTime - elapsedTime;
            return countdown > 0 ? formatTime(countdown) : "Start Now!";
        });

    }, 1000);
}
