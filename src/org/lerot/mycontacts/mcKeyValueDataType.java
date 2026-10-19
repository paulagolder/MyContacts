package org.lerot.mycontacts;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

public abstract class mcKeyValueDataType extends mcDataType
{

	LinkedHashMap<String, mcfield> fieldlist;
	Vector<String> keylist;

	public mcKeyValueDataType(String key, String type)
	{
		super(key, type);
		dbLoadFieldList();
	}

	@Override
	public Vector<String> getFieldKeyList()
	{
		return keylist;
	}

	@Override
	public LinkedHashMap<String, mcfield> getFieldList()
	{
		return fieldlist;
	}

	public void dbLoadFieldList()
	{
		String query = "select * from dataTypeFields where typekey LIKE ? "
				+ " order by displayOrder ";
		ArrayList<Map<String, String>> rowlist = doQuery(query, getTypekey());
		LinkedHashMap<String, mcfield> afieldlist = new LinkedHashMap<String, mcfield>();
		Vector<String> akeylist = new Vector<String>();
		for (Map<String, String> row : rowlist)
		{
			mcfield afield = new mcfield();
			afield.load(row);
			afieldlist.put(afield.getKey(), afield);
			akeylist.add(afield.getKey());
		}
		fieldlist = afieldlist;
		keylist = akeylist;
	}

	public String makeValuefromVector(Vector<String> valuelist)
	{
		String newstr = "{ ";
		int f = 0;
		for (String akey : keylist)
		{
			String fieldname = akey;
			String newfieldvalue = valuelist.get(f);
			newstr += fieldname + ":\"" + newfieldvalue + "\"" + ",";
			f++;
		}
		StringBuilder b = new StringBuilder(newstr);
		b.replace(newstr.lastIndexOf(","), newstr.lastIndexOf(",") + 1, " }");
		newstr = b.toString();
		System.out.println("update from " + newstr);
		return newstr;
	}

	public String keyvaluesToArrayString(Map<String, String> vals)
	{

		Map<mcfield, String> arrayvalues = new LinkedHashMap<mcfield, String>();
		Map<String, mcfield> fieldlist = getFieldList();
		for (Entry<String, mcfield> row : fieldlist.entrySet())
		{
			mcfield afield = row.getValue();
			String fkey = afield.getKey().toLowerCase();
			String value = vals.get(fkey);
			arrayvalues.put(afield, value);
		}
		for (Entry<String, String> row : vals.entrySet())
		{
			String akey = row.getKey();
			String value = row.getValue();
			if (!fieldlist.containsKey(akey))
			{
				mcfield emptyfield = null;

				for (Entry<mcfield, String> row2 : arrayvalues.entrySet())
				{
					String avalue = row2.getValue();
					if ((avalue == null || avalue.equals("null"))
							&& emptyfield == null)
					{
						emptyfield = row2.getKey();
					}
				}
				if (emptyfield != null)
				{
					arrayvalues.put(emptyfield, value);
				}
			}
		}
		String valuestring = "{ ";
		String sep = "";
		for (Entry<mcfield, String> entry : arrayvalues.entrySet())
		{
			String avalue = entry.getValue();
			if (avalue != null && !avalue.isEmpty()  && !avalue.trim().isEmpty())
			{
				String akey = entry.getKey().getKey().toLowerCase();
				valuestring = valuestring + sep + akey + "=\"" + avalue+ "\" ";
				sep = ", ";
			}

		}
		valuestring = valuestring + " }";
		if (sep.isEmpty()) return null;
		else
			return valuestring;
	}

	public static String mapToArrayString(Map<String, String> valuemap)
	{
		String valuestring = "{ ";
		String sep = "";
		for (Entry<String, String> entry : valuemap.entrySet())
		{
			String avalue = entry.getValue();
			if (avalue != null && !avalue.isEmpty())
			{
				String akey = entry.getKey().toLowerCase();
				valuestring = valuestring + sep + akey + "=\"" + avalue + "\" ";
				sep = ", ";
			}

		}
		valuestring = valuestring + " }";
		if (sep.isEmpty()) return null;
		else
			return valuestring;
	}

