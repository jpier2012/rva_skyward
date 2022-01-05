// username and password from the named credential
private String endpoint = 'callout:RVA_Skyward';
private String uName = '{!$Credential.UserName}';
private String pWord = '{!$Credential.Password}';
private String AUTH_TOKEN = getAuthToken();

private String SCHOOL_ID = '800';

private String schoolsEndpoint = endpoint + '/v1/schools';
private String guardiansEndpoint = endpoint + '/v1/schools/' + SCHOOL_ID + '/guardians';

// list of endpoints from which to get data
private List<String> dataEndpoints = new List<String> { schoolsEndpoint, guardiansEndpoint };
    
// recipient email addresses for the .csv email
private List<String> toAddresses = new List<String> {'james.pier@servioconsulting.com'};

/////////////////
//// Schools ////
/////////////////

// get the JSON data root from the schools endpoint
private List<valence.JSONParse> schoolsDataRoot = getJsonData(schoolsEndpoint);
    
sendCsvEmail('RVA schools data from the API', 'RVA schools.csv', createCsvString(schoolsDataRoot), toAddresses);


/////////////////////////////////////////////////
//////////////////  Helpers  ////////////////////
/////////////////////////////////////////////////

// Return the token from the token endpoint

private String getAuthToken() {
    
    // send the HTTP request to the token endpoint with the username and password credentials 
    // in the request body
    String reqBody = 'grant_type=password&username=' + uName + '&password=' + pWord; 
	String tokenEndpoint = endpoint + '/token';
    
    HttpRequest req = new HttpRequest();
    req.setMethod('POST');
    req.setEndpoint(tokenEndpoint);
    req.setBody(reqBody);
    req.setHeader('Content-Type', 'application/x-www-form-urlencoded');
    req.setHeader('Content-Length', '\'reqBody.length()\'');
    
    // Parse the response into JSON
    HttpResponse res = new Http().send(req);
    valence.JSONParse root = new valence.JSONParse(res.getBody());
    
    // assign the auth token to authToken
    return root.get('access_token').getStringValue();
}

// GET data from the endpoint using the AUTH_TOKEN variable

private List<valence.JSONParse> getJsonData(String endpoint) {
    HttpRequest req = new HttpRequest();
    req.setMethod('GET');
    req.setEndpoint(endpoint);
    req.setHeader('Authorization', 'Bearer ' + AUTH_TOKEN);
    req.setHeader('Content-Type', 'application/json');
    
    // Parse the returned body as a list of objects
    HttpResponse res = new Http().send(req);
    return new valence.JSONParse(res.getBody()).asList();
}

// create .csv headers and data from the fields/keys

private String createCsvString(List<valence.JSONParse> data) {

	// pull the first record in the data to get the field names
	List<String> fields = new List<String>(data[0].asMap().keySet());

	String header;
    String recordString = '';
    String dataString = '';

    for (valence.JSONParse school : data) {
        header = '';
        
        // to avoid a 2nd for loop just to get the field headers, I set the headers during each loop through the data values
        // I set header = '' at the start of each school loop so the headers will only be retained in the last run of the loop
        // probably a better way to do this but I think performance impacts are negligible 
        
        for (String field : fields) {
    		header = header + field + ',';
            recordString = recordString + school.get(field).getStringValue() + ',';
        }
        dataString = dataString + recordString + '\n';
        recordString = '';
    }

	dataString = header + '\n' + dataString;
    
    return dataString;
}

// send the .csv file in an email attachment

private void sendCsvEmail(String emailSubject, String csvName, String csvData, List<String> toAddresses) {
    // create a blob from the csv data string
    Blob csvBlob = Blob.valueOf(csvData);
    
    // create attachment
    Messaging.EmailFileAttachment csvAttachment = new Messaging.EmailFileAttachment();
    csvAttachment.setFileName(csvName);
    csvAttachment.setBody(csvBlob);
    
    Messaging.SingleEmailMessage email = new Messaging.SingleEmailMessage();
    email.setSubject(emailSubject);
    email.setToAddresses(toAddresses);
    email.setPlainTextBody('');
    email.setFileAttachments(new Messaging.EmailFileAttachment[] { csvAttachment } );
    Messaging.SendEmailResult[] result = Messaging.sendEmail(new Messaging.SingleEmailMessage[] {email});
}


