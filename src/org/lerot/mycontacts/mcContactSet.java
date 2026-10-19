package org.lerot.mycontacts;

import java.util.Comparator;
import java.util.TreeSet;

public class mcContactSet extends TreeSet<mcContact>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void printlist(String flag)
	{
		if(this.isEmpty())
		{
			System.out.println( flag+" isempty");
			return;
		}
		for (mcContact value : this)
		{
			System.out.println( flag+" "+value.toString());
		}

	}

	public mcContacts toContactList()
	{
		mcContacts contactlist = new mcContacts();
		for (mcContact value : this)
		{
			contactlist.put(value);

		}
		return contactlist;
	}

	class ContactComp implements Comparator<mcContact>
	{
		@Override
		public int compare(mcContact e1, mcContact e2)
		{
			return e1.getTID().compareTo(e2.getTID());
		}
	}

	public mcContactSet()
	{

		// this.comparator(new ContactComp());
	}

}