	@Override
	public Map<mcfield, String> getFieldValueMap(String value)
	{

		Map<mcfield, String> arrayvalues = new LinkedHashMap<mcfield, String>();

		Map<String, String> vals = getValueMap(value);
		Map<String, mcfield> fieldlist = getFieldList();
		// System.out.println(" in Attribute " + this);
		for (Entry<String, mcfield> row : fieldlist.entrySet())
		{
			mcfield afield = row.getValue();
			String fkey = afield.getKey().toLowerCase();
			String avalue = vals.get(fkey);
			arrayvalues.put(afield, avalue);
		}

		for (Entry<String, String> row : vals.entrySet())
		{
			String akey = row.getKey();
			String avalue = row.getValue();
			if (!fieldlist.containsKey(akey))
			{
				mcfield emptyfield = null;

				for (Entry<mcfield, String> row2 : arrayvalues.entrySet())
				{
					String bvalue = row2.getValue();
					if ((bvalue == null || bvalue.equals("null"))
							&& emptyfield == null)
					{
						emptyfield = row2.getKey();
					}
				}
				if (emptyfield != null)
				{
					arrayvalues.put(emptyfield, avalue);
				}
			}
		}
		return arrayvalues;
	}



	@Override
	public Map<String, String> getKeyValueMap(String value)
	{

		Map<mcfield, String> fieldvalues = getFieldValueMap(value);
		Map<String, String> keyvalues = new LinkedHashMap<String, String>();
		for (Entry<mcfield, String> entry : fieldvalues.entrySet())
		{
			mcfield afield = entry.getKey();
			String avalue = entry.getValue();
			keyvalues.put(afield.getKey().toLowerCase(), avalue);
		}
		return keyvalues;
	}

	public static Vector<String> ToVector(String newvalue)
	{
		Vector<String> names = new Vector<String>();
		if (isArray(newvalue))
		{
			Map<String, String> fields = parseArray(newvalue);
			for (Entry<String, String> entry : fields.entrySet())
			{
				String aname = entry.getValue().trim();
				if (aname != null && !aname.isEmpty()) names.add(aname);
			}
			return names;

		} else
		{
			Map<String, String> lname = parseArray(newvalue);
			if (lname == null) return null;
			for (Entry<String, String> entry : lname.entrySet())
			{
				String aname = entry.getValue().trim();
				if (aname != null && !aname.isEmpty()) names.add(aname);
			}
			return names;
		}
	}

	protected static Map<String, String> parseArray(String invalue)
	{
		Map<String, String> outarray = new LinkedHashMap<String, String>();
		invalue = invalue.trim();
		if (invalue.isEmpty()) return null;
		if (invalue.startsWith("{")) invalue = invalue.substring(1);
		if (invalue.endsWith("}"))
			invalue = invalue.substring(0, invalue.length() - 1);
		invalue = invalue.trim();
		if (invalue.isEmpty()) return null;
		outarray = parseText(invalue);
		return outarray;
	}

	public static String formattedArrayValue(String avalue)
	{
		Map<String, String> mapvalues = parseArray(avalue);
		String valuestring = "";
		boolean isfirst = true;
		for (Entry<String, String> field : mapvalues.entrySet())
		{
			field.getKey();
			String value = field.getValue();
			if (!value.isEmpty())
			{
				if (isfirst) valuestring = valuestring + value;
				else
					valuestring = valuestring + ", " + value;
				isfirst = false;
			}
		}
		return valuestring;
	}

	Map<String, String> getValueMap(String avalue)
	{
		Map<String, String> mappedvalue = new LinkedHashMap<String, String>();
		Map<String, String> mapvalues = new LinkedHashMap<String, String>();
		if(avalue==null)
		{
			return null;
		}
		else if (isArray(avalue))
		{
			mappedvalue = parseArray(avalue);
		} else
		{
			mappedvalue = mcUtilities.parse_csv(avalue, ',');
		}
		if(mappedvalue==null)
			{
			System.out.println("problem in arraydata type no mappedvalues "+avalue);
			return mapvalues;
			}
		for (Entry<String, String> entry : mappedvalue.entrySet())
		{
			String key = entry.getKey().toLowerCase();
			String value = entry.getValue();
			if (value != null && !value.isEmpty()
					&& !value.equalsIgnoreCase("null") && !value.equals("''"))
			{
				if (isNumeric(key))
				{
					try
					{
						int ikey = Integer.parseInt(key);
						String aikey = keylist.get(ikey);
						mapvalues.put(aikey, value);
					} catch (Exception e)
					{
						mapvalues.put(key, value);
					}
				} else
					mapvalues.put(key, value);
			}
		}
		return mapvalues;

	}

