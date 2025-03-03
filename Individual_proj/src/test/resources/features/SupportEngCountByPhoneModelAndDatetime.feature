Feature: Call Failure count by Phone Model and Datetime range
  Background:
  	# upload dataset
  	* call read('upload-file.feature')
  	
  	# login as network engineer
    * url baseUrl + '/api/call-failures'
    * def networkAuthResponse = call read('login-support-engineer.feature')
    * def token = networkAuthResponse.token
    * header Authorization = 'Bearer ' + token
    * header Content-Type = 'application/json'

  Scenario: Get call failure count for a phone model
    Given path '/count'
    And param phoneModel = 'VEA3'
    And param startDate = '2020-11-01 17:15:00'
    And param endDate = '2020-11-01 17:30:00'
    When method get
    Then status 200
    