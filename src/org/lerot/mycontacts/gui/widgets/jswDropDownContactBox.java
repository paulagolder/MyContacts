package org.lerot.mycontacts.gui.widgets;

import org.lerot.mycontacts.mcContact;
import org.lerot.mycontacts.mcContacts;
import org.lerot.mywidgets.jswLabel;
import org.lerot.mywidgets.jswPanel;
import org.lerot.mywidgets.jswStyle;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

public class jswDropDownContactBox extends jswPanel implements ActionListener
{

    private static final long serialVersionUID = 1L;
    private ActionListener actionlistener;
    public JComboBox<mcContact> contactddbox;
    jswLabel label;
    int bl = 100;
    int bh = 30;
    DefaultComboBoxModel<mcContact> listModel;

    public jswDropDownContactBox(ActionListener c, String inlabel)
    {
        this(c,inlabel, false);
        actionlistener = c;
        contactddbox.addActionListener(this);
    }

    public jswDropDownContactBox(ActionListener al,String inLabel, boolean hasborder)
    {
        super("dropdown");
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        listModel = new DefaultComboBoxModel<mcContact>();
        contactddbox = new JComboBox<mcContact>(listModel);
        contactddbox.setPreferredSize(new Dimension(bl, 24));
        contactddbox.setRenderer(new ContactRenderer());
        contactddbox.addActionListener(this);
        actionlistener = al;
      //  addActionListener(al);
        setName(inLabel);
        if (hasborder)
        {
            setBorder(getStyle().makecborder(inLabel));
        } else
            setBorder(jswStyle.makeborder());
        add("FILLW", contactddbox);
        //doStyling();
    }

    public boolean contains(mcContact target)
    {
        int fnd = listModel.getIndexOf(target);
        if (fnd == -1) return false;
        else return true;
    }

    public void applyStyle(jswStyle style)
    {
        contactddbox.setFont(style.getFont());
        int wd = style.getIntegerStyle("mywidth", bl);
        if (wd > bl)
            bl = wd;
        int ht = style.getIntegerStyle("myheight", bh);
        if (ht > bh)
            bh = ht;
        Dimension d = new Dimension(bl, bh);
        contactddbox.setPreferredSize(d);
        contactddbox.setMaximumSize(d);
        contactddbox.setMinimumSize(d);
        setBackground(jswStyle.transparentColor());
        setPreferredSize(d);
        setMaximumSize(d);
        setMinimumSize(d);
    }

    public void addActionListener(ActionListener c)
    {
        contactddbox.addActionListener(c);
    }

    public void addActionListener(ActionListener c, String actionlabel)
    {
        contactddbox.addActionListener(c);
        contactddbox.setActionCommand(actionlabel);
    }

    public void addList(Vector<mcContact> list)
    {
        if (list.size() > 0)
        {
            for (int i = 0; i < list.size(); i++)
            {
                listModel.addElement(list.get(i));
            }
            contactddbox.setSelectedIndex(0);
        }
    }

    public mcContact setNextValue()
    {
        mcContact selcon = getSelectedValue();
        if (selcon == null)
        {
            contactddbox.setSelectedIndex(0);
        } else
        {
            int currentindex = contactddbox.getSelectedIndex();
            if (currentindex < 0) currentindex = 0;
            int nextindex = currentindex + 1;
            if (nextindex < contactddbox.getModel().getSize())
            {
                mcContact next = contactddbox.getItemAt(nextindex);
                contactddbox.setSelectedItem(next);
            } else
            {
                contactddbox.setSelectedIndex(0);
            }
        }
        return getSelectedValue();
    }

    public mcContact setPreviousValue()
    {
        mcContact selcon = getSelectedValue();
        if (selcon == null)
        {
            contactddbox.setSelectedIndex(0);
        } else
        {
            int currentindex = contactddbox.getSelectedIndex();
            if (currentindex > 0)
            {
                int nextindex = currentindex - 1;
                mcContact next = contactddbox.getItemAt(nextindex);
                contactddbox.setSelectedItem(next);
            } else
            {
                contactddbox.setSelectedIndex(0);
            }
        }
        return getSelectedValue();
    }