	public static boolean isArray(String invalue)
	{
		if (invalue == null) return false;
		invalue = invalue.trim();
		if (invalue != null && invalue != "")
		{
			if (invalue.startsWith("{") && invalue.endsWith("}"))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public abstract String getFormattedValue(String value);
	
	@Override
	public String getFormattedValue(String value, String fmt)
	{

		if (fmt == null) fmt = "sn , fn mn title ";
		if (isArrayType() && isArray(value))
		{
			String output = "";
			String[] fmtlist = fmt.split(" ");
			Map<String, String> darry = getValueMap(value);
			Vector<String> keys = getFieldKeyList();
			if (darry == null)
			{
				System.out.println(" name array empty " + value);
				return null;
			}
			for (String sep : fmtlist)
			{
				if (keys.contains(sep))
				{
					if (darry.containsKey(sep))
					{
						if (darry.get(sep) != null
								&& !darry.get(sep).equalsIgnoreCase("null"))
						{
							output = output + darry.get(sep) + " ";
						}
					}
				} else
				{
					if (output.isEmpty())
					{
						if (!sep.equals(",")) output = sep + " ";
					}

					else
					{
						if (sep.equals(",")) output = output.trim() + sep + " ";
						else
							output = output + sep + " ";
					}

				}
			}
			output = output.trim();
			return output;
		} else
		{
			return value;
		}
	}

	@Override
	public String toXML(String svalue)
	{
		if (isArray(svalue))
		{
			String outxml = "";
			Map<String, String> kv = getValueMap(svalue);
			for (Entry<String, String> entry : kv.entrySet())
			{
				String key = entry.getKey();
				String value = entry.getValue();
				if (value != null && !value.isEmpty()
						&& !value.equalsIgnoreCase("null"))
				{
					String escvalue = StringEscapeUtils.escapeXml(value);
					outxml += "  <field key='" + entry.getKey() + "' value='"
							+ escvalue + "' />\n";
				}
			}
			return outxml;
		} else
		{
			return toXMLnonArray(svalue);
		}
	}

	protected abstract String toXMLnonArray(String svalue);

	public static Map<String, String> parseText(String text)
	{
		Map<String, String> outarray = new LinkedHashMap<String, String>();
		int l = text.length();
		int i = 0;
		String token = "";
		char sep =';';
		if(!text.contains(";")) sep =',';
		int count = 0;
		boolean instring = false;
		char endquote = '"'; // problem here single also
		while (i < l)
		{
			char c = text.charAt(i);
			if (instring)
			{
				if (c == endquote) instring = false;
				else
					token = token + c;
			} else
			{
				if (c == sep)
				{
					String[] ptoken = parseToken(token, count);
					outarray.put(ptoken[0], ptoken[1]);
					token = "";
					count = count + 1;

				} else
				{
					if (c == '"')
					{
						instring = true;
						endquote = c;
					}else if (c == '\'')
					{
						instring = true;
						endquote = c;
					}  else
						token = token + c;
				}
			}
			i++;
		}
		if (token != "")
		{
			String[] ptoken = parseToken(token, count);
			outarray.put(ptoken[0].toLowerCase(), ptoken[1]);
			token = "";
			count = count + 1;
		}
		return outarray;
	}

	private static String[] parseToken(String token, int r)
	{
		String[] parsed = { "", "" };
		int k = token.indexOf(":");
		int j = token.indexOf("=");
		if (k > 0 && j > 0)
		{
			if (k > j)
			{
				parsed[0] = token.substring(0, j).trim();
				parsed[1] = token.substring(j + 1).trim();
			} else
			{
				parsed[0] = token.substring(0, k).trim();
				parsed[1] = token.substring(k + 1).trim();
			}
		} else if (k > 0 || j > 0)
		{
			if (k > 0)
			{
				parsed[0] = token.substring(0, k).trim();
				parsed[1] = token.substring(k + 1).trim();
			} else
			{
				parsed[0] = token.substring(0, j).trim();
				parsed[1] = token.substring(j + 1).trim();
			}
		} else
		{
			parsed[0] = "";
			parsed[1] = token;
		}
		if (parsed[0].isEmpty())
		{
			parsed[0] = (new Integer(r)).toString();
		}
		int l = parsed[1].length();
		if (l > 2)
		{
			if (parsed[1].startsWith("'") || parsed[1].startsWith("\""))
				parsed[1] = parsed[1].substring(1, l - 1);
		}
		return parsed;
	}

	public static Map<String, String> parse_csv(String csv_string,
			char delimiter, char sep)
	{ // echo " in parse 2". csv_string;

		Vector<Character> terminator = new Vector<Character>(10);
		for (int i = 0; i < 10; i++)
			terminator.add(' ');
		Map<String, String> outarray = new LinkedHashMap<String, String>();
		int l = csv_string.length();
		int i = 0;
		String token = "";
		int count = 0;
		int depth = 1;
		while (i < l)
		{
			char c = csv_string.charAt(i);
			if (depth == 1)
			{
				if (c == delimiter)
				{
					int k = token.indexOf(sep);
					if (k == -1)
					{
						outarray.put(Integer.toString(count), token);
					} else
					{
						String key = token.substring(0, k).trim();
						String value = token.substring(k + 1).trim();
						if (value != null && value.length() > 0)
						{
							if (value.charAt(0) == '\''
									|| value.charAt(0) == '\"')
								value = value.substring(1);
							int tl = value.length();
							if (value.charAt(tl - 1) == '\''
									|| value.charAt(tl - 1) == '\"')
								value = value.substring(tl - 1);
						}
						outarray.put(key, value);
					}
					token = "";
					count = count + 1;

				} else
				{
					if (c == '"' || c == '\'')
					{
						depth = depth + 1;
						terminator.add(depth, c);
					} else
						token = token + c;
				}
			} else
			{
				if (c == terminator.get(depth))
				{
					depth = depth - 1;
				}
				/*
				 * else if (c == '"' || c == '\'') { depth = depth + 1;
				 * terminator.add(depth, c); }
				 */
				else
					token = token + c;
			}
			i++;
		}
		if (token != "")
		{
			int k = token.indexOf(sep);
			if (k == -1)
			{
				outarray.put(Integer.toString(count), token);
			} else
			{
				String key = token.substring(0, k).trim();
				String value = token.substring(k + 1).trim();
				if (value != null && value.length() != 0)
				{
					if (value.charAt(0) == '\'' || value.charAt(0) == '\"')
						value = value.substring(1);
					int tl = value.length();
					if (value.charAt(tl - 1) == '\''
							|| value.charAt(tl - 1) == '\"')
						value = value.substring(tl - 1);
					outarray.put(key, value);
				}
			}
		}
		return outarray;
	}
	
	public String format(Map<String,String> darry , String fmt)
	{
				String output = fmt;
				String[] fmtlist = fmt.split(" ");
				
				for (String sep : fmtlist)
				{
					int wordcap = detectCase(sep);
					String lsep= sep.toLowerCase();
						if (darry.containsKey(lsep))
						{
							if (darry.get(lsep) != null && !darry.get(lsep).isEmpty()
									&& !darry.get(lsep).equalsIgnoreCase("null"))
							{
								String tvalue = darry.get(lsep).toLowerCase() ;
								switch (wordcap)
								{
								
								case 1:
									tvalue = WordUtils.capitalize(tvalue);
									break;
								case 2:
									tvalue = tvalue.toUpperCase();
									break;
								default :
									
								}
								output = output.replace(sep, tvalue) ;
							}
							else
							{
								output = output.replace(sep,"") ;
							}
						}
						else
						{
							output = output.replace(sep,"") ;
						}	
				}
				output = output.trim();
				return output;		
	}

	private int detectCase(String sep)
	{
		String lower = sep.toLowerCase();
		String upper = sep.toUpperCase();
		String cap = WordUtils.capitalize(sep.toLowerCase());
		if (sep.equals(lower )) return 0;
		if(sep.equals(upper)) return 2;
		if(sep.equals(cap)) return 1;
		else return 0;
	}
	
	@Override
	public boolean matchesVcardValue(String avalue, String bvalue)
	{ 
		
		//System.out.println( " array compare "+avalue+ " with " + bvalue);
		if(compareTo(avalue,bvalue)==0) return true;
		return false;
	}

	@Override
	public int compareTo(String aarray, String barray)
	{
		Map<String, String> avaluemap = parseArray(aarray);
		Map<String, String> bvaluemap = parseArray(barray);
		for (Entry<String, String> bfield : bvaluemap.entrySet())
		{
			String bkey = bfield.getKey();
			String bvalue = bfield.getValue();
			if (!bvalue.isEmpty())
			{
				String avalue = avaluemap.get(bkey);
				//System.out.println( " array callcompare "+bkey+ " cf " + bvalue+ " with "+avalue);
				if(avalue==null || avalue.isEmpty() ) return 1;
				if(!avalue.equals(bvalue)) return 1;
			}
		}
		return 0;
	}
	
	public String makeArray(String[] fieldlist, String deffield, String value, String sep)
	{
		String[] split = value.split(sep);
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
		if (j == 1) return "{"+deffield+":"+onlytoken+"}";
		return out;
	}

	public abstract String makeArray(String stringvalue);
	
}



