Feature: Get Call Failures by IMSI

  Background:
  	# Upload dataset
  	* call read('upload-file.feature')
    
    # Login as customer service
    * url baseUrl + '/api/call-failures'
    * def networkEngResponse = call read('login-customer.feature')
    * def token = networkEngResponse.token
    * header Authorization = 'Bearer ' + token
    * header Content-Type = 'application/json'

  Scenario: Retrieve Call Failures by IMSI
    Given path '/by-imsi'
    And param imsi = "344930000000011"
    When method get
    Then status 200
    And match response.imsi == 344930000000011
    And match response.failures[0].eventId == '#present'
    And match response.failures[0].causeCode == '#present'
    And match response.failures[0].description == '#present'
    And match response.failures[0].dateTime == '#present'
