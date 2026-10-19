package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.mcContact;
import org.lerot.mycontacts.mcContacts;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mywidgets.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;

public class searchPanel extends jswVerticalPanel implements ActionListener
{

    private static final long serialVersionUID = 1L;
    jswTextBox searchfield;
    private ActionListener parentlistener;

    public searchPanel(String title, boolean border, boolean titledborder)
    {
        super(title, border, titledborder);
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {

        String cmd = evt.getActionCommand();
        System.out.println(" here we are sp " + cmd);
    //    HashMap<String, String> cmdmap = jswUtils.parsecsvstring(cmd);
      //  String action = cmdmap.get("command").toUpperCase();
String action = cmd;
        if (action.startsWith("SEARCHATTRIBUTES"))
        {
            String searchterm = searchfield.getText();
            if (searchterm.isEmpty())
            {
                MyContacts.selbox.clearSearchResultList();
                MyContacts.selbox.setSearchterm("");
            } else
            {
                MyContacts.selbox.searchresultlist = MyContacts.selbox.searchAttribute(searchterm);
                // MyContacts.selbox.browsecontactlist = MyContacts.selbox.searchresultlist;
            }
            // if (!searcherror)
            {
                searchfield.clear();
            }
        } else if (action.startsWith("TAGSELECTION"))
        {
            mcContacts sellist = MyContacts.selbox.getSearchResultList();
            for (Entry<String, mcContact> contactentry : sellist.entrySet())
            {
                mcContact scontact = contactentry.getValue();
                MyContacts.topgui.templist.add(scontact);
            }
        } else if (action.startsWith("SEARCHTAGS"))
        {
            String searchterm = searchfield.getText();
            if (searchterm.isEmpty())
            {
                MyContacts.selbox.clearSearchResultList();
                MyContacts.selbox.setSearchterm("");
            } else
                MyContacts.selbox.searchresultlist = MyContacts.selbox.searchTag(searchterm);
            // if (!searcherror)
            {
                searchfield.clear();
            }
        } else if (action.startsWith("REMOVE:"))
        {
            String selcon = action.substring(7);
            MyContacts.selbox.removesearchcontact(selcon);
            MyContacts.topgui.refreshView();

        } else if (action.startsWith("VIEW:"))
        {
            String selcon = action.substring(5);
            MyContacts.topgui.mode = "BROWSE";
            MyContacts.selbox.setSelcontact(selcon);
            //mcContact scon = MyContacts.selbox.getSelcontact();
            MyContacts.topgui.refreshView();
        } else
            System.out.println("search action " + action + " unrecognised ");
        MyContacts.topgui.asearchpanel.makesearchPanel(MyContacts.selbox, parentlistener);
        MyContacts.topgui.getContentPane().validate();
    }

    public void makesearchPanel(selectorBox selbox, ActionListener alistener)
    {
        jswStyle scrollstyle = MyContacts.panelstyles.getStyle("jswScrollPaneStyles");
        Color bcolor = scrollstyle.getColor("backgroundColor", Color.BLUE);
        setBackground(bcolor);
        parentlistener = alistener;
        jswVerticalPanel searchpanel = this;
        //this.setTag("trace");
        //this.setBorder(setLineBorder(Color.red, 4));
        searchpanel.removeAll();
        jswHorizontalPanel idbox = new jswHorizontalPanel("idbox", false);
        searchpanel.add(" FILLW ", idbox);
        jswLabel idpanel1 = new jswLabel("Enter Search Term:");
        idbox.add(idpanel1);
        searchfield = new jswTextBox(this, "Search for");
        //	searchfield.setBorder(jswStyle.makeLineBorder(Color.red, 5));
        if (selbox.getSearchterm() != null && !selbox.getSearchterm().isEmpty())
        {
            searchfield.setText(selbox.getSearchterm());
        }
        idbox.add(" FILLW WIDTH=30 ", searchfield);
        searchfield.setEnabled(true);
        searchfield.setVisible(true);
        searchfield.setStyleAttribute("mywidth", 100);
        searchfield.applyStyle();
        jswButton searchbutton = new jswButton(this, "Search", "SEARCHATTRIBUTES");
        idbox.add(" RIGHT ", searchbutton);
        jswButton searchtagbutton = new jswButton(this, "Search Tags", "SEARCHTAGS");
        idbox.add(" RIGHT ", searchtagbutton);
        jswHorizontalPanel summary = new jswHorizontalPanel();
        mcContacts sellist = selbox.getSearchResultList();
        if (sellist.size() > 0)
        {
            jswButton tagbutton = new jswButton(this, "Tag selection", "TAGSELECTION");
            summary.add(" LEFT ", tagbutton);
        }
        jswLabel summ = new jswLabel(" Total Found =" + sellist.size());
        summary.add(" FILLW ", summ);
        searchpanel.add(" FILLW ", summary);
        jswTable resulttable = new jswTable(this, "contactsfound", searchTableStyles());

        if (sellist.size() == 0)
        {
            searchpanel.add(" FILLW ", resulttable);
            resulttable.addCell(new jswLabel(" no contact selected "), 0, 0);
        } else
        {
            int row = 0;
            for (mcContact acontact : selbox.getSearchResultList().makeOrderedContactsVector())
            {
                jswLabel atid = new jswLabel(acontact.getIDstr());
                jswLabel atTID = new jswLabel(acontact.getTID());
                resulttable.addCell(atid, row, 0);
                resulttable.addCell(atTID, " FILLW ", row, 1);
                jswButton viewcontact = new jswButton(this, "VIEW", "VIEW:"
                        + acontact.getIDstr());
                resulttable.addCell(viewcontact, row, 2);
                jswButton removecontact = new jswButton(this, "REMOVE",
                        "REMOVE:" + acontact.getIDstr());
                resulttable.addCell(removecontact, row, 3);
                row++;
            }
            if (sellist.size() < 10)
            {
                searchpanel.add(" FILLW ", resulttable);
            } else
            {
                resulttable.setBackground(Color.lightGray);
                jswScrollPane scrollableTextArea = new jswScrollPane(resulttable,
                        -10, -10);
                resulttable.setStyleAttribute("mywidth",500);
                resulttable.setStyleAttribute("width",500);
                resulttable.applyStyle();
                scrollableTextArea.setName("resultscroll");
                scrollableTextArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                scrollableTextArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                searchpanel.add(" FILLH FILLW ", scrollableTextArea);
                scrollableTextArea.setStyleAttribute( "mywidth",400);
                scrollableTextArea.setStyleAttribute( "width",400);
                scrollableTextArea.applyStyle();
                //scrollableTextArea.setBorder(setLineBorder(Color.red, 4));
            }
        }
        resulttable.repaint();
        searchpanel.repaint();
        MyContacts.topgui.pack();
        MyContacts.topgui.setVisible(true);
        MyContacts.topgui.mainpanel.repaint();
        this.repaint();
        MyContacts.topgui.getContentPane().validate();
    }


    public static jswStyles searchTableStyles()
    {
        jswStyles tablestyles = new jswStyles();
        tablestyles.name = "defaulttable";
        jswStyle tablestyle = tablestyles.makeStyle("table");
        //tablestyle.putAttribute("backgroundColor", "#c0d6f2");
        tablestyle.putAttribute("borderwidth", "1");
        tablestyle.putAttribute("borderColor", "green");
        jswStyle cellstyle = tablestyles.makeStyle("cell");
        //	cellstyle.putAttribute("backgroundColor", "#c0d6f2");
        cellstyle.putAttribute("fontSize", "12");
        cellstyle.putAttribute("fontStyle", Font.PLAIN);
        cellstyle.putAttribute("borderColor", "black");
        cellstyle.putAttribute("borderWidth", 1);
        cellstyle.putAttribute("padding", 4);

        jswStyle rowstyle = tablestyles.makeStyle("row");
        //row0style.putAttribute("fontStyle", Font.BOLD + Font.ITALIC);
        rowstyle.putAttribute("minheight", 50);

        jswStyle cell00style = tablestyles.makeStyle("cell");
        //cell00style.putAttribute("foregroundColor", "transparent");
        cell00style.putAttribute("minheight", 50);
        jswStyle colstyle = tablestyles.makeStyle("col");
        colstyle.setHorizontalAlign("RIGHT");
        jswStyle col0style = tablestyles.makeStyle("col_0");
        col0style.putAttribute("fontStyle", Font.BOLD);
        col0style.putAttribute("fontSize", "16");
        col0style.putAttribute("backgroundColor", "PINK");
        col0style.putAttribute("minwidth", true);
        col0style.setHorizontalAlign("LEFT");

        jswStyle col1style = tablestyles.makeStyle("col_1");
        //col1style.putAttribute("fontStyle", Font.BOLD);
        //col1style.putAttribute("fontSize", "16");
        //col1style.putAttribute("backgroundColor", "BLUE");

        jswStyle col2style = tablestyles.makeStyle("col_2");
        //col1style.putAttribute("fontStyle", Font.BOLD);
        //col2style.putAttribute("fontSize", "16");
        col2style.putAttribute("minwidth", true);
        col2style.putAttribute("backgroundColor", "GREEN");
        jswStyle col3style = tablestyles.makeStyle("col_3");
        //col1style.putAttribute("fontStyle", Font.BOLD);
        //col2style.putAttribute("fontSize", "16");
        col3style.putAttribute("minwidth", true);
        col3style.putAttribute("backgroundColor", "GREEN");

        return tablestyles;
    }
}
