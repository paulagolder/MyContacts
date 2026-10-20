package org.lerot.mycontacts;

import org.apache.commons.text.StringEscapeUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class mcPhoneDataType extends mcDataType
{

	mcPhoneDataType()
	{
		super("phone", "phone");
	}

	public mcPhoneDataType(String key, String value)
	{
		super(key, value);
	}

	@Override
	public String getFormattedValue(String value, String fmt)
	{

		return value;
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
	public Map<String, String> getKeyValueMap(String value)
	{
		return null;
	}

	@Override
	public String toVcardValue(String value)
	{
		return value;
	}

	@Override
	public String toXML(String value)
	{
		return "  <value>"+StringEscapeUtils.escapeXml10(value)+"</value>";
	}

	@Override
	public boolean valueContained(String avalue, String bvalue)
	{
		String fnvalue = mcUtilities.normalisephonenumber(avalue);
		String lctestvalue = mcUtilities.normalisephonenumber(bvalue);
		if (fnvalue.contains(lctestvalue)) return true;
		if (lctestvalue.length() > 7)
		{
			lctestvalue = lctestvalue.substring(lctestvalue.length() - 7);
		}
		if (fnvalue.contains(lctestvalue)) return true;
		else
		{
			return false;
		}
	}


	

	@Override
	public int compareTo(String avalue, String bvalue)
	{
		// TODO Auto-generated method stub
		if(valueContained(avalue,bvalue))
		{
			if(valueContained(bvalue,avalue)) return 0;
			else return 1;
		}
		return -1;
	}

	@Override
	public boolean matchesVcardValue(String avalue, String bvalue)
	{
		if(compareTo(avalue,bvalue) == 0) return true;
		else
			return false;
	}
	
	
	
	

}
