package org.lerot.mycontacts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

// A backup (Tools > Backup > Make backup) read back with Import backup
// gives the same contacts, into an empty database or the one it came from
class XmlBackupTest
{
	@TempDir
	Path folder;
	mcDataSource ds;
	String backup;

	@BeforeEach
	void setUp() throws Exception
	{
		ds = TestDatabase.create(folder);
		mcContacts db = new mcContacts();
		// cases met in real data: several tags; a list name that is not the
		// formal name (a nickname); a note with commas; a group whose name
		// has an "&", linked to; a contact without tags; two with one name
		db.doExecute("update attributeValues set value = 'friends;family;' where cid = 1 and root = 'tags'");
		db.doExecute("update attributeValues set value = 'Smith  Ally' where cid = 1 and root = 'tid'");
		db.doExecute("insert into attributeValues(cid, root, qualifier, value) values (3, 'note', '', 'met in Paris, then Lyon, 2019')");
		db.doExecute("insert into attributeValues(cid, root, qualifier, value) values "
				+ "(4, 'tid', '', 'Brown  Ann & Tom'), (4, 'name', '', '{ fn:''Ann & Tom'' , sn:''Brown'' ,  }'), "
				+ "(4, 'hasmember', '000002', '000002'), (2, 'memberof', '000004', '000004')");
		db.doExecute("insert into attributeValues(cid, root, qualifier, value) values "
				+ "(5, 'tid', '', 'Jones  Carol'), (5, 'name', '', '{ fn:''Carol'' , sn:''Jones'' ,  }'), "
				+ "(5, 'email', '', 'carol.two@example.com'), (5, 'tags', '', 'work;')");
		backup = folder.resolve("backup.xml").toString();
	}

	@AfterEach
	void tearDown()
	{
		ds.close();
	}

	@Test
	void backupRestoresIntoAnEmptyDatabase() throws Exception
	{
		Map<String, List<String>> before = contents();
		assertEquals(5, mcContacts.writeBackupXML(allContacts(), backup));

		Path empty = folder.resolve("empty.sqlite");
		mcStarter.createDatabase(empty);
		ds.close();
		ds = new mcDataSource(empty.toString());
		mcDataObject.useDataSource(ds);
		new mcImportXML(backup).importall(false, null);

		assertEquals(before, contents());
	}

	@Test
	void backupRestoresIntoTheSameDatabase() throws Exception
	{
		Map<String, List<String>> before = contents();
		mcContacts.writeBackupXML(allContacts(), backup);

		new mcImportXML(backup).importall(false, null);

		assertEquals(before, contents());
	}

	private static java.util.Vector<mcContact> allContacts()
	{
		mcContacts all = new mcContacts();
		all.refreshAllContacts();
		return all.makeOrderedContactsVector();
	}

	// every contact's details by name; ids differ between databases, so a
	// link shows the name of the contact it points to, and tags are a set
	private static Map<String, List<String>> contents()
	{
		Map<String, String> names = new TreeMap<>();
		for (Map<String, String> row : mcDataObject.doQuery("select cid, value from attributeValues where root = 'tid'"))
			names.put(row.get("cid"), row.get("value"));
		Map<String, List<String>> contents = new TreeMap<>();
		for (Map<String, String> row : mcDataObject.doQuery("select cid, root, qualifier, value from attributeValues"))
		{
			String root = row.get("root"), qualifier = row.get("qualifier"), value = row.get("value");
			if (root.equals("memberof") || root.equals("hasmember") || root.equals("related"))
			{
				qualifier = "";
				value = "-> " + names.get(String.valueOf(Integer.parseInt(value)));
			} else if (root.equals("tags"))
				value = new TreeSet<>(List.of(value.split("\\s*;\\s*"))).toString();
			contents.computeIfAbsent(names.get(row.get("cid")), k -> new ArrayList<>())
					.add(root + "/" + qualifier + " = " + value);
		}
		contents.values().forEach(Collections::sort);
		return contents;
	}
}
