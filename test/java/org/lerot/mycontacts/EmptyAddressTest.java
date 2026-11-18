package org.lerot.mycontacts;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

// An address saved with every field blank is stored as "{ }"
class EmptyAddressTest
{
	@Test
	void emptyAddressIsEmptyWithoutComplaint()
	{
		PrintStream console = System.out;
		ByteArrayOutputStream printed = new ByteArrayOutputStream();
		System.setOut(new PrintStream(printed));
		try
		{
			assertTrue(new mcAddressDataType().getValueMap("{ }").isEmpty());
		} finally
		{
			System.setOut(console);
		}
		assertEquals("", printed.toString());
	}
}
