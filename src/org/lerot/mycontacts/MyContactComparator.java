package org.lerot.mycontacts;


import java.util.Comparator;
 
public class MyContactComparator  implements Comparator<String>{
 
    @Override
    public int compare(String str1, String str2) 
    {
    	if(str1 == null)
    		{
    		return 0;
    		}
        return str1.toLowerCase().compareTo(str2.toLowerCase());
    }
     
}

