Feature: Log in as CUSTOMER_SERVICE
	Background: Ensure Network Engineer Account exists
    * def adminResponse = call read('login-admin.feature')
    * def adminToken = adminResponse.token
 
    Given url baseUrl + '/api/users'
    * header Authorization = 'Bearer ' + adminToken
    And request
    """
    {
      "email": "customerService@networksys.com",
      "password": "CustServ123!",
      "role": "CUSTOMER_SERVICE"
    }
    """
    When method post
    Then assert responseStatus == 201
    
  Scenario: Authenticate Customer Service User and Get Token
    Given url baseUrl + '/api/auth/login'
    * header Content-Type = 'application/json'
    And request { email: "customerservice@networksys.com", password: "CustServ123!" }
    When method post
    Then status 200
    * print 'Customer Service Login Response:', response
    * def token = response.token
