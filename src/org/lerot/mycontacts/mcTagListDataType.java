package org.lerot.mycontacts;

import org.apache.commons.text.StringEscapeUtils;

import java.util.*;

public class mcTagListDataType extends mcTextListDataType
{

	public static String deleteTags(String att, Set<String> ataglist)
	{
		SortedSet<String> oldtags = getSetfromString(att);
		for (String atag : ataglist)
		{
			oldtags.remove(atag);
		}
		return makeTagString(oldtags);
	}

	public static Set<String> getTags(mcAttribute tatt)
	{
		return getSetfromString(tatt.getValue());
	}

	static SortedSet<String> getSetfromString(String tags)
	{
		SortedSet<String> tokens = new TreeSet<String>();
		char sep = ';';
		tags = tags.trim();
		if(tags.endsWith(";"))sep = ';';
		else if (tags.endsWith(",")) sep=',';
		else sep = ';';
		Vector<String> tokenarray = parseTagString(tags, sep);
		for (String token : tokenarray)
		{
			token = token.trim();
			if (!token.isEmpty() && !token.equals(";") && !token.equals("null") && !token.equals(",")
					&& !tokens.contains(token) && token.length() > 1)
			{
				token = token.replace("#", "");
				token = token.replace(";", "");
				if(token.startsWith(",")) token = token.substring(1);
				tokens.add(token);
			}
		}
		return tokens;
	}

	public static String insertTag(String stringvalue, String atag)
	{
		Set<String> oldtags = getSetfromString(stringvalue);
		if (atag.length() > 1)
		{
			if (atag.contains(";")) atag.replace(";", ":");
			oldtags.add(atag);
		}
		return makeTagString(oldtags);
	}

	public static String insertTags(String stringvalue, Set<String> ataglist)
	{
		Set<String> oldtags = getSetfromString(stringvalue);
		for (String atag : ataglist)
		{
			if (atag.length() > 1)
			{
				if (atag.contains(";")) atag.replace(";", ":");
				oldtags.add(atag);
			}
		}
		return makeTagString(oldtags);
	}

	public static String replaceTags( Set<String> ataglist)
	{
		for (String atag : ataglist)
		{
			if (atag.length() > 1)
			{
				if (atag.contains(";")) atag.replace(";", ":");
			}
		}
		return makeTagString(ataglist);
	}
	
	public static String makeString(Set<String> taglist)
	{
		String outtags = "";
		for (String atag : taglist)
		{
			// System.out.println("|"+atag+"|"+atag.length());
			if (!atag.isEmpty() && !atag.equalsIgnoreCase("null")
					&& atag.length() > 2)
			{
				outtags = outtags + atag + "; ";
			}

		}
		return outtags;
	}

	public static String makeTagString(Set<String> taglist)
	{
		String outtags = "";
		for (String atag : taglist)
		{
			// System.out.println("|"+atag+"|"+atag.length());
			if (!atag.isEmpty() && !atag.equalsIgnoreCase("null")
					&& atag.length() > 2)
			{
				outtags = outtags +  atag + "; ";
			}

		}
		return outtags;
	}

	public static Vector<String> parseTagString(String csv_string,
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

	public mcTagListDataType()
	{
		super("taglist", "taglist");
	}

	@Override
	public int compareTo(String aarray, String barray)
	{
		SortedSet<String> aset = getSetfromString(aarray);
		SortedSet<String> bset = getSetfromString(barray);
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
		Set<String> str = getSetfromString(value);
		return makeString(str);
	}

	@Override
	public Map<String, String> getKeyValueMap(String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getTextListMap(String value)
	{
		Map<String, String> sortedmap = new LinkedHashMap<String, String>();
		SortedSet<String> tags = getSetfromString(value);
		int k = 0;
		for (String atag : tags)
		{
			String akey = new Integer(k).toString();
			sortedmap.put(akey, atag);
		}
		return sortedmap;
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
	public String toVcardValue(String value)
	{
		SortedSet<String> taglist = getSetfromString(value);
		String outlist = "";
		for (String tag : taglist)
		{
			// if(tag.contains(";")) tag.replace(";",":");// should not be
			// necessary if data properly formated paul fix
			outlist += tag + ";";
		}
		return outlist;
	}

	@Override
	public String toXML(String value)
	{
		SortedSet<String> taglist = getSetfromString(value);
		String outxml = "  <value>";
		String sep = "";
		for (String atag : taglist)
		{
			String outtag = StringEscapeUtils.escapeXml10(atag);
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
		SortedSet<String> taglist = getSetfromString(attvalue);
		if (taglist.contains(testvalue))
		{ return true; }
		return false;
	}

}
