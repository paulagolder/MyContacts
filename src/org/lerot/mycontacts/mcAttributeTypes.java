package org.lerot.mycontacts;

import java.util.*;
import java.util.Map.Entry;


public class mcAttributeTypes extends mcDataObject
{
	static HashMap<String, mcAttributeType> attributetypelist;

	public mcAttributeTypes()
	{
		super();		
	}

	public static ArrayList<String> toList()
	{
		Set<String> list = attributetypelist.keySet();
		ArrayList<String> mainList = new ArrayList<String>();
		mainList.addAll(list);
		return  mainList;
	}

	public static boolean containsKey(String key)
	{
		return attributetypelist.containsKey(key);
	}

	public static mcAttributeType findType(String atttype)
	{
		if (attributetypelist.containsKey(atttype)) return attributetypelist
				.get(atttype);
		else
		{
			return null;
		}
	}

	public static HashMap<String, mcAttributeType> getAttributeTypes()
	{
		return attributetypelist;
	}

	public static void loadAttributeTypes()
	{
		ArrayList<Map<String, String>> rowlist = doQuery("select * from attribute order by displayOrder ");
		attributetypelist = new LinkedHashMap<String, mcAttributeType>();
		for (Map<String, String> row : rowlist)
		{
			mcAttributeType alabtype = new mcAttributeType();
			alabtype.load(row);
			alabtype.setDataType();
			String akey = alabtype.getKey();
			attributetypelist.put(akey, alabtype);
		}
		System.out.println( " loaded attributetypes "+attributetypelist.size());
	}
	
	public static Set<Entry<String, mcAttributeType>> entrySet()
	{
		return attributetypelist.entrySet();
	}

	public static HashMap<String, mcAttributeType> getAll()
	{
		return attributetypelist;
	}

}
