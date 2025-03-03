Feature: Create a Network Engineer User
 
  Background: Login as admin
    * def adminResponse = call read('login-admin.feature')
    * def adminToken = adminResponse.token
 	
 	Scenario: Create a Network Engineer User as Admin
    Given url baseUrl + '/api/users'
    * header Authorization = 'Bearer ' + adminToken
    And request
      """
      {
        "email": "networkEng@networksys.com",
        "password": "NetworkEng123!",
        "role": "NETWORK_MANAGEMENT_ENGINEER"
      }
      """
    When method post
    Then assert responseStatus == 201