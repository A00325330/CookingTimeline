Feature: Update User API Test

  Background: Ensure user exists before updating
    # Login as admin
    * def adminAuthResponse = call read('login-admin.feature')  
    * def adminToken = adminAuthResponse.token
    * header Authorization = 'Bearer ' + adminToken
    * header Content-Type = 'application/json'
    
    # Create the user to be updated
    Given url baseUrl + '/api/users'
    And request
    """
    {
      "email": "userUpdateTest@networksys.com",
      "password": "Password123!",
      "role": "CUSTOMER_SERVICE"
    }
    """
    When method POST
    Then status 201
    
  Scenario: Update the user
    Given url baseUrl + '/api/users/userUpdateTest@networksys.com'
    And request
    """
    {
      "email": "userUpdateTest@networksys.com",
      "password": "NewPass123!",
      "role": "CUSTOMER_SERVICE"
    }
    """
    * header Authorization = 'Bearer ' + adminToken
    * header Content-Type = 'application/json'
    When method put
    Then status 200
    And match response == "User successfully updated."
