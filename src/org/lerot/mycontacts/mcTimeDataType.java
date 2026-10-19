package org.lerot.mycontacts;

import org.apache.commons.lang3.StringEscapeUtils;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class mcTimeDataType extends mcDataType
{

		public mcTimeDataType()
		{
			super("timestamp", "timestamp");
		}

		public mcTimeDataType(String key, String value)
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
		public boolean valueContained(String testvalue, String attvalue)
		{
			String fnvalue = attvalue;
			String lctestvalue = testvalue;
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
		public int compareTo(String attvalue, String impvalue)
		{
			 Timestamp testts = Timestamp.valueOf(impvalue);
			 Timestamp attts = Timestamp.valueOf(attvalue);
			 return attts.compareTo(testts);
		}

		@Override
		public boolean matchesVcardValue(String avalue, String bvalue)
		{ 
			if(compareTo(avalue,bvalue)==0)return true;
			return false;
		}
	}
	
