package com.tus.group_project.karate;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import com.tus.group_project.test_helper.DatabaseManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RecipeKarateTest {

	@Autowired
	private DatabaseManager databaseManager;

	@BeforeAll
	void setup() {
		databaseManager.clearDatabase();
		databaseManager.executeSetupScripts();
	}

	@Karate.Test
	Karate runRecipeTests() {
		return Karate.run("classpath:features/recipes/createRecipe.feature",
				"classpath:features/recipes/getMyRecipes.feature",
				"classpath:features/recipes/getRecipeById.feature",
				"classpath:features/recipes/getPublicRecipes.feature").relativeTo(getClass());
	}
//    @Karate.Test
//    Karate runRecipeTests() {
//    	return Karate.run("classpath:features/recipes/getRecipeByTag.feature", 
//    			"classpath:features/recipes/createRecipe.feature", 
//    			"classpath:features/recipes/getMyRecipes.feature",
//    			"classpath:features/recipes/getPublicRecipes.feature", 
//    			"classpath:features/recipes/getRecipeById.feature",
//    			"classpath:features/recipes/updateRecipe.feature", 
//    			"classpath:features/recipes/deleteRecipe.feature")
//    			.relativeTo(getClass());
//    }
}
