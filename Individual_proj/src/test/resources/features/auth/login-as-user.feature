Feature: Reusable login for normal user

Scenario: Authenticate and return token
  * def baseUrl = karate.properties['karate.baseUrl']
  * url baseUrl + '/api/auth'
  * path 'login'
  * request { email: 'user@example.com', password: 'Admin123!' }
  * method post
  * status 200
  * match response.token == '#string'
  * def token = response.token
