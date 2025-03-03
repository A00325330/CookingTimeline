Feature: Log File Download

	Background: 
		# Upload dataset
  	* call read('upload-file.feature')
  	# Login as admin
  	* call read('login-admin.feature')
  	* def adminResponse = call read('login-admin.feature')
    * def adminToken = adminResponse.token
    
  Scenario: Download skipped rows log file
    Given url baseUrl + '/logs/skipped_rows.log'
    * header Authorization = 'Bearer ' + adminToken
    When method get
    Then status 200
    And match responseHeaders['Content-Disposition'][0] contains 'attachment; filename=skipped_rows.log'
