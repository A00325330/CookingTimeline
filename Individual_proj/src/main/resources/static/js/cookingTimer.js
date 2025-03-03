import { chart, totalCookingTime } from "./recipeGraph.js";

let timerInterval = null;
let timeElapsed = 0;

export function startCookingTimer() {
    timeElapsed = 0;

    if (timerInterval) clearInterval(timerInterval);
    updateTimerDisplay(totalCookingTime);

    timerInterval = setInterval(() => {
        if (!chart) return clearInterval(timerInterval);

        let timeLeft = totalCookingTime - timeElapsed;
        updateTimerDisplay(timeLeft);

        // ✅ Move the red vertical line correctly
        chart.options.plugins.annotation.annotations = {
            currentTimeLine: {
                type: "line",
                mode: "vertical",
                xMin: timeElapsed,
                xMax: timeElapsed,
                borderColor: "red",
                borderWidth: 3,
                label: { content: "Now", enabled: true, position: "top" }
            }
        };

        chart.update();

        timeElapsed++;
        if (timeElapsed > totalCookingTime) {
            clearInterval(timerInterval);
            document.getElementById("timer-display").textContent = "Cooking Done!";
        }
    }, 1000);
}

// ✅ Update the countdown timer display
function updateTimerDisplay(minutesLeft) {
    let minutes = Math.floor(minutesLeft);
    let seconds = Math.floor((minutesLeft - minutes) * 60);
    document.getElementById("timer-display").textContent = `Time Left: ${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
}
