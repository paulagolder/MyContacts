package org.lerot.mycontacts;

import org.lerot.mywidgets.jswImage;
import org.lerot.mywidgets.jswLabel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class mcAttribute extends mcDataObject
{

    public mcAttributeType attributetype = null;
    int displayOrder = 0;
    private mcAttributeValue attributevalue = null;
    private Integer cid = null;
    private String displaygroup = "";
    private String qualifier = null;
    private String root = null;
    private String tag = null;

    public mcAttribute(int nid)
    {
        super();
        cid = nid;
    }

    public mcAttribute(int k, String string)
    {
        super();
        new mcAttribute(k, root, "");
    }

    public mcAttribute(int nid, String akey, String attqual)
    {
        super();
        cid = nid;
        root = akey;
        qualifier = attqual;
        setType();
        setAttributevalue(new mcAttributeValue(this));
    }

    public static mcAttribute attribute_a_fromXML(Node anode)
    {
        // Element element = (Element) nodes.item(i);
        Element at = (Element) anode;
        // String = at.getNodeName();
        String attroot = at.getNodeName();
        String qualifier = at.getAttribute("qualifier");
        String typename = at.getAttribute("datatype");
        String updatetext = at.getAttribute("update");

			/* try { SimpleDateFormat dateFormat = new
			 SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS"); Date parsedDate =
			 dateFormat.parse( updatetext); update_ts = new
			 java.sql.Timestamp(parsedDate.getTime()); } catch(Exception e) {
			 update_ts=null; }*/

        // String update = at.getAttribute("updated");
        mcAttribute newatt = new mcAttribute(-1, attroot, qualifier);
        if (newatt.attributetype.dt.isImage())
        {
            newatt.getAttributevalue().loadImageXML(anode, updatetext);
        } else if (newatt.attributetype.dt.isArrayType())
        {
            newatt.getAttributevalue().loadArrayValueXML(anode, updatetext);
        }else
            newatt.getAttributevalue().loadValueXML(anode, updatetext);
        return newatt;

    }

    public static mcAttribute attribute_b_fromXML(Node anode)
    {
        Element at = (Element) anode;
        String attroot = at.getNodeName();
        String attkey = at.getAttribute("key");
        attroot = null;
        String qualifier = null;
        if (attkey.contains("/"))
        {
            attroot = attkey.substring(0, attkey.indexOf("/"));
            qualifier = attkey.substring(attkey.indexOf("/") + 1);
        } else
        {
            attroot = attkey;
            qualifier = "";
        }
        String updatetext = at.getAttribute("updated");
        String typename = at.getAttribute("type");
        mcAttribute newatt = new mcAttribute(-1, attroot, qualifier);
        if (newatt.attributetype.dt.isImage())
        {
            newatt.getAttributevalue().loadImageXML(anode, updatetext);
        } else if (newatt.attributetype.dt.isArrayType())
        {
            newatt.getAttributevalue().loadArrayValueXML(anode, updatetext);
        } else
            newatt.getAttributevalue().loadValueXML_b(anode, updatetext);
        return newatt;
    }

    public void dbDeleteAttribute(Integer acid, String aroot, String aqual)
    {
        PreparedStatement st;
        String query = "delete from attributeValues where cid= ?  && root = ? && qualifier = ? ";
        try
        {
            getConnection();
            st = con.prepareStatement(query);
            st.setInt(1, acid);
            st.setString(2, aroot);
            st.setString(3, aqual);
            st.executeUpdate();
            st.close();
            disconnect();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        System.out.println(" attribute deleted ");
    }

    public boolean containsValue(String testvalue)
    {
        if (getAttributevalue().isNull()) return false;
        String value = getAttributevalue().getStringvalue();
        mcDataType type = getType();
        return type.valueContained(testvalue, value);
    }

    public void dbdeleteAttribute()
    {
        PreparedStatement st;
        String query = "delete from attributeValues where cid= ?  and root = ? and qualifier = ? ";
        try
        {
            getConnection();
            st = con.prepareStatement(query);
            st.setInt(1, cid);
            st.setString(2, getRoot());
            st.setString(3, getQualifier());
            st.executeUpdate();
            st.close();
            disconnect();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        System.out.println(" attribute deleted ");
    }

    public void dbinsertAttribute()
    {
        PreparedStatement st;
        String query = "insert into attributeValues(cid,root, qualifier,value, update_dt)  values( ?, ?,?, ?, ?)";
        try
        {
            getConnection();
            st = con.prepareStatement(query);
            st.setInt(1, cid);
            st.setString(2, getRoot());
            st.setString(3, getQualifier());
            st.setString(4, getValue());
            st.setString(5, getDateTime());
            st.executeUpdate();
            st.close();
            disconnect();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Vector<String> dbloadAttributeKeyList()
    {
        ArrayList<Map<String, String>> rowlist = doQuery(
                "select * from attribute order by displayOrder ");
        Vector<String> attributelist = new Vector<>();
        for (Map<String, String> row : rowlist)
        {
            mcAttribute alabtype = new mcAttribute(0);
            String attkey = row.get("attributeKey");
            attributelist.add(attkey);
        }
        return attributelist;
    }

    public void dbupdateAttribute()
    {
        // (new mcDataObject()).setConnection(MyContacts.topgui.currentcon);
        PreparedStatement st;
        String query = "update attributeValues set 'value' = ? , 'update_dt'= ? where cid= ? and root=?  and qualifier= ? ";
        try
        {
            con = datasource.getConnection();
            st = con.prepareStatement(query);
            st.setString(1, getValue());
            st.setString(2, getDateTime());
            st.setInt(3, cid);
            st.setString(4, getRoot());
            st.setString(5, getQualifier());
            st.executeUpdate();
            st.close();
            datasource.disconnect();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    public void dbupsertAttribute()
    {
        try
        {
            PreparedStatement st;
            String query = " UPDATE attributeValues "
                    + " SET   value = ? , update_dt = ? "
                    + " WHERE   cid = ?  and root = ? and qualifier = ?  ";
            getConnection();
            st = con.prepareStatement(query);
            st.setString(1, getValue());
            String update = getAttributevalue().getUpdate();
            if (update == null || update.isEmpty())
            {
                update = getDateTime();
            }
            st.setString(2, update);
            st.setInt(3, cid);
            st.setString(4, getRoot());
            st.setString(5, getQualifier());
            st.executeUpdate();
            int rescount = st.getUpdateCount();
            st.close();
            disconnect();
            if (rescount == 0)
            {
                dbinsertAttribute();
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public Integer getCid()
    {
        return cid;
    }

    void setCid(Integer cid)
    {
        this.cid = cid;
    }

    public mcDataType getDataType()
    {
        return attributetype.getDatatype();
    }

    public boolean isDisplaygroup(String dg)
    {
        return displaygroup.contains(dg);
    }

    public String getDisplaygroup()
    {
        return displaygroup;
    }

    void setDisplaygroup(String displaygroup)
    {
        this.displaygroup = displaygroup;
    }

    public Vector<String> getFieldKeyList()
    {
        if (attributetype == null) return null;
        return attributetype.getFieldKeyList();
    }

    public LinkedHashMap<String, mcfield> getFieldList()
    {
        return attributetype.getFieldList();
    }

    public Map<mcfield, String> getFieldValueMap()
    {
        return attributetype.getFieldValueMap(getAttributevalue().getValue());
    }

    public String getFormattedValue()
    {
        return getFormattedValue(null);
    }

    public String getFormattedValue(String fmt)
    {
        if (getAttributevalue() == null) return null;
        return attributetype.getFormattedValue(getAttributevalue(), fmt);
    }

    public JComponent getImage(int targetheight)
    {
        jswLabel img = null;

        img = new jswImage(getValue()).DisplayImage();

        return img;
    }

    public String getKey()
    {
        String attkey = root;
        if (qualifier != null && !qualifier.isEmpty())
            attkey += "/" + qualifier;
        return attkey;
    }

    void setKey(String atttkey)
    {
        parseKey(atttkey);
    }

    public Map<String, String> getMap()
    {
        if (getAttributevalue() == null) return null;

        return attributetype.getKeyValueMap(getAttributevalue().getValue());

    }

    public int getOrder()
    {

        return displayOrder;

    }

    public String getQualifier()
    {
        return qualifier;
    }

    public void setQualifier(String qualifier)
    {
        this.qualifier = qualifier;
    }

    public String getRoot()
    {
        return root;
    }

    public Set<String> getTags()
    {

        return attributetype.getTags(this.getValue());
    }

    public mcDataType getType()
    {
        if (attributetype == null) return new mcTextDataType();
        return attributetype.getDatatype();
    }

    public String getDataTypeKey()
    {
        return attributetype.getDatatype().getTypekey();
    }

    public String getValue()
    {
        return getAttributevalue().getValue();
    }

    public String getVcardValue()
    {
        if (getAttributevalue() == null) return "";
        String value = attributetype.getVcardValue(getAttributevalue());
        if (value == null)
            return "";
        else
            return value.trim();
    }

    public String getVcardValue(String attvalue)
    {
        if (attvalue == null) return "";
        return attributetype.dt.toVcardValue(attvalue);
    }

    public boolean isArray()
    {
        return attributetype.isArray();
    }

    public boolean isEmpty()
    {
        String val = getValue();
        if (val == null) return true;
        if (val.equalsIgnoreCase("null")) return true;
        return (val.isEmpty());
    }

    public boolean isImage()
    {
        if (attributetype != null)
            return attributetype.isImage();
        else
            return false;
    }

    public boolean isType(String string)
    {
        return attributetype.isType(string);
    }

    public boolean matches(mcAttribute existingatt)
    {
        if ((existingatt == null) || !getKey().equalsIgnoreCase(existingatt.getKey()) || !getType().equals(existingatt.getType())) return false;
        // String avalue = existingatt.getValue()
        String bvalue = existingatt.getValue();
        return matchesValue(bvalue);
        // return true;
    }

    public boolean matchesValue(String testValue)
    {
        String avalue = getValue();

        if (testValue == null && avalue == null) return true;
        if ((testValue != null && avalue == null) || (testValue == null && avalue != null)) return false;

        boolean matches = attributetype.dt.matchesVcardValue(avalue, testValue);
        // System.out.println(" error matching attributes "+this.toString()+
        // " for " + avalue + " with " + testValue);
        return matches;
    }

    public boolean matchesVcardValue(String testValue)
    {
        String avalue = getValue();

        if (testValue == null && avalue == null) return true;
        if ((testValue != null && avalue == null) || (testValue == null && avalue != null)) return false;

        boolean matches = attributetype.dt.matchesVcardValue(avalue, testValue);
        // System.out.println(" error matching attributes "+this.toString()+
        // " for " + avalue + " with " + testValue);
        return matches;
    }

    private void parseKey(String akey)
    {
        String[] parts = mcUtilities.parseLabel(akey);
        root = parts[0];
        if (root == null || root.isEmpty())
            System.out.println(" problem with 3 " + akey + " in " + this);

        qualifier = parts[1];
    }

    private boolean qualifierEquals(String testvalue)
    {
        String qual = getQualifier();
        String ntestvalue = mcUtilities.normalisename(testvalue);
        if (mcUtilities.containedBy(ntestvalue, qual))
            return true;
        else return mcUtilities.containedBy(qual, ntestvalue);
    }

    public void setID(int newid)
    {
        cid = newid;
    }

    private void setType()
    {
        String attkey = getKey();
        attributetype = mcAttributeTypes.findType(attkey);
        if (attributetype == null)
            attributetype = mcAttributeTypes.findType(attkey.toLowerCase());
        if (attributetype == null)
            attributetype = mcAttributeTypes.findType(root);
        if (attributetype == null)
            attributetype = mcAttributeTypes.findType(root.toLowerCase());
        if (attributetype == null)
        {
            System.out.println(
                    " not found type in mcattribute " + getKey() + "?" + root);
            attributetype = mcAttributeTypes.findType("default");
        }
        displayOrder = attributetype.getOrder();
        displaygroup = attributetype.getDisplaygroup();
    }

    public void setValue(Map<String, String> valuelist, String update)
    {
        String arrayvaluestring = attributetype.arrayToArrayString(valuelist);
        setAttributevalue(new mcAttributeValue(this, arrayvaluestring, update));
    }

    public void setValue(Set<String> tokenlist, String update)
    {
        String tokenstring = attributetype.arrayToString(tokenlist);
        setAttributevalue(new mcAttributeValue(this, tokenstring, update));
    }

    public void setValue(String strvalue, String update)
    {
        String newvalue = strvalue.trim();
        if (!newvalue.isEmpty())
        {
            mcDataType attype = getType();
            if (attype.isType("phone") || attype.isType("cellphone"))
            {
                newvalue = mcUtilities.normalisephonenumber(newvalue);
            } else if (attype.isType("address"))
            {
                // fix this
                if (!mcUtilities.isArray(newvalue))
                    newvalue = mcUtilities.tidyValue(newvalue);

            } else if (attype.isType("name"))
            {
                if (!mcUtilities.isArray(newvalue))
                    newvalue = attype.tidyValue(newvalue);
            } else if (attype.isType("text"))
            {
                newvalue = newvalue.trim();
            } else if (attype.isType("image"))
            {

            } else if (attype.isType("textlist"))
            {
                newvalue = newvalue.trim();
            } else if (attype.isType("taglist"))
            {
                newvalue = newvalue.trim();
            } else if (attype.isType("cumulativetext"))
            {
                System.out.println(" updating cumtextlist for " + this);
                String oldvalue = getValue();
                newvalue = oldvalue + ";"
                        + mcUtilities.formatCumulativeText(newvalue);
            }
        }
        String olddate = getAttributevalue().getUpdate();
        if(update == null )
        {
            if(olddate == null)
            {
                update = mcDateDataType.getNow("yyyy-MM-dd");
            }else
            {
                update = olddate;
            }
        }
        setAttributevalue(new mcAttributeValue(this, newvalue, update));
    }

    public void addTag(String strvalue, String update)
    {
        String newvalue = strvalue.trim();
        if (!newvalue.isEmpty())
        {
            mcDataType attype = getType();
            if (attype.isType("taglist"))
            {
                Set<String> oldtags = getTags();
                if (oldtags == null)
                {

                }
                oldtags.add(newvalue);
                setValue(oldtags, update);
            } else if (attype.isType("textlist"))
            {
                Set<String> oldtags = getTags();
                if (oldtags == null)
                {
                    oldtags = new TreeSet<>();
                }
                oldtags.add(newvalue);
                setValue(oldtags, update);
            } else if (attype.isType("cumulativetext"))
            {
                System.out.println(" updating cumtextlist for " + this);
                String oldvalue = getValue();
                newvalue = oldvalue + ";"
                        + mcUtilities.formatCumulativeText(newvalue);
                setAttributevalue(new mcAttributeValue(this, newvalue, update));
            } else
            {
                setValue(strvalue, update);
            }
        }

    }

    public void setValue(Vector<String> valuelist, String update)
    {
        String arrayvaluestring = attributetype.arrayToString(valuelist);
        setAttributevalue(new mcAttributeValue(this, arrayvaluestring, update));
    }

    @Override
    public String toString()
    {
        //return getKey() + "(" + attributetype.getAttributeLabel() + ")" + ":"
        //		+ getAttributevalue().getStringvalue() + "  ";
        return this.cid + "+" + this.attributetype.getAttributeLabel() + "+" + this.qualifier + "+" + this.attributevalue.getValue();
    }

    public String toXCard()
    {
        if (getAttributevalue() == null) return "";
        String outxml = "";

        outxml = " <" + getRoot() + " >";
        String parameters = "";
        if (getQualifier() != null)
        {
            parameters += "<type><text>" + getQualifier() + "</text></type>\n";
        }
        parameters += "<update><date>" + getAttributevalue().getUpdate() + "</date></update>\n";
        outxml += "<parameters>" + parameters + "</parameters >\n";

        outxml += attributetype.getXMLValue(getAttributevalue()) + "\n";

        outxml += " </" + getRoot() + ">" + "\n";

        return outxml;
    }

    public String toXML()
    {
        if (getAttributevalue() == null) return "";
        String outxml = "";
        outxml = " <" + getRoot() + "  ";
        if (getQualifier() != null && !getQualifier().isEmpty())
        {
            outxml += " qualifier='" + getQualifier() + "' ";
        }
        outxml += " datatype='" + getDataTypeKey() + "' ";

        if (getAttributevalue().getUpdate() != null && !getAttributevalue().getUpdate().isEmpty())
        {
            outxml += " update='" + getAttributevalue().getUpdate() + "' ";
        }
        outxml += " > \n";
        outxml += attributetype.getXMLValue(getAttributevalue()); // + "\n";// paul
        outxml += " </" + getRoot() + ">" + "\n";
        return outxml;
    }

    public String toXML(String value)
    {
        if (getAttributevalue() == null) return "";
        String outxml = "";
        outxml = " <" + getRoot() + "  ";
        if (getQualifier() != null && !getQualifier().isEmpty())
        {
            outxml += " qualifier='" + getQualifier() + "' ";
        }
        outxml += " datatype='" + getDataTypeKey() + "' ";

        if (getAttributevalue().getUpdate() != null && !getAttributevalue().getUpdate().isEmpty())
        {
            outxml += " update='" + getAttributevalue().getUpdate() + "' ";
        }
        outxml += " > \n";
        outxml += attributetype.toXML(value);
        //outxml += value; // + "\n";// paul
        outxml += " </" + getRoot() + ">" + "\n";
        return outxml;
    }

    public void updateAttribute(mcAttribute aniattribute)
    {
        String value = aniattribute.getAttributevalue().getValue();
        String update = aniattribute.getAttributevalue().getUpdate();
        getAttributevalue().setValue(value, update);
        dbupsertAttribute();
    }

    public void updateQualifier(String newqualifier)
    {
        String rt = getRoot();
        String oq = getQualifier();
        String nk = rt + "/" + newqualifier;
        if (!nk.equals(getKey()))
        {
            try
            {
                getConnection();
                PreparedStatement st;
                String query = " UPDATE attributeValues "
                        + " SET   qualifier= ?   "
                        + " WHERE   cid = ?  and root= ? and qualifier= ? ";
                st = con.prepareStatement(query);
                st.setString(1, newqualifier);
                st.setInt(2, cid);
                st.setString(3, rt);
                st.setString(4, oq);
                st.executeUpdate();
                st.close();
                disconnect();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        setQualifier(newqualifier);

    }

    public mcAttributeValue getAttributevalue()
    {
        return attributevalue;
    }

    public void setAttributevalue(mcAttributeValue attributevalue)
    {
        this.attributevalue = attributevalue;
    }

    public boolean moreRecentThan(mcAttribute otheratt)
    {
        String thisupdate = this.attributevalue.getUpdate();
        String otherupdate = otheratt.attributevalue.getUpdate();
        if (thisupdate == null && otherupdate != null) return true;
        return thisupdate.compareTo(otherupdate) > 0;
    }

    public boolean islink()
    {
        mcDataType type = getType();
        if (type == null)
        {
            System.out.println(" not found type" + this);
            return false;
        } else
        {
            return type.getTypekey().equalsIgnoreCase("link");
        }
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }
}
