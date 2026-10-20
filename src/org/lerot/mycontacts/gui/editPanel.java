package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.gui.widgets.jswDropDownContactBox;
import org.lerot.mycontacts.*;
import org.lerot.mywidgets.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;

import static org.lerot.mycontacts.MyContacts.panelstyles;

public class editPanel extends jswVerticalPanel implements ActionListener
{

    private static final long serialVersionUID = 1L;
    private static final int YES = 0;
    private static jswStyles tagtablestyles;
    private static jswStyles arraytablestyles;
    private static jswStyles tablestyles;
    private static jswStyles linktablestyles;
    private final jswTextBox[] attfieldeditbox = new jswTextBox[10];
    private final jswCheckbox[] tagcheckbox = new jswCheckbox[10];
    private final Color background = Color.decode(jswTheme.EDITAREA);
    boolean addselector = false;
    private jswTextBox atteditbox;
    private mcAttribute edattribute;
    private String edit = "";
    private String editattributekey;
    private jswDropDownBox newlabel;
    private jswDropDownContactBox parentselect;
    private jswTextBox tideditbox;
    private jswLabel tagspanel;
    private jswTextBox newtagpanel;
    private String vcarddirectory;
    private jswTextBox atype;
    private String edattributename;
    private jswDropDownBox linkselect;
    private jswDropDownBox groupselect;
    private int selectedtag=-1;

    public editPanel()
    {
        super("edit", false, false);
        vcarddirectory = MyContacts.topgui.desktop;
        tagtablestyles = makeTagTableStyles();
        //  tablestyles = makeTableStyles();
        tablestyles = searchTableStyles();
        arraytablestyles = makeArrayTableStyles();
        linktablestyles = makeLinkTableStyles();
    }

