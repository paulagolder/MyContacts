package org.lerot.mycontacts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public abstract class mcDataType extends mcDataObject
{

    private String datatype = null;
    private String typekey = null;

    public mcDataType(String key, String type)
    {
        super();
        datatype = type;
        typekey = key;
    }

    public static mcDataType getDefault()
    {
        mcDataType atype = new mcTextDataType();
        return atype;
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
            d = d + 1;// just to keep it happy
        } catch (NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static mcDataType factory(String datatype)
    {
        switch (datatype)
        {
            case "text":
                return new mcTextDataType();
            case "image":
                return new mcImageDataType();
            case "textlist":
                return new mcTextListDataType();
            case "taglist":
                return new mcTagListDataType();
            case "name":
                return new mcNameDataType();
            case "address":
                return new mcAddressDataType();
            case "date":
                return new mcDateDataType();
            case "cellphone":
                return new mcCellphoneDataType();
            case "phone":
                return new mcPhoneDataType();
            case "email":
                return new mcEmailDataType();
            case "http":
                return new mcHttpDataType();
            case "tid":
                return new mcTIDDataType();
            case "cid":
                return new mcCIDDataType();
            case "link":
                return new mcTextDataType();
            case "timestamp":
                return new mcTimeDataType();
            default:
                System.out.println(" unrecognised datatype " + datatype);
                return null;
        }
    }

    public boolean isImage()
    {
        if (this instanceof mcImageDataType) return true;
        else
            return false;
    }

    public boolean isType(String type)
    {
        boolean isid = datatype.equalsIgnoreCase(type);
        return isid;
    }

    void load(Map<String, String> inmap)
    {

        if (inmap.containsKey("Type")) datatype = inmap.get("Type");
        if (inmap.containsKey("Key")) typekey = inmap.get("Key");

    }

    @Override
    public String toString()
    {
        return " TYPE : " + datatype + "(" + typekey + ")";
    }

    public boolean isArrayType()
    {
        if (this instanceof mcKeyValueDataType) return true;
        else
            return false;
    }

    public String arrayToArrayString(Vector<String> valuelist)
    {
        if (this.isArrayType()) return ((mcKeyValueDataType) this)
                .makeValuefromVector(valuelist);
        else
        {
            String outstring = "";
            for (String aterm : valuelist)
            {
                outstring = outstring + aterm + ";";
            }
            return outstring;
        }

    }

    String getDatatype()
    {
        return datatype;
    }

    void setDatatype(String datatype)
    {
        this.datatype = datatype;
    }

    String getTypekey()
    {
        return typekey;
    }

    void setTypekey(String typekey)
    {
        this.typekey = typekey;
    }

    public abstract Vector<String> getFieldKeyList();

    public abstract LinkedHashMap<String, mcfield> getFieldList();

    public abstract Map<mcfield, String> getFieldValueMap(String value);

    public String arrayToString(Map<String, String> valuelist)
    {
        if (this.isArrayType()) return ((mcKeyValueDataType) this)
                .keyvaluesToArrayString(valuelist);
        else
        {
            System.out.println(" should we be here dtats ");
            String outstring = "";
            String sep = "";
            for (Entry<String, String> aterm : valuelist.entrySet())
            {
                outstring = outstring + sep + aterm.getKey() + "="
                        + aterm.getValue();
                sep = ", ";
            }
            return outstring;
        }
    }


    public String getFormattedValue(String value)
    {
        return getFormattedValue(value, null);
    }

    public abstract String getFormattedValue(String value, String fmt);

    public void load(ResultSet inmap)
    {
        try
        {
            datatype = inmap.getString("Type");
            typekey = inmap.getString("Key");
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public abstract Map<String, String> getKeyValueMap(String value);

    public abstract String toVcardValue(String value);

    public abstract String toXML(String value);

    public abstract boolean valueContained(String attvalue, String testvalue);

    public String tidyValue(String newvalue)
    {
        return newvalue;
    }

    public boolean isTID()
    {

        if (this instanceof mcTIDDataType) return true;
        else
            return false;

    }

    public abstract boolean matchesVcardValue(String avalue, String bvalue);

    public abstract int compareTo(String aarray, String barray);

    public String setToString(Set<String> tokenlist)
    {
        switch (datatype)
        {
            case "text":
                return ((mcTextDataType) this)
                        .asetToString(tokenlist);
            case "textlist":
                return mcTextListDataType
                        .makeString(tokenlist);
            case "taglist":
                return mcTagListDataType
                        .makeString(tokenlist);
            case "image":
            case "name":
            case "address":
            case "date":
            case "phone":
            case "email":
            case "http":
            case "tid":
            case "cid":
            case "link":
            case "timestamp":
            case "cellphone":
                return null;

            default:
                System.out.println(" unrecognised datatype " + datatype);
                return null;
        }
    }


}
