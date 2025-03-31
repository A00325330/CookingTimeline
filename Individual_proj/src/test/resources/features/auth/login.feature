Feature: AuthController Login Endpoint

  Background:
    * url 'http://localhost:8081/api/auth'

  Scenario: Successful login for admin user
    Given path '/login'
    And request { email: 'admin@example.com', password: 'Admin123!' }
    When method post
    Then status 200
    And match response.token == '#string'

  Scenario: Successful login for regular user
    Given path '/login'
    And request { email: 'user@example.com', password: 'Admin123!' }
    When method post
    Then status 200
    And match response.token == '#string'

  Scenario: Login with invalid credentials
    Given path '/login'
    And request { email: 'invalid@example.com', password: 'Admin123!' }
    When method post
    Then status 401
	And match response == { token: 'Invalid email or password.' }
