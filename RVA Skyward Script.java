String uName = '{!$Credential.UserName}';
String pWord = '{!$Credential.Password}';
String reqBody = 'grant_type=password&username=' + uName + '&password=' + pWord;
String endpoint = 'callout:RVA_Skyward';
String tokenEndpoint = endpoint + '/token';
    
String authToken;
String studentsEndpoint = endpoint + '/v1/students';
String schoolsEndpoint = endpoint + '/v1/schools';
    
// Request token from the token endpoint

// send the HTTP request to the token endpoint with the username and password credentials 
// in the BODY of the request
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
authToken = root.get('access_token').getStringValue();

// GET sample data from the API using the token
HttpRequest schoolsReq = new HttpRequest();
schoolsReq.setMethod('GET');
schoolsReq.setEndpoint(schoolsEndpoint);
schoolsReq.setHeader('Authorization', 'Bearer ' + authToken);
schoolsReq.setHeader('Content-Type', 'application/json');

// Take the disctrict JSON object and pull out values
HttpResponse schoolsRes = new Http().send(schoolsReq);
List<valence.JSONParse> schoolDataRoot = new valence.JSONParse(schoolsRes.getBody()).asList();
Map<String, valence.JSONParse> firstRecord = new valence.JSONParse(schoolsRes.getBody()).get('[0]').asMap();
List<String> fields = new List<String>(firstRecord.keySet());

String header = '';

for (String item : fields ) {
    header = header + item + ',';
}

header = header + '\n';

// System.debug('Object? : ' + testRoot.isObject());
// System.debug('Array? : ' + testRoot.isArray());

String csvName = 'RVA Schools.csv';
String subject = 'RVA Schools data from the API';
String recordString = '';
String finalString = '';

for (valence.JSONParse school : schoolDataRoot) {
    for (String field : fields) {
        recordString = recordString + school.get(field).getStringValue() + ',';
    }
    finalString = finalString + recordString + '\n';
    recordString = '';
}

finalString = header + finalString;
    
// send an email with the .csv file

List<String> toAddresses = new List<String> {'james.pier@servioconsulting.com'};

Blob csvBlob = Blob.valueOf(finalString);

Messaging.EmailFileAttachment csvAttachment = new Messaging.EmailFileAttachment();

csvAttachment.setFileName(csvName);
csvAttachment.setBody(csvBlob);
Messaging.SingleEmailMessage email = new Messaging.SingleEmailMessage();

email.setSubject(subject);
email.setToAddresses(toAddresses);
email.setPlainTextBody('');
email.setFileAttachments(new Messaging.EmailFileAttachment[] { csvAttachment } );
Messaging.SendEmailResult[] result = Messaging.sendEmail(new Messaging.SingleEmailMessage[] {email});

