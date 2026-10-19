package org.lerot.mycontacts;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public class mcMappings //extends mcDataObject
{

	private LinkedHashMap<String, String> collection;
	String direction;
	String mappingname;

	public mcMappings(String adirection, String amappingname)
	{
		super();
		mappingname = amappingname;
		direction = adirection;
		collection = new LinkedHashMap<String, String>();
	}

	public Set<Entry<String, String>> entrySet()
	{

		return collection.entrySet();
	}

	public String get(String quallabel)
	{
		return collection.get(quallabel.toLowerCase());

	}

	public Vector<String> getKeys()
	{
		if(!collection.isEmpty())
		{
			Vector<String> vkeys = new Vector<String>();
			Set<String>  a = collection.keySet();
			for(String akey: a)
			{
				vkeys.add(akey);
			}
			return vkeys;
		}
		else
			return null;	
	}
	
	@Override
	public String toString()
	{
		String out = " empty";
		if(!collection.isEmpty())
		{
			out="";
			Vector<String> vkeys = new Vector<String>();
			Set<String>  a = collection.keySet();
			for(String akey: a)
			{
				out += "<"+akey + "> =" + get(akey)+ "  " ;
			}
			return out;
		}
		else
			return out;
	}
	
	
	
	public int size()
	{
		return collection.size();
	}

	LinkedHashMap<String, String> getCollection()
	{
		return collection;
	}

	void setCollection(LinkedHashMap<String, String> collection)
	{
		this.collection = collection;
	}

}