    public mcContact getSelectedValue()
    {
        if (contactddbox.getSelectedItem() != null)
        {
            return (mcContact) contactddbox.getSelectedItem();
        } else
            return null;
    }


    public boolean isSelected()
    {
        mcContact selcon = getSelectedValue();
        if (selcon == null) return false;
        else return true;
    }

    public void setSelected(mcContact selitem)
    {
        if (contains(selitem))
            contactddbox.setSelectedItem(selitem);
        else
            contactddbox.setSelectedItem(0);
    }

    public void setSelected(int selindex)
    {
        contactddbox.setSelectedIndex(selindex);
    }

    @Override
    public void setEnabled(boolean e)
    {
        if (label != null) label.setEnabled(e);
        contactddbox.setEnabled(e);
        // listModel.setEnabled(e);
    }

    public void clearList()
    {
        listModel.removeAllElements();
        contactddbox.removeAllItems();
    }

    public void setList(Vector<mcContact> list)
    {
        DefaultComboBoxModel<mcContact> newModel = new DefaultComboBoxModel<mcContact>();
        if (list.size() > 0)
        {
            for (int i = 0; i < list.size(); i++)
            {
                newModel.addElement(list.get(i));
            }
        }
        contactddbox.setModel(newModel);
        if (list.size() > 0)
            contactddbox.setSelectedIndex(0);
        else
            contactddbox.setSelectedIndex(-1);
    }

    public void setContactList(mcContacts contactlist)
    {
        Vector<mcContact> cv = contactlist.makeOrderedContactsVector();
        setList(cv);
    }

    public int countSize()
    {
        return contactddbox.getModel().getSize();
    }

    public void setComboBoxEnabled(boolean b)
    {
        contactddbox.setEnabled(b);
    }

    public void removeActionListener(ActionListener al)
    {
        contactddbox.removeActionListener(al);
    }

    public void setActionCommand(String cmd)
    {
        contactddbox.setActionCommand(cmd);
    }

    public mcContact findNext(String filter)
    {
        int currentindex = contactddbox.getSelectedIndex() + 1;
        for (int i = currentindex; i < contactddbox.getModel().getSize(); i++)
        {
            mcContact obj = contactddbox.getItemAt(i);
            if (obj.getTID().toLowerCase().contains(filter))
            {
                return obj;
            }
        }
        return null;
    }

    public void addElement(String text)
    {
        // TODO Auto-generated method stub

    }

    class ContactRenderer extends BasicComboBoxRenderer
    {

        private static final long serialVersionUID = 1L;

        @Override
        public JComponent getListCellRendererComponent(JList list,
                                                       Object value, int index, boolean isSelected,
                                                       boolean cellHasFocus)
        {
            super.getListCellRendererComponent(list, value, index, isSelected,
                    cellHasFocus);

            if (value instanceof mcContact)
            {
                mcContact foo = (mcContact) value;
                setText(foo.getName());
            }

            return this;
        }
    }


    public void actionPerformed(ActionEvent evt)
    {
        //System.out.println(" happy days ");
        //String cmd = evt.getActionCommand();
       // System.out.println(" here we are ddcb " + cmd);
      //  HashMap<String, String> cmdmap = jswUtils.parsecsvstring(cmd);
       // String action= cmdmap.get("command");

      //  HashMap<String,String> am = jswPanel.createActionMap(this, evt) ;
      //  am.put("selected",getSelectedValue().getIDstr());
        Long t = System.currentTimeMillis() / 10000;
        int uniqueId = t.intValue();
        ActionEvent event = new ActionEvent(this, uniqueId, evt.getActionCommand()+":"+getSelectedValue().getIDstr());
        if(actionlistener != null)
            actionlistener.actionPerformed(event);
    }

}
