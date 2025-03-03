Feature: Delete User API Test

  Background: Authenticate as admin
    * def adminAuthResponse = call read('login-admin.feature')  
    * def adminToken = adminAuthResponse.token
    * header Authorization = 'Bearer ' + adminToken
    * header Content-Type = 'application/json'
    
    # Create the user to be deleted
    Given url baseUrl + '/api/users'
    And request
    """
    {
      "email": "userDeleteTest@networksys.com",
      "password": "Password123!",
      "role": "CUSTOMER_SERVICE"
    }
    """
    When method POST
    Then status 201
    
  Scenario: Delete User
    # Delete the user
    Given url baseUrl + '/api/users/userDeleteTest@networksys.com'
    * header Authorization = 'Bearer ' + adminToken
    * header Content-Type = 'application/json'
    When method delete
    Then status 200
    And match response == "User successfully deleted."
