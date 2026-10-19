package org.lerot.mycontacts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;


public class mcCorrespondance extends mcDataObject
{

    private int correspondanceid;
    private int contactid;
    private String filename;
    private String lastmodifieddate;
    private String mime;
    private String path;
    private String role;


    /*    public int compareTo(mcCorrespondance two ) {
            int diff = this.date.compareTo(two.date);//<-- compare ints
            if( diff != 0 ) { // they have different int
                return diff;
            }
            else return 0;
        }*/

    public mcCorrespondance(int i)
    {
        setCorrespondanceid(i);
    }

    public mcCorrespondance(int lid, int cid, String name, String sdate, String instatus)
    {
        correspondanceid = lid;
        this.contactid = cid;
        filename = name;
        lastmodifieddate = sdate;
        role = instatus;
    }

  /*  public void fill(Map<String, String> row)
    {
        setContactid(Integer.parseInt(row.get("cid")));
        correspondanceid = Integer.parseInt(row.get("correspondanceid"));
        filename = row.get("subject");
        lastmodifieddate = row.get("date");
        mime = row.get("mime");
        setPath(row.get("path"));
        setRole(row.get("status"));
    }*/

    public int getCorrespondanceid()
    {
        return correspondanceid;
    }

    public void setCorrespondanceid(int correspondanceid)
    {
        this.correspondanceid = correspondanceid;
    }

    @Override
    public String toString()
    {
        return getRole() + " " + lastmodifieddate + " " + filename + " " + mime;
    }
		
	/*	public  void getLetter(int lettkey)
		{
			ArrayList<Map<String, String>> rowlist = doQuery(
					"select * from correspondance where correspondanceid = "+lettkey);
			//System.out.println("letters found:"+ rowlist.size()+" "+lettkey);	
			//mcCorrespondance aletter = new mcCorrespondance(0);
			//System.out.println(rowlist.toString());
			this.fill(rowlist.get(0));
			//return aletter;
		}*/
		
	/*	public  void saveLetter()
		{
			doExecute("update correspondance set status ='"
			+this.status+"', date='"+this.date+"', subject = '"+this.subject+"' where correspondanceid = "+this.correspondanceid);
				
			System.out.println("letter updated:"+  +this.correspondanceid);	
		}*/


    public String getPath()
    {
        return path;
    }

    public void setPath(String text)
    {
        this.path = text;
    }

    public String getLastmodifieddate()
    {
        return lastmodifieddate;
    }

    public void setLastmodifieddate(String text)
    {
        this.lastmodifieddate = text;
    }

    public String getFilename()
    {
        return filename;
    }

    public void setFilename(String text)
    {
        this.filename = text;
    }

    public int getContactid()
    {
        return contactid;
    }

    public void setContactid(int contactid)
    {
        this.contactid = contactid;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String test)
    {
        this.role = test;
    }



    public static class SortByDate implements Comparator
    {
        public int compare(Object obj1, Object obj2)
        {
            mcCorrespondance a = (mcCorrespondance) obj1;
            mcCorrespondance b = (mcCorrespondance) obj2;
            return b.getLastmodifieddate().compareTo(a.getLastmodifieddate());
        }
    }
}
