-- Test database for MyContacts: the real schema and settings tables,
-- with made-up contacts. Generated from mcdb5_2024.sqlite (no personal data).

CREATE TABLE `attribute` (
  `attributeKey` varchar(100)  DEFAULT NULL,
  `attributeLabel` varchar(40)  NOT NULL,
  `dataType` varchar(40)  DEFAULT NULL,
  `displayOrder` int(11) NOT NULL,
  `group` varchar(20)  NOT NULL
, "displaygroups" VARCHAR);
INSERT INTO "attribute" VALUES ('address', 'Address', 'address', 20, 'all', 'EBSO');
INSERT INTO "attribute" VALUES ('birthday', 'Birthday', 'date', 80, 'all', 'EB');
INSERT INTO "attribute" VALUES ('department', 'Department', 'text', 17, 'all', 'EB');
INSERT INTO "attribute" VALUES ('email', 'Email', 'email', 61, 'all', 'EBS');
INSERT INTO "attribute" VALUES ('mobile', 'Mobile', 'cellphone', 42, 'all', 'EBS');
INSERT INTO "attribute" VALUES ('name', 'Name', 'name', 1, 'all', 'E');
INSERT INTO "attribute" VALUES ('phone', 'Phone', 'phone', 40, 'all', 'EBS');
INSERT INTO "attribute" VALUES ('photo', 'Photo', 'image', 0, 'all', 'E');
INSERT INTO "attribute" VALUES ('position', 'Position', 'text', 16, 'all', 'EBS');
INSERT INTO "attribute" VALUES ('tags', 'Tags', 'textlist', 90, 'all', 'EB');
INSERT INTO "attribute" VALUES ('webpage', 'Webpage', 'http', 70, 'all', 'EB');
INSERT INTO "attribute" VALUES ('fax', 'Fax', 'phone', 20, 'all', 'EBS');
INSERT INTO "attribute" VALUES ('anniversary', 'Anniversary', 'date', 81, 'all', 'EB');
INSERT INTO "attribute" VALUES ('note', 'Note', 'textlist', 90, 'all', 'EB');
INSERT INTO "attribute" VALUES ('key', 'Key', 'text', 90, 'all', 'EB');
INSERT INTO "attribute" VALUES ('kind', 'Kind', 'text', 90, 'all', 'E');
INSERT INTO "attribute" VALUES ('default', 'Default', 'text', 90, 'all', 'EB');
INSERT INTO "attribute" VALUES ('tid', 'TID', 'text', 90, 'all', 'E');
INSERT INTO "attribute" VALUES ('memberof', 'MemberOf', 'link', 101, 'all', NULL);
INSERT INTO "attribute" VALUES ('hasmember', 'HasMember', 'link', 101, 'all', NULL);
INSERT INTO "attribute" VALUES ('related', 'IsRelatdTo', 'link', 101, 'all', ' ');

CREATE TABLE "attributeValues" ("cid" INT,"root" TEXT DEFAULT (null) ,"qualifier" TEXT DEFAULT (null) ,"value" TEXT DEFAULT (null) ,"country" TEXT,"update_dt" NUM DEFAULT (CURRENT_TIMESTAMP) , "user" VARCHAR);

CREATE TABLE "correspondance" (
	"cid"	INTEGER,
	"date"	INTEGER,
	"path"	TEXT,
	"status"	TEXT,
	"subject"	TEXT,
	PRIMARY KEY("cid" AUTOINCREMENT)
);

CREATE TABLE `dataType` (
  `Key` varchar(100)  DEFAULT NULL,
  `Type` varchar(40)  NOT NULL,
  `format` varchar(40)  NOT NULL
);
INSERT INTO "dataType" VALUES ('address', 'address', '');
INSERT INTO "dataType" VALUES ('cellphone', 'cellphone', '');
INSERT INTO "dataType" VALUES ('date', 'date', '');
INSERT INTO "dataType" VALUES ('email', 'email', '');
INSERT INTO "dataType" VALUES ('http', 'http', '');
INSERT INTO "dataType" VALUES ('image', 'image', '');
INSERT INTO "dataType" VALUES ('name', 'name', '');
INSERT INTO "dataType" VALUES ('phone', 'phone', '');
INSERT INTO "dataType" VALUES ('text', 'text', '');
INSERT INTO "dataType" VALUES ('textlist', 'textlist', '');
INSERT INTO "dataType" VALUES ('cid', 'cid', '');
INSERT INTO "dataType" VALUES ('link', 'text', ' ');

