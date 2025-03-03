Feature: Upload File

  Background:
  	# Log in as Admin
    * def adminAuthResponse = call read('login-admin.feature')
    * def token = adminAuthResponse.token
    * def baseUrl = karate.get('baseUrl')
    * header Authorization = 'Bearer ' + token
    * header Content-Type = 'application/json'
    
  Scenario: Upload dataset
    Given url baseUrl + '/import/Test%20Sample%20Dataset%20-%20Errors.xlsx'
    When method POST
    Then status 200