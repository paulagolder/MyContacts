package org.lerot.mycontacts;

import org.lerot.mywidgets.jswLabel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class mcImports extends mcDataObject
{

	public static void updateImportAttribute(int contactnumber,
			String locallabel, String value)
	{
		mcImportAttribute impatt = new mcImportAttribute(locallabel, "", null);
		impatt.update(contactnumber);
	}

	public static void updateImportAttribute(int contactnumber,
			String locallabel, String value, String importstatus)
	{
		if (value == null || value.trim().isEmpty()) return;
		mcImportAttribute impatt = new mcImportAttribute(locallabel,
				value.trim(), importstatus);
		impatt.update(contactnumber);
	}

	private LinkedHashMap<String, mcImportContact> collection;

	protected int currentposition = 0;

	//private String filedate;
	//private String importdate;
	protected String importfilename;
	protected String importtype;
	protected mcMappings mappings;
	//private mcImportContact newimportcontact;
	//private int importcount;
	
	public mcImports()
	{
		super();
		collection = new LinkedHashMap<String, mcImportContact>();
	}
	
	public mcImports(String animportfilename, String animporttype)
	{
		super();
		importfilename = animportfilename;
		importtype = animporttype;
		collection = new LinkedHashMap<String, mcImportContact>();
	}

	void addException(LinkedHashMap<String, mcImportexception> exceptions,
			String token, String value)
	{
		String shortvalue = value;
		if (value.length() > 30) shortvalue = value.substring(0, 30);
		if (exceptions.containsKey(token))
		{
			mcImportexception impex = exceptions.get(token);
			impex.setCount(impex.getCount() + 1);
			if (impex.getExample() == "") impex.setExample(shortvalue);
		} else
		{
			mcImportexception impex = new mcImportexception();
			if (impex.getExample() == "") impex.setExample(shortvalue);
			impex.setToken(token);
			exceptions.put(token, impex);
		}
	}

	private void addImportContact(int id, mcImportContact acontact)
	{
		collection.put(String.valueOf(id), acontact);

	}

	public void clear()
	{
		collection.clear();

	}

	public void deleteAllImports()
	{
		String sql = "delete from importDataTable where 1 ";
		doExecute(sql);
	}

	@SuppressWarnings("unused")
	private Set<Entry<String, mcImportContact>> entrySet()
	{
		return collection.entrySet();
	}

	public mcImportContact get(int rownumber)
	{
		return collection.get(String.valueOf(rownumber));
	}

	public Set<Entry<String, mcImportContact>> getArray()
	{
		return collection.entrySet();
	}

	public int getCurrentposition()
	{
		return currentposition;
	}

	public String getImportfilename()
	{
		return importfilename;
	}

	public String getImporttype()
	{
		return importtype;
	}

	public mcMappings getMappings()
	{
		return mappings;
	}

	public LinkedHashMap<String, mcImportexception> importcsv(boolean test,
			jswLabel message)
	{

		mappings = MyContacts.topgui.currentcon.createMappings("import", importtype);
		int k = 0;
		LinkedHashMap<String, mcImportexception> exceptions = new LinkedHashMap<String, mcImportexception>();
		try
		{
			InputStream inputStream = new FileInputStream(importfilename);
			Reader bufferedreader = new InputStreamReader(inputStream, "UTF8");

			Vector<String> out = new Vector<String>();
			Vector<String> keys = mcUtilities.readLinetoArray(bufferedreader);
			Vector<Integer> countvalues = new Vector<Integer>();
			Vector<String> examples = new Vector<String>();
			int maxfield = keys.size();
			for (int r = 0; r < keys.size(); r++)
			{
				countvalues.add(r, 0);
				examples.add(r, "");
			}
			deleteAllImports();
			initialiseImportDataTable(importfilename, importtype);
			k = 1;
			while ((out = mcUtilities.readLinetoArray(bufferedreader)) != null)
			{
				int r = 0;
				int n = 0;
				for (String fred : out)
				{
					if (r < maxfield)
					{
						String inkey = keys.get(r);
						String key = mappings.get(inkey);
						String value = fred;
						if (key != null && !key.equalsIgnoreCase("ignore")
								&& value != null && !value.isEmpty())
						{
							mcImportAttribute impatt = new mcImportAttribute(
									key, "", value);
							impatt.update(k);
							n++;
						} else
						{
						}
					}
					r++;
				}
				if (n > 0)
				{
					k++;
				}
			}
		} catch (Exception e)
		{
			System.out.println("error in line " + k);
			e.printStackTrace();
		}
		return exceptions;
	}

	public void initialiseImportDataTable(String filename, String type)
	{
		deleteAllImports();
		mcImportAttribute impatt = new mcImportAttribute("filename", "",
				filename);
		impatt.insert(0);
		impatt = new mcImportAttribute("importtype", "", type);
		impatt.insert(0);
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		String now = sdf.format(date);
		impatt = new mcImportAttribute("importdate", "", now);
		impatt.insert(0);
		Path file = (new File(filename)).toPath();
		BasicFileAttributes attr;
		try
		{
			attr = Files.readAttributes(file, BasicFileAttributes.class);
			String then1 = attr.lastModifiedTime().toString();
			then1 = then1.replace("T", " ");
			then1 = then1.replace("Z", " ");
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date then = sdf.parse(then1);
			String thenstr = sdf.format(then);
			impatt = new mcImportAttribute("filedate", "", thenstr);
			impatt.insert(0);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public LinkedHashMap<String, mcImportexception> makeImport(boolean test,
			jswLabel message)
	{
		clear();
		LinkedHashMap<String, mcImportexception> exceptions = null;

		if (importtype.equalsIgnoreCase("vcard"))
		{
			exceptions = ((mcImportVcard) this).importall(test, message);

		} else if (importtype.equalsIgnoreCase("ldif"))
		{
			exceptions = ((mcImportLdif) this).importall(test, message);
		} else if (importtype.equalsIgnoreCase("xml"))
		{
			exceptions = ((mcImportXML) this).importall(test, message);
		} else if (importtype.equalsIgnoreCase("gOutlookExport"))
		{
			exceptions = importcsv(test, message);
		} else if (importtype.equalsIgnoreCase("gGoogleExport"))
		{
			exceptions = importcsv(test, message);
		} else
		{
			System.out.println("unknown Import Source:" + importtype);
			return null;
		}
		if (!test)
		{
			selectAllImports();
			scanImports();
		}

		return exceptions;
	}

	public void scanImports()
	{
		for (Entry<String, mcImportContact> entry : getArray())
		{
			mcImportContact currentimport = entry.getValue();
			int row = currentimport.getRownumber();
			currentimport.getAttributes();

			Vector<String> foundcontacts = currentimport.findMatchingContacts();

			if (foundcontacts.isEmpty())
			{
				currentimport.setImportstatus("notfound");
				currentimport.update();
			} else if (foundcontacts.size() > 0)
			{

				String id = foundcontacts.get(0);
				mcContact fcontact = MyContacts.selbox.FindbyID(id);
				currentimport.setImportstatus("contactfound");
				currentimport.setTID(fcontact.getTID());
				currentimport.update();
			}
		}
	}

	public void selectAllImports()
	{
		String sql = "select * from importDataTable order by ROWNUMBER ";
		ArrayList<Map<String, String>> rowlist = doQuery(sql);
		int currentrownumber = -1;
		mcImportContact currentcontact = null;
		for (Map<String, String> row : rowlist)
		{

			mcImportAttribute anattribute = new mcImportAttribute();
			anattribute.load(row);
			int newrownumber = anattribute.getRownumber();

			if (newrownumber != currentrownumber)
			{
				if (currentcontact != null)
				{
					addImportContact(currentrownumber, currentcontact);
				}
				currentcontact = new mcImportContact(newrownumber);
				currentrownumber = newrownumber;
			}
			if (anattribute.getAttkey().equalsIgnoreCase("importstatus"))
			{
				currentcontact.setImportstatus(anattribute.getValue());
			} else

			if (anattribute.getAttkey().equalsIgnoreCase("TID"))
			{
				currentcontact.setTID(anattribute.getValue());
			} // else
			{
				if (anattribute.getAttkey().equalsIgnoreCase("name"))
				{
					currentcontact.setName(anattribute.getValue());
				}
				currentcontact.addImportAttribute(anattribute);
			}

		}
		mcImportContact header = collection.get("0");
		if (header != null)
		{
			mcImportAttribute filenameatt = header.getAttribute("filename");
			importfilename = filenameatt.getValue();
			mcImportAttribute typeatt = header.getAttribute("importtype");
			importtype = typeatt.getValue();
			mcImportAttribute dateatt = header.getAttribute("importdate");
			//importdate = dateatt.getValue();
			mcImportAttribute filedateatt = header.getAttribute("filedate");
			//filedate = filedateatt.getValue();
		} else
			importfilename = " no file found";
		//importcount = collection.size();
	}

	public void setCurrentposition(int currentposition)
	{
		this.currentposition = currentposition;
	}

	public void setImportfilename(String importfilename)
	{
		this.importfilename = importfilename;
	}

	public void setImportFileName(String importfilename)
	{
		this.importfilename = importfilename;

	}

	public void setImportrownumber(int i)
	{
		currentposition = i;

	}

	public void setImportType(String type)
	{
		this.importtype = type;
	}

	public void setMappings(mcMappings mappings)
	{
		this.mappings = mappings;
	}

	public int size()
	{
		return collection.size();

	}

}
