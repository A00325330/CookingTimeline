Feature: Get public recipes

  Scenario: Get all public recipes
    * def baseUrl = karate.properties['karate.baseUrl']
    * url baseUrl + '/api'
    Given path 'recipes', 'public'
    When method get
    Then status 200

    * def publicRecipes = response._embedded.recipeDtoList
    * match publicRecipes == '#[]' || publicRecipes.length > 0
