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

import io.github.bonigarcia.wdm.WebDriverManager;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class LoginIT {

    @Autowired
    private DatabaseManager databaseManager;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setupClass() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=3840,2160");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--enable-javascript");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--start-maximized");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        databaseManager.clearDatabase();
        databaseManager.executeSetupScripts();
    }

    @AfterAll
    void tearDown() {
        if (driver != null) driver.quit();
        databaseManager.clearDatabase();
    }

    @Test
    void testLoginSuccessAndNavigateToDashboard() throws InterruptedException {
        driver.get("http://localhost:8081/index.html");

        WebElement loginNav = driver.findElement(By.id("login-btn"));
        loginNav.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-email")));
        WebElement pass = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-password")));

        email.sendKeys("user@example.com");
        pass.sendKeys("Admin123!");

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));
        loginBtn.click();

        Thread.sleep(1000);
        Thread.sleep(2000);
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout-btn")));
        assertTrue(logoutBtn.isDisplayed(), "❌ Logout button not visible after login");
    }

    @Test
    void testAddRecipeFlow() throws InterruptedException {
        driver.get("http://localhost:8081/index.html");

        WebElement loginNav = driver.findElement(By.id("login-btn"));
        loginNav.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).sendKeys("user@example.com");
        driver.findElement(By.id("login-password")).sendKeys("Admin123!");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("add-recipe-btn"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recipe-name"))).sendKeys("Classic Chicken Curry");
        driver.findElement(By.id("recipe-description")).sendKeys("A classic Indian curry recipe.");

        WebElement tagInput = driver.findElement(By.id("manual-tag"));
        tagInput.sendKeys("indian");
        driver.findElement(By.id("add-tag-btn")).click();

        String[][] ingredients = {
            {"Chicken", "20", "Cook"},
            {"Onion", "5", "Fry"},
            {"Spices", "3", "Mix"}
        };

        for (String[] ing : ingredients) {
            driver.findElement(By.id("ingredient-name")).sendKeys(ing[0]);
            driver.findElement(By.id("ingredient-time")).sendKeys(ing[1]);
            driver.findElement(By.id("ingredient-method")).sendKeys(ing[2]);
            WebElement addIngredientBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("add-ingredient-btn")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addIngredientBtn);
            addIngredientBtn.click();
            Thread.sleep(300);
        }

        driver.findElement(By.id("submit-recipe-btn")).click();
        Thread.sleep(500);
        driver.findElement(By.id("logout-btn")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-btn")));

        driver.get("http://localhost:8081/index.html");

        WebElement loginnavi = driver.findElement(By.id("login-btn"));
        loginnavi.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).sendKeys("user@example.com");
        driver.findElement(By.id("login-password")).sendKeys("Admin123!");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();

        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recipe-dropdown")));
        List<WebElement> options = dropdown.findElements(By.tagName("option"));

        boolean recipeFound = options.stream()
            .anyMatch(opt -> opt.getText().contains("Classic Chicken Curry"));

        assertTrue(recipeFound, "❌ Recipe not found in dropdown!");
    }
}
