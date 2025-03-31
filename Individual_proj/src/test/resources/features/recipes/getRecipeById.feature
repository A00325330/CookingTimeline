Feature: Get recipe by ID

  Background:
    * url 'http://localhost:8081/api'
    * def login = call read('classpath:features/auth/login-as-user.feature')
    * def token = login.token
    * header Authorization = 'Bearer ' + token

  Scenario: Successfully get recipe by ID
    Given path 'recipes', 1
    When method get
    Then status 200
    And match response.name == 'Classic Chicken Curry'

  Scenario: Get a recipe that is forbidden
    Given path 'recipes', 999
    When method get
    Then status 403
