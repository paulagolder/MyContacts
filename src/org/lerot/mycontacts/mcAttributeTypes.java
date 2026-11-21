package org.lerot.mycontacts;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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

	// the types the Edit page shows in its table (E in displaygroups), for
	// its "Create new attribute" list
	public static ArrayList<String> editableList()
	{
		ArrayList<String> list = new ArrayList<String>();
		for (Entry<String, mcAttributeType> entry : attributetypelist.entrySet())
		{
			String groups = entry.getValue().getDisplaygroup();
			if (groups != null && groups.contains("E")) list.add(entry.getKey());
		}
		return list;
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
		// types added since older databases were made
		addMissingType("whatsapp", "WhatsApp", "cellphone", 43, "EBS");
		// tid is edited in the Edit page's heading, and kind (a vCard's
		// person/group/organisation) is not edited at all; older databases
		// showed both in the Edit table
		hideFromEdit("tid");
		hideFromEdit("kind");
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

	private static void addMissingType(String key, String label, String datatype, int order, String displaygroups)
	{
		if (!doQuery("select attributeKey from attribute where attributeKey = ?", key).isEmpty()) return;
		String sql = "insert into attribute (attributeKey, attributeLabel, dataType, displayOrder, `group`, displaygroups)"
				+ " values (?, ?, ?, ?, 'all', ?)";
		try (PreparedStatement st = conn().prepareStatement(sql))
		{
			st.setString(1, key);
			st.setString(2, label);
			st.setString(3, datatype);
			st.setInt(4, order);
			st.setString(5, displaygroups);
			st.executeUpdate();
		} catch (SQLException e)
		{
			sqlError(e, sql);
		}
	}
	
	private static void hideFromEdit(String key)
	{
		String sql = "update attribute set displaygroups = replace(displaygroups, 'E', '')"
				+ " where attributeKey = ? and displaygroups like '%E%'";
		try (PreparedStatement st = conn().prepareStatement(sql))
		{
			st.setString(1, key);
			st.executeUpdate();
		} catch (SQLException e)
		{
			sqlError(e, sql);
		}
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
