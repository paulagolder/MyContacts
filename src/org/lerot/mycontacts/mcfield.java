package org.lerot.mycontacts;

import java.util.Map;

public class mcfield
{
	private String key;
	private String label;
	private int order;
	private String type;

	public mcfield(String akey, String alabel)
	{
		key = akey;
		label = alabel;
	}

	public mcfield()
	{
	}

	public String getKey()
	{
		return key;
	}

	public String getLabel()
	{
		return label;
	}

	public void load(Map<String, String> inmap)
	{
		if (inmap.containsKey("fieldLabel")) setLabel(inmap.get("fieldLabel"));
		if (inmap.containsKey("type")) type = inmap.get("type");
		if (inmap.containsKey("fieldKey")) setKey(inmap.get("fieldKey"));
		if (inmap.containsKey("displayOrder"))
			order = Integer.valueOf(inmap.get("displayOrder"));
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public int getOrder()
	{
		return order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

}