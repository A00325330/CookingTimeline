let cookingInterval = null;

export function attachCookingTimer({
  startButton,
  maxTime,
  totalTimeLabel,
  countdownLabels, // D3 selection
  processedData
}) {
  // If “Start Cooking” is clicked, we begin an interval
  startButton.addEventListener("click", () => {
    // If we already had an interval, clear it:
    if (cookingInterval) {
      clearInterval(cookingInterval);
    }

    const startTimestamp = Date.now();

    cookingInterval = setInterval(() => {
      const elapsedMs  = Date.now() - startTimestamp;
      const elapsedMin = elapsedMs / 60000; // ms -> minutes
      const timeLeft   = maxTime - elapsedMin;

      // Overall “Total Time Left” label
      if (timeLeft <= 0) {
        totalTimeLabel.text(`Total Time Left: 0m (Done!)`);
        clearInterval(cookingInterval);
      } else {
        const mm = Math.floor(timeLeft);
        const ss = Math.floor((timeLeft - mm) * 60);
        totalTimeLabel.text(`Total Time Left: ${mm}m ${ss}s`);
      }

      // For each ingredient, time until start:
      countdownLabels.text(d => {
        const timeUntil = d.start - elapsedMin; 
        if (timeUntil <= 0) {
          return "Now!";
        } else {
          const m = Math.floor(timeUntil);
          const s = Math.floor((timeUntil - m) * 60);
          return `${m}m ${s}s to start`;
        }
      });

      // If we've run out of total time, label them “Done!” & clear
      if (timeLeft <= 0) {
        countdownLabels.text("Done!");
        clearInterval(cookingInterval);
      }
    }, 1000);
  });
}
