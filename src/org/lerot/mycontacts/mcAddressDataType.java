package org.lerot.mycontacts;

import org.apache.commons.lang3.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.util.*;

public class mcAddressDataType extends mcKeyValueDataType
{
	static String[] Addressfields = { "pobox", "building", "street", 
			"city", "county", "postcode", "country" };
	static List<String> countries = Arrays.asList("france", "spain",
			"luxembourg", "united kingdom", "gb", "uk", "u.k", "g.b", "u.k.",
			"g.b.", "great britain", "england");
	static String postcode = "\\w{1,2}\\d{1,2}\\s";
	static Map<String, Vector<String>> adtemps = null;

	public static Map<String, String> parse(String svalue)
	{
		HashMap<String, String> adrmap = new HashMap<String, String>();
		String[] splitstr = svalue.split("\n");

		// find country
		int i = 0;
		for (String astring : splitstr)
		{
			if (astring != null)
			{
				if (countries.contains(astring.toLowerCase()))
				{
					adrmap.put("country", astring);
					splitstr[i] = "";
					break;
				}
			}
			i++;
		}
		// find postcode
		i = 0;
		for (String astring : splitstr)
		{
			if (astring.matches("^(?i)\\w{1,2}\\d{1,2}\\s\\d\\w*"))
			{
				adrmap.put("postcode", astring);
				splitstr[i] = "";
				break;
			}

			i++;
		}
		// find street
		i = 0;
		for (String astring : splitstr)
		{
			if (astring.matches("^(?i)\\d{1,4}\\s(\\w|\\s)*"))
			{
				adrmap.put("street", astring);
				splitstr[i] = "";
				break;
			}

			i++;
		}
		// find building

		if (splitstr[0] != null && !splitstr[0].isEmpty())
		{
			adrmap.put("building", splitstr[0]);
			splitstr[0] = "";
		}

		int k = 0;
		String[] residual = new String[10];
		for (String astring : splitstr)
		{
			if (astring != null && !astring.isEmpty())
			{

				residual[k] = astring;
				k++;
			}
		}
		if (k == 1)
			adrmap.put("city", residual[0]);
		else if (k == 3)
		{
			adrmap.put("locality", residual[0]);
			adrmap.put("city", residual[1]);
			adrmap.put("county", residual[2]);
		} else if (k == 2)
		{
			adrmap.put("city", residual[0]);
			adrmap.put("county", residual[1]);
		}

		return adrmap;
	}

	mcAddressDataType()
	{
		super("address", "address");
		if (adtemps == null)
		{
			adtemps = readTemplates();
		}
	}

	public String getBlockFormattedValue(String value, String sep)
	{
		return getBlockFormattedValue(value, sep, false);
	}

	public String getBlockFormattedValue(String avalue, String sep,
			boolean showuk)
	{
		String ostr = "";
		if (sep == null) sep = ", ";
		String vsep = "";
		Map<String, String> address = getKeyValueMap(avalue);
		String ctry = address.get("country");
		String csel = "DEF";
		if (ctry == null)
		{
			csel = "UK";
		} else if (!adtemps.containsKey(ctry)
				&& !adtemps.containsKey(ctry.toLowerCase()))
		{
			ctry = ctry.toLowerCase();
			if (ctry.contains("france"))
				csel = "EUR";
			else if (ctry.contains("united kingdom"))
				csel = "UK";
			else if (ctry.contains("u.k"))
				csel = "UK";
			else if (ctry.contains("uk"))
				csel = "UK";
			else if (ctry.contains("germany")) csel = "EUR";
			if (csel.equalsIgnoreCase("uk"))
			{
				ctry = "U.K.";
			}
		} else
			csel = ctry.toUpperCase();

		boolean foundcountry = false;
		Vector<String> template = adtemps.get(csel);
		for (String line : template)
		{
			if (line.toUpperCase().contains("COUNTRY"))
			{
				foundcountry = true;
				if(ctry!=null)
				{
				if (ctry.equalsIgnoreCase("U.K."))
				{
					if (showuk)
					{
						ostr += vsep + format(address, line);
						vsep = sep;
					}
				} else
				{
					ostr += vsep + format(address, line);
					vsep = sep;
				}
				}

			} else
			{
				String fline = format(address, line);

				if (fline != null && !fline.isEmpty())
				{
					ostr += vsep + format(address, line);
					vsep = sep;
				}
			}
		}
		if (!foundcountry && showuk)
		{
			ostr += vsep + csel;
			vsep = sep;
		}
		return ostr;
	}

	@Override
	public String getFormattedValue(String value)
	{
		return getFormattedValue(value, ",");
	}

