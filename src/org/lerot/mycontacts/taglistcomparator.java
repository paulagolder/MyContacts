package org.lerot.mycontacts;

import java.util.Comparator;
import java.util.Map;

public class taglistcomparator implements Comparator<String>
{
	public Map<String, Integer> base;

	public taglistcomparator(Map<String, Integer> base)
	{
		this.base = base;
	}

	@Override
	public int compare(String a, String b)
	{
		if (base.get(a) >= base.get(b))
		{
			return -1;
		} else
		{
			return 1;
		} // returning 0 would merge keys
	}
}