package com.tus.group_project.test_helper;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

public class TestHelper {
    private WebDriver driver;
    private WebDriverWait wait;

    public TestHelper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void login(String email, String password) {
        driver.get("http://localhost:8081/index.html");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-btn"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email"))).sendKeys(email);
        driver.findElement(By.id("login-password")).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button"))).click();
    }

    public void addRecipe(String name, String description, String tag, List<Ingredient> ingredients) {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("add-recipe-btn"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recipe-name"))).sendKeys(name);
        driver.findElement(By.id("recipe-description")).sendKeys(description);
        driver.findElement(By.id("manual-tag")).sendKeys(tag);
        driver.findElement(By.id("add-tag-btn")).click();

        for (Ingredient i : ingredients) {
            driver.findElement(By.id("ingredient-name")).sendKeys(i.name());
            driver.findElement(By.id("ingredient-time")).sendKeys(String.valueOf(i.time()));
            driver.findElement(By.id("ingredient-method")).sendKeys(i.method());
            driver.findElement(By.id("add-ingredient-btn")).click();
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.id("submit-recipe-btn"))).click();
    }

    public boolean recipeExistsInDropdown(String recipeName) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("recipe-dropdown")));
        WebElement dropdown = driver.findElement(By.id("recipe-dropdown"));
        return dropdown.getText().contains(recipeName);
    }

    public record Ingredient(String name, int time, String method) {}
}
