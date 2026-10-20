package org.lerot.mycontacts;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/*
 * Where MyContacts keeps its settings and database, for any program that
 * needs them (the main app and myContactsLite).
 */
public class mcSettings
{
	// the settings folder, ~/.mccontacts/
	public static String folder()
	{
		return System.getProperty("user.home").replace('\\', '/') + "/.mccontacts/";
	}

	// properties.xml in the settings folder; empty if it is missing or unreadable
	public static Properties properties()
	{
		Properties prop = new Properties();
		String propsfile = folder() + "properties.xml";
		try (FileInputStream in = new FileInputStream(propsfile))
		{
			prop.loadFromXML(in);
		} catch (FileNotFoundException e)
		{
			System.err.println("No properties file " + propsfile + ", using defaults");
		} catch (IOException e)
		{
			System.err.println("Cannot read properties " + propsfile + " : " + e.getMessage()
					+ ", using defaults");
		}
		return prop;
	}

	// the contacts database chosen in properties.xml
	public static String databasePath()
	{
		return folder() + properties().getProperty("database", "mcdb.sqlite");
	}
}
