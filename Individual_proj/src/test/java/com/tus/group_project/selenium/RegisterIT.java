package com.tus.group_project.selenium;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EnabledIfSystemProperty(named = "runSeleniumTests", matches = "true")
class RegisterIT {

    @LocalServerPort
    private int port;

    private WebDriver driver;

    @BeforeAll
    void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu", "--no-sandbox", "--headless=new");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testUserRegistrationFlow() {
        driver.get("http://localhost:" + port + "/index.html");

        driver.findElement(By.id("register-btn")).click();

        String uniqueEmail = "aaron" + System.currentTimeMillis() + "@example.com";
        driver.findElement(By.id("register-email")).sendKeys(uniqueEmail);
        driver.findElement(By.id("register-password")).sendKeys("Admin123!");
        driver.findElement(By.cssSelector("#register-form button[type='submit']")).click();

        await().atMost(Duration.ofSeconds(5)).until(() -> {
            try {
                Alert alert = driver.switchTo().alert();
                return alert.getText().toLowerCase().contains("registration successful");
            } catch (NoAlertPresentException e) {
                return false;
            }
        });

        Alert alert = driver.switchTo().alert();
        assertTrue(alert.getText().toLowerCase().contains("registration successful"),
                "Alert did not confirm success. Actual text: " + alert.getText());
        alert.accept();
    }
}