	@Override
	public String getFormattedValue(String avalue, String sep)
	{
		if (sep == null) sep = ", ";
		String asep = "";
		String address = "";
		if (isArrayType())
		{
			Map<String, String> darry = getKeyValueMap(avalue);
			if (darry == null)
			{
				System.out.println(" address array empty " + avalue);
				return null;
			}
			Vector<String> fieldkeylist = this.getFieldKeyList();
			for (String fkey : fieldkeylist)
			{
				String aval = darry.get(fkey);

				if (aval != null && !aval.equals("null") && !aval.isEmpty())
				{
					address = address + asep + aval;
					asep = sep;
				}
			}

			return address;
		} else
		{

			return avalue;
		}
	}

	public String getFormattedValueb(String avalue, String sep)
	{
		if (sep == null) sep = ", ";
		if (isArrayType())
		{
			Map<String, String> darry = getKeyValueMap(avalue);
			if (darry == null)
			{
				System.out.println(" address array empty " + avalue);
				return null;
			}

			String asep = "";
			String pobox = darry.get("pobox");
			String address = "";
			if (pobox != null && !pobox.equals("null") && !pobox.isEmpty())
			{
				address = pobox;
				asep = sep;
			}
			String building = darry.get("building");

			if (building != null && !building.equals("null"))
			{
				address = address + asep + building;
				asep = sep;
			}
			String street = darry.get("street");
			if (street != null && !street.equals("null"))
			{
				address = address + asep + street;
				asep = sep;
			}
			String locality = darry.get("locality");
			if (locality != null && !locality.equals("null"))
			{
				address = address + asep + locality;
				asep = sep;
			}
			String city = darry.get("city");
			if (city != null && !city.equals("null"))
			{
				address = address + asep + city;
			}
			String county = darry.get("county");
			if (county != null && !county.equals("null"))
			{
				address = address + asep + county;
			}
			String postcode = darry.get("postcode");
			if (postcode != null && !postcode.equals("null"))
			{
				address = address + asep + postcode;
			}

			String country = darry.get("country");
			if (country != null && !country.equals("null"))
			{
				address = address + asep + country;
			}
			return address;
		} else
		{
			Vector<String> namearry = mcKeyValueDataType.ToVector(avalue);
			if (namearry.size() > 0)
			{
				String name = namearry.get(0);
				for (int i = 1; i < namearry.size(); i++)
				{
					if (!namearry.get(i).equals("null"))
						name = name + sep + namearry.get(i);
				}
				return name;
			} else
				return null;
		}
	}

	@Override
	public String toVcardValue(String value)
	{

		String[] Addressfields = { "pobox", "building", "street", 
				"city", "county", "postcode", "country" };
		String sep = "; ";
		if (isArrayType())
		{
			Map<String, String> darry = getKeyValueMap(value);
			if (darry == null)
			{
				System.out.println(" address array empty " + value);
				return null;
			}
			String address = "";
			
			for(String addtag : Addressfields)
			{
			String pobox = darry.get(addtag);
			if (pobox != null && !pobox.equals("null") && !pobox.isEmpty())
			{
				address = address + pobox;
			}
			address = address + sep;
			}
			return address;
		} else
		{
			return null;
		}
	}

	public Map<String, Vector<String>> readTemplates()
	{
		try
		{
			HashMap<String, Vector<String>> addlist = new HashMap<String, Vector<String>>();
			SAXReader reader = new SAXReader();
			Document doc = reader
					.read(MyContacts.settingsdir + "/addressTemplates.xml");
			List<Node> nodes = doc.selectNodes("/addresses/address");
			for (Node node : nodes)
			{
				Vector<String> vlines = new Vector<String>();
				String key = node.valueOf("@country");
				List<Node> lines = node.selectNodes("line");
				for (Node line : lines)
				{
					vlines.add(line.getText());
				}
				addlist.put(key, vlines);
			}
			return addlist;

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected String toXMLnonArray(String svalue)
	{
		String outxml = "";
		String escvalue = StringEscapeUtils.escapeXml(svalue);
		outxml += "  <field key='address' value='" + escvalue + "' />\n";
		return outxml;
	}

	@Override
	public boolean valueContained(String testvalue, String attvalue)
	{
		String oldaddress = attvalue.toLowerCase();
		oldaddress = oldaddress.replace("united kingdom", "u.k.").replace("  ",
				" ");
		testvalue = testvalue.toLowerCase().replace("united kingdom", "u.k.")
				.replace("  ", " ");
		Vector<String> addresstokens = ToVector(testvalue);
		if (addresstokens == null || addresstokens.isEmpty()) return false;
		for (String token : addresstokens)
		{
			if (token != null)
			{
				if (token.contains(";"))
				{
					String[] splitokens = token.split(";");
					for (String atoken : splitokens)
					{
						if (!atoken.trim().isEmpty())
						{
							if (!oldaddress
									.contains(atoken.trim().toLowerCase()))
								return false;
						}
					}
				} else
				{
					if (!oldaddress.contains(token)) return false;
				}
			}
		}
		return true;
	}

	@Override
	public String makeArray(String stringvalue)
	{
		return makeArray(Addressfields, "street", stringvalue,",");
	}

}
