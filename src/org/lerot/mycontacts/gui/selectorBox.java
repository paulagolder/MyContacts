package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.gui.widgets.jswDropDownContactBox;
import org.lerot.mycontacts.*;
import org.lerot.mywidgets.*;
import org.lerot.mywidgets.jswTextBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

public class selectorBox extends jswVerticalPanel
        implements ActionListener, FocusListener, KeyListener
{
    private static final long serialVersionUID = 1L;
    private final jswButton temptag;
    private final jswHorizontalPanel filterpanel;
    public mcContacts browsecontactlist;
    public mcContacts searchresultlist;
   // mcImports imported;
    jswDropDownContactBox contactselectbox;
    private final jswTextBox filterbox;
    private final jswDropDownBox tagselectbox;
    private final jswLabel allcontacts;
    private final jswLabel browsecontacts;
    private mcContact selcontact;
    private final JButton setposition;
    private final mcContacts currentcontactlist;
    private mcContacts allcontactlist;
    private String searchterm;
    private String browsefilter = "all";
    private final jswHorizontalPanel filterbar;
    private final jswHorizontalPanel selectbox;
    private mctagList taglist;

    public selectorBox(MyContacts aparent, ActionListener al)
    {
        super("selectorbox", true, true);
        this.applyStyle(createStyle());
        currentcontactlist = new mcContacts();
        browsecontactlist = new mcContacts();
        allcontactlist = new mcContacts();
        searchresultlist = new mcContacts();
        setposition = new JButton("freddy");
        setposition.addActionListener(al);
        filterbar = new jswHorizontalPanel();
        allcontacts = new jswLabel("All Contacts");
        filterbar.add(" LEFT ", allcontacts);
        tagselectbox = new jswDropDownBox(this, "Select Group");
        tagselectbox.setPanelname("tagselect");
        tagselectbox.addActionListener(this, "BROWSESELECT");
        filterbar.add(" MIDDLE WIDTH=200 ", tagselectbox);
        browsecontacts = new jswLabel("Browsing:");
        filterbar.add(" RIGHT ", browsecontacts);
        this.add(" FILLW", filterbar);
        this.setVisible(true);
        selectbox = new jswHorizontalPanel();
        jswButton previous = new jswButton(this, "Previous");
        selectbox.add(" LEFT ", previous);
        filterpanel= new jswHorizontalPanel("filterpanel",false,false);
        filterbox = new jswTextBox(this, "stuff",100,"1234");
        filterbox.setEnabled(true);
        filterbox.addFocusListener(this);
        filterbox.addKeyListener(this);
        filterbox.setBorder(BorderFactory.createEmptyBorder());
        filterpanel.add(" WIDTH=90 ", filterbox);
        filterbox.setStyleAttribute("mywidth", 200);
        filterbox.applyStyle();
        jswButton filterbutton = new jswButton(this, ">", "NEXTFILTER");
        filterbutton.setMinimumSize(new Dimension(40, 30));
        filterbutton.setMaximumSize(new Dimension(40, 30));
        filterpanel.add("  ", filterbutton);
        selectbox.add("  ", filterpanel);
        contactselectbox = new jswDropDownContactBox(this, "Select Contact", false);
        contactselectbox.setPanelname("contactselect");
        selectbox.add(" MIDDLE  ", contactselectbox);
        temptag = new jswButton(this, "TempTag");
        selectbox.add("  ", temptag);
        contactselectbox.addList(browsecontactlist.makeOrderedContactsVector());
        contactselectbox.addActionListener(this, "contactselected");
        jswButton next = new jswButton(this, "Next");
        selectbox.add(" RIGHT ", next);
        this.add(" FILLW ", selectbox);
        this.setBorder(jswStyle.makeLineBorder());
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        String cmd = evt.getActionCommand().toUpperCase();
		System.out.println(" sb:"+ cmd);
        String action = "";
      //  HashMap<String, String> cmdmap = jswUtils.parsecsvstring(cmd);
        //action = cmdmap.get("COMMAND");
        action = cmd;
        if (action == null) return;
        if (action.equals("BROWSESELECT"))
        {
            setBrowseFilter(tagselectbox);
            browsecontactlist = searchTag(browsefilter);
          //  searchterm = cmdmap.get("VALUE");
            selcontact = null;
            update();
        } else if (action.equalsIgnoreCase("TEMPTAG"))
        {
            MyContacts.topgui.templist.add(selcontact);
            MyContacts.topgui.templist.printlist("temptag");
        } else if (action.equals("REFRESH"))
        {
            // refreshSelection();
        } else if (action.equals("PREVIOUS"))
        {
            selcontact = contactselectbox.setPreviousValue();
            MyContacts.topgui.aneditpanel.clearEdit();
        } else if (action.equals("NEXTFILTER"))
        {
            findnext();
            MyContacts.topgui.aneditpanel.clearEdit();
        } else if (action.equals("NEXT"))
        {
            selcontact = contactselectbox.setNextValue();
            MyContacts.topgui.aneditpanel.clearEdit();
        } else if (action.startsWith("CONTACTSELECTED:"))
        {
            selcontact = contactselectbox.getSelectedValue();
            System.out.println(selcontact);
            update2();
        } else if (action.equals("CONTACTSELECTED"))
        {
            // the raw combo box event; the drop-down box also forwards it as
            // CONTACTSELECTED:<id>, which does the work
            return;
        } else
            System.out.println("action  " + action + " unrecognised in selectorbox ");
        MyContacts.topgui.refreshView();
    }

    public void addtoGroupfilter(String filter)
    {
        browsecontactlist.addtoGroupfilter(filter);
    }

    public void clearGroupfilter()
    {
        browsecontactlist.clearGroupfilter();
    }

    public void clearSearchResultList()
    {
        searchresultlist.clear();
    }

    public int countAll()
    {
        return allcontactlist.size();
    }

    public void filterContacts()
    {
        // browsecontactlist.filterContacts(allcontactlist);
    }

    public mcContact FindbyID(String id)
    {
        String lid = mcContact.makeIDstr(id);
        mcContact found = allcontactlist.FindbyID(lid);
        if (found == null)
        {
            System.out.println(" contact not found :" + id);
            return null;
        }
        return found;
    }

    public mcContact FindbyID(int cid)
    {
        return allcontactlist.FindbyID(cid);
    }

    public mcContact FindbyIDstr(String selid)
    {
        return allcontactlist.FindbyID(selid);
    }

    public mcContact FindbyTID(String sname)
    {
        return allcontactlist.FindbyTID(sname);
    }

    public mcContact findFirst()
    {
        return currentcontactlist.findFirst();
    }

    public mcContact findName(String aname)
    {
        return allcontactlist.findName(aname);
    }

    public mcContact findName2(String aname)
    {
        return allcontactlist.findName2(aname);
    }

    public mcContact findName3(String aname)
    {
        return allcontactlist.findName3(aname);
    }

    public mcContact findPhone(String attkey, String text)
    {
        return allcontactlist.findPhone(attkey, text);
    }

    public mcContact findValue(String attkey, String text)
    {
        return allcontactlist.findValue(attkey, text);
    }

    @Override
    public void focusGained(FocusEvent arg0)
    {
        // TODO Auto-generated method stub
    }

    public void findnext()
    {
        String filter = filterbox.getText();
        selcontact = contactselectbox.findNext(filter);
        contactselectbox.setSelected(selcontact);
        contactselectbox.contactddbox.setSelectedItem(selcontact);
    }

    @Override
    public void focusLost(FocusEvent e)
    {
        Object source = e.getSource();
        if (source instanceof javax.swing.JTextField)
        {
            findnext();
            MyContacts.topgui.refreshView();
        }
    }

    public mcContact get(String selectid)
    {
        return allcontactlist.get(selectid);
    }

    public mcContacts getAllcontactlist()
    {
        return allcontactlist;
    }

    public void setAllcontactlist(mcContacts allcontactlist)
    {
        this.allcontactlist = allcontactlist;
    }

    public mcContacts getBrowsecontactlist()
    {
        return browsecontactlist;
    }

    public void setBrowsecontactlist(mcContacts browsecontactlist)
    {
        this.browsecontactlist = browsecontactlist;
    }

    public String getSearchterm()
    {
        return searchterm;
    }

    public void setSearchterm(String searchterm)
    {
        this.searchterm = searchterm;
    }

    public mcContact getSelcontact()
    {
        return selcontact;
    }

    public void setSelcontact(mcContact aselcontact)
    {
        this.selcontact = aselcontact;
    }

    public void setSelcontact(String selcon)
    {
        selcontact = FindbyIDstr(selcon);
    }

    public mcContacts getSearchResultList()
    {
        return searchresultlist;
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        int keyCode = e.getKeyCode();
        Object source = e.getSource();
        if (source instanceof javax.swing.JTextField)
        {
            if (keyCode == 10 || keyCode == 8)
            {
                // System.out.println("keypressed 2 ");
                if (keyCode == 10) contactselectbox.requestFocus();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyTyped(KeyEvent arg0)
    {
        // TODO Auto-generated method stub
    }

    public Vector<mcContact> makeContactsVector()
    {
        return currentcontactlist.makeOrderedContactsVector();
    }

    private void makeEvent(String cmd)
    {
        setposition.setActionCommand(cmd);
        setposition.doClick();
    }

    public void navVisible(boolean b)
    {
        selectbox.setVisible(b);
    }

    public void filterboxVisible(boolean b)
    {
        filterpanel.setVisible(b);
    }

    public void put(mcContact newcontact)
    {
        allcontactlist.put(newcontact);
        currentcontactlist.put(newcontact);
    }

    public void refreshAll()
    {
        /*
         * //setAllContacts("ref"); filterbox.setAlert(false);
         * //browsecontactlist.setGroupFilter(browseselect);
         * browsecontactlist.clearError();
         * browsecontactlist.filterContacts(allcontactlist);
         * System.out.println(" browsecontacts refresh " +
         * browsecontactlist.size()); if (browsecontactlist.textfiltererror) {
         * System.out.println(" browse refresh textfiltererror ");
         * filterbox.setAlert(true); }
         */
    }

    public void remove(mcContact selcontact)
    {
        allcontactlist.remove(selcontact);
        searchresultlist.remove(selcontact);
        browsecontactlist.remove(selcontact);

    }

    public void removesearchcontact(String selcon)
    {
        mcContact delcontact = allcontactlist.FindbystrID(selcon);
        searchresultlist.remove(delcontact);
        MyContacts.selbox.setSelectedcontactlist(searchresultlist);
        //// mcContacts currentsearchlist = browsecontactlist;
        // currentsearchlist = selectedcontactlist;

    }

    public mcContacts searchAttribute(String searchterm2)
    {
        mcContacts currentsearchlist = browsecontactlist;
        mcContacts found = currentsearchlist.searchAttribute(searchterm2);
        searchterm = searchterm2;
        return found;
    }

    public mcContacts searchTag(String tag)
    {
        mcContacts found = new mcContacts();
        if (tag == null)
        {
            return null;
        } else if (tag == "all")
        {
            allcontactlist.refreshAllContacts();
            found = allcontactlist.createCopy();
            return found;
        } else if (tag == "selection")
        {
            if (searchresultlist.size() > 0)
            {
                found = searchresultlist.createCopy();
            } else
                found = allcontactlist.createCopy();
            return found;
        } else if (tag.startsWith("."))
        {
            TreeSet<String> foundids = mcContacts.searchAllTags(tag);
            for (String id : foundids)
            {
                mcContact fcontact = mcContact.getContact(id);
                found.put(fcontact.getIDstr(), fcontact);
            }
            return found;
        } else
        {
            TreeSet<String> foundids = allcontactlist.searchTags(tag);
            for (String id : foundids)
            {
                // System.out.println(" foundid " + id);
                mcContact fcontact = this.FindbyID(id);
                if (fcontact != null)
                    found.put(fcontact.getIDstr(), fcontact);
                else
                    System.out.println(" problem with " + tag);
            }
            return found;
        }
    }

    public void refreshAllContacts(String flag)
    {
        allcontactlist.refreshAllContacts();
        //System.out.println("refreshallcontacts .." + flag);
        update();
    }

    @Override
    public void setEnabled(boolean b)
    {
        contactselectbox.setEnabled(b);
    }

    public void setMode(String string)
    {
        // TODO Auto-generated method stub
        // not needed ?
    }

    public void setSelectedcontactlist(mcContacts selectedcontactlist)
    {
        this.searchresultlist = selectedcontactlist;
    }

    public void update()
    {
        browsecontactlist = searchTag(browsefilter);
        contactselectbox.removeActionListener(this);
        contactselectbox.setContactList(browsecontactlist);
        contactselectbox.addActionListener(this, "contactselected");
        allcontacts.setText("All Contacts " + allcontactlist.size());
        browsecontacts.setText("Browse Contacts " + browsecontactlist.size());
        if (selcontact != null)
        {
            contactselectbox.setSelected(selcontact);
        } else
        {
            if (contactselectbox.countSize() > 0)
            {
                contactselectbox.setSelected(0);
            } else
                contactselectbox.setSelected(-1);
        }
        // keep the selected contact in step with the drop-down
        selcontact = contactselectbox.getSelectedValue();
        filterbox.clear();
    }

    public void update2()
    {
        contactselectbox.removeActionListener(this);
        if (selcontact != null)
        {
            contactselectbox.setSelected(selcontact);
        } else
        {
            if (contactselectbox.countSize() > 0)
            {
                contactselectbox.setSelected(0);
                selcontact = contactselectbox.getSelectedValue();
            }
        }
        contactselectbox.addActionListener(this);
        // filterbox.clear();
    }

    public void setBrowseFilter(jswDropDownBox sb)
    {
        browsefilter = sb.getSelectedValue();
    }

    public void setBrowseFilter(String string)
    {
        browsefilter = string;
    }

    public void setTaglist()
    {
        taglist = new mctagList();
        taglist.reloadTags();
        tagselectbox.removeActionListener(this);
        tagselectbox.addItem("all");
        tagselectbox.addItem("selection");
        tagselectbox.addItem("friend");
        tagselectbox.addList(taglist.getTaglist());
        tagselectbox.addActionListener(this,"BROWSESELECT");

    }

    public Vector<String> getTaglist()
    {
        return taglist.toList();
    }

    public jswStyle createStyle()
    {
        jswStyles panelstyles = new jswStyles("selectorbox");
        jswStyle jswBorderStyle = panelstyles.makeStyle("borderstyle");
        jswBorderStyle.putAttribute("borderWidth", "1");
        // jswBorderStyle.putAttribute("borderColor", "#C0C0C0");
        jswBorderStyle.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle hpanelStyle = panelstyles.makeStyle("hpanelstyle");
        hpanelStyle.putAttribute("padding", "5");
        hpanelStyle.putAttribute("borderWidth", "2");
        hpanelStyle.putAttribute("borderColor", jswTheme.BORDER);
        hpanelStyle.putAttribute("height", "100");
        jswStyle pbStyle = panelstyles.makeStyle("jswPushButton");
        pbStyle.putAttribute("fontsize", jswTheme.TEXT);
        pbStyle.putAttribute("foregroundColor", "black");
        jswStyle greenfont = panelstyles.makeStyle("greenfont");
        greenfont.putAttribute("foregroundColor", "green");
        return hpanelStyle;
    }

}
