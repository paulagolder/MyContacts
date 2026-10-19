package org.lerot.mycontacts;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class mcAttributeValue
{
	static boolean isArrayData(String invalue)
	{
		if (invalue == null) return false;
		invalue = invalue.trim();
		if (invalue != null && invalue != "")
		{
			if (invalue.startsWith("{") && invalue.endsWith("}"))
			{ return true; }
		}
		return false;
	}

	private mcAttribute parent = null;
	private String stringvalue = "";
	private String update = null;

	public mcAttributeValue(mcAttribute aparent, String avalue, String update)
	{
		parent = aparent;
		setStringvalue(avalue);
		setUpdate(update);
	}

	public mcAttributeValue(mcAttribute aparent)
	{
		parent = aparent;
	}

	public String getFormattedValue()
	{
		if (isArrayType())
		{
			return mcKeyValueDataType.formattedArrayValue(getStringvalue());
		} else
			return getStringvalue();
	}

	public String getValue()
	{
		if (isArrayType())
		{
			if (isArrayData(getStringvalue()))
				return getStringvalue();
			else
			{
				mcKeyValueDataType dt = (mcKeyValueDataType) parent.getType();
				String arraydata = dt.makeArray(getStringvalue());
				return arraydata;
			}
		}
		return getStringvalue();
	}

	public boolean isArrayType()
	{
		mcDataType type = parent.getType();
		if (type.isArrayType()) return true;
		return false;
	}

	public boolean isNull()
	{
		if (length() < 1)
			return true;
		else
			return false;
	}

	public int length()
	{
		if (getStringvalue() == null)
			return 0;
		else
			return getStringvalue().length();
	}

	public void setValue(String newstr,String update )
	{
		setStringvalue(newstr);
		if(update !=null && update.equalsIgnoreCase("now")) setUpdateNow();
		else setUpdate(update);
	}

	public void setValue(Map<String, String> valuelist, String update)
	{
		String value = mcTextListDataType.makestring(valuelist);
		setValue(value, update);
		
	}
	
	public String getStringvalue()
	{
		return stringvalue;
	}

	private void setStringvalue(String stringvalue)
	{
		this.stringvalue = stringvalue;
	}

	public String getUpdate()
	{
		return update;
	}

	public void setUpdate(String update)
	{
		this.update = update;
	}
	
	public void setUpdateNow()
	{

		Calendar calendar = Calendar.getInstance();
		java.util.Date now = calendar.getTime();
		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(
				now.getTime());
		this.update = currentTimestamp.toString();

	}
	
	public void deleteTags(Set<String> ataglist)
	{
		String newtagvalues = mcTagListDataType.deleteTags(this.getValue(),
				ataglist);
		setValue(newtagvalues,"now");
	}
	
	public void insertTagValues(Set<String> ataglist)
	{
		String newtagvalues = mcTagListDataType.insertTags(this.getValue(),
				ataglist);
		setValue(newtagvalues,"now");
	}
	
	public void replaceTagValues(Set<String> ataglist)
	{
		String newtagvalues = mcTagListDataType.replaceTags(ataglist);
		setValue(newtagvalues,"now");
	}

	public void setValue(String value, String updated, String country,
			String user)
	{
		// currentlty ignoring country and user
		setValue( value,updated);
		
	}
	
	public void loadArrayValueXML(Node anode,String updated)
	{
		Element at = (Element) anode;
			NodeList nl = at.getElementsByTagName("field");
			if (nl != null && nl.getLength() > 0)
			{
				String outline = " { ";
				for (int i = 0; i < nl.getLength(); i++)
				{
					Element fld = (Element) nl.item(i);
					String key = fld.getAttribute("key");
					String value = fld.getAttribute("value");
					if (value.contains("'"))
					{
						outline = outline + key + ":\"" + value + "\" , ";
					} else
						outline = outline + key + ":\'" + value + "\' , ";
				}
				outline += " }";
				setValue(outline,updated);
			}
	}

	
	public void loadValueXML(Node anode,String updated)
	{
		Element at = (Element) anode;
				NodeList nl = at.getElementsByTagName("value");
				Node valuenode;
				if (nl.getLength() > 0)
				{
					valuenode = nl.item(0);
					String value = valuenode.getTextContent();
					setValue(value,updated);
				}			
	}
	
	public void loadImageXML(Node anode,String updated)
	{
		Element at = (Element) anode;
				NodeList nl = at.getElementsByTagName("value");
				String value="";
				for( int i=0;i<nl.getLength();i++)
				{
					Node valuenode = nl.item(i);
					String val = valuenode.getTextContent();
					value += val;					
				}
				setValue(value,updated);
							
	}
	
	public void loadValueXML_b(Node anode,String updated)
	{
		Element at = (Element) anode;
		
					String value = at.getAttribute("value");
					setValue(value,updated);		
		
	}
	
	protected void loadfromDB(ResultSet inmap)
	{
		try
		{
			String strvalue = "*";
			//cid = inmap.getInt("cid");
			strvalue = inmap.getString("value");
			String updated = inmap.getString("update_dt");
			String country = inmap.getString("country");
			String user = inmap.getString("user");
			if (strvalue == null || strvalue.isEmpty())
				setValue("null",updated,country,user);
			else
				setValue(strvalue.trim(),updated,country,user);

		} catch (SQLException e)
		{

		}
	}

	public void insertTagValue(String atag)
	{
   
		String newtagvalues = mcTagListDataType.insertTag(stringvalue,atag);
		setValue(newtagvalues,"now");
	}

	


}
