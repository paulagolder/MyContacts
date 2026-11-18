package org.lerot.mycontacts.lite;

/*
 * A phone number written two ways, for copying:
 *   uk()             as dialled in the UK: 01234567890, or 0033... abroad
 *   international()  with the country code: +441234567890
 * Spaces, dots, dashes and brackets are dropped, and a "(0)" after the
 * country code (+44 (0)1234 ...) is left out. A number with no country
 * code is taken to be in the UK.
 */
public record PhoneNumber(String international)
{
	private static final String UK = "+44";

	public static PhoneNumber of(String stored)
	{
		String s = stored.trim().replaceAll("\\(0\\)", "");
		boolean plus = s.startsWith("+");
		String digits = s.replaceAll("\\D", "");
		if (plus) return new PhoneNumber("+" + digits);
		if (digits.startsWith("00")) return new PhoneNumber("+" + digits.substring(2));
		if (digits.startsWith("0")) return new PhoneNumber(UK + digits.substring(1));
		return new PhoneNumber(UK + digits);
	}

	public String uk()
	{
		if (international.startsWith(UK)) return "0" + international.substring(UK.length());
		return "00" + international.substring(1);
	}
}
