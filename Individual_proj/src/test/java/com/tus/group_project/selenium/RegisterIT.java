package com.tus.group_project.selenium;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.bonigarcia.wdm.WebDriverManager;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@EnabledIfSystemProperty(named = "runSeleniumTests", matches = "true")

public class RegisterIT {
	@LocalServerPort
	private int port; // ✅ this injects a random available port
	private WebDriver driver;
	private WebDriverWait wait;

	@BeforeAll
	void setupClass() {
		WebDriverManager.chromedriver().setup();
	}

	@BeforeEach
	void setupTest() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--disable-gpu", "--no-sandbox", "--headless=new");
		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(10));
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

		// Click the "Register" nav button
		WebElement registerNav = driver.findElement(By.id("register-btn"));
		registerNav.click();

		// Fill in the registration form
		String uniqueEmail = "aaron" + System.currentTimeMillis() + "@example.com";
		driver.findElement(By.id("register-email")).sendKeys(uniqueEmail);
		driver.findElement(By.id("register-password")).sendKeys("Admin123!");

		// Click the form's Register button
		driver.findElement(By.cssSelector("#register-form button[type='submit']")).click();

		// Wait and handle the JS alert
		Alert alert = wait.until(ExpectedConditions.alertIsPresent());
		String alertText = alert.getText();
		assertTrue(alertText.toLowerCase().contains("registration successful"),
				"Alert did not confirm success. Actual text: " + alertText);
		alert.accept();
	}
}
