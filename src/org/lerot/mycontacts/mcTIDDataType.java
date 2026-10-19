package org.lerot.mycontacts;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class mcTIDDataType extends mcDataType
{

	public mcTIDDataType()
	{
		super("tid", "tid");
	}

	public mcTIDDataType(String key, String value)
	{
		super(key, value);
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
	public String getFormattedValue(String value)
	{
		return value;
	}

	@Override
	public String getFormattedValue(String value, String fmt)
	{
		return value;
	}

	@Override
	public Map<String, String> getKeyValueMap(String value)
	{
		return null;
	}

	@Override
	public String toVcardValue(String value)
	{
		return value.toLowerCase().trim();
	}

	@Override
	public String toXML(String value)
	{
		return "  <value>"+StringEscapeUtils.escapeXml(value)+"</value>";
	}

	@Override
	public boolean valueContained(String attvalue, String testvalue)
	{
		if (attvalue.contains(testvalue)) return true;
		else
			return false;
	}

	@Override
	public boolean matchesVcardValue(String avalue, String bvalue)
	{
		if (avalue.equals(bvalue)) return true;
		if (avalue.equalsIgnoreCase(bvalue)) return true;
		return false;
	}

	@Override
	public int compareTo(String avalue, String bvalue)
	{
		String avalueic = avalue.toLowerCase();
		String bvalueic = bvalue.toLowerCase();
		
		return avalueic.compareTo(bvalueic);
	}

    public static String toTidyStr(String value)
    {
        String tidytid = value.toString().replace("&", " ");
        tidytid = tidytid.replace("  ", " ");
        return tidytid;
    }
	

}
