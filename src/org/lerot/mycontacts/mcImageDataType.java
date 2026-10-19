package org.lerot.mycontacts;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class mcImageDataType extends mcDataType
{

	public mcImageDataType()
	{
		super("image", "image");
	}

	public static String[] getVCardEncodedLines(String foriegnlabel, String rawvalue)
	{

		int linelen = 75;
		String alltext = foriegnlabel + ":" + rawvalue;
		int k = 0;
		int len = alltext.length();
		int linecount = (len - 1) / linelen;
		String[] outlist = new String[linecount + 2];
		int lineno = 0;
		String padding = "";
		while (k + linelen < len)
		{
			String subtext = alltext.substring(k, k + linelen);
			outlist[lineno] = padding + subtext;
			lineno++;
			k = k + linelen;
			linelen = 74;
			padding = " ";
		}
		String lastline = alltext.substring(k);
		int groupcount = (lastline.length() + 2) / 3;
		int lastlinelength = groupcount * 3;
		String vlastline = (lastline + "==").substring(0, lastlinelength - 1);
		outlist[lineno] = padding + vlastline;
		return outlist;
	}
	
	public static String[] getEncodedLines( String rawvalue)
	{

		int linelen = 75;
		String alltext = rawvalue;
		int k = 0;
		int len = alltext.length();
		int linecount = (len - 1) / linelen;
		String[] outlist = new String[linecount + 1];
		int lineno = 0;
		while (k + linelen < len)
		{
			String subtext = alltext.substring(k, k + linelen);
			outlist[lineno] =  subtext;
			lineno++;
			k = k + linelen;
		}
		String lastline = alltext.substring(k);
		outlist[lineno] = lastline;
		return outlist;
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
		return null;
	}

	@Override
	public Map<String, String> getKeyValueMap(String value)
	{
		return null;
	}

	@Override
	public String toVcardValue(String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toXML(String value)
	{
		String output="";
		String[] lines = getEncodedLines(value);
		for(String aline : lines)
		{
			output  += "  <value>"+aline+"</value>"+ "\n";;
		}
		return output;
		
	}

	@Override
	public boolean valueContained(String attvalue, String testvalue)
	{

		if (testvalue.equals(attvalue)) return true;
		else
			return false;
	}

	

	@Override
	public int compareTo(String aarray, String barray)
	{
		return 0;
	}
	
	@Override
	public boolean matchesVcardValue(String avalue, String bvalue)
	{
		if(avalue!=null && !avalue.isEmpty())
		{
			if(bvalue!=null && ! bvalue.isEmpty())
			{
				int al = avalue.length();
				int bl = bvalue.length();
				if (Math.abs(al-bl) > 1 ) return false;
				else return true;
			}
			return false ;
		}
		else		
		{
			if(bvalue!=null && ! bvalue.isEmpty())
			{
				 return false;
			}
			else return true;
		}
	}
}
