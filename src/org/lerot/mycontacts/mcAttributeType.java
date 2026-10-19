package org.lerot.mycontacts;

import java.util.*;

public class mcAttributeType extends mcDataObject
{

	String attributeKey;
	private String attributeLabel;
	private String datatypekey;
	private String displaygroup = "E";
	private int displayOrder;
	protected mcDataType dt;

	public mcAttributeType()
	{

	}

	public String getDisplaygroup()
	{
		return displaygroup;
	}

	String getKey()
	{
		return attributeKey;
	}

	public int getOrder()
	{
		return displayOrder;
	}

	void load(Map<String, String> row)
	{
		if (row.containsKey("attributeKey"))
			attributeKey = row.get("attributeKey");
		if (row.containsKey("attributeLabel"))
			attributeLabel = row.get("attributeLabel");
		if (row.containsKey("dataType")) datatypekey = row.get("dataType");
		if (row.containsKey("displayOrder"))
			displayOrder = Integer.parseInt(row.get("displayOrder"));
		if (row.containsKey("displaygroups"))
			displaygroup = row.get("displaygroups");
		if ( displaygroup==null) displaygroup="";
	}

	void setDisplaygroup(String displaygroup)
	{
		this.displaygroup = displaygroup;
	}

	String getAttributeLabel()
	{
		return attributeLabel;
	}

	void setAttributeLabel(String attributeLabel)
	{
		this.attributeLabel = attributeLabel;
	}

	void setDataType()
	{
		dt = mcDataTypes.getType(datatypekey);
	}

	public String arrayToString(Vector<String> valuelist)
	{
		return dt.arrayToArrayString(valuelist);
	}

	public mcDataType getDatatype()
	{
		return dt;
	}

	public Vector<String> getFieldKeyList()
	{
		return null;
	}

	public Map<mcfield, String> getFieldValueMap(String value)
	{
		if (isArray()) return dt.getFieldValueMap(value);
		else
		{
			LinkedHashMap<mcfield, String> dummymap = new LinkedHashMap<mcfield, String>();
			dummymap.put(new mcfield(" fkey", "flabel"), "gfvm");
			return dummymap;
		}

	}

	public LinkedHashMap<String, mcfield> getFieldList()
	{
		if (isArray()) return dt.getFieldList();
		else
		{
			LinkedHashMap<String, mcfield> dummymap = new LinkedHashMap<String, mcfield>();
			dummymap.put("gfl", new mcfield(" fkey", "flabel"));
			return dummymap;
		}
	}

	public Map<String, String> getKeyValueMap(String value)
	{
		if (isArray()) return dt.getKeyValueMap(value);
		else
		{
			Map<String, String> dummymap = new HashMap<String, String>();
			dummymap.put("kvm", "dummy");
			return dummymap;
		}
	}

	public boolean isArray()
	{
		if (dt instanceof mcKeyValueDataType) return true;
		else
			return false;
	}

	public String getFormattedValue(mcAttributeValue attributevalue)
	{
		return dt.getFormattedValue(attributevalue.getValue());
	}

	public String getFormattedValue(mcAttributeValue attributevalue, String fmt)
	{
		return dt.getFormattedValue(attributevalue.getValue(), fmt);
	}

	public String getVcardValue(mcAttributeValue attributevalue)
	{
		return dt.toVcardValue(attributevalue.getValue());
	}

	public boolean isImage()
	{
		if (dt instanceof mcImageDataType) return true;
		else
			return false;
	}

	public boolean isType(String atype)
	{
		String type = dt.getTypekey();
		if (atype.equalsIgnoreCase(type)) return true;
		else
			return false;
	}

	public String arrayToArrayString(Map<String, String> valuelist)
	{
		return dt.arrayToString(valuelist);
	}

	public Set<String> getTags(String value)
	{
		if (isType("textlist")) return mcTagListDataType.getSetfromString(value);
		else
			return null;
	}

	public String getXMLValue(mcAttributeValue attributevalue)
	{
		return dt.toXML(attributevalue.getValue());
	}

	public String toXML(String attributevalue)
	{
		return dt.toXML(attributevalue);
	}

	public String arrayToString(Set<String> tokenlist)
	{
		return dt.setToString(tokenlist);
	}

	

	

}
