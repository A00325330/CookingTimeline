Feature: Get recipes of the authenticated user

  Scenario: Get my recipes using valid token
    * url 'http://localhost:8081/api'
    * def login = call read('classpath:features/auth/login-as-user.feature')
    * def token = login.token
    * header Authorization = 'Bearer ' + token

    Given path 'recipes', 'mine'
    When method get
    Then status 200
    * def myRecipes = response._embedded.recipeDtoList
    And match myRecipes == '#[]' || myRecipes.length > 0
