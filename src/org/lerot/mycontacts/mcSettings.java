package org.lerot.mycontacts;

import java.io.File;
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

	// letters, notes and labels, ~/Documents/MyCorrespondance unless properties.xml
	// names another "correspondence" folder (relative to the home folder)
	public static String correspondenceFolder()
	{
		String home = System.getProperty("user.home").replace('\\', '/');
		String dir = properties().getProperty("correspondence", "Documents/MyCorrespondance");
		if (!new File(dir).isAbsolute()) dir = home + "/" + dir;
		return dir.replaceAll("/+$", "");
	}

	// all labels in one folder, named to sort above the contact folders
	public static String labelsFolder()
	{
		return correspondenceFolder() + "/AllLabels";
	}

	// exported vCards, next to the labels
	public static String vcardsFolder()
	{
		return correspondenceFolder() + "/AllVcards";
	}

	// the folders for the whole program (MyContacts.docsfolder and letterfolder),
	// and the shared ones made if missing; contact folders are made when needed
	public static void setUpFolders()
	{
		MyContacts.docsfolder = correspondenceFolder();
		MyContacts.letterfolder = labelsFolder();
		for (String dir : new String[] { labelsFolder(), vcardsFolder() })
			if (!new File(dir).isDirectory() && !new File(dir).mkdirs())
				System.err.println("Cannot create folder " + dir);
	}

	// a contact's name with no spaces or punctuation, e.g. GolderPaulRandRachel
	public static String fileName(mcContact contact)
	{
		String name = contact.getName("sn fn").replaceAll("[^\\p{L}\\p{N}_-]", "");
		if (name.isEmpty()) name = "contact" + contact.getID();
		return name;
	}

	// a contact's folder of letters and notes; it is made when the first one is saved
	public static String contactFolder(mcContact contact)
	{
		return MyContacts.docsfolder + "/" + fileName(contact);
	}
}
