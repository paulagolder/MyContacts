package org.lerot.mycontacts;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

public class mctagList extends mcDataObject
{

	private  Map<String, Integer> taglist;

	public mctagList()
	{
		super();
	}

	public  void reloadTags()
	{
		Map<String, Integer> ustaglist = new HashMap<String, Integer>();
		taglistcomparator comparator = new taglistcomparator(ustaglist);
		setTaglist(new TreeMap<String, Integer>(comparator));
		String query = " select * from attributeValues where root = 'tags'  ";
		PreparedStatement st;
		int k = 1;
		try
		{
			getConnection();
			st = conn().prepareStatement(query);
			ResultSet resset = st.executeQuery();
			while (resset.next())
			{
				mcAttribute tatt = new mcAttribute(k, "tags", "");
				tatt.getAttributevalue().loadfromDB(resset);
				if (tatt.isType("textlist"))
				{
					Set<String> tagset = mcTagListDataType.getTags(tatt);
					for (String tag : tagset)
					{
						if (ustaglist.containsKey(tag))
						{
							ustaglist.put(tag, ustaglist.get(tag) + 1);
						} else
						{
							ustaglist.put(tag, 1);
						}
					}
					k++;
				}
			}
			st.close();
			getTaglist().putAll(ustaglist);
			disconnect();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public  void renorm()
	{
		String query = " select * from attributeValues where root =  'tags'   ";
		PreparedStatement st;
		int k = 1;
		try
		{
			getConnection();
			st = conn().prepareStatement(query);
			ResultSet resset = st.executeQuery();
		
			Vector<mcAttribute> attarr = new Vector<mcAttribute>();
			while (resset.next())
			{
				int cid = resset.getInt("cid");
				String qual = resset.getString("qualifier");
				mcAttribute tatt = new mcAttribute(cid, "tags",qual);
				
				tatt.getAttributevalue().loadfromDB(resset);
				if (tatt.isType("textlist"))
				{		
					 attarr.add(tatt);						
					 k++;
				}
			}
			st.close();
			disconnect();
			k=0;
			for (mcAttribute tatt : attarr)
			{					
				Set<String> tagset = mcTagListDataType.getTags(tatt);
				mcAttributeValue attv = tatt.getAttributevalue();
				attv.replaceTagValues(tagset);
				tatt.setAttributevalue(attv);
				tatt.dbupdateAttribute();
				System.out.println("===="+tatt.getValue());				
				k++;
			}
			System.out.println("===="+k +" updated ");
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void replaceall(String attkey, String mergefrom,
			String mergeto)
	{
		String query = " update attributeValues set value = replace(value, ?, ?)  where root LIKE ?  ";
		PreparedStatement st;
		try
		{
			getConnection();
			st = conn().prepareStatement(query);
			st.setString(1,  mergefrom );
			st.setString(2,  mergeto );
			st.setString(3, attkey + "%");
			System.out.println("After : " + st.toString());
			st.executeUpdate();
			st.close();
			disconnect();
		} catch (SQLException e)
		{
			mcDataObject.sqlError(e);
		}
	}
	
	public void duplicateall(String attkey, String oldtag, String newtag)
	{
		String query = " update attributeValues set value =( value || ? )  where root LIKE ? and value LIKE ? ";
		PreparedStatement st;
		try
		{
			getConnection();
			st = conn().prepareStatement(query);
			st.setString(1, newtag );
			st.setString(2, attkey + "%");
			st.setString(3, "%"+oldtag + ",%");
			//System.out.println("After : " + st.toString());
			st.executeUpdate();
			st.close();
			disconnect();
		} catch (SQLException e)
		{
			mcDataObject.sqlError(e);
		}
	}

	public void delete(String attkey, String todelete)
	{
		String query = " update attributeValues set value = replace(value, ?, '')  where root LIKE ? ";
		PreparedStatement st;
		try
		{
			getConnection();
			st = conn().prepareStatement(query);
			st.setString(1,  todelete );
			st.setString(2, attkey);

			st.executeUpdate();

			st.close();
			disconnect();
		} catch (SQLException e)
		{
			mcDataObject.sqlError(e);
		}

	}

	public  Map<String, Integer> getTaglist()
	{
		return taglist;
	}

	public void setTaglist(Map<String, Integer> ataglist)
	{
		taglist = ataglist;
	}

	public boolean isEmpty()
	{
		if(taglist.size()<1) return true;
		return false;
	}

	public int size()
	{
		return taglist.size();
	}

	public Set<Entry<String, Integer>> entrySet()
	{
		return taglist.entrySet();
	}

	public Vector<String> toList()
	{
		Vector<String> tags = new Vector<String>();
		for( Entry<String, Integer> entrytag : entrySet())
		{
			tags.add(entrytag.getKey());
		}
		return tags;
	}

	public String get(int i)
	{
		return toList().get(0);
	}

	
}
