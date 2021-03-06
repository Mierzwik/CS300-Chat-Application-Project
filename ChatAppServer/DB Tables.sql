create table "CHATSERVER".USERS
(
	USERID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	USERNAME VARCHAR(32) unique,
	PASSWORD VARCHAR(32),
	EMAIL VARCHAR(32) unique,
	FIRSTNAME VARCHAR(32),
	LASTNAME VARCHAR(32),
	GENDER NUMERIC,
	COUNTRY NUMERIC,
	DATE DATE,
	CONSTRAINT primary_key PRIMARY KEY (USERID)
)

create table "CHATSERVER".MESSAGELOGS
(
	MESSAGEID INTEGER not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,
	DATE VARCHAR(20),
	TIME VARCHAR(10),
	MESSAGE VARCHAR(1024),
	SENDERID INTEGER,
	RECIPIENTID INTEGER
)
