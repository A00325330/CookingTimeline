package com.tus.group_project.karate;

import com.intuit.karate.junit5.Karate;
import com.tus.group_project.test_helper.DatabaseManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthKarateTest {

    @Autowired
    private DatabaseManager databaseManager;

    @BeforeAll
    void setup() {
    	databaseManager.clearDatabase();
        databaseManager.executeSetupScripts();
    }

    @Karate.Test
    Karate runAuthTests() {
        return Karate.run("classpath:features/auth/login.feature", "classpath:features/auth/register.feature")
                     .relativeTo(getClass());
    }

    @AfterAll
    void teardown() {
        databaseManager.clearDatabase();
    }
}
