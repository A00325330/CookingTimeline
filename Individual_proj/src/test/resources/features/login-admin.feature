Feature: Log in as ADMINISTRATOR

  Scenario: Authenticate Admin and Get Token
    Given url baseUrl + '/api/auth/login'
    * header Content-Type = 'application/json'
    And request { email: "admin@networksys.com", password: "Admin123!" }
    When method post
    Then status 200
    * def token = response.token
