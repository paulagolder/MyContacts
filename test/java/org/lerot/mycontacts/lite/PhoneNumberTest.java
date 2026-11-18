package org.lerot.mycontacts.lite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// The two ways a number is copied from the Contacts viewer
class PhoneNumberTest
{
	@Test
	void ukNumberStoredWithCountryCode()
	{
		PhoneNumber n = PhoneNumber.of("+441234567890");
		assertEquals("01234567890", n.uk());
		assertEquals("+441234567890", n.international());
	}

	@Test
	void ukNumberStoredWithLeadingZeroAndSpaces()
	{
		PhoneNumber n = PhoneNumber.of("01234 567890");
		assertEquals("01234567890", n.uk());
		assertEquals("+441234567890", n.international());
	}

	@Test
	void bracketedZeroAfterTheCountryCodeIsLeftOut()
	{
		assertEquals("+441234567890", PhoneNumber.of("+44 (0)1234 567-890").international());
	}

	@Test
	void numberWithNoCodeOrZeroIsTakenAsUk()
	{
		assertEquals("01234567890", PhoneNumber.of("1234567890").uk());
	}

	@Test
	void foreignNumberIsDialledWith00FromTheUk()
	{
		PhoneNumber n = PhoneNumber.of("+33 3 12 34 56 78");
		assertEquals("+33312345678", n.international());
		assertEquals("0033312345678", n.uk());
	}

	@Test
	void doubleZeroIsTheSameAsPlus()
	{
		assertEquals("+352123456", PhoneNumber.of("00352 123456").international());
	}

	@Test
	void whatsAppLinkHasTheNumberWithoutPlusAndTheGreeting()
	{
		assertEquals("https://web.whatsapp.com/send?phone=441234567890&text=Hi%20Zo%C3%AB%2C%20",
				PhoneNumber.of("01234 567890").whatsApp("Hi Zoë, ").toString());
	}

	@Test
	void whatsAppLinkWithoutAGreeting()
	{
		assertEquals("https://web.whatsapp.com/send?phone=33312345678",
				PhoneNumber.of("+33 3 12 34 56 78").whatsApp(null).toString());
	}

	@Test
	void trailingDotIsDropped()
	{
		assertEquals("01234567890", PhoneNumber.of("01234567890.").uk());
	}
}
