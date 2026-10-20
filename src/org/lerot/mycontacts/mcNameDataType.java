package org.lerot.mycontacts;

import org.apache.commons.text.StringEscapeUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

public class mcNameDataType extends mcKeyValueDataType
{
	static String[]Namefields = { "Sn", "Fn", "Mn", "Title" };
	
	mcNameDataType()
	{
		super("name", "name");
	}

	@Override
	public String getFormattedValue(String value, String fmt)
	{

		if (fmt == null) fmt = "Sn , Fn Mn Title ?? ";
		if (isArrayType() && isArray(value))
		{
			String output = "";
			Map<String, String> darry = getValueMap(value);
			if (darry == null)
			{
				System.out.println(" name array empty " + value);
				return null;
			}
			output = format(darry,fmt);
			output = output.trim();
			return output;
		} else
		{
			return value;
		}
	}

	@Override
	public String toVcardValue(String value)
	{
		String sep = "; ";
		if (isArrayType())
		{
			Map<String, String> darry = getKeyValueMap(value);
			if (darry == null)
			{
				System.out.println(" name array empty " + value);
				return null;
			}
			String name = "";
			String sn = darry.get("sn");
			if (sn != null && !sn.equals("null"))
			{
				name = name + sn;
			}
			name = name + sep;
			String fn = darry.get("fn");
			if (fn != null && !fn.equals("null"))
			{
				name = name + fn;
			}
			name = name + sep;
			String mn = darry.get("mn");
			if (mn != null && !mn.equals("null"))
			{
				name = name + mn;
			}
			name = name + sep;
			String title = darry.get("title");
			if (title != null && !title.equals("null"))
			{
				name = name + title;

			}
			name = name + sep;
			return name;
		} else
		{
			// todo make more intelligent
			Vector<String> namearry = mcKeyValueDataType.ToVector(value);
			if (namearry.size() > 0)
			{
				String name = namearry.get(0);
				for (int i = 1; i < namearry.size(); i++)
				{
					if (!namearry.get(i).equals("null"))
						name = name + namearry.get(i);
					name = name + sep;
				}
				return name;
			} else
				return null;
		}
	}

	@Override
	public boolean valueContained(String testvalue, String attvalue)
	{
		attvalue = attvalue.toLowerCase().trim();
		String filter = " and et & ";
		Map<String, String> testarray;
		if (isArray(testvalue))
		{
			testarray = getValueMap(testvalue);
		} else
		{
			testarray = parsetoValueMap(testvalue);
		}

		for (Entry<String, String> entry : testarray.entrySet())
		{
			String value = entry.getValue().toLowerCase().trim();
			if (!value.isEmpty() && !filter.contains(value))
			{
				if (!attvalue.contains(value)) return false;
			}
		}
		return true;
	}

	private Map<String, String> parsetoValueMap(String testvalue)
	{
		if (testvalue.contains(",")) return parsetoValueMapComma(testvalue);
		else
			return parsetoValueMapSpace(testvalue);
	}

	private Map<String, String> parsetoValueMapComma(String testvalue)
	{
		Map<String, String> pmap = new LinkedHashMap<String, String>();
		testvalue = testvalue.replace("  ", " ").trim();
		testvalue = testvalue.replace(" and ", "+");
		testvalue = testvalue.replace(" et ", "+");
		testvalue = testvalue.replace(" & ", "+");
		String[] subname = testvalue.split(",");
		int k = subname.length;
		for (int i = 0; i < k; i++)
		{
			subname[i] = subname[i].replace("+", " & ").trim();
		}

		if (k == 1)
		{
			pmap.put("title", subname[0]);
		} else if (k == 2)
		{
			pmap.put("sn", subname[0]);
			pmap.put("fn", subname[1]);
		} else if (k == 3)
		{
			pmap.put("sn", subname[0]);
			pmap.put("fn", subname[1]);
			pmap.put("mn", subname[2]);
		} else
		{
			testvalue = testvalue.replace("+", " & ");
			pmap.put("title", testvalue);
		}

		return pmap;
	}

	private Map<String, String> parsetoValueMapSpace(String testvalue)
	{
		Map<String, String> pmap = new LinkedHashMap<String, String>();
		testvalue = testvalue.replace("  ", " ").trim();
		testvalue = testvalue.replace(" and ", "+");
		testvalue = testvalue.replace(" et ", "+");
		testvalue = testvalue.replace(" & ", "+");
		String[] subname = testvalue.split(" ");
		int k = subname.length;
		for (int i = 0; i < k; i++)
		{
			subname[i] = subname[i].replace("+", " & ").trim();
		}

		if (k == 1)
		{
			pmap.put("title", subname[0]);
		} else if (k == 2)
		{
			pmap.put("fn", subname[0]);
			pmap.put("sn", subname[1]);
		} else if (k == 3)
		{
			pmap.put("fn", subname[0]);
			pmap.put("mn", subname[1]);
			pmap.put("sn", subname[2]);
		} else
		{
			testvalue = testvalue.replace("+", " & ");
			pmap.put("title", testvalue);
		}

		return pmap;
	}

	@Override
	protected String toXMLnonArray(String svalue)
	{
		Map<String, String> map = parsetoValueMap(svalue);
		String outxml = "";
		for (Entry<String, String> entry : map.entrySet())
		{
			String key = entry.getKey().trim();
			String value = entry.getValue().trim();
			String escvalue = StringEscapeUtils.escapeXml10(value);
			outxml += "  <field key='" + key + "' value='" + escvalue
					+ "' />\n";
		}
		return outxml;
	}

	@Override
	public String tidyValue(String sname)
	{
		String normname = "";
		sname = sname.replace(" ", ",");
		sname = sname.replace("?", ",");
		sname = sname.replace(";", ",");
		String[] split = sname.split(",");
		for (String apart : split)
		{
			if (!apart.equals(",")) normname = normname + apart + " ";
		}
		return normname.trim();
	}

	@Override
	public String getFormattedValue(String value)
	{
		return getFormattedValue(value ,  "Sn , Fn Mn Title ## ");
	}
	
	@Override
	public String makeArray(String value)
	{
		return makeArray(Namefields,"Title", value,";");
	}

}
