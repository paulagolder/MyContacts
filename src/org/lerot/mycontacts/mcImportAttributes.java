package org.lerot.mycontacts;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class mcImportAttributes extends mcDataObject
{

	private TreeMap<String, mcImportAttribute> attributes;

	public mcImportAttributes()
	{
		attributes = new TreeMap<String, mcImportAttribute>();
	}

	public void addImportAttribute(mcImportAttribute anattribute)
	{
		
		attributes.put(anattribute.getAttkey(), anattribute);
	}

	public Set<Entry<String, mcImportAttribute>> entrySet()
	{
		return attributes.entrySet();
	}

	public mcImportAttribute get(String akey)
	{
		return attributes.get(akey);
	}

	TreeMap<String, mcImportAttribute> getAttributes()
	{
		return attributes;
	}

	public void put(String akey, mcImportAttribute att)
	{
		attributes.put(akey, att);
	}

	void setAttributes(TreeMap<String, mcImportAttribute> attributes)
	{
		this.attributes = attributes;
	}

	public int size()
	{
		return attributes.size();
	}

	public boolean contains(String akey)
	{
		return attributes.containsKey(akey);
	}

}
