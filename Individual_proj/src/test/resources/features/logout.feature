Feature: Log Out Any User

  Scenario: Log out any active user
    * def authResponse = call read('login-admin.feature')  # Try logging in as Admin
    * def token = authResponse.token

    # If Admin Login fails, try Customer Service Login
    * if (!token) 
      * def authResponse = call read('login-customer.feature')
      * def token = authResponse.token

    # If Customer Service Login fails, try Network Engineer Login
    * if (!token) 
      * def authResponse = call read('login-network-engineer.feature')
      * def token = authResponse.token

    * print 'Logging out with token:', token
    * if (token) 
      Given url baseUrl + '/api/auth/logout'
      * header Authorization = 'Bearer ' + token
      When method post
      Then status 200
      * print 'User Logged Out Successfully'
