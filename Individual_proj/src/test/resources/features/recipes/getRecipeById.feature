Feature: Get recipe by ID

  Background:
    * def baseUrl = karate.properties['karate.baseUrl']
    * url baseUrl + '/api'
    * def login = call read('classpath:features/auth/login-as-user.feature')
    * def token = login.token
    * header Authorization = 'Bearer ' + token

  Scenario: Successfully get recipe by ID
    Given path 'recipes', 1
    When method get
    Then status 200
    And match response.name == 'Classic Chicken Curry'

  Scenario: Attempt to get a recipe the user doesn't own
    Given path 'recipes', 999
    When method get
    Then status 404
