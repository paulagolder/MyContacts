package org.lerot.mycontacts;

import org.junit.jupiter.api.Test;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

// Reading a dropped email file (javax.mail), the same calls jswDropPane
// makes to find the sender and subject
class MailTest
{
	@Test
	void readsSenderAndSubjectOfAnEmailFile() throws Exception
	{
		String eml = String.join("\r\n",
				"From: Alice Smith <alice@example.com>",
				"To: me@example.com",
				"Subject: Christmas plans",
				"Date: Tue, 20 Oct 2026 10:00:00 +0100",
				"",
				"See you soon.", "");
		Session session = Session.getDefaultInstance(new Properties(), null);
		MimeMessage message = new MimeMessage(session,
				new ByteArrayInputStream(eml.getBytes(StandardCharsets.US_ASCII)));
		Address[] froms = message.getFrom();
		assertEquals("alice@example.com", ((InternetAddress) froms[0]).getAddress());
		assertEquals("Christmas plans", message.getSubject());
		assertNotNull(message.getSentDate());
	}
}
