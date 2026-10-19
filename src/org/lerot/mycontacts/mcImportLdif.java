package org.lerot.mycontacts;

import org.lerot.mywidgets.jswLabel;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;

public class mcImportLdif extends mcImports
{

	boolean test = true;
	jswLabel message;
	private String buffer;
	static String[] Nfields = { "sn", "fn", "mn", "title", "sufix" };
	static String[] ADRfields = { "pobox", "extaddr", "street", "city",
			"county", "postcode", "country" };

	public mcImportLdif(String importfilename)
	{
		super(importfilename, "ldif");
	}

	public mcImporttoken getToken(BufferedReader bufferedreader)
	{
		String newline;
		mcImporttoken atoken = new mcImporttoken();
		atoken.setStatus( "notoken");
		atoken.setToken(null);
		String token = "";
		try
		{
			newline = getNewLine(bufferedreader);
			if (newline == null)
			{
				return null;
			}
			if (newline.isEmpty()) return atoken;
			String invalue = "";
			int j = newline.indexOf(":");
			int j64 = newline.indexOf("::");
			if (j64 > 0)
			{
				token = newline.substring(0, j64);
				invalue = newline.substring(j + 2).trim();
				atoken.setEncoding( "b64");
				atoken.setStatus( "ok");
			} else if (j > 0)
			{
				token = newline.substring(0, j);
				invalue = newline.substring(j + 1).trim();
				atoken.setStatus( "ok");;
			} else
			{
				invalue = newline;
			}
			atoken.setToken(token) ;
			atoken.setValue( invalue.trim());
			return atoken;

		} catch (Exception e)
		{
			System.out.println(" this is here ");
			return null;
		}

	}

	private String getNewLine(BufferedReader bufferedreader)
	{
		if (buffer == null) return null;
		String oldline = buffer;
		String newline;
		try
		{
			newline = bufferedreader.readLine();
			if (newline == null)
			{
				buffer = null;
				return oldline;
			}
			if (newline.isEmpty() || newline.startsWith("#"))
			{
				buffer = "";
				return oldline;
			}

			while (newline.startsWith(" ") || newline.startsWith("\t"))
			{
				oldline = oldline + newline.substring(1);
				newline = bufferedreader.readLine();
			}

			buffer = newline;
			return oldline;

		} catch (IOException e)
		{
			buffer = null;
			return oldline;
		}
	}

	private Vector<mcImporttoken> parsevalue(String mtoken, String invalue)
	{
		Vector<mcImporttoken> tokenlist = new Vector<mcImporttoken>();
		String[] split = invalue.split(";");
		String rest = "";

		for (String atoken : split)
		{
			if (atoken != null && !atoken.isEmpty())
			{
				mcImporttoken stoken = new mcImporttoken();
				int k = 0;
				k = atoken.indexOf(":");
				if (k > 0)
				{
					String btoken = atoken.substring(0, k).trim();
					String bvalue = atoken.substring(k + 1).trim();
					stoken.setToken( btoken);
					stoken.setValue( bvalue);
					tokenlist.add(stoken);
					// System.out.println("*" + btoken + "*" + bvalue + "*");
				} else
					rest = rest + atoken + "; ";
			}

		}
		if (!rest.isEmpty())
		{
			mcImporttoken rtoken = new mcImporttoken();
			rtoken.setToken(mtoken);
			rtoken.setValue( rest);
			tokenlist.add(rtoken);
		}
		return tokenlist;
	}

	public LinkedHashMap<String, mcImportexception> importall(boolean test,
			jswLabel inmessage)
	{
		buffer = "";
		HashMap<String, Integer> namelist = new HashMap<String, Integer>();
		message = inmessage;
		if (message != null)
			message.setText(" importing ldif " + importfilename);
		mappings = MyContacts.topgui.currentcon.createMappings("import", "Ldif");
	
		if (!test)
		{
			System.out.println(" importing ldif " + importfilename);
			initialiseImportDataTable(importfilename, importtype);
		} else
			System.out.println(" testing ldif " + importfilename);
		BufferedReader bufferedreader;
		 LinkedHashMap<String, mcImportexception> exceptions = new LinkedHashMap<String, mcImportexception>();
		InputStream inputStream;
		try
		{
			inputStream = new FileInputStream(importfilename);

			bufferedreader = new BufferedReader(new InputStreamReader(
					inputStream, "UTF8"));
			int currentposition = 1;
			mcImportContact nextcontact = getContact(bufferedreader,
					currentposition, exceptions);
			while (nextcontact != null)
			{
				if (nextcontact.isloadable())
				{
					if (!test)
					{
						String name = nextcontact.getName();
						if (!namelist.containsKey(name))
						{
							namelist.put(name, currentposition);
							nextcontact.InsertImportContact();
							currentposition++;
							if (message != null)
							{
								message.setText(" loading contact details no:"
										+ currentposition);
								message.repaint();
							}
						} else
						{
							int cp = namelist.get(name);
							nextcontact.InsertImportContact(cp);
							System.out.println(" already stored " + name
									+ " at " + cp);
						}

					} else
					{
						if (message != null)
						{
							if (test)
								message.setText(" testing contact details no:"
										+ currentposition);
							message.repaint();
						}
					}
				} else
				{
					// System.out.println(" Skipping " + nextcontact);
				}
				nextcontact = getContact(bufferedreader, currentposition,
						exceptions);
			}

		} catch (FileNotFoundException e)
		{
			System.out.println(" FileNotFoundException");
		} catch (UnsupportedEncodingException e)
		{
			System.out.println(" UnsupportedEncodingException ");
		} /*
		 * catch (Exception e) { System.out.println(" Exception " + e); }
		 */
		if (message != null)
		{
			message.setText("loaded  " + " contacts " + currentposition);
			message.repaint();
		}
		return exceptions;
	}

	private mcImportContact getContact(BufferedReader bufferedreader,
			int contactnumber, LinkedHashMap<String, mcImportexception> exceptions)
	{
		mcImportContact newcontact = new mcImportContact(contactnumber);
		mcImporttoken intoken = getToken(bufferedreader);
		//if (intoken == null) return null;
		while (intoken != null && intoken.getToken()!= null
				&& !(intoken.getToken().equalsIgnoreCase("end")))
		{
			newcontact.processinputtoken(intoken, mappings, exceptions);
			intoken = getToken(bufferedreader);
		}
		return newcontact;
	}

	private String makeArray(String[] fieldlist, String value)
	{
		String[] split = value.split(";");
		String out = "{ ";
		int k = 0;
		int j = 0;
		String onlytoken = "";
		for (String atoken : split)
		{
			if (atoken != null && !atoken.isEmpty())
			{
				if (atoken.contains(","))
				{
					atoken = '"' + atoken + '"';
				}
				if (!onlytoken.isEmpty()) out = out + ", ";
				out = out + fieldlist[k] + ":" + atoken;
				onlytoken = atoken;
				j++;
			}
			k++;
			if (k > fieldlist.length) break;
		}
		out = out + "}";
		if (j == 1) return onlytoken;
		return out;
	}

}
