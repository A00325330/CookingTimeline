Feature: Create a recipe

  Scenario: Create and expose response
    * url 'http://localhost:8081/api'
    * def login = call read('classpath:features/auth/login-as-user.feature')
    * def token = login.token
    * header Authorization = 'Bearer ' + token
    * header Content-Type = 'application/json'

    Given path 'recipes'
    And request
    """
    {
      name: "Chocolate Cake",
      ingredients: [
        { "name": "Flour", "cookingMethod": "BAKE", "cookingTime": 10 },
        { "name": "Sugar", "cookingMethod": "RAW", "cookingTime": 0 },
        { "name": "Cocoa", "cookingMethod": "RAW", "cookingTime": 0 }
      ],
      instructions: "Mix and bake",
      visibility: "PRIVATE"
    }
    """
    When method post
    Then status 201
    * def createdRecipe = response
