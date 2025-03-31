Feature: Get public recipes

  Scenario: Get all public recipes
    * url 'http://localhost:8081/api'
    Given path 'recipes', 'public'
    When method get
    Then status 200
    * def publicRecipes = response._embedded.recipeDtoList
    And match publicRecipes == '#[]' || publicRecipes.length > 0
