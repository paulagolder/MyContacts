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

	// the label a type shows, else its key
	public static String labelOf(String key)
	{
		mcAttributeType type = findType(key);
		String label = type == null ? null : type.getAttributeLabel();
		return label == null || label.isBlank() ? key : label;
	}

	// the Edit page's first suggestion for a new detail: of the types the
	// contact has none of yet, the one most contacts have (ties in display
	// order); never name (every contact has one) or photo (better chosen than
	// offered); else the first type
	public static String suggestedNew(List<String> keys, Collection<String> has, Map<String, Integer> usage)
	{
		String best = null;
		int most = -1;
		for (String key : keys)
		{
			if (key.equals("photo") || key.equals("name") || has.contains(key)) continue;
			int n = usage.getOrDefault(key, 0);
			if (n > most)
			{
				best = key;
				most = n;
			}
		}
		if (best != null) return best;
		return keys.isEmpty() ? null : keys.get(0);
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
		addMissingType("whatsapp", "WhatsApp", "cellphone", 43, "EBSL");
		// tid is edited in the Edit page's heading, and kind (a vCard's
		// person/group/organisation) is not edited at all; older databases
		// showed both in the Edit table
		hideFromEdit("tid");
		hideFromEdit("kind");
		addLiteGroup();
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
	
	// the types the Contacts window (myContactsLite) shows: L in displaygroups.
	// Older databases had no L; they get it on the types the window always
	// showed, once only, so a type unticked later stays unticked
	static final List<String> LITE_DEFAULT = List.of("name", "photo", "address", "phone", "mobile", "whatsapp",
			"email", "birthday");

	private static void addLiteGroup()
	{
		if (!doQuery("select attributeKey from attribute where displaygroups like '%L%'").isEmpty()) return;
		for (String key : LITE_DEFAULT)
			execute("update attribute set displaygroups = coalesce(displaygroups, '') || 'L' where attributeKey = ?", key);
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

	// the types the program relies on, which Tools > Attribute Types will not delete
	public static final Set<String> CORE = Set.of("name", "tid", "tags", "default", "kind", "key", "photo",
			"memberof", "hasmember", "related", "note", "address", "phone", "mobile", "whatsapp", "email",
			"birthday");

	// how many contacts have a detail of each type
	public static Map<String, Integer> usage()
	{
		Map<String, Integer> counts = new HashMap<String, Integer>();
		for (Map<String, String> row : doQuery("select root, count(distinct cid) as n from attributeValues group by root"))
			counts.put(row.get("root"), Integer.parseInt(row.get("n")));
		return counts;
	}

	// the key a new type's label gives: lower case letters and digits only
	public static String keyFor(String label)
	{
		return label.toLowerCase().replaceAll("[^a-z0-9]", "");
	}

	public static void addType(String label, String datatype, int order, String displaygroups)
	{
		addMissingType(keyFor(label), label, datatype, order, displaygroups);
		loadAttributeTypes();
	}

	public static void updateType(String key, String label, int order, String displaygroups)
	{
		execute("update attribute set attributeLabel = ?, displayOrder = ?, displaygroups = ? where attributeKey = ?",
				label, order, displaygroups, key);
		loadAttributeTypes();
		fireContactsChanged(); // the Contacts window shows the change
	}

	// only a type no contact uses
	public static void deleteType(String key)
	{
		execute("delete from attribute where attributeKey = ? and attributeKey not in"
				+ " (select distinct root from attributeValues)", key);
		loadAttributeTypes();
	}

	// the tag Tools > Attribute Types gives the contacts with a type: "has fax"
	public static String usersTag(String key)
	{
		return "has " + key;
	}

	// gives every contact with a detail of this type the tag usersTag(key), and
	// takes it off contacts that no longer have one, so the tag lists exactly
	// the contacts that have it now; returns how many have it
	public static int tagUsers(String key)
	{
		String tag = usersTag(key);
		Set<String> users = new HashSet<String>();
		for (Map<String, String> row : doQuery("select distinct cid from attributeValues where root = ?", key))
			users.add(row.get("cid"));
		for (Map<String, String> row : doQuery("select cid, value from attributeValues where root = 'tags'"))
		{
			String cid = row.get("cid");
			if (users.contains(cid) || !mcTagListDataType.getSetfromString(row.get("value")).contains(tag)) continue;
			mcContact former = mcContact.getContact(cid);
			if (former != null) former.removeTag(tag);
		}
		for (String cid : users)
		{
			mcContact user = mcContact.getContact(cid);
			if (user != null) user.addTag(tag);
		}
		return users.size();
	}

	private static void execute(String sql, Object... params)
	{
		try (PreparedStatement st = conn().prepareStatement(sql))
		{
			for (int i = 0; i < params.length; i++) st.setObject(i + 1, params[i]);
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
