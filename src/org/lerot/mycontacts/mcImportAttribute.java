package org.lerot.mycontacts;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class mcImportAttribute extends mcDataObject
{

	private String attimportstatus;
	private String attroot;
	private String attqual;
	private String value;
	private String update;
	private int rownumber;

	

	public mcImportAttribute( String root,String qualifier, String newname)
	{
		super();
		attroot = root;
		attqual=qualifier;
		value = newname;
		attimportstatus = null;
		parseKey(root);
	}

	public mcImportAttribute(String root,String qualifier,  String newname, String status)
	{
		super();
		
		attroot = root;
		attqual=qualifier;
		
		value = newname;
		attimportstatus = status;
		parseKey(root);
		if(attroot.equalsIgnoreCase("phone/home"))
		{
			System.out.println(" problem with 1 " + root + " in " + this);
		}
	}

	private void parseKey(String akey)
	{
		String[] parts = mcUtilities.parseLabel(akey);
		String root = parts[0];
		if (root == null || root.isEmpty())
		{
			System.out.println(" problem with 2 " + akey + "in " + this);
			return;
		}
        if(parts[1]!=null && !parts[1].isEmpty())
        {
        	attroot = parts[0];
		attqual = parts[1];
        }
	}

	public mcImportAttribute()
	{
		
	}

	private void deleteImportAttribute(int rownumber, String aroot, String aqual)
	{
		try
		{
			PreparedStatement st;
			String query = " delete from importDataTable "
					+ " where ROWNUMBER = ? and root = ?  and  qualifier = ?";
			st = con.prepareStatement(query);
			st.setLong(1, rownumber);
			st.setString(2, aroot);
			st.setString(3, aqual);
			st.close();
		} catch (SQLException e)
		{
			e.printStackTrace();

		}
	}

	public String getImportstatus()
	{
		if (attimportstatus == null) return "";
		else
			return attimportstatus;
	}

	public String getAttkey()
	{
		String key = attroot;
		if (attqual !=null && !attqual.isEmpty())
			key+="/"+attqual;
		return key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String avalue)
	{
		value = avalue;
	}

	public boolean ignore()
	{
		if (attroot.equalsIgnoreCase("ignore")) return true;
		else if (attimportstatus != null
				&& (attimportstatus.equalsIgnoreCase("hide") || attimportstatus
						.equalsIgnoreCase("done"))) return true;
		return false;
	}

	private void insertImportAttribute(int contactnumber, String aroot, String aqual,
			String avalue, String importstatus)
	{

		try
		{
			PreparedStatement st;
			String query = " insert into importDataTable ( ROWNUMBER, root, qualifier,value, importstatus) "
					+ " values (?,?,?,?,?)";
			st = con.prepareStatement(query);
			st.setInt(1, contactnumber);
			st.setString(2, aroot.trim());
			st.setString(3, aqual.trim());
			st.setString(4, avalue.trim());
			st.setString(5, importstatus);
			int rescount = st.executeUpdate();
			st.close();

			if (rescount == 0)
				System.out
						.println(" no insert made (" + avalue + ","
								+ importstatus + "," + contactnumber + ","
								+ getAttkey());
			// else
			// System.out.println("Sucessful insert " + locallabel + "="
			// + avalue);
		} catch (SQLException e2)
		{
			e2.printStackTrace();
		}

	}

	void load(Map<String, String> inmap)
	{
		if (inmap.containsKey("root"))
			attroot = (inmap.get("root"));
		if (inmap.containsKey("value")) value = (inmap.get("value"));
		if (inmap.containsKey("updatedt")) setUpdate((inmap.get("updatedt")));
		if (inmap.containsKey("qualifier")) attqual = (inmap.get("qualifier"));
		if (inmap.containsKey("importstatus"))
			attimportstatus = (inmap.get("importstatus"));
		if (inmap.containsKey("ROWNUMBER"))
			rownumber = Integer.parseInt(inmap.get("ROWNUMBER"));
	}

	public void setImportstatus(String importstatus)
	{
		this.attimportstatus = importstatus;
	}

	public void setAttkey(String newlabel)
	{
		String[] part = mcUtilities.parseLabel(newlabel);
		attroot= part[0];
		attqual=part[1];
	}

	@Override
	public String toString()
	{
		String out = " locallabel= " + getAttkey() + " value =" + getValue()
				+ " status=" + attimportstatus;
		return out;
	}

	public void update(int rownumber)
	{
		if (value == null || value == "" || value.isEmpty()) deleteImportAttribute(
				rownumber, attroot,attqual);
		else
			updateImportAttribute(rownumber, attroot,attqual, value, attimportstatus);
	}

	public void insert(int rownumber)
	{
		if (value == null || value.trim().isEmpty()) return;
		else
			insertImportAttribute(rownumber, attroot,attqual, value, attimportstatus);
	}

	public void updateFrom(int rownumber, String oldvalue)
	{
		if (value == null || value == "" || value.isEmpty()) deleteImportAttribute(
				rownumber, attroot,attqual);
		else if (oldvalue == null || oldvalue == "null" || oldvalue.isEmpty()) insertImportAttribute(
				rownumber, attroot, attqual,value, attimportstatus);
		else if (!value.equals(oldvalue))
			updateImportAttribute(rownumber, attroot, attqual,value, attimportstatus);
	}

	private void updateImportAttribute(int rownumber, String aroot,String aqual,
			String avalue, String importstatus)
	{

		try
		{
			PreparedStatement st;
			String query = " update  importDataTable set value = ? , importstatus = ? "
					+ " where ROWNUMBER = ? and root = ? and qualifier =  ";
			st = con.prepareStatement(query);

			st.setString(1, avalue);
			st.setString(2, importstatus);
			st.setInt(3, rownumber);
			st.setString(4, aroot);
			st.setString(5, aqual);
			int rescount = st.executeUpdate();
			st.close();
			if (rescount == 0)
			{
				insertImportAttribute(rownumber, aroot, aqual, avalue,
						importstatus);
			} // else
				// System.out.println(" successful update " + locallabel + "="
				// + avalue);
		} catch (SQLException e)
		{
			e.printStackTrace();

		}

	}

	

	public void updateStatus(String newstatus)
	{
		updateImportAttribute(rownumber, attroot,attqual, value, newstatus);
	}

	public void setValue()
	{
		// TODO Auto-generated method stub

	}

	public boolean containedBy(String invalue)
	{
		String nvalue = value.toLowerCase();
		String[] values = invalue.split(",");

		for (String avalue : values)
		{
			if (!(avalue.trim().isEmpty()))
			{
				if (!nvalue.contains(avalue))
				{
					System.out.println(" not found " + avalue + " in " + value);
					return false;
				}
			}
		}
		return true;
	}

	public int getRownumber()
	{
	
		return rownumber;
	}

	public String getRoot()
	{

		return attroot;
	}

	public String getQualifier()
	{
	
		return attqual;
	}

	public String getUpdate()
	{
		return update;
	}

	private void setUpdate(String update)
	{
		this.update = update;
	}
}