CREATE TABLE `dataTypeFields` (
  `typeKey` varchar(100)  DEFAULT NULL,
  `fieldKey` varchar(100)  DEFAULT NULL,
  `fieldLabel` varchar(20)  NOT NULL,
  `displayOrder` int(11) NOT NULL,
  `type` varchar(20)  NOT NULL
);
INSERT INTO "dataTypeFields" VALUES ('address', 'building', 'Building', 1, '');
INSERT INTO "dataTypeFields" VALUES ('address', 'city', 'City', 4, '');
INSERT INTO "dataTypeFields" VALUES ('address', 'country', 'Country', 7, '');
INSERT INTO "dataTypeFields" VALUES ('address', 'county', 'County', 6, '');
INSERT INTO "dataTypeFields" VALUES ('address', 'locality', 'Locality', 3, '');
INSERT INTO "dataTypeFields" VALUES ('address', 'postcode', 'Postcode', 5, '');
INSERT INTO "dataTypeFields" VALUES ('address', 'street', 'Street', 2, '');
INSERT INTO "dataTypeFields" VALUES ('date', 'day', 'Day', 3, '');
INSERT INTO "dataTypeFields" VALUES ('date', 'month', 'Month', 2, '');
INSERT INTO "dataTypeFields" VALUES ('date', 'year', 'Year', 1, '');
INSERT INTO "dataTypeFields" VALUES ('name', 'fn', 'Personal Name', 2, '');
INSERT INTO "dataTypeFields" VALUES ('name', 'mn', 'Middle Name', 3, '');
INSERT INTO "dataTypeFields" VALUES ('name', 'sn', 'Family Name', 4, '');
INSERT INTO "dataTypeFields" VALUES ('name', 'title', 'Title', 1, '');

CREATE TABLE `importDataTable` (
  `ROWNUMBER` INTEGER NOT NULL,
   `root` varchar(200)  NOT NULL,
  `qualifier` varchar(200)  NOT NULL,
  `value` text   DEFAULT NULL,
  "importstatus" VARCHAR DEFAULT null);

