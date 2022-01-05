// username and password from the named credential
private String endpoint = 'callout:RVA_Skyward';
private String uName = '{!$Credential.UserName}';
private String pWord = '{!$Credential.Password}';
private String AUTH_TOKEN = getAuthToken();

private String SCHOOL_ID = '800';

// private String studentsEndpoint = endpoint + '/v1/students';
private String schoolsEndpoint = endpoint + '/v1/schools';

// GET sample data from the API using the token
HttpRequest schoolsReq = new HttpRequest();
schoolsReq.setMethod('GET');
schoolsReq.setEndpoint(schoolsEndpoint);
schoolsReq.setHeader('Authorization', 'Bearer ' + AUTH_TOKEN);
schoolsReq.setHeader('Content-Type', 'application/json');

// Parse the returned body as a list of objects
HttpResponse schoolsRes = new Http().send(schoolsReq);
List<valence.JSONParse> schoolDataRoot = new valence.JSONParse(schoolsRes.getBody()).asList();
List<String> fields = new List<String>(schoolDataRoot[0].asMap().keySet());
   
// send an email with the .csv file
String csvName = 'RVA Schools.csv';
String dataString = createCsvString(fields, schoolDataRoot);
String subject = 'RVA Schools data from the API';
List<String> toAddresses = new List<String> {'james.pier@servioconsulting.com'};
    
sendCsvEmail(csvName, dataString, subject, toAddresses);

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

// create .csv headers and data from the fields/keys
private String createCsvString(List<String> fields, List<valence.JSONParse> data) {

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
private void sendCsvEmail(String csvName, String csvData, String emailSubject, List<String> toAddresses) {
    
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


