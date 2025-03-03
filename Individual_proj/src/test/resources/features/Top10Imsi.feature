Feature: Top 10 IMSIs with Call Failures API Test

  Background:
    * url baseUrl + '/api/call-failures'
    * def networkAuthResponse = call read('login-networkEng.feature')
    * def token = networkAuthResponse.token
    * header Authorization = 'Bearer ' + token
    * header Content-Type = 'application/json'

  Scenario: Get Top 10 IMSIs with Call Failures
    Given path '/top10-imsis'
    And param startDate = '2020-01-01T00:00'
    And param endDate = '2020-12-12T00:00'
    When method get
    Then status 200
    And match response == '#notnull'
    And match response == '#[]'