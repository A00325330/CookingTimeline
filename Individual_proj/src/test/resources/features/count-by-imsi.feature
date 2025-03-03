Feature: Retrieve Call Failure Stats by IMSI
  
	  Background:
	    * url baseUrl + '/api/call-failures'
	    * def networkAuthResponse = call read('login-support-engineer.feature')
	    * def token = networkAuthResponse.token
	    * header Authorization = 'Bearer ' + token
	    * header Content-Type = 'application/json'
	
	  Scenario: Retrieve Call Failure Stats by IMSI
	    Given path '/count-by-imsi'
	    And param startDate = '2020-01-11 17:15:00'
	    And param endDate = '2020-01-11 17:30:00'
	    When method get
	    Then status 200
	
	    # Print the full API response to debug the date format
	    * print 'API Response:', response
	
	    # Match conditions
	    And match response.startDate == '2020-01-11 17:15:00'
	    And match response.endDate == '2020-01-11 17:30:00'
	    And match response.failureStats == '#notnull'
	    And match response.failureStats == '#[] #notempty'
	    And match each response.failureStats[*].imsi == '#present'
	    And match each response.failureStats[*].failureCount == '#present'
	    And match each response.failureStats[*].totalDuration == '#present'