CREATE TABLE "labelMappings" ("direction" , "mapping" , "foriegnLabel" , "localLabel" , "sequence" );
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Additional Name', 'name(mn)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Additional Name Yomi', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Address 1 - City', 'address(city)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Address 1 - Country', 'address(country)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Address 1 - Extended Address', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Address 1 - Formatted', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Address 1 - PO Box', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Address 1 - Postal Code', 'address(postcode)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Address 1 - Region', 'address(county)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Address 1 - Street', 'address(street)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Address 1 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Billing Information', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Birthday', 'birthday', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Custom Field 1 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Custom Field 1 - Value', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Custom Field 2 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Custom Field 2 - Value', 'phone/alt', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Custom Field 3 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Custom Field 3 - Value', 'email/alt', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Custom Field 4 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Custom Field 4 - Value', 'address/alt', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Directory Server', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'E-mail 1 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'E-mail 1 - Value', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'E-mail 2 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'E-mail 2 - Value', 'email/alt', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Family Name', 'name(sn)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Family Name Yomi', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Given Name', 'name(fn)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Given Name Yomi', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Group Membership', 'tags', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Hobby', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Maiden Name', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Mileage', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Name', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Name', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Name Prefix', 'name(title)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Name Suffix', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Nickname', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Notes', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Occupation', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Organization 1 - Department', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Organization 1 - Job Description', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Organization 1 - Location', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Organization 1 - Name', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Organization 1 - Symbol', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Organization 1 - Title', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Organization 1 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Organization 1 - Yomi Name', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Phone 1 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Phone 1 - Value', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Phone 2 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Phone 2 - Value', 'mobile', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Phone 3 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Phone 3 - Value', 'phone/work', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Sensitivity', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Short Name', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Subject', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Website 1 - Type', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Website 1 - Value', 'webpage', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gGoogleExport', 'Yomi Name', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Account', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Anniversary', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Assistant''s Name', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Assistant''s Phone', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Birth Day', 'birthday(day)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Birth Month', 'birthday(month)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Birth Year', 'birthday(year)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Birthday', 'birthday', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Address', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Address PO Box', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business City', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Country', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Fax', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Phone', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Phone 2', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Postal Code', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business State', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Street', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Street 2', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Business Street 3', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Categories', 'tags', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Children', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Company', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Company Main Phone', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Custom 1', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Custom 2', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Custom 3', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Custom 4', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Department', 'department', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Display Name', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'E-mail 2 Address', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'E-mail 3 Address', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'E-mail Address', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Fax Number', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'FIELD38', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'First Name', 'name(fn)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Gender', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Address', 'address(street)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Address 2', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Address PO Box', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home City', 'address(city)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Country', 'address(country)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home County', 'address(county)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Fax', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Phone', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Phone 2', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Post Code', 'address(postcode)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Postal Code', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home State', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Street', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Street 2', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Home Street 3', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Initials', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Internet Free Busy', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Job Title', 'position', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Language', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Last Name', 'name(sn)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Location', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Manager''s Name', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Middle Name', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Mobile Number', 'mobile', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Mobile Phone', 'mobile', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Nickname', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Notes', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Office Location', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Organisation', 'organisation', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Organizational ID Number', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Other Fax', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Other Phone', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Pager', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Pager Number', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Primary Email', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Primary Phone', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Priority', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Profession', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Referred By', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Screen Name', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Secondary Email', 'email/alt', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Spouse', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Suffix', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Title', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Web Page', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Web Page 1', 'webpage', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Web Page 2', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Work Address', 'address(street)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Work Address 2', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Work City', 'address(city)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Work Country', 'address(country)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Work County', 'address(county)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Work Phone', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'gOutlookExport', 'Work Post Code', 'address(postcode)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', '0', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'Address home', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'birthyear', 'birthday(year)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'description', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'dn', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'E-mail', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'givenName', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'Home County', 'address(county)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'Home Post Code', 'address(postcode)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'homePhone', 'phone/home', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mail', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mobile', 'mobile', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mozillaCustom1', 'birthday', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mozillaHomeCountryName', 'address(country)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mozillaHomeLocalityName', 'address(city)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mozillaHomePostalCode', 'address(postcode)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mozillaHomeState', 'address(county)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mozillaHomeStreet', 'address(street)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mozillaNickname', 'name/alt', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mozillaSecondEmail', 'email/alt', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'mozillaWorkUrl', 'webpage', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'Phone Home', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'Phone mobile', 'mobile', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'sn', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'street', 'address(street)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'telephoneNumber', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'title', 'name(title)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'ADR', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'ADR;HOME', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'ADR;WORK', 'address/work', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'BDAY', 'birthday', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'EMAIL', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'EMAIL;HOME', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'EMAIL;INTERNET', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'EMAIL;INTERNET;PREF', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'EMAIL;PREF', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'EMAIL;PREF;HOME', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'EMAIL;PREF;X-Custom', 'email', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'EMAIL;X-Obsolete', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'FN', 'tid', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'N', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'NOTE', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'PHOTO', 'photo', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL;CELL', 'mobile', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL;CELL;PREF', 'mobile', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL;CELL;PREF;HOME', 'mobile', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL;CELL;PREF;X-Custom', 'mobile', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL;HOME', 'phone/home', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL;PREF', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL;VOICE', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL;VOICE;WORK', 'phone/work', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'TEL;WORK', 'phone/work', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'VERSION', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'X-ANDROID-CUSTOM', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'X-IRMC-LUID', 'key', NULL);
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'note', 'description', '09');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'address', 'Address home', '06');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'name', 'cn', '01');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'email', 'E-mail', '02');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'phone/home', 'homePhone', '05');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'email', 'mail', '03');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'mobile', 'mobile', '04');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'birthday', 'mozillaCustom1', '06');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'name/alt', 'mozillaNickname', '01');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'email/alt', 'mozillaSecondEmail', '02');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'webpage', 'mozillaWorkUrl', '07');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'phone', 'Phone Home', '01');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'mobile', 'Phone mobile', '01');
INSERT INTO "labelMappings" VALUES ('export', 'Ldif', 'phone', 'telephoneNumber', '03');
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'comment', 'note', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'phone', 'phone', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'address', 'address', NULL);
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'name', 'N', '01');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'key', 'UID', '01');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'email', 'EMAIL', '02');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'phone', 'TEL', '03');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'mobile', 'TEL;TYPE=CELL', '04');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'address', 'ADR', '07');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'birthday', 'BDAY', '08');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'note', 'NOTE', '08');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'photo', 'PHOTO;TYPE=JPEG;ENCODING=b', 10);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'PHOTO;JPEG', 'photo', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'Work Post Code', 'address(postcode)', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'objectClass', 'ignore', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'displayName', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Ldif', 'cn', 'name', NULL);
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'tags', 'CATEGORIES', '09');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'note', 'note', '09');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'address', 'address', '06');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'name', 'name', '01');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'email', 'email', '02');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'phone/home', 'phone/home', '05');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'mobile', 'mobile', '04');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'birthday', 'birthday', '06');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'name/alt', 'name/alt', '01');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'email/alt', 'email/alt', '02');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'phone', 'phone', '01');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'webpage', 'webpage', '07');
INSERT INTO "labelMappings" VALUES ('export', 'XML', 'tags', 'tags', '07');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'note', 'note', '09');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'address', 'address', '06');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'name', 'name', '01');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'email', 'email', '02');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'phone/home', 'phone/home', '05');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'mobile', 'mobile', '04');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'birthday', 'birthday', '06');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'name/alt', 'name/alt', '01');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'email/alt', 'email/alt', '02');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'phone', 'phone', '01');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'webpage', 'webpage', '07');
INSERT INTO "labelMappings" VALUES ('export', 'CSV', 'tags', 'tags', '07');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'anniversary', 'ANNIVERSARY', '08');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'position', 'TITLE', '015');
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'CATEGORIES', 'tags', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'KIND', 'kind', NULL);
INSERT INTO "labelMappings" VALUES ('import', 'Vcard', 'RELATED', 'related', NULL);
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'kind', 'KIND', '08');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'related', 'RELATED', '08');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'fax', 'FAX', '02');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'org', 'ORG', '08');
INSERT INTO "labelMappings" VALUES ('export', 'Vcard', 'member', 'MEMBER', '08');

