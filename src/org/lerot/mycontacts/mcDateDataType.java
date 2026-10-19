package org.lerot.mycontacts;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class mcDateDataType extends mcKeyValueDataType
{

	static String[] Datefields = { "year", "month", "day" };
	
	public mcDateDataType()
	{
		super("date", "date");
	}

	private static List<SimpleDateFormat>

	dateFormats = new ArrayList<SimpleDateFormat>()
	{

		private static final long serialVersionUID = 1L;

		{
			add(new SimpleDateFormat("yyyy-MM-dd"));
			add(new SimpleDateFormat("M/dd/yyyy"));
			add(new SimpleDateFormat("dd.M.yyyy"));
			add(new SimpleDateFormat("dd.MMM.yyyy"));
			add(new SimpleDateFormat("dd-MMM-yyyy"));
		}
	};

	
	public static Date convertToDate(String input)
	{
		Date date = null;
		if (null == input) { return null; }
		for (SimpleDateFormat format : dateFormats)
		{
			try
			{
				format.setLenient(false);
				date = format.parse(input);
			} catch (ParseException e)
			{
				// Shhh.. try other formats
			}
			if (date != null)
			{
				break;
			}
		}
		return date;
	}

	public Date getDate(String value)
	{
		return getDate(value, "yyyy-mm-dd");
	}
	
	public Date getDate(String value, String fmt)
	{
		String fdate;
		if (!isArray(value))
		{
			fdate = value;
		} else
		{
			Map<String, String> darry = getKeyValueMap(value);
			String year = darry.get("year");
			String month = darry.get("month");
			String day = darry.get("day");
			fdate = year + "-" + month + "-" + day;
		}
		SimpleDateFormat df = new SimpleDateFormat(fmt);
		try
		{
			Date adate = df.parse(fdate);
			return adate;
		} catch (ParseException e)
		{
			return null;
		}
	}

	public String getFormattedDate(String value, String sep)
	{
		if (sep == null) sep = "-";
		if (isArray(value))
		{
			Map<String, String> darry = getKeyValueMap(value);
			if (darry == null)
			{
				System.out.println(" date array empty " + value);
				return null;
			}
			String year = darry.get("year");
			String month = darry.get("month");
			if (month == null || month.length() == 0)
				month = "00";
			else if (month.length() == 1) month = "0" + month;
			String day = darry.get("day");
			
			if (day == null || day.length() == 0)
				day = "00";
			else if (day.length() == 1) day = "0" + day;
			String fdate = year + sep + month + sep + day;
			return fdate;
		} else
		{
			Vector<String> datearry = mcUtilities.datetoVector(value);
			if (datearry.size() == 3)
			{
				String year = datearry.get(0);

				String month = datearry.get(1);
				if (month.length() == 0)
					month = "00";
				else if (month.length() == 1) month = "0" + month;
				String day = datearry.get(2);
				if (day.length() == 0)
					day = "00";
				else if (day.length() == 1) day = "0" + day;
				String fdate = year + sep + month + sep + day;
				return fdate;
			}
			return null;
		}
	}

	

	static boolean isValidDate(String dt)
	{
		Date date = convertToDate(dt);
		if (date == null)
			return false;
		else
			return true;
	}

	@Override
	public String toVcardValue(String value)
	{
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		Date adate;
		try
		{
			adate = df.parse(value);
		} catch (ParseException e)
		{
			return "date error";
		}
		return df.format(adate);
	}

	public static String getNow(String fmt)
	{
		DateFormat dateFormat = new SimpleDateFormat(fmt);
		Date date = new Date();
		return dateFormat.format(date);

	}

	public static String getNow()
	{
		return getNow("yyyy-MM-dd");
	}

	@Override
	public boolean valueContained(String testvalue, String attvalue)
	{
		Date adate = getDate(attvalue);
		Date testdate = getDate(testvalue);
		if (testdate != null && testdate.compareTo(adate) == 0) return true;
		return false;
	}

	@Override
	protected String toXMLnonArray(String svalue)
	{
		String outxml = "";
		Date adate = convertToDate(svalue);
		GregorianCalendar calendar = new GregorianCalendar();
		// Calendar calendar = Calendar.getInstance();
		calendar.setTime(adate);
		int iyear = calendar.get(Calendar.YEAR);
		int imonth = calendar.get(Calendar.MONTH) + 1;
		int iday = calendar.get(Calendar.DAY_OF_MONTH);

		outxml += "  <field key='year' value='" + iyear + "' />\n";
		outxml += "  <field key='month' value='" + imonth + "' />\n";
		outxml += "  <field key='day' value='" + iday + "' />\n";
		return outxml;
	}

	@Override
	public String getFormattedValue(String value,String fmt)
	{
		return getFormattedDate(value,"-");
	}

	@Override
	public String getFormattedValue(String value)
	{
		return getFormattedDate(value,":");
	}
	
	@Override
	public int compareTo(String aarray, String barray)
	{
		Map<String, String> avaluemap = parseArray(aarray);
		Map<String, String> bvaluemap = parseArray(barray);
		for (Map.Entry<String, String> bfield : bvaluemap.entrySet())
		{
			String bkey = bfield.getKey();
			String bvalue = bfield.getValue();
			if (!bvalue.isEmpty())
			{
				String avalue = avaluemap.get(bkey);
				//System.out.println( " array date compare "+bkey+ " cf " + bvalue+ " with "+avalue);
				if(avalue==null || avalue.isEmpty() ) return 1;
				if(isNumeric(bvalue) && isNumeric(avalue))
				{
				if(Integer.parseInt(avalue)!=Integer.parseInt(bvalue)) return 1;
				}
			}
		}
		return 0;
	}

	
	@Override
	public String makeArray(String value)
	{
		return makeArray(Datefields, "year", value,"-");
	}

	public static String makeArrayStringfromVcard(String invalue)
	{
		    String sep ="-";
			String[] split = invalue.split(sep);
			String out = "{ ";
			int k = 0;
			int j = 0;
			String onlytoken = "";
			//paul fix insert quaotes round data?
			for (String atoken : split)
			{
				if (atoken != null && !atoken.trim().isEmpty() && mcUtilities.IsInteger(atoken))
				{
					if (!onlytoken.isEmpty()) out = out + ", ";
					out = out + Datefields[k] + ": \"" + atoken.trim() + "\"";
					onlytoken = atoken;
					j++;
				}
				k++;
				if (k >= Datefields.length) break;
			}
			out = out + "}";
		//	if (j == 1) return onlytoken;
			return out;
		
	}
	
}
