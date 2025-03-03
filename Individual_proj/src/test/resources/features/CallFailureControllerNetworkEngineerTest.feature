Feature: Call Failure API Tests

  Background:
    * url baseUrl + '/api/call-failures'
    * def networkAuthResponse = call read('login-networkEng.feature')
    * def token = networkAuthResponse.token
    * header Authorization = 'Bearer ' + token
    * header Content-Type = 'application/json'

  Scenario: Get IMSIs with call failures
    Given path '/imsis'
    And param startTime = '2020-11-01 17:15:00'
    And param endTime = '2020-11-01 17:30:00'
    When method get
    Then status 200
    And match response.startTime == '2020-11-01 17:15:00'
    And match response.endTime == '2020-11-01 17:30:00'
    And match response.imsis == '#[]'  