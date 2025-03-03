export let chart;
export let totalCookingTime;  // ✅ Export totalCookingTime for cookingTimer.js

import { startCookingTimer } from "./cookingTimer.js";

export function graphRecipe(recipe) {
    const ctx = document.getElementById("ganttChart").getContext("2d");

    let datasets = [];
    totalCookingTime = Math.max(...recipe.ingredients.map(ing => ing.cookingTime)); // ✅ Store max cooking time globally

    // ✅ Sort ingredients by longest cooking time first
    recipe.ingredients.sort((a, b) => b.cookingTime - a.cookingTime);

    recipe.ingredients.forEach((ingredient, index) => {
        let startTime = totalCookingTime - ingredient.cookingTime; // ✅ Ensure all bars end at the same time

        datasets.push({
            label: ingredient.name,
            data: [{ x: startTime, y: index, width: ingredient.cookingTime }],
            backgroundColor: getRandomColor(),
            borderColor: "black",
            borderWidth: 2,
            barPercentage: 0.8
        });
    });

    if (chart) chart.destroy();

    chart = new Chart(ctx, {
        type: "bar",
        data: {
            labels: recipe.ingredients.map(ing => ing.name),
            datasets: datasets
        },
        options: {
            indexAxis: "y",
            animation: { duration: 500, easing: "linear" },
            scales: {
                x: {
                    title: { display: true, text: "Time (Minutes)" },
                    min: 0,
                    max: totalCookingTime
                },
                y: {
                    title: { display: true, text: "Ingredients" },
                    ticks: { autoSkip: false }
                }
            },
            plugins: {
                annotation: {
                    annotations: {
                        currentTimeLine: {
                            type: "line",
                            mode: "vertical",
                            xMin: 0,
                            xMax: 0,
                            borderColor: "red",
                            borderWidth: 3,
                            label: { content: "Now", enabled: true, position: "top" }
                        }
                    }
                }
            }
        }
    });

    document.getElementById("timer-display").textContent = `Total Cooking Time: ${totalCookingTime} mins`;

    // ✅ Start cooking timer automatically
    startCookingTimer();
}

function getRandomColor() {
    return `rgba(${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)}, ${Math.floor(Math.random() * 255)}, 0.7)`;
}
