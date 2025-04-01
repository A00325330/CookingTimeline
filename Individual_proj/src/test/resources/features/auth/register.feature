Feature: AuthController Register Endpoint

  Background:
    * def baseUrl = karate.properties['karate.baseUrl']
    * url baseUrl + '/api/auth'

  Scenario: Successful user registration
    Given path 'register'
    And request { email: 'aaron@example.com', password: 'Admin123!', role: 'USER' }
    When method post
    Then status 200
    And match response.message == 'User registered successfully!'

  Scenario: Registration with existing email
    Given path 'register'
    And request { email: 'user@example.com', password: 'Admin123!', role: 'USER' }
    When method post
    Then status 400
    And match response.message == 'Email is already taken.'