CREATE TABLE "parameters" ("key" VARCHAR NOT NULL , "value" VARCHAR NOT NULL );
INSERT INTO "parameters" VALUES ('Title', 'Users Contacts');

CREATE TABLE `user` (
  `userid` varchar(10)  NOT NULL DEFAULT '',
  `username` varchar(30)  DEFAULT NULL,
  `password` varchar(32)  DEFAULT NULL,
  `forename` varchar(20)  DEFAULT NULL,
  `surname` varchar(30)  DEFAULT NULL,
  `email` varchar(40)  DEFAULT NULL,
  `role` varchar(20)  DEFAULT NULL
);

CREATE VIEW proup AS SELECT  DISTINCT  t1.cid, t1.value as 'tid' , t2.value as 'type' FROM attributeValues as t1  , attributeValues as t2 where t1.cid = t2.cid  and t1.root = 'tid'  and t2.root= 'kind';

-- made-up contacts
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'tid', '', 'Smith  Alice', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'name', '', '{ fn:''Alice'' , sn:''Smith'' ,  }', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'tags', '', 'friends;', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'email', '', 'alice@example.com', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (1, 'address', '', '{ street:''1 High Street'' , city:''Testtown'' , postcode:''TT1 1AA'' ,  }', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (2, 'tid', '', 'O''Brien  Bob', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (2, 'name', '', '{ fn:''Bob'' , sn:''O''''Brien'' ,  }', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (2, 'tags', '', 'family;', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (3, 'tid', '', 'Jones  Carol', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (3, 'name', '', '{ fn:''Carol'' , sn:''Jones'' ,  }', '2026-01-01 12:00:00');
INSERT INTO attributeValues(cid, root, qualifier, value, update_dt) VALUES (3, 'tags', '', 'work;', '2026-01-01 12:00:00');
