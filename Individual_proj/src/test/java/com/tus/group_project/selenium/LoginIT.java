package com.tus.group_project.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import com.tus.group_project.test_helper.DatabaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class LoginIT {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseManager databaseManager;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setupClass() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless",
                "--disable-gpu", "--no-sandbox", "--window-size=3840,2160",
                "--disable-dev-shm-usage", "--remote-allow-origins=*", "--enable-javascript",
                "--disable-extensions", "--disable-infobars", "--disable-popup-blocking", "--start-maximized"
        );
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        databaseManager.clearDatabase();
        databaseManager.executeSetupScripts();
    }

    @AfterAll
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
        // databaseManager.clearDatabase(); // keep disabled as discussed
    }

    @Test
    void testAddRecipeFlow() {
        // Ensure backend is up and frontend is ready
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            try {
                driver.get("http://localhost:" + port + "/index.html");
                return driver.findElement(By.id("login-btn")).isDisplayed();
            } catch (Exception e) {
                return false;
            }
        });

        // Handle alert from failed fetch if any
        try {
            Alert alert = driver.switchTo().alert();
            System.out.println("Alert: " + alert.getText());
            alert.dismiss();
        }catch (NoAlertPresentException e) {
            System.out.println("No alert present to dismiss.");
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-btn"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).sendKeys("user@example.com");
        driver.findElement(By.id("login-password")).sendKeys("Admin123!");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("add-recipe-btn"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recipe-name")))
             .sendKeys("Classic Chicken Curry");
        driver.findElement(By.id("recipe-description")).sendKeys("A classic Indian curry recipe.");

        driver.findElement(By.id("manual-tag")).sendKeys("indian");
        driver.findElement(By.id("add-tag-btn")).click();

        String[][] ingredients = {
            {"Chick", "20", "Cook"},
            {"Onion", "5", "Fry"},
            {"Spices", "3", "Mix"}
        };

        for (String[] ing : ingredients) {
            driver.findElement(By.id("ingredient-name")).sendKeys(ing[0]);
            driver.findElement(By.id("ingredient-time")).sendKeys(ing[1]);
            driver.findElement(By.id("ingredient-method")).sendKeys(ing[2]);

            WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-ingredient-btn")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addBtn);
            addBtn.click();

            await().pollDelay(Duration.ofMillis(300)).until(() -> true);
        }

        driver.findElement(By.id("submit-recipe-btn")).click();

        await().atMost(Duration.ofSeconds(3)).until(() -> elementIsVisible(By.id("logout-btn")));
        driver.findElement(By.id("logout-btn")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-btn")));

        // Re-login
        driver.get("http://localhost:" + port + "/index.html");
        driver.findElement(By.id("login-btn")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).sendKeys("user@example.com");
        driver.findElement(By.id("login-password")).sendKeys("Admin123!");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();

        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recipe-dropdown")));
        List<WebElement> options = dropdown.findElements(By.tagName("option"));

        boolean recipeFound = options.stream().anyMatch(opt -> opt.getText().contains("Classic Chicken Curry"));
        assertTrue(recipeFound, "❌ Recipe not found in dropdown!");
    }

    private boolean elementIsVisible(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
