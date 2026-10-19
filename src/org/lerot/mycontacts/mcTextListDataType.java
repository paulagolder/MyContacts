package org.lerot.mycontacts;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.*;
import java.util.Map.Entry;

public class mcTextListDataType extends mcDataType
{

	private static String makeSimpleString(Set<String> tokenlist)
	{
		String tokenstring = "";
		for (String atoken : tokenlist)
		{
			if (!atoken.isEmpty() && !atoken.equalsIgnoreCase("null"))
			{
				tokenstring = tokenstring + atoken + ", ";
			}
		}
		return tokenstring;
	}

	static String makeString(Set<String> tokenlist)
	{
		String tokenstring = "";
		for (String atoken : tokenlist)
		{
			// System.out.println("|"+atag+"|"+atag.length());
			if (!atoken.isEmpty() && !atoken.equalsIgnoreCase("null")
					&& atoken.length() > 2)
			{
				tokenstring = tokenstring + atoken + "; ";
			}

		}
		return tokenstring;
	}

	static String makeString(String atoken)
	{
		String tokenstring = "";

		if (!atoken.isEmpty() && !atoken.equalsIgnoreCase("null")
				&& atoken.length() > 2)
		{
			tokenstring = atoken + "; ";
		}
		return tokenstring;
	}

	public static String makestring(Map<String, String> valuelist)
	{
		String tokenstring = "{";
		for (Entry<String, String> anentry : valuelist.entrySet())
		{
			tokenstring += anentry.getKey() + ":" + anentry.getValue() + ", ";
		}
		tokenstring += "}";
		return tokenstring;
	}

	public mcTextListDataType()
	{
		super("textlist", "textlist");
	}

	public mcTextListDataType(String astring, String bstring)
	{
		super(astring, bstring);
	}

	@Override
	public int compareTo(String aarray, String barray)
	{
		SortedSet<String> aset = getTokens(aarray);
		SortedSet<String> bset = getTokens(barray);
		for (final Iterator<String> it = bset.iterator(); it.hasNext();)
		{
			String btag = it.next();
			if (!aset.contains(btag))
			{ return 1; }
		}
		for (final Iterator<String> it = aset.iterator(); it.hasNext();)
		{
			String atag = it.next();
			if (!bset.contains(atag))
			{ return -1; }
		}
		return 0;
	}

	@Override
	public Vector<String> getFieldKeyList()
	{
		return null;
	}

	@Override
	public LinkedHashMap<String, mcfield> getFieldList()
	{
		return null;
	}

	@Override
	public Map<mcfield, String> getFieldValueMap(String value)
	{
		return null;
	}

	@Override
	public String getFormattedValue(String value, String fmt)
	{
		Set<String> str = getTokens(value);
		return makeString(str);
	}

	@Override
	public Map<String, String> getKeyValueMap(String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getTextListMap(String value)
	{
		Map<String, String> sortedmap = new LinkedHashMap<String, String>();
		SortedSet<String> tokens = getTokens(value);
		int k = 0;
		for (String atoken : tokens)
		{
			String akey = new Integer(k).toString();
			sortedmap.put(akey, atoken);
		}
		return sortedmap;
	}

	@Override
	public String toVcardValue(String value)
	{
		SortedSet<String> tokenlist = getTokens(value);
		String outlist = "";
		for (String token : tokenlist)
		{
			// if(tag.contains(";")) tag.replace(";",":");// should not be
			// necessary if data properly formated paul fix
			outlist += token + ";";
		}
		return outlist;
	}

	@Override
	public boolean matchesVcardValue(String avalue, String bvalue)
	{
		if (compareTo(bvalue, avalue) == 0)
			return true;
		else
			return false;
	}

	@Override
	public String toXML(String value)
	{
		SortedSet<String> tokenlist = getTokens(value);
		String outxml = "  <value>";
		String sep = "";
		for (String atoken : tokenlist)
		{
			String outtag = StringEscapeUtils.escapeXml(atoken);
			if (outtag.contains(",")) outtag = "\"" + outtag + "\"";
			outxml += sep + outtag;
			sep = ",";
		}
		outxml += " </value>";
		return outxml;
	}

	@Override
	public boolean valueContained(String testvalue, String attvalue)
	{
		SortedSet<String> taglist = getTokens(attvalue);
		if (taglist.contains(testvalue))
		{ return true; }
		return false;
	}

	public static Vector<String> parseTokenString(String csv_string,
			char delimiter)
	{
		Vector<String> outarray = new Vector<String>();
		int l = csv_string.length();
		int i = 0;
		String token = "";
		int count = 0;
		boolean instring = false;
		char endquote = '"';
		while (i < l)
		{
			char c = csv_string.charAt(i);
			if (instring)
			{
				if (c == endquote)
					instring = false;
				else
					token = token + c;
			} else
			{
				if (c == delimiter)
				{
					outarray.add(count, token);
					token = "";
					count = count + 1;

				} else
				{
					if (c == '"')
					{
						instring = true;
						endquote = c;
					} else
						token = token + c;
				}
			}
			i++;
		}
		if (token != "")
		{
			outarray.add(count, token);
			token = "";
			count = count + 1;
		}
		return outarray;
	}

	static SortedSet<String> getTokens(String tags)
	{
		SortedSet<String> tokens = new TreeSet<String>();
		char sep = ';';
		if (!tags.contains(";")) sep = ',';
		Vector<String> tokenarray = parseTokenString(tags, sep);
		for (String token : tokenarray)
		{
			token = token.trim();
			if (!token.isEmpty() && !token.equals(";") && !token.equals("null")
					&& !tokens.contains(token) && token.length() > 1)
			{
				token = token.replace("#", "");
				token = token.replace(";", "");
				tokens.add(token);
			}
		}
		return tokens;
	}

}
