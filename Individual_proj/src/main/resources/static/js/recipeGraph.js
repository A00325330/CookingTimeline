import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";

export let totalCookingTime;
let startTime = null;

export function graphRecipe(recipe) {
    totalCookingTime = Math.max(...recipe.ingredients.map(ing => ing.cookingTime));

    // ✅ Sort ingredients by longest cooking time first
    recipe.ingredients.sort((a, b) => b.cookingTime - a.cookingTime);

    // ✅ Remove any existing SVG before drawing a new one
    d3.select("#ganttChart").select("svg").remove();

    const margin = { top: 40, right: 150, bottom: 50, left: 160 }; // Adjusted for extra column
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

    // ✅ Add Start Time Column (Initially Empty)
    svg.selectAll(".start-time-text")
        .data(recipe.ingredients)
        .enter()
        .append("text")
        .attr("class", "start-time-text")
        .attr("x", width + 20) // Position to the right of bars
        .attr("y", d => y(d.name) + y.bandwidth() / 2)
        .attr("dy", ".35em")
        .text("")
        .style("fill", "black")
        .style("font-size", "14px");

    document.getElementById("timer-display").textContent = `Total Cooking Time: ${totalCookingTime} mins`;

    // ✅ Enable the Start Cooking Button
    const startButton = document.getElementById("start-timer-btn");
    startButton.disabled = false;
    startButton.onclick = () => startCookingTimer(recipe, svg, x, y);
}

// ✅ Function to generate random colors for bars
function getRandomColor() {
    return `rgba(${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)}, 0.7)`;
}

// ✅ Function to start the cooking timer
function startCookingTimer(recipe, svg, x, y) {
    const startTime = new Date();
    console.log("Cooking started at:", startTime);

    let timeElapsed = 0;

    function updateCountdown() {
        const timeLeft = totalCookingTime - timeElapsed;
        
        if (timeLeft > 0) {
            // ✅ Update the main "Time Left" display
            const minutes = Math.floor(timeLeft);
            const seconds = Math.floor((timeLeft - minutes) * 60);
            document.getElementById("time-left-display").textContent = 
                `Time Left: ${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
        } else {
            // ✅ When done, set time-left to "Done!"
            document.getElementById("time-left-display").textContent = "Cooking Done!";
            clearInterval(timer);
            return;
        }

        // ✅ Update the ingredient countdowns
        svg.selectAll(".start-time-text")
            .data(recipe.ingredients)
            .text(d => {
                const remainingTime = (totalCookingTime - d.cookingTime) - timeElapsed;

                if (remainingTime <= 0) {
                    const finishTime = new Date(startTime.getTime() + totalCookingTime * 60000);
                    return `Finish: ${finishTime.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`;
                } else {
                    const min = Math.floor(remainingTime);
                    const sec = Math.floor((remainingTime - min) * 60);
                    return `${min}:${sec < 10 ? '0' : ''}${sec}`;
                }
            });

        timeElapsed += 1 / 60;  // ✅ Update every second
    }

    // ✅ Start updating every second
    const timer = setInterval(updateCountdown, 1000);
    updateCountdown();  // Run immediately to avoid 1-sec delay
    document.getElementById("start-timer-btn").disabled = true;
}

