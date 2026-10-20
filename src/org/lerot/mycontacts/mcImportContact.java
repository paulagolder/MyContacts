package org.lerot.mycontacts;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class mcImportContact extends mcDataObject
{

	public  mcImportContact selectImportContact(int rownumber)
	{

		String sql = "select * from importDataTable where ROWNUMBER = "
				+ rownumber;
		ArrayList<Map<String, String>> rowlist = doQuery(sql);

		mcImportContact currentcontact = new mcImportContact(rownumber);
		for (Map<String, String> row : rowlist)
		{
			mcImportAttribute anattribute = new mcImportAttribute();
			anattribute.load(row);

			if (anattribute.getAttkey().equalsIgnoreCase("importstatus"))
			{
				currentcontact.setImportstatus(anattribute.getValue());
			} else if (anattribute.getAttkey().equalsIgnoreCase("TID"))
			{
				currentcontact.setTID(anattribute.getValue());
			} else
			{
				if (anattribute.getAttkey().equalsIgnoreCase("name"))
				{
					currentcontact.setName(mcUtilities.tidyValue(anattribute
							.getValue()));
				}
				currentcontact.addImportAttribute(anattribute);
			}
		}
		return currentcontact;
	}
	private mcImportAttributes attributes = null;
	private String importstatus = "";
	private String name = null;
	private Integer rownumber;
	private String TID = null;
	private String impupdate;

	public mcImportContact(int newrownumber)
	{
		rownumber = newrownumber;
		attributes = new mcImportAttributes();
	}

	public void addImportAttribute(mcImportAttribute anattribute)
	{
		attributes.addImportAttribute(anattribute);
	}

	public void checkforduplicatevalues()
	{
		for (Entry<String, mcImportAttribute> anentry : attributes.entrySet())
		{
			mcImportAttribute aniattribute = anentry.getValue();

			if (aniattribute != null
					&& !aniattribute.getImportstatus().equals("DONE")
					&& !aniattribute.getImportstatus().equals("HIDE")
					&& !aniattribute.getImportstatus().equals("IGNORE"))
			{
				String attkey = anentry.getKey();
				String imvalue = mcUtilities.tidyValue(aniattribute.getValue());
				for (Entry<String, mcImportAttribute> anentry2 : attributes
						.entrySet())
				{
					mcImportAttribute aniattribute2 = anentry2.getValue();
					if (aniattribute2.getImportstatus().equals("DONE"))
						continue;
					String attkey2 = anentry2.getKey();
					if (attkey2 == attkey)
					{
						aniattribute2.setImportstatus("DONE");
						continue;
					}
					String imvalue2 = mcUtilities.tidyValue(aniattribute2
							.getValue());
					if (mcUtilities.containedBy(imvalue, imvalue2))
					{
						aniattribute2.setImportstatus("DONE");
					}

				}
			}
		}

	}

	

	public int checkforfieldmatches(mcContact fcontact)
	{
		if (fcontact == null) return 1;
		int unmatched = 0;

		for (Entry<String, mcImportAttribute> anentry : attributes.entrySet())
		{
			mcImportAttribute aniattribute = anentry.getValue();
			if (aniattribute != null)
			{
				if (!aniattribute.getImportstatus().equals("DONE")
						&& !aniattribute.getImportstatus().equals("HIDE")
						&& !aniattribute.getImportstatus().equals("OK")
						&& !aniattribute.getImportstatus().equals("IGNORE"))
				{

					if (fcontact.containsValue(aniattribute.getValue()))
					{
						aniattribute.setImportstatus("OK");
					} else
					{
						//paul fix search org
					/*	if (!fcontact.getMemberOf().isEmpty())
						{
							boolean found = false;
							for (Entry<String, mcMember> parent : fcontact
									.getMemberOf().entrySet())
							{
								String parenttid = parent.getKey();
								mcContact pcontact = MyContacts.selbox
										.FindbyTID(parenttid);
								if (pcontact.containsValue(aniattribute
										.getValue()))
								{
									found = true;
								}

							}
							if (found)
							{
								aniattribute.setImportstatus("OK");
							} else
								unmatched++;
						} else
							unmatched++; */
					}
				}
			}
		}
		return unmatched;
	}

	public int checkforfieldmatchesAll(mcContact fcontact)
	{
		int k = checkforfieldmatches(fcontact);
		if (k == 0) return k;
		//paul fix
		//Map<String, mcMember> members = fcontact.getMembers();
		//for (Entry<String, mcMember> anentry : members.entrySet())
		/*{
			mcMember amember = anentry.getValue();
			mcContact acontact = MyContacts.selbox.get(amember.getMemberidstr());
			k = checkforfieldmatches(acontact);
			if (k == 0) return k;
		}
		Map<String, mcMember> membersof = fcontact.getMemberOf();
		for (Entry<String, mcMember> anentry : membersof.entrySet())
		{
			mcMember amember = anentry.getValue();
			mcContact acontact = MyContacts.selbox.get(amember.getParentidstr());
			k = checkforfieldmatches(acontact);
			if (k == 0) return k;
		}*/
		return k;
	}

	public Vector<String> findMatchingContacts()
	{
		mcImportContact currentimport = this;
		Vector<String> orderedcontacts = new Vector<String>();
		mcImportAttributes attributes = currentimport.getAttributes();
		String sname = "?", fname = "?", tname = "?", sphone = "?", smobile = "?", semail = "?";
		if (attributes != null)
		{
			for (Entry<String, mcImportAttribute> anentry : attributes
					.entrySet())
			{
				anentry.getKey();
				mcImportAttribute anattribute = anentry.getValue();
				if (anattribute != null)
				{
					String quallabel = anattribute.getAttkey();
					String root = mcUtilities.parseKey(quallabel);
					if (!anattribute.ignore())
					{
						String value = anattribute.getValue();
						if (root.startsWith("name"))
						{
							if (quallabel.contains("(fn)")) fname = value;
							else if (quallabel.contains("(sn)")) sname = value;
							else
								tname = value;
						}
						if (quallabel.startsWith("phone")) sphone = value;
						if (quallabel.startsWith("mobile")) smobile = value;
						if (quallabel.startsWith("email")) semail = value;
					}
				}
			}
		}
		String bestname = tname;
		if (fname != "?" && sname != "?") bestname = sname.trim() + ", "
				+ fname.trim();
		else if (sname != "?") bestname = sname;
		else if (fname != "?") bestname = fname;

		HashMap<String, Integer> foundcontact = new HashMap<String, Integer>();
		mcContact fcontact = MyContacts.selbox.findName(bestname);
		if (fcontact != null)
		{
			String fkey = fcontact.getIDstr();
			int count = 0;
			if (foundcontact.containsKey(fkey)) count = foundcontact.get(fkey);
			foundcontact.put(fkey, count + 1);
		}
		// else
		{
			if (semail != "?")
			{
				fcontact = MyContacts.selbox.findValue("email", semail);
				if (fcontact != null)
				{
					String fkey = fcontact.getIDstr();
					int count = 0;
					if (foundcontact.containsKey(fkey))
						count = foundcontact.get(fkey);
					foundcontact.put(fkey, count + 1);
				}
			}
			if (sphone != "?")
			{
				fcontact = MyContacts.selbox.findPhone("phone", sphone);
				if (fcontact != null)
				{
					String fkey = fcontact.getIDstr();
					int count = 0;
					if (foundcontact.containsKey(fkey))
						count = foundcontact.get(fkey);
					foundcontact.put(fkey, count + 1);
				}
				fcontact = MyContacts.selbox.findPhone("mobile", sphone);
				if (fcontact != null)
				{
					String fkey = fcontact.getIDstr();
					int count = 0;
					if (foundcontact.containsKey(fkey))
						count = foundcontact.get(fkey);
					foundcontact.put(fkey, count + 1);
				}
			}
			if (smobile != "?")
			{
				fcontact = MyContacts.selbox.findPhone("mobile", smobile);
				if (fcontact != null)
				{
					String fkey = fcontact.getIDstr();
					int count = 0;
					if (foundcontact.containsKey(fkey))
						count = foundcontact.get(fkey);
					foundcontact.put(fkey, count + 1);
				}
				fcontact = MyContacts.selbox.findPhone("phone", smobile);
				if (fcontact != null)
				{
					String fkey = fcontact.getIDstr();
					int count = 0;
					if (foundcontact.containsKey(fkey))
						count = foundcontact.get(fkey);
					foundcontact.put(fkey, count + 1);
				}
			}
			if (foundcontact.size() == 0 && bestname != "?")
			{
				fcontact = MyContacts.selbox.findName2(bestname);
				if (fcontact != null)
				{
					String fkey = fcontact.getIDstr();
					int count = 0;
					if (foundcontact.containsKey(fkey))
						count = foundcontact.get(fkey);
					foundcontact.put(fkey, count + 1);
				} else
				{
					fcontact = MyContacts.selbox.findName3(bestname);
					if (fcontact != null)
					{
						String fkey = fcontact.getIDstr();
						int count = 0;
						if (foundcontact.containsKey(fkey))
							count = foundcontact.get(fkey);
						foundcontact.put(fkey, count + 1);
					}
				}
			}
		}
		orderedcontacts = mcUtilities.sortByValues(foundcontact);

		return orderedcontacts;
	}

	public mcImportAttribute getAttribute(String attkey)
	{

		return attributes.get(attkey);
	}

	public mcImportAttributes getAttributes()
	{
		return attributes;
	}

	public String getAttributeValue(String key)
	{
		mcImportAttribute impatt = getAttribute(key);
		if (impatt == null) return null;
		else
			return impatt.getValue();
	}

	public String getImportstatus()
	{
		return importstatus;
	}

	public String getName()
	{
		if (name != null && !name.isEmpty()) return name;
		else
		{
			for (Entry<String, mcImportAttribute> entry : attributes.entrySet())
			{
				mcImportAttribute anattribute = entry.getValue();
				if (anattribute.getAttkey().equalsIgnoreCase("name"))
				{
					name = mcUtilities.tidyValue(anattribute.getValue());
					return name;
				}
				if (anattribute.getAttkey().contains("mail"))
				{
					name = mcUtilities.getHost(anattribute.getValue());
					// System.out.println(" setting name to " + name);
				}
			}
			return name;
		}

	}

	public int getRownumber()
	{
		return rownumber;
	}

	public String getTID()
	{
		return TID;
	}

	public void setAttributes(mcImportAttributes attributes)
	{
		this.attributes = attributes;
	}

	public void setImportstatus(String importstatus)
	{
		this.importstatus = importstatus;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setTID(String tid)
	{
		this.TID = tid;
	}

	@Override
	public String toString()
	{
		String outstring = "import (" + rownumber + "," + TID + ","
				+ importstatus + " " + getName() + ")";
		return outstring;
	}

	public void xTranslateLabels(mcMappings mappings)
	{
		mcImportAttributes newmap = new mcImportAttributes();
		for (Entry<String, mcImportAttribute> enentery : attributes.entrySet())
		{
			String keylabel = enentery.getKey();
			mcImportAttribute att = enentery.getValue();
			String locallabel = mappings.get(keylabel);
			if (locallabel != null) att.setAttkey(locallabel);
			// newattributes.addImportAttribute(att);
			newmap.put(locallabel, att);
		}
		attributes = newmap;

	}

	public void update()
	{
		mcImportContact oldcontact = selectImportContact(rownumber);
		mcImportAttribute tidatt = attributes.get("TID");
		if (tidatt == null)
		{
			tidatt = new mcImportAttribute("TID","", getTID(), "HIDE");
			attributes.addImportAttribute(tidatt);
		}
		tidatt.setValue(getTID());
		mcImportAttribute statusatt = attributes.get("IMPORTSTATUS");
		if (statusatt == null)
		{
			statusatt = new mcImportAttribute("IMPORTSTATUS","",
					getImportstatus(), "HIDE");
			attributes.addImportAttribute(statusatt);
		}
		statusatt.setValue(getImportstatus());
		for (Entry<String, mcImportAttribute> entry : attributes.entrySet())
		{
			mcImportAttribute impatt = entry.getValue();
			String key = entry.getKey();
			impatt.updateFrom(rownumber, oldcontact.getAttributeValue(key));
		}
	}

	public boolean allOk()
	{
		mcImportAttributes attributes = this.getAttributes();

		if (attributes != null)
		{
			for (Entry<String, mcImportAttribute> anentry : attributes
					.entrySet())
			{
				mcImportAttribute anattribute = anentry.getValue();
				System.out.println(" cheking " + anattribute);
				if (anattribute != null)
				{
					String Importstatus = anattribute.getImportstatus();
					if (!(Importstatus.equalsIgnoreCase("OK")
							|| Importstatus.equalsIgnoreCase("HIDE") || Importstatus
								.equalsIgnoreCase("DONE"))) return false;
				}
			}
		}
		return true;
	}

	public void InsertImportContact(int rownumber)
	{
		if (attributes != null)
		{
			for (Entry<String, mcImportAttribute> anentry : attributes
					.entrySet())
			{
				mcImportAttribute impatt = anentry.getValue();
				impatt.insert(rownumber);
			}
		}

	}

	public void InsertImportContact()
	{
		InsertImportContact(rownumber);
	}

	public void processinputtoken(mcImporttoken intoken,
			mcMappings mappings,
			LinkedHashMap<String, mcImportexception> exceptions)
	{

		String token = intoken.getToken();
		//System.out.println(" processing "+intoken.shortstring());
		String value = intoken.getValue();
		String shortvalue = intoken.shortValue();
		if (token == null) return;
		else
		{
			String ttoken = token;
			String localtoken = null;// mappings.get(token);
			String type = intoken.getType().toLowerCase();
			if(!type.isEmpty())
			{
				ttoken= token+";"+type;
				//System.out.println(" processing with token "+ttoken);
				if(mappings.get(ttoken)!=null)
				{
					type="";
					localtoken=  mappings.get(ttoken);
					//System.out.println(" processing "+intoken.shortstring()+ " found local"+localtoken);
				}
				else
				{
					//System.out.println(" not found  "+ttoken);
				}
			}//System.out.println(" processing "+intoken.shortstring());
		    if(localtoken == null)
		    {
		    	localtoken =  mappings.get(token);
		    }
		
			if (localtoken != null)
			{
				if (!localtoken.equalsIgnoreCase("ignore"))
				{
					mcImportAttribute impatt = new mcImportAttribute(
							localtoken,type, value, null);
					addImportAttribute(impatt);
					//System.out.println(" found "+localtoken +" = "+ impatt.toString());
				}
				else 
				{ 
					if(token.equalsIgnoreCase("rev"))
					{
						setimpupdate(value);
					}
					//else
					//	System.out.println(" ignoring "+intoken.shortstring());
				}
			  	
			} else
			{
				if( token.trim().isEmpty())
				{
					// empty line - ignore 
				}
				else if (exceptions.containsKey(token))
				{
					//System.out.println(" adding exception "+token);
					mcImportexception impex = exceptions.get(token);
					impex.setCount(impex.getCount() + 1);
					if (impex.getExample().length()< shortvalue.length()) impex.setExample(shortvalue);
				} else
				{
					//System.out.println(" adding exception "+token);
					mcImportexception impex = new mcImportexception();
					 impex.setExample(shortvalue);
					impex.setToken(token);
					exceptions.put(token, impex);
				}
			}
		}

		return;
	}

	
	public void processvcardinputtoken(mcImporttoken intoken,
			mcMappings mappings,
			LinkedHashMap<String, mcImportexception> exceptions)
	{

		String token = intoken.getToken();
		String value = intoken.getValue();
		String shortvalue = intoken.shortValue();
		if (token == null) return;
		else
		{
			String ttoken = token;
			String localtoken = null;// mappings.get(token);
			String type = intoken.getType().toLowerCase();
			String qual="";
			if(!type.isEmpty())
			{			
				ttoken= token+";"+type;
				String stoken = ttoken;
			
				String mappedkey = mappings.get(stoken);
				while(mappedkey == null)
				{
					int k  = stoken.lastIndexOf(";");
					if(k<1) break;
					stoken= stoken.substring(0,k);
					qual = ttoken.substring(k+1);
					mappedkey = mappings.get(stoken);
				}
				if(mappedkey!=null)
				{
					
					localtoken=  mappings.get(stoken);
				}
				else
				{
					
					localtoken=  mappings.get(token);
					//System.out.println(" processing  3"+intoken.shortstring()+ " found local"+localtoken);
				}
				//System.out.println(" processing 1  with token "+ttoken +"=" + localtoken);
			}
			//System.out.println(" processing 4 "+intoken.shortstring());
		    if(localtoken == null)
		    {
		    	//System.out.println(" processing 4 "+intoken.shortstring());
		    	localtoken =  mappings.get(token);
		    }
		
			if (localtoken != null)
			{
				if (!localtoken.equalsIgnoreCase("ignore"))
				{
					mcImportAttribute impatt = new mcImportAttribute(
							localtoken,qual, value, null);
					addImportAttribute(impatt);
					//System.out.println(" found "+localtoken +"*"+type+ " = "+ impatt.toString());
				}
				else 
				{ 
					if(token.equalsIgnoreCase("rev"))
					{
						setimpupdate(value);
					}
					//else
					//	System.out.println(" ignoring "+intoken.shortstring());
				}
			  	
			} else
			{
				if( token.trim().isEmpty())
				{
					// empty line - ignore 
				}
				else if (exceptions.containsKey(token))
				{
					//System.out.println(" adding exception "+token);
					mcImportexception impex = exceptions.get(token);
					impex.setCount(impex.getCount() + 1);
					if (impex.getExample().length()< shortvalue.length()) impex.setExample(shortvalue);
				} else
				{
					//System.out.println(" adding exception "+token);
					mcImportexception impex = new mcImportexception();
					 impex.setExample(shortvalue);
					impex.setToken(token);
					exceptions.put(token, impex);
				}
			}
		}

		return;
	}
	

	public int countAttributes()
	{
		return attributes.size();
	}

	public void deleteImportContact()
	{
		try
		{
			PreparedStatement st;
			String query = " delete from importDataTable where  ROWNUMBER = ? ";
			st = conn().prepareStatement(query);
			st.setInt(1, rownumber);
			int rescount = st.executeUpdate();
			st.close();
		} catch (SQLException e2)
		{
			mcDataObject.sqlError(e2);
		}
	}

	public boolean isloadable()
	{
		boolean select = false;
		if (attributes.contains("email")) select = true;
		if (attributes.contains("phone")) select = true;
		if (attributes.contains("address")) select = true;
		return select;
	}

	public Vector<String> AttributeValues()
	{
		Vector<String> filter = new Vector<String>();
		if (attributes != null)
		{
			for (Entry<String, mcImportAttribute> anentry : attributes
					.entrySet())
			{
				mcImportAttribute impatt = anentry.getValue();
				filter.add(impatt.getValue());
			}
		}
		return filter;
	}

	public boolean matches(mcContact newcon, mcContact econ)
	{
		boolean match = true;
		this.TID = newcon.getTID();
		if (!newcon.getTID().equalsIgnoreCase(econ.getTID())) match = false;
		for (Entry<String, mcAttribute> entry : newcon.attributes.entrySet())
		{
			mcAttribute newatt = entry.getValue();
			mcAttribute existingatt = econ.getAttributebyKey(newatt.getKey());
			if (!newatt.matches(existingatt))
			{
				mcImportAttribute impatt = new mcImportAttribute(
						newatt.getKey(), newatt.getValue(), null);
				addImportAttribute(impatt);
				match = false;
			}
		}
		return match;
	}

	public String getFN()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getimpupdate()
	{
		
		return impupdate;
	}
	
	public void setimpupdate(String indatetime)
	{
		
		impupdate = indatetime;;
	}

	

}
