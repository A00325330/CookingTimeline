Feature: Retrieve Call Failure Summary by Phone Model

  Background:
    * url baseUrl + '/api/call-failures'
    * def networkAuthResponse = call read('login-networkEng.feature')
    * def token = networkAuthResponse.token
    * print 'Using Network Engineer Token:', token
    * header Authorization = 'Bearer ' + token
    * header Content-Type = 'application/json'

  Scenario: Retrieve Failure Summary by Phone Model
    Given path '/summary'
    And param phoneModel = 'VEA3'
    When method get
    Then status 200

    # Print the API response for debugging purposes
    * print 'API Response:', response

    # Match conditions
    And match response.phoneModel == 'VEA3'
    And match response.failures != null
    And match response.failures == '#[] #notempty'
    And match each response.failures[*].eventId == '#present'
    And match each response.failures[*].causeCode == '#present'
    And match each response.failures[*].occurrences == '#present'
