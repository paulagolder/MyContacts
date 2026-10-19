package org.lerot.mycontacts;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class mcCIDDataType extends mcDataType
	{

		mcCIDDataType()
		{
			super("cid", "cid");
		}

		public mcCIDDataType(String key, String value)
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

}