    public static jswStyles searchTableStyles()
    {
        jswStyles tablestyles = new jswStyles();
        tablestyles.name = "defaulttable";
        jswStyle tablestyle = tablestyles.makeStyle("table");
        tablestyle.putAttribute("borderwidth", "1");
        tablestyle.putAttribute("borderColor", jswTheme.BORDER);
        jswStyle cellstyle = tablestyles.makeStyle("cell");
        cellstyle.putAttribute("fontSize", jswTheme.TEXT);
        cellstyle.putAttribute("fontStyle", Font.PLAIN);
        cellstyle.putAttribute("borderColor", jswTheme.CELLBORDER);
        cellstyle.putAttribute("borderWidth", 1);
        cellstyle.putAttribute("padding", 4);
        jswStyle rowstyle = tablestyles.makeStyle("row");
        rowstyle.putAttribute("minheight", 50);
        jswStyle cell00style = tablestyles.makeStyle("cell");
        //cell00style.putAttribute("foregroundColor", "transparent");
        cell00style.putAttribute("minheight", 50);
        jswStyle colstyle = tablestyles.makeStyle("col");
        colstyle.setHorizontalAlign("RIGHT");
        jswStyle col0style = tablestyles.makeStyle("col_0");
        col0style.putAttribute("fontStyle", Font.BOLD);
        col0style.putAttribute("fontSize", jswTheme.HEADING);
        col0style.putAttribute("minwidth", true);
        col0style.setHorizontalAlign("LEFT");
        jswStyle col1style = tablestyles.makeStyle("col_1");
        col1style.putAttribute("width", 100);
        jswStyle col2style = tablestyles.makeStyle("col_2");
        jswStyle col3style = tablestyles.makeStyle("col_3");
        col3style.putAttribute("minwidth", true);
        return tablestyles;
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        mcContact selcontact = MyContacts.selbox.getSelcontact();
        String cmd = evt.getActionCommand();
        System.out.println(" action ep: " + cmd);
       // HashMap<String, String> cmdmap = jswUtils.parsecsvstring(cmd);
       // String action = cmdmap.get("command");
        String action= cmd;
        if (action != null && !action.isEmpty())
        {
            action = action.toUpperCase();
            if(action.startsWith("TAG:"))
            {
                int tagno = Integer.parseInt(action.substring(4));
                selectedtag=tagno;
            }
            else if (action.equals("IMPORTVCARD"))
            {
                mcImportContact imcontact = importVcard();
                if (imcontact != null)
                {
                    String imtid = imcontact.getAttributeValue("tid");
                    int n = JOptionPane.showConfirmDialog(this, "Do you want to import this Vcard " + imtid, "Accept Import?", JOptionPane.YES_NO_OPTION);
                    // System.out.println("reply =" + n);
                    if (n == YES)
                    {
                        selcontact.updateFromVcardImport(imcontact);
                    }
                }

            } else if (action.equalsIgnoreCase("COMBOBOXCHANGED"))
            {
                String value= "help";
               // String value = cmdmap.get("value");
                linkselect.removeActionListener(this);
                linkselect.setSelected(value);
                linkselect.addActionListener(this,"changebox");
            } else if (action.startsWith("NEWCONTACT"))
            {
                selcontact = mcContacts.createNewContact();
                MyContacts.topgui.refreshView();
                MyContacts.selbox.setSelcontact(selcontact);
            } else if (action.startsWith("IMPORT"))
            {
                JTextArea textArea = new JTextArea(6, 25);
                textArea.setText("");
                textArea.setEditable(true);
                int result = JOptionPane.showConfirmDialog(this, textArea, "Text Box and Text Area Example", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == 0)
                {
                    String imaddr = textArea.getText();
                    Map<String, String> addmap = mcAddressDataType.parse(imaddr);
                    String addarray = mcUtilities.keyvaluesmaptoArrayString(addmap, "=");
                    edattribute.getAttributevalue().setValue(addarray, "now");
                    edattribute.dbupdateAttribute();
                    edit = "";
                    System.out.println("    update  " + editattributekey);
                    System.out.println("    update to.. " + imaddr);
                }
            } else if (action.startsWith("VIEW:"))
            {
                String vstr = action.substring(5);
                selcontact = MyContacts.selbox.FindbyID(vstr);
                MyContacts.selbox.setSelcontact(selcontact);
                edit = "";
                MyContacts.topgui.refreshView();
            } else if (action.startsWith("DISCONNECT:"))
            {
                String vstr = action.substring(11);
                selcontact.deleteAttributebyKey(vstr);
                System.out.println("removing " + vstr + " from " + selcontact);
            } else if (action.equals("ADDLINK"))
            {
                mcContact linkcontact = parentselect.getSelectedValue();
                String linktype = linkselect.getSelectedValue();
                System.out.println("adding " + selcontact + " as " + linktype + " : " + linkcontact);
                mcAttribute newatt = selcontact.createAttribute(linktype, linkcontact.getIDstr(), false);
                if (newatt != null)
                {
                    newatt.getAttributevalue().setValue(linkcontact.getIDstr(), "now");
                    System.out.println("newlink:" + newatt);
                    newatt.dbupsertAttribute();
                } else System.out.println(" cannot create duplicate");
            } else if (action.startsWith("REFLECT:"))
            {
                System.out.println(" reflect=" + action);
                String newdata = action.substring(8).toLowerCase();
                String[] data = newdata.split(":");
                mcContact linkcontact = MyContacts.selbox.FindbyID(data[0]);
                System.out.println(" link contact " + linkcontact + " ++ " + data[0] + " + " + data[1] + " + " + data[2]);
                String root = data[1];
                String qualifier = data[0];
                // qualifier = selcontact.getIDstr();
                if (root.equalsIgnoreCase("hasmember"))
                {
                    root = "memberof";
                    qualifier = selcontact.getIDstr();
                } else if (root.equalsIgnoreCase("memberof"))
                {
                    root = "hasmember";
                    qualifier = selcontact.getIDstr();
                } else if (root.equalsIgnoreCase("related)"))
                {
                    root = "related";
                    qualifier = "rev-" + qualifier;
                }
                String newid = mcUtilities.makeIDstr(data[0]);
                System.out.println("adding  link + to " + data[2] + " " + root + " " + qualifier);
                mcAttribute newatt = selcontact.createAttribute(root, qualifier);
                newatt.getAttributevalue().setValue(newid, "now");
                System.out.println("new attribute:" + newatt);
                newatt.dbupsertAttribute();
            } else if (action.startsWith("ADDGROUP"))
            {
                System.out.println("adding " + groupselect.getSelectedValue() + " to " + selcontact);
                selcontact.addGroup(groupselect.getSelectedValue());
            } else if (action.startsWith("ADDTOGROUP"))
            {
                mcContact parent = parentselect.getSelectedValue();
                System.out.println("adding " + selcontact + " to " + parent);
            } else if (action.equals("CANCEL"))
            {
                edit = "";
            } else if (action.equals("EDITID"))
            {
                edit = "editid";
            } else if (action.startsWith("EDITATTRIBUTE:"))
            {
                edit = "editattribute";
                editattributekey = action.substring(14).toLowerCase();
                edattribute = selcontact.getAttributebyKey(editattributekey);
                if (edattribute == null) return;
            } else if (action.startsWith("REPLACE"))
            {
                JFileChooser chooser = new JFileChooser();
                File file = null;
                int returnValue = chooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    file = chooser.getSelectedFile();
                }
                System.out.println(" file selected " + file);
                jswImage newimage = new jswImage();
                newimage.importfile(file.getPath());
                String newattributevalue = newimage.getEncodedImage();
                selcontact.updateAttributebyKey(editattributekey, newattributevalue);
                System.out.println(" updated image " + editattributekey + " for " + selcontact);
                edit = "";
            } else if (action.equalsIgnoreCase("UPDATEID"))
            {
                String newcontacttid = tideditbox.getText();
                selcontact.updateContactTID(newcontacttid);
                MyContacts.selbox.refreshAll();
                MyContacts.topgui.getContentPane().validate();
                edit = "";
            } else if (action.equalsIgnoreCase("UPDATELINKTOATTRIBUTE"))
            {
                String newattributequalifier = atteditbox.getText();
                edattribute.updateQualifier(newattributequalifier);
                edit = "";
                System.out.println("    update  " + editattributekey + " update to.. " + newattributequalifier);
            } else if (action.equals("UPDATEATTRIBUTE"))
            {
                String newattributevalue = atteditbox.getText();
                String newattributequalifier = atype.getText();
                edattribute.setQualifier(newattributequalifier);
                edattribute.getAttributevalue().setValue(newattributevalue, "now");
                edattribute.dbupdateAttribute();
                edit = "";
                // System.out.println(" attribute "+edattribute.getRoot()+"
                // update to.. " + newattributevalue);
            } else if (action.equalsIgnoreCase("UPDATEARRAYATTRIBUTE"))
            {
                Map<String, String> valuelist = new LinkedHashMap<String, String>();
                for (jswTextBox abox : attfieldeditbox)
                {
                    if (abox == null) break;
                    valuelist.put(abox.getTag(), abox.getText().trim());
                }
                edattribute.getAttributevalue().setValue(valuelist, "now");
                String newattributequalifier = atype.getText();
                edattribute.updateQualifier(newattributequalifier);
                edattribute.dbupdateAttribute();
                if (edattribute.getRoot().equals("name"))
                {
                    if (selcontact.getTID().equals("new contact"))
                    {
                        String newtid = edattribute.getFormattedValue();
                        mcAttribute tidattribute = selcontact.updateAttribute("tid", "", newtid);
                        tidattribute.dbupdateAttribute();
                        selcontact.setTID(newtid);
                    }
                }
                edit = "";
            } else if (action.equalsIgnoreCase("DELETECONTACT"))
            {
                int n = JOptionPane.showConfirmDialog(this, "Do you want to delete this contact?", "DELETE CONTACT?", JOptionPane.YES_NO_OPTION);
                // System.out.println("reply =" + n);
                if (n == YES)
                {
                    MyContacts.selbox.getSelcontact().deleteContact();
                    MyContacts.selbox.refreshAll();
                    edit = "";
                }
                edit = "editid";
            } else if (action.equalsIgnoreCase("DELETETAGS"))
            {
                Set<String> ataglist = new HashSet<String>();
                int k = 0;
                for (jswCheckbox atag : tagcheckbox)
                {
                    if (atag != null)
                    {
                        String tag = atag.getTag();

                        if (atag.isSelected())
                        {
                            System.out.println(" TAG " + tag);
                            ataglist.add(tag);
                            k++;
                        }
                    }
                }
                int n = JOptionPane.showConfirmDialog(this, "Do you want to delete these " + k + " Tags?", "DELETE TAGS?", JOptionPane.YES_NO_OPTION);
                // System.out.println("reply =" + n);
                if (n == YES)
                {
                    edattribute.getAttributevalue().deleteTags(ataglist);
                    edattribute.dbupdateAttribute();
                    edit = "";
                }
                edit = "editattribute";

            } else if (action.equalsIgnoreCase("INSERTTAG"))
            {
                Set<String> ataglist = new HashSet<String>();
                // jswTextBox atag = newtagpanel;
                String newtagvalue = newtagpanel.getText().trim();
                ataglist.add(newtagvalue);
                edattribute.getAttributevalue().insertTagValues(ataglist);
                edattribute.dbupdateAttribute();
                edit = "editattribute";
            } else if (action.equalsIgnoreCase("DELETEATTRIBUTE"))
            {
                int n = JOptionPane.showConfirmDialog(this, "Do you want to delete this attribute?", "DELETE ATTRIBUTE?", JOptionPane.YES_NO_OPTION);
                if (n == YES)
                {
                    selcontact.removeAttributebyKey(edattribute.getKey());
                    edattribute.dbdeleteAttribute();
                    edit = "";
                }
                edit = "editid";

            } else if (action.equalsIgnoreCase("CREATE NEW ATTRIBUTE"))
            {
                String newattlabel = newlabel.getSelectedValue();
                mcAttribute newatt = selcontact.createAttribute(newattlabel, "");
                newatt.dbinsertAttribute();
            } else System.out.println("ep action1 " + action + " unrecognised ");
        } else
        {
            System.out.println("ep action1 " + " is null ");
            System.out.println(evt.getSource().toString());
        }
        makeEditPanel();
    }

    public void clearEdit()
    {
        edit = "";
        editattributekey = "";
    }

    private mcImportContact importVcard()
    {
        String fname = vcarddirectory;
        File vfile = new File(fname);
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Vcard");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("vcf", "vcf");
        fc.setFileFilter(filter);
        fc.setSelectedFile(vfile);
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            // String vout = selcontact.toVcard();
            File fileToLoad = fc.getSelectedFile();
            String filepath = fileToLoad.getPath();

            try
            {
                mcImportContact nextcontact = null;
                vcarddirectory = fileToLoad.getParentFile().getCanonicalPath();
                mcMappings mappings = MyContacts.topgui.currentcon.createMappings("import", "Vcard");
                vcardContactReader cr = new vcardContactReader(filepath, mappings);
                if (cr != null)
                {
                    try
                    {
                        nextcontact = cr.getContact();
                        System.out.println(" Loading.... " + nextcontact);
                    } catch (mcGetContactException e)
                    {
                        System.out.println(" exception... " + cr.getExceptions().size());
                    }
                }
                System.out.println(" returniung.with exceptions  " + cr.getExceptions().size());
                return nextcontact;

            } catch (UnsupportedEncodingException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FileNotFoundException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return null;
    }

    private jswStyles makeArrayTableStyles()
    {
        jswStyles tablestyles = jswStyles.clone("ArrayTableStyles", MyContacts.getTableStyles());
        jswStyle cellstyle = tablestyles.makeStyle("cell");
        cellstyle.putAttribute("foregroundColor", "BLACK");
        cellstyle.putAttribute("borderWidth", "1");
        cellstyle.putAttribute("borderColor", jswTheme.CELLBORDER);
        cellstyle.setHorizontalAlign("LEFT");
        cellstyle.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle cellcstyle = tablestyles.makeStyle("xcellcontent");
        // cellcstyle.putAttribute("backgroundColor", "transparent");
        cellcstyle.putAttribute("foregroundColor", "Red");
        cellcstyle.setHorizontalAlign("LEFT");
        cellcstyle.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle col0style = tablestyles.makeStyle("col_0");
        col0style.putAttribute("fontStyle", Font.BOLD);
        col0style.setHorizontalAlign("RIGHT");
        col0style.putAttribute("minwidth", "true");

        jswStyle col1style = tablestyles.makeStyle("col_1");
        col1style.putAttribute("fontStyle", Font.BOLD);
        col1style.setHorizontalAlign("RIGHT");

        jswStyle tablestyle = tablestyles.makeStyle("table");
        tablestyle.putAttribute("foregroundColor", "Green");
        tablestyle.putAttribute("borderWidth", "2");
        tablestyle.putAttribute("borderColor", jswTheme.BORDER);

        return tablestyles;
    }

    private jswVerticalPanel makeLinkedFromPanel(mcContact selcontact, String selector, String title)
    {
        jswVerticalPanel frame = new jswVerticalPanel("title", false, false);
        jswLabel memberheading = new jswLabel(title);
        frame.add(memberheading);
        jswTable memberpanel = new jswTable(this, selector, linktablestyles);
        if (selcontact != null)
        {
            mcAttributes cattributes = selcontact.getAttributes();
            // cattributes.printList("ca ");
            mcAttributes getlinked = (new mcAttributes()).FindByAttributeValue(selector, selcontact.getIDstr());
            //  getlinked.printList("lk ");
            int row = 0;
            for (Entry<String, mcAttribute> anentry : getlinked.entrySet())
            {
                mcAttribute anattribute = anentry.getValue();
                String aroot = anattribute.getRoot();
                if (aroot.equalsIgnoreCase("memberof")) aroot = "hasmember";
                else if (aroot.equalsIgnoreCase("hasmember")) aroot = "memberof";
                String avalue = mcUtilities.makeIDstr(anattribute.getCid());
                String aqualifier = mcUtilities.makeIDstr(anattribute.getQualifier());
                if (!selcontact.hasAttributeByValue(aroot, avalue, avalue))
                {
                    int cid = anattribute.getCid();
                    mcContact linkedcontact = MyContacts.selbox.FindbyID(cid);
                    if (linkedcontact != null)
                    {
                        jswLabel alabel = new jswLabel(linkedcontact.getName());
                        memberpanel.addCell(alabel, row, 0);
                        jswLabel aqual = new jswLabel(anattribute.getQualifier());
                        memberpanel.addCell(aqual, row, 1);
                        jswHorizontalPanel buttonpanel = new jswHorizontalPanel();
                        jswButton viewcontact = new jswButton(this, "VIEW", "VIEW:" + linkedcontact.getIDstr());
                        buttonpanel.add(viewcontact);
                        jswButton disconnect = new jswButton(this, "REFLECT", "REFLECT:" + cid + ":" + selector + ":" + aqualifier);
                        buttonpanel.add(disconnect);
                        memberpanel.addCell(buttonpanel, row, 2);
                    } else
                    {
                        jswLabel alabel = new jswLabel(" invalid link ");
                        memberpanel.addCell(alabel, row, 0);
                        jswLabel aqual = new jswLabel(anattribute.getQualifier());
                        memberpanel.addCell(aqual, row, 1);
                        jswHorizontalPanel buttonpanel = new jswHorizontalPanel();
                        jswButton disconnect = new jswButton(this, "DELETE", "DELETE" + anattribute.getKey());
                        buttonpanel.add(disconnect);
                        memberpanel.addCell(buttonpanel, row, 2);
                    }
                    row++;
                }
            }
            if (row == 0)
            {
                return null;
            } else
            {
                frame.add(memberpanel);
                return frame;
            }
        } else
        {
            return null;
        }
    }


    private jswVerticalPanel makeLinkedFromPanel(mcAttributes getlinked, String selector, String title)
    {
        jswVerticalPanel frame = new jswVerticalPanel("title", false, false);
        jswLabel memberheading = new jswLabel(title);
        frame.add(memberheading);
        jswTable memberpanel = new jswTable(this, selector, linktablestyles);

        int row = 0;
        for (Entry<String, mcAttribute> anentry : getlinked.entrySet())
        {
            mcAttribute anattribute = anentry.getValue();
            String aroot = anattribute.getRoot();
            if (aroot.equalsIgnoreCase("memberof")) aroot = "hasmember";
            else if (aroot.equalsIgnoreCase("hasmember")) aroot = "memberof";
            String avalue = mcUtilities.makeIDstr(anattribute.getCid());
            String aqualifier = mcUtilities.makeIDstr(anattribute.getQualifier());

            int cid = anattribute.getCid();
            mcContact linkedcontact = MyContacts.selbox.FindbyID(cid);
            if (linkedcontact != null)
            {
                jswLabel alabel = new jswLabel(linkedcontact.getName());
                memberpanel.addCell(alabel, row, 0);
                jswLabel aqual = new jswLabel(anattribute.getQualifier());
                memberpanel.addCell(aqual, row, 1);
                jswHorizontalPanel buttonpanel = new jswHorizontalPanel();
                jswButton viewcontact = new jswButton(this, "VIEW", "VIEW:" + linkedcontact.getIDstr());
                buttonpanel.add(viewcontact);
                jswButton disconnect = new jswButton(this, "REFLECT", "REFLECT:" + cid + ":" + selector + ":" + aqualifier);
                buttonpanel.add(disconnect);
                memberpanel.addCell(buttonpanel, row, 2);
            } else
            {
                jswLabel alabel = new jswLabel(" invalid link ");
                memberpanel.addCell(alabel, row, 0);
                jswLabel aqual = new jswLabel(anattribute.getQualifier());
                memberpanel.addCell(aqual, row, 1);
                jswHorizontalPanel buttonpanel = new jswHorizontalPanel();
                jswButton disconnect = new jswButton(this, "DELETE", "DELETE" + anattribute.getKey());
                buttonpanel.add(disconnect);
                memberpanel.addCell(buttonpanel, row, 2);
            }
            row++;

        }
        if (row == 0)
        {
            return null;
        } else
        {
            frame.add(memberpanel);
            return frame;
        }

    }


    private jswStyles makeLinkTableStyles()
    {
        jswStyles tablestyles = jswStyles.clone("ArrayTableStyles", MyContacts.getTableStyles());
        jswStyle cellstyle = tablestyles.makeStyle("cell");
        cellstyle.putAttribute("foregroundColor", "Blue");
        cellstyle.putAttribute("borderWidth", "1");
        cellstyle.putAttribute("borderColor", jswTheme.CELLBORDER);
        cellstyle.setHorizontalAlign("LEFT");
        cellstyle.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle hpstyle = tablestyles.makeStyle("xjswHorizontalPanel");
        hpstyle.putAttribute("foregroundColor", "Green");
        hpstyle.putAttribute("borderWidth", "2");
        hpstyle.putAttribute("borderColor", "gray");
        hpstyle.putAttribute("cellBorderColor", jswTheme.CELLBORDER);
        hpstyle.putAttribute("cellBorderWidth", "10");

        jswStyle bpstyle = tablestyles.makeStyle("buttonpanel");
        bpstyle.putAttribute("foregroundColor", "Green");
        bpstyle.putAttribute("borderWidth", "2");
        bpstyle.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle col0style = tablestyles.makeStyle("col_0");
        col0style.putAttribute("fontStyle", Font.BOLD);
        col0style.setHorizontalAlign("RIGHT");
        col0style.putAttribute("minwidth", "true");

        jswStyle col1style = tablestyles.makeStyle("col_1");
        col1style.putAttribute("fontStyle", Font.BOLD);
        col1style.setHorizontalAlign("LEFT");

        jswStyle col2style = tablestyles.makeStyle("col_2");
        col2style.putAttribute("horizontalAlignment", "RIGHT");
        col2style.putAttribute("minwidth", "true");

        return tablestyles;
    }

 /*   private jswVerticalPanel xmakeLinkToPanel(mcContact selcontact, String selector, String title)
    {
        jswVerticalPanel frame = new jswVerticalPanel("LinkTo", false, false);
        jswLabel memberheading = new jswLabel(title);
        frame.add(memberheading);
        jswTable memberpanel = new jswTable(this, selector, linktablestyles);

        if (selcontact != null)
        {
            Map<String, mcAttribute> attributes = selcontact.getAttributesbyRoot(selector);
            if (attributes.size() < 1)
            {
                return null;
            }

            int row = 0;

            for (Entry<String, mcAttribute> anentry : attributes.entrySet())
            {
                mcAttribute anattribute = anentry.getValue();
                // mcAttributeType attype = anentry.getValue();
                // if (anattribute.getDisplaygroup().contains("E"))
                if (anattribute.getRoot().equalsIgnoreCase(selector))
                {
                    String value = anattribute.getValue();
                    String qualifier = anattribute.getQualifier();
                    String attributekey = anattribute.getKey();
                    mcContact linkedcontact = MyContacts.selbox.FindbyID(qualifier);

                    if (linkedcontact != null)
                    {
                        if (editattributekey.equalsIgnoreCase(attributekey))
                        {
                            jswLabel alabel = new jswLabel(linkedcontact.getName());
                            memberpanel.addCell(alabel, row, 0);
                            jswLabel aqlabel = new jswLabel(qualifier);
                            memberpanel.addCell(aqlabel, row, 1);
                            atteditbox = new jswTextBox(this, "qualifier", 100);
                            atteditbox.setText(qualifier);
                            atteditbox.setEnabled(true);
                            memberpanel.addCell(atteditbox, " FILLW ", row, 1);
                            atteditbox.setStyleAttribute("myheight", 50);
                            atteditbox.applyStyle();
                            jswHorizontalPanel buttonpanel = new jswHorizontalPanel();
                            jswButton viewcontact2 = new jswButton(this, "UPDATE", "UPDATELINKTOATTRIBUTE");
                            buttonpanel.add(viewcontact2);
                            jswButton viewcontact3 = new jswButton(this, "DELETE", "DELETEATTRIBUTE");
                            buttonpanel.add(viewcontact3);
                            memberpanel.addCell(buttonpanel, row, 2);
                        } else
                        {
                            jswLabel alabel = new jswLabel(linkedcontact.getName());
                            //   jswLabel alabel = new jswLabel(value);
                            memberpanel.addCell(alabel, row, 0);
                            jswLabel aqlabel = new jswLabel(qualifier);
                            memberpanel.addCell(aqlabel, row, 1);
                            jswHorizontalPanel buttonpanel = new jswHorizontalPanel();
                            jswButton viewcontact2 = new jswButton(this, "VIEW", "VIEW:" + linkedcontact.getIDstr());
                            buttonpanel.add(viewcontact2);
                            jswButton disconnect = new jswButton(this, "EDIT ME", "EDITATTRIBUTE:" + anattribute.getKey());
                            buttonpanel.add(disconnect);
                            memberpanel.addCell(buttonpanel, row, 2);
                        }
                    } else
                    {
                        jswLabel alabel = new jswLabel(value);
                        memberpanel.addCell(alabel, row, 0);
                        alabel = new jswLabel(qualifier + " (not found linked contact )");
                        memberpanel.addCell(alabel, row, 1);
                        jswHorizontalPanel buttonpanel = new jswHorizontalPanel();
                        jswButton disconnect = new jswButton(this, "DISCONNECT", "DISCONNECT:" + anattribute.getKey());
                        buttonpanel.add(disconnect);
                        memberpanel.addCell(buttonpanel, row, 2);
                        buttonpanel.applyStyle(linktablestyles.getStyle("buttonpanel"));
                    }
                    row++;
                }
            }
            if (row == 0)
            {
                return null;
            } else frame.add(memberpanel);
            return frame;
        } else
        {
            return null;
        }

    }*/

    private jswVerticalPanel makeLinkToPanel(mcAttributes attributes, String selector, String title)
    {
        jswVerticalPanel frame = new jswVerticalPanel("LinkTo", false, false);
        jswLabel memberheading = new jswLabel(title);
        frame.add(memberheading);
        jswTable memberpanel = new jswTable(this, selector, linktablestyles);

        int row = 0;
        for (Entry<String, mcAttribute> anentry : attributes.entrySet())
        {
            mcAttribute anattribute = anentry.getValue();
            String value = anattribute.getValue();
            String qualifier = anattribute.getQualifier();
            String attributekey = anattribute.getKey();
            String tid = anattribute.getTag();

            if (editattributekey.equalsIgnoreCase(attributekey))
            {
                jswLabel alabel = new jswLabel(tid);
                memberpanel.addCell(alabel, row, 0);
                jswLabel aqlabel = new jswLabel(qualifier);
                memberpanel.addCell(aqlabel, row, 1);
                atteditbox = new jswTextBox(this, "qualifier", 100,"");
                atteditbox.setText(qualifier);
                atteditbox.setEnabled(true);
                memberpanel.addCell(atteditbox, " FILLW ", row, 1);
                atteditbox.setStyleAttribute("myheight", 50);
                atteditbox.applyStyle();
                jswHorizontalPanel buttonpanel = new jswHorizontalPanel();
                jswButton viewcontact2 = new jswButton(this, "UPDATE", "UPDATELINKTOATTRIBUTE");
                buttonpanel.add(viewcontact2);
                jswButton viewcontact3 = new jswButton(this, "DELETE", "DELETEATTRIBUTE");
                buttonpanel.add(viewcontact3);
                memberpanel.addCell(buttonpanel, row, 2);
            } else
            {
                jswLabel alabel = new jswLabel(tid);
                //   jswLabel alabel = new jswLabel(value);
                memberpanel.addCell(alabel, row, 0);
                jswLabel aqlabel = new jswLabel(qualifier);
                memberpanel.addCell(aqlabel, row, 1);
                jswHorizontalPanel buttonpanel = new jswHorizontalPanel();

                jswButton viewcontact2 = new jswButton(this, "VIEW", "VIEW:" + value);
                buttonpanel.add(viewcontact2);
                jswButton disconnect = new jswButton(this, "EDIT ME", "EDITATTRIBUTE:" + anattribute.getKey());
                buttonpanel.add(disconnect);
                memberpanel.addCell(buttonpanel, row, 2);
            }
            row++;
        } frame.add(memberpanel);
        return frame;
    }

    private jswStyles makeTableStyles()
    {
        jswStyles tablestyles = jswStyles.clone("TableStyles", MyContacts.getTableStyles());

        jswStyle tablestyle = tablestyles.makeStyle("table");
        tablestyle.putAttribute("foregroundColor", "Green");
        tablestyle.putAttribute("borderWidth", "2");
        tablestyle.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle jtablestyle = tablestyles.makeStyle("jswtable");
        jtablestyle.putAttribute("borderWidth", "2");
        jtablestyle.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle bpstyle = tablestyles.makeStyle("buttonpanel");
        bpstyle.putAttribute("borderWidth", "2");
        bpstyle.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle cellstyle = tablestyles.makeStyle("cell");
        cellstyle.setHorizontalAlign("LEFT");

        jswStyle cellcstyle = tablestyles.makeStyle("xcellcontent");
        cellcstyle.putAttribute("foregroundColor", "Red");
        cellcstyle.setHorizontalAlign("LEFT");
        cellcstyle.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle col0style = tablestyles.makeStyle("col_0");
        col0style.putAttribute("fontStyle", Font.BOLD);
        col0style.setHorizontalAlign("RIGHT");
        col0style.putAttribute("minwidth", "true");

        jswStyle col1style = tablestyles.makeStyle("col_1");
        col1style.putAttribute("fontStyle", Font.BOLD);
        col1style.setHorizontalAlign("LEFT");
        col1style.putAttribute("width", 70);

        jswStyle col2style = tablestyles.makeStyle("col_2");
        col2style.putAttribute("horizontalAlignment", "RIGHT");
        col2style.putAttribute("FILLW", "true");
        col2style.putAttribute("foregroundColor", "Red");

        jswStyle col3style = tablestyles.makeStyle("col_3");
        col3style.putAttribute("horizontalAlignment", "RIGHT");
        col3style.putAttribute("minwidth", "true");

        return tablestyles;
    }

    public jswStyles makeTagTableStyles()
    {
        jswStyles tablestyles = jswStyles.clone("TagTableStyles", MyContacts.getTableStyles());

        jswStyle tablestyle = tablestyles.makeStyle("table");
        tablestyle.putAttribute("foregroundColor", "Green");
        tablestyle.putAttribute("borderWidth", "2");
        tablestyle.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle cellstyle = tablestyles.makeStyle("cell");
        cellstyle.putAttribute("foregroundColor", "Blue");
        cellstyle.putAttribute("borderWidth", "1");
        cellstyle.putAttribute("borderColor", jswTheme.CELLBORDER);
        cellstyle.setHorizontalAlign("LEFT");
        cellstyle.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle cellcstyle = tablestyles.makeStyle("cellcontent");
        // cellcstyle.putAttribute("backgroundColor", "xtransparent");
        cellcstyle.putAttribute("foregroundColor", "Blue");
        cellcstyle.setHorizontalAlign("LEFT");
        cellcstyle.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle col0style = tablestyles.makeStyle("col_0");
        col0style.putAttribute("fontStyle", Font.BOLD);
        // col0style.putAttribute("backgroundColor", "Yellow");
        col0style.putAttribute("minwidth", 1);
        col0style.putAttribute("width", "50");

        jswStyle tabletyle = tablestyles.makeStyle("table");
        tabletyle.putAttribute("foregroundColor", "Green");
        tabletyle.putAttribute("borderWidth", "2");
        tabletyle.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle col3style = tablestyles.makeStyle("col_1");
        col3style.putAttribute("horizontalAlignment", "RIGHT");
        col3style.putAttribute("FILLW", "true");

        return tablestyles;
    }

    public void makeEditPanel()
    {
        this.removeAll();
        jswHorizontalPanel newcontactbox = new jswHorizontalPanel("newcontact", false);
        add(" FILLW ", newcontactbox);
        jswButton ncbutton = new jswButton(this, "New Contact", "NEWCONTACT");
        newcontactbox.add("MIDDLE", ncbutton);
        mcContact selcontact = MyContacts.selbox.getSelcontact();
        //setBackground(new Color(0, 0, 0, 0));
        setBackground(background);
        jswHorizontalPanel idbox = new jswHorizontalPanel("idbox", false);
        add(" fillW ", idbox);
        jswLabel idpanel1 = new jswLabel(" ");
        idbox.add(" ", idpanel1);
        idpanel1.applyStyle(panelstyles.getStyle("mediumLabel"));
        if (selcontact != null) idpanel1.setText(selcontact.getIDstr());
        else idpanel1.setText(" no contact selected ");

        jswLabel idpanel2;
        jswLabel idpanel3;

        if (edit.equalsIgnoreCase("editid"))
        {
            editattributekey = "";
            String tid = selcontact.getTID();
            if (tid.equalsIgnoreCase("new contact") || tid.isEmpty())
            {
                tid = selcontact.getName();
            }

            tideditbox = new jswTextBox(this, "");
            tideditbox.setStyleAttribute("mywidth", 200);
            tideditbox.setText(tid);
            tideditbox.setEnabled(true);
            tideditbox.setStyleAttribute("width", 200);
            tideditbox.applyStyle();
            // tideditbox.applyStyle(panelstyles.getStyle("mediumLabel"));
            tagspanel = new jswLabel("tags");
            tagspanel.setText(selcontact.getTags());
            tagspanel.applyStyle();
            idbox.add("FILLW", tideditbox);
            idbox.add("FILLW", tagspanel);
            jswButton idupdate = new jswButton(this, "UPDATE", "UPDATEID");
            idbox.add("RIGHT", idupdate);
            jswButton iddelete = new jswButton(this, "DELETE", "DELETECONTACT");
            idbox.add("RIGHT", iddelete);
            jswButton idcancel = new jswButton(this, "CANCEL", "CANCEL");
            idbox.add("RIGHT", idcancel);
        } else if (edit.equalsIgnoreCase("editattribute"))
        {
            idpanel2 = new jswLabel(" ");
            idbox.add("  ", idpanel2);
            mcContact acontact = MyContacts.selbox.getSelcontact();
            if (acontact != null) idpanel2.setText(acontact.getTID());
            idpanel3 = new jswLabel(" ");
            idbox.add(idpanel3);
            if (acontact != null) idpanel3.setText(acontact.getTags());
            jswButton idcancel = new jswButton(this, "CANCEL", "CANCEL");
            idbox.add("RIGHT", idcancel);
        } else
        {
            edit = "";
            editattributekey = "";
            idpanel2 = new jswLabel("panel");
            idpanel2.applyStyle(panelstyles.getStyle("mediumLabel"));
            mcContact acontact = MyContacts.selbox.getSelcontact();
            if (acontact != null)
            {
                String atid = acontact.getTID();
                idpanel2.setText(atid);
                idpanel2.applyStyle(panelstyles.getStyle("mediumLabel"));
            }
            //String atid = acontact.getTID();
            //idpanel2 = new jswLabel(atid);
            idbox.add(" LEFT ", idpanel2);
            // idpanel2.setText(MyContacts.selbox.getSelcontact().getTID());

            idpanel3 = new jswLabel(" ");
            idbox.add(" ", idpanel3);
            if (acontact != null) idpanel3.setText(acontact.getTags());
            jswButton idedit = new jswButton(this, "EDIT ME", "EDITID");
            idbox.add("RIGHT", idedit);
            jswButton imvcard = new jswButton(this, "VCARD", "IMPORTVCARD");
            idbox.add("RIGHT", imvcard);
        }

        jswTable attributepanel = new jswTable(this, "attributes", tablestyles);
        attributepanel.setMarker("edittable");
        add(" FILLW ", attributepanel);
        attributepanel.removeAll();
        MyContacts.selbox.getSelcontact().fillContact();
        int row = 0;
        for (Entry<String, mcAttribute> anentry : MyContacts.selbox.getSelcontact().getAttributes().entrySet())
        {
            if (row > 40) continue;
            mcAttribute anattribute = anentry.getValue();
            String attributeroot = anattribute.getRoot();
            String attributekey = anattribute.getKey();
            String attributequalifier = anattribute.getQualifier();

            if (anattribute.isDisplaygroup("E"))
            {
                jswLabel alabel = new jswLabel(attributeroot);
                attributepanel.addCell(alabel, row, 0);
                if (edit.equalsIgnoreCase("editattribute"))
                {
                    if (editattributekey.equalsIgnoreCase(attributekey))
                    {
                        atype = new jswTextBox(this, "Qualifier?");
                        atype.setText(attributequalifier);
                        atype.setEnabled(true);
                        atype.setStyleAttribute("mywidth", 90);
                        atype.setStyleAttribute("myheight", 50);
                        atype.applyStyle();
                        attributepanel.addCell(atype, " FILLW ", row, 1);
                        if (anattribute.isImage())
                        {
                            jswImage animage = new jswImage(anattribute.getValue());
                            attributepanel.addCell(animage.DisplayImage(), row, 2);
                            jswPanel imagebox = new jswVerticalPanel("title", false, false);
                            jswPanel buttonbox = new jswHorizontalPanel("Editpanel buttonbox", false);
                            jswButton idupdate = new jswButton(this, "REPLACE");
                            buttonbox.add("RIGHT", idupdate);
                            jswButton iddelete = new jswButton(this, "DELETE", "DELETEATTRIBUTE");
                            buttonbox.add("RIGHT", iddelete);
                            imagebox.add(buttonbox);
                            jswLabel imagesize = new jswLabel(" size=" + anattribute.getValue().length());
                            imagebox.add(imagesize);
                            attributepanel.addCell(imagebox, row, 3);
                        } else if (anattribute.isArray())
                        {
                            jswPanel buttonbox = new jswVerticalPanel(" button box ", false, false);
                            Map<mcfield, String> attarry = anattribute.getFieldValueMap();
                            jswTable fieldlistbox = new jswTable(this, "fieldtable", arraytablestyles);
                            int frow = 0;
                            for (Entry<mcfield, String> arow : attarry.entrySet())
                            {
                                mcfield rfield = arow.getKey();
                                String fkey = rfield.getKey();
                                String label = rfield.getLabel();
                                String rvalue = arow.getValue();
                                if (rvalue == null || rvalue.equals("null")) rvalue = " ";
                                jswLabel keypanel = new jswLabel(label);
                                attfieldeditbox[frow] = new jswTextBox(this, "edbox");
                                attfieldeditbox[frow].setText(rvalue);
                                attfieldeditbox[frow].setEnabled(true);
                                attfieldeditbox[frow].setTag(fkey);
                                fieldlistbox.addCell(keypanel, frow, 0);
                                fieldlistbox.addCell(attfieldeditbox[frow], " FILLW ", frow, 1);
                                attfieldeditbox[frow].setStyleAttribute("mywidth", 200);
                                attfieldeditbox[frow].setStyleAttribute("myheight", 50);
                                attfieldeditbox[frow].applyStyle();
                                frow++;
                            }
                            fieldlistbox.setEnabled(true);
                            attributepanel.addCell(fieldlistbox, " FILLW ", row, 2);
                            jswButton idupdate = new jswButton(this, "UPDATE", "UPDATEARRAYATTRIBUTE");

                            buttonbox.add(" ", idupdate);
                            buttonbox.setStyleAttribute("verticallayoutstyle", jswLayout.MIDDLE);
                            buttonbox.applyStyle();
                            jswButton iddelete = new jswButton(this, "DELETE", "DELETEATTRIBUTE");
                            buttonbox.add(" ", iddelete);
                            if (anattribute.isType("address"))
                            {
                                jswButton idimport = new jswButton(this, "IMPORT", "IMPORTADDRESS");
                                buttonbox.add(" ", idimport);
                            }
                            attributepanel.addCell(buttonbox, row, 3);
                        } else if (anattribute.isType("textlist"))
                        {
                            jswPanel buttonbox = new jswVerticalPanel("title", false, false);
                            Set<String> attarry = anattribute.getTags();
                            jswTable fieldlistbox = new jswTable(this, "tagtable", tagtablestyles);
                            int frow = 0;
                            jswLabel keypanel = new jswLabel("new");

                            fieldlistbox.addCell(keypanel, frow, 0);
                            newtagpanel = new jswTextBox(this, "textbox");
                            newtagpanel.setText(" ");
                            newtagpanel.setEnabled(true);
                            fieldlistbox.addCell(newtagpanel, " FILLW ", frow, 1);
                            frow++;
                            for (String arow : attarry)
                            {
                              //  tagcheckbox[frow] = new jswCheckbox(this, "");

                                tagcheckbox[frow] = new jswCheckbox(this, "", "tag:"+frow);
                                tagcheckbox[frow].setTag(arow);
                                if(selectedtag== frow)tagcheckbox[frow].setSelected(true);
                                fieldlistbox.addCell(tagcheckbox[frow], " WIDTH=100 ", frow, 0);
                                jswLabel keylabel = new jswLabel(arow);
                                fieldlistbox.addCell(keylabel, frow, 1);
                                frow++;
                            }
                            if (frow == 1)
                            {
                                jswLabel keypanel2 = new jswLabel("");
                                fieldlistbox.addCell(keypanel2, frow, 1);
                            }
                            fieldlistbox.setEnabled(true);
                            attributepanel.addCell(fieldlistbox, " FILLW ", row, 2);
                            jswButton idupdate = new jswButton(this, "ADD TAG", "INSERTTAG");
                            buttonbox.add("RIGHT", idupdate);
                            if (frow > 2)
                            {
                                jswButton iddelete = new jswButton(this, "DELETE SELECTED", "DELETETAGS");
                                buttonbox.add("RIGHT", iddelete);
                            }
                            jswButton alldelete = new jswButton(this, "DELETE ALL", "DELETEATTRIBUTE");
                            buttonbox.add("RIGHT", alldelete);
                            attributepanel.addCell(buttonbox, row, 3);
                        } else
                        {
                            String value = anattribute.getFormattedValue();
                            atteditbox = new jswTextBox(this, "Box_" + value);
                            atteditbox.setText(value);
                            atteditbox.setEnabled(true);
                            atteditbox.setStyleAttribute("mywidth", 300);
                            atteditbox.setStyleAttribute("myheight", 50);
                            atteditbox.applyStyle();
                            attributepanel.addCell(atteditbox, " FILLW ", row, 2);
                            jswPanel buttonbox = new jswHorizontalPanel("button box", false);
                            jswButton idupdate = new jswButton(this, "UPDATE", "UPDATEATTRIBUTE");
                            buttonbox.add("RIGHT", idupdate);
                            jswButton iddelete = new jswButton(this, "DELETE", "DELETEATTRIBUTE");
                            buttonbox.add("RIGHT", iddelete);
                            attributepanel.addCell(buttonbox, row, 3);
                        }
                    } else
                    {
                        jswLabel atype = new jswLabel("type");
                        atype.setText(attributequalifier);
                        attributepanel.addCell(atype, " FILLW ", row, 1);
                        if (anattribute.isImage())
                        {
                            jswImage animage = new jswImage(anattribute.getValue());
                            attributepanel.addCell(animage.DisplayImage(), row, 2);
                        } else
                        {
                            String value = anattribute.getFormattedValue();
                            attributepanel.addCell(new jswLabel(value), row, 2);
                        }
                        jswPanel buttonbox = new jswVerticalPanel("title", false, false);
                        jswButton idedit = new jswButton(this, "EDIT ME", "EDITATTRIBUTE:" + attributekey);
                        buttonbox.add(idedit);
                        // jswLabel imagesize = new jswLabel(
                        // " size=" + anattribute.getValue().length());
                        // buttonbox.setBackground(Color.pink);
                        attributepanel.addCell(buttonbox, row, 3);
                    }
                } else
                {
                    jswLabel atype = new jswLabel("type");
                    atype.setText(attributequalifier);
                    // atype.setEnabled(false);
                    attributepanel.addCell(atype, " FILLW ", row, 1);
                    if (anattribute.isImage())
                    {
                        jswImage animage = new jswImage(anattribute.getValue());
                        // animage.setHeight
                        attributepanel.addCell(animage.DisplayImage(), row, 2);

                    } else
                    {
                        String value = anattribute.getFormattedValue();
                        jswLabel alabel2 = new jswLabel(value);
                        attributepanel.addCell(alabel2, " FILLW ", row, 2);
                    }
                    jswPanel buttonbox = new jswVerticalPanel("title", false, false);
                    jswButton idedit = new jswButton(this, "EDIT ME.", "EDITATTRIBUTE:" + attributekey);
                    buttonbox.add(idedit);
                    attributepanel.addCell(buttonbox, row, 3);
                }

                if (edit.equalsIgnoreCase(""))
                {
                    jswButton idedit = new jswButton(this, "EDIT ME..", "EDITATTRIBUTE:" + attributekey);
                    attributepanel.addCell(idedit, row, 3);
                } else if (edit == "editattribute")
                {

                }
                row++;
            }
        }
        if (edit.equalsIgnoreCase(""))
        {
            jswHorizontalPanel newattributepanel = new jswHorizontalPanel();
            newlabel = new jswDropDownBox(null, "Select:", "something");
            ArrayList<String> varry = mcAttributeTypes.toList();
            newlabel.setEnabled(false);
            newlabel.addList(varry);
            newlabel.setEnabled(true);
            newattributepanel.add(" WIDTH=200 ", newlabel);
            jswPanel buttonbox = new jswHorizontalPanel();
            jswButton idupdate = new jswButton(this, "CREATE NEW ATTRIBUTE");
            buttonbox.add("RIGHT", idupdate);
            newattributepanel.add("RIGHT", buttonbox);
            add(" FILLW ", newattributepanel);
        }

        mcAttributes reltionslist = selcontact.selectLinkToAttributes("related");
        if (reltionslist != null)
        {
            jswVerticalPanel reltionspanel = makeLinkToPanel(reltionslist, "related", "Related to");
            add(reltionspanel);
        }

        mcAttributes memberstlist = selcontact.selectLinkToAttributes("hasmember");
        if (memberstlist != null)
        {
            jswVerticalPanel memberspanel = makeLinkToPanel(memberstlist, "hasmember", "Has Members");
            add(memberspanel);
        }

        mcAttributes orglist = selcontact.selectLinkToAttributes("memberof");
        if (orglist != null)
        {
            jswVerticalPanel orgpanel = makeLinkToPanel(orglist, "memberof", "Member Of");
            add(orgpanel);
        }

        mcAttributes relatedlist = selcontact.selectLinkFromAttributes("related");
        if (relatedlist != null)
        {
            jswVerticalPanel linkfrompanel = makeLinkToPanel(relatedlist,"related", "ex-Related");
            add(linkfrompanel);
        }

        mcAttributes linkedmemberlist = selcontact.selectLinkFromAttributes("memberof");
        if (linkedmemberlist != null)
        {
            jswVerticalPanel linkfrompanel = makeLinkToPanel(linkedmemberlist, "memberof", "ex-MemberOf");
            add(linkfrompanel);
        }




        if (edit.equalsIgnoreCase(""))
        {
            jswHorizontalPanel newmemberpanel = new jswHorizontalPanel("newmember", false);
            //newmemberpanel.setTrace(true);
            newmemberpanel.applyStyle(panelstyles.getStyle("borderstyle"));
            linkselect = new jswDropDownBox(null, edattributename);
            Vector<String> llist = new Vector<String>();
            llist.add("memberof");
            llist.add("hasmember");
            llist.add("related");
            linkselect.addList(llist);
            newmemberpanel.add(" LEFT ", linkselect);
            linkselect.setStyleAttribute("mywidth", 120);
            linkselect.applyStyle();
            parentselect = new jswDropDownContactBox(null, "Select Contact", false);
            parentselect.addList(MyContacts.selbox.getAllcontactlist().makeOrderedContactsVector());
            newmemberpanel.add("  ", parentselect);// paul to fix
            parentselect.setStyleAttribute("mywidth", 300);
            parentselect.applyStyle();
            atteditbox = new jswTextBox(this, "attedit");
            atteditbox.setEnabled(true);
            newmemberpanel.add("  ", atteditbox);
            atteditbox.setStyleAttribute("mywidth", 100);
            atteditbox.applyStyle();
            jswPanel buttonbox = new jswHorizontalPanel();
            jswButton addmember = new jswButton(this, "ADD AS LINK", "ADDLINK");
            buttonbox.add("RIGHT", addmember);
            newmemberpanel.add("RIGHT", buttonbox);
            add(" FILLW ", newmemberpanel);
            jswHorizontalPanel groupmemberpanel = new jswHorizontalPanel();
            groupmemberpanel.applyStyle(panelstyles.getStyle("borderstyle"));
            groupselect = new jswDropDownBox(this, "Select Group");
            groupselect.addList(MyContacts.selbox.getTaglist());
            groupmemberpanel.add(" WIDTH=300 ", groupselect);// paul to fix
            // atteditbox = new jswTextBox();
            // atteditbox.setEnabled(true);
            // groupmemberpanel.add(atteditbox);
            jswPanel bbuttonbox = new jswHorizontalPanel();
            jswButton addgroup = new jswButton(this, "ADD GROUP AS MEMBERS", "ADDGROUP");
            bbuttonbox.add("RIGHT", addgroup);
            groupmemberpanel.add("RIGHT", bbuttonbox);
            add(" FILLW ", groupmemberpanel);
        }
        this.repaint();
        this.validate();
        //MyContacts.topgui.refreshView();
        MyContacts.topgui.getContentPane().validate();
    }
}
