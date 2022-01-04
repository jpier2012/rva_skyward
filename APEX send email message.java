String header = 'SchoolId, SchoolName, CurrentSchoolYear, GradeLow, GradeHigh, StreetAddress, City, State, ZipCode, OneLineAddress, PrincipalNameId, \n';

String recordString = 'test, test, 	test, 	test, 	test, 	test, 	test, 	test, 	test, 	test, 	test, \n';
String finalString = header + recordString;
String csvName = 'test.csv';
String subject = 'Test.csv';

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