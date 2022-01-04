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
valence.JSONParse dataRoot = new valence.JSONParse(schoolsRes.getBody());

// get the specific District Name and District Code values from the parsed JSON
// String districtName = testRoot.get('DistrictName').getStringValue();
// String districtCode = testRoot.get('DistrictCode').getStringValue();

// test print
// System.debug(dataRoot);
System.debug(dataRoot.toStringPretty());

// System.debug('Object? : ' + testRoot.isObject());
// System.debug('Array? : ' + testRoot.isArray());

// System.debug(districtName);
// System.debug(districtCode);



SchoolId,
SchoolName,
CurrentSchoolYear,
GradeLow,
GradeHigh,
StreetAddress,
City,
State,
ZipCode,
OneLineAddress,
PrincipalNameId

