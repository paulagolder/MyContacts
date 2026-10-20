package org.lerot.mycontacts;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

public class mcDataTypes extends mcDataObject
{
	static HashMap<String, mcDataType> typelist;
	static Vector<mcDataType> vectoroftypes = new Vector<mcDataType>();

	public mcDataTypes()
	{
		super();
	}

	public static mcDataType getType(String typekey)
	{
		if (typelist.containsKey(typekey)) return typelist.get(typekey);
		else
			return null;
	}

	public static  void loadTypes()
	{
		getConnection();
		typelist = new HashMap<String, mcDataType>();
		String datatype = "+";
		String query = "select * from dataType order by key ";
		PreparedStatement st;
		try
		{
			st = conn().prepareStatement(query);
			ResultSet resset = st.executeQuery();
			while (resset.next())
			{
				datatype = resset.getString("Type");
				mcDataType atype = mcDataType.factory(datatype);
				atype.load(resset);
				if (atype.getTypekey() != null)
					typelist.put(atype.getTypekey(), atype);
			}
		} catch (Exception e)
		{
			System.out.println(" Error loading datatypes " + datatype);
		}
		vectoroftypes = new Vector<mcDataType>();
		for (Entry<String, mcDataType> entry : typelist.entrySet())
		{
			mcDataType atype = entry.getValue();
			vectoroftypes.add(atype);
		}
		System.out.println("loaded datatypes " +vectoroftypes.size());
		disconnect();
	}

}
