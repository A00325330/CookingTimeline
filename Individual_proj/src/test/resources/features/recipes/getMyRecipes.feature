Feature: Get recipes of the authenticated user

  Scenario: Get my recipes using valid token
    * def baseUrl = karate.properties['karate.baseUrl']
    * url baseUrl + '/api'
    * def login = call read('classpath:features/auth/login-as-user.feature')
    * def token = login.token
    * header Authorization = 'Bearer ' + token

    Given path 'recipes', 'mine'
    When method get
    Then status 200

    * def myRecipes = response._embedded.recipeDtoList
    * match myRecipes == '#[]' || myRecipes.length > 0
