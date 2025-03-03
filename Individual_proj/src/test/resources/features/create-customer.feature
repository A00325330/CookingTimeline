Feature: Create a Network Engineer User

  Scenario: Create a Customer Service User as Admin
    * def adminResponse = call read('login-admin.feature')
    * def adminToken = adminResponse.token
    * print 'Using Admin Token:', adminToken

    Given url baseUrl + '/api/users'
    * header Authorization = 'Bearer ' + adminToken
    And request 
      """
      {
        "email": "customerservice@networksys.com",
        "password": "CustServ123!",
        "role": "CUSTOMER_SERVICE"
      }
      """
    When method post
    Then status 201
    * print 'Customer Service User Created:', response
