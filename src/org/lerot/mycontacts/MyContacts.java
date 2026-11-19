package org.lerot.mycontacts;

import org.lerot.mycontacts.gui.*;
import org.lerot.mycontacts.gui.widgets.jswDropDownContactBox;
import org.lerot.mywidgets.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MyContacts extends JFrame implements ActionListener
{

    private static final long serialVersionUID = 1L;
    public static JFrame mframe;
    public static jswStyles panelstyles;
    public static Component browserpanel;
    public static String certificatepath;
    public static boolean started = false;
    public static boolean showborders;
    public static String temppath;
    public static MyContacts topgui;
    public static selectorBox selbox;
    public static String letterfolder;
    public static String docsfolder;
    public static Map<String, Map<String, String>> labeltemplates = null;
    public static mcDataTypes alldatatypes;
    public static mcAttributeTypes myattributetypes;
    // the version, written into the jar by the build (-PappVersion; the
    // GitHub workflow takes it from the release tag)
    public static String version = appVersion();
    // the program's icon, inside the program
    public static final String ICON = "/org/lerot/mycontacts/mccontacts.png";
    // settings folder (~/.mccontacts/), read by non-GUI code; tests point it elsewhere
    public static String settingsdir;
    private final Dimension defdimension;
    public mcDataSource currentcon;
    public browsePanel abrowsepanel;
    public editPanel aneditpanel;
    public searchPanel asearchpanel;
    public jswDropDownContactBox contactselect;
    public String edattributename;
    public Font footnotefont;
    public Font headingfont;
    public Font promptfont;
    public jswPanel mainpanel;
    public String mode = "main";
    public String user;
    public String username;
    public String view = "main";
    public String dotcontacts;
    public mcImports imported;
    public String desktop;
    public Properties props;
    public String propsfile;
    public String dbsource;
    public String budir;
    jswVerticalPanel bigpanel;
    jswPushButtonset buttonset;
    String userdir;
    private final String os;
    private final String osversion;
    private final ToolsPanel toolspanel;
    private final String userhome;
    private ImageIcon jstatIcon;
    private String dbtitle;
    private jswPushButton infobutton;

    public MyContacts(int w, int h)
    {
        super("MyContacts");
        // FlatLaf draws the title bar, with soft corners, like the viewer's
        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        // in the colour of the mode buttons' strip below it
        getRootPane().putClientProperty("JRootPane.titleBarBackground", Color.decode(jswTheme.NAVAREA));
        Main.roundCorners(this);
        defdimension = new Dimension(w, h);
        userdir = System.getProperty("user.dir");
        userhome = System.getProperty("user.home");
        user = System.getProperty("user.name");
        osversion = System.getProperty("os.version");
        os = System.getProperty("os.name");
        String home = userhome.replace('\\', '/');
        dotcontacts = mcSettings.folder();
        desktop = home + "/Desktop/";
        mcSettings.setUpFolders();
        java.net.URL jstatIconURL = MyContacts.class.getResource(ICON);

        if (jstatIconURL != null)
        {
            jstatIcon = new ImageIcon(jstatIconURL);
            Image jstatIconImage = jstatIcon.getImage();
            this.setIconImage(jstatIconImage);
            new Dimension(jstatIcon.getIconWidth() + 2,
                    jstatIcon.getIconHeight() + 2);
        } else
            System.out.println("no icon");
        System.out.println("user :" + user);
        System.out.println("user directory :" + userdir);
        System.out.println("operating system :" + os + "(" + osversion + ")");
        settingsdir = dotcontacts;
        propsfile = dotcontacts + "properties.xml";
        props = readProperties(propsfile);
        dbsource = props.getProperty("database", "mcdb.sqlite");
        budir = props.getProperty("backupdir", dotcontacts + "backups");
        if (!new File(budir).isAbsolute()) budir = home + "/" + budir;
        budir = budir.replaceAll("/+$", "");
        File budirfile = new File(budir);
        if (!budirfile.isDirectory() && !budirfile.mkdirs())
            System.err.println("Cannot create backup directory " + budir);
        // the connection Main opened, which the Contacts viewer shares
        currentcon = mcDataObject.current();
        if (currentcon == null) currentcon = new mcDataSource(dotcontacts + dbsource);
        topgui = this;
        Main.closeHides(this);
        initiateStyles();
        bigpanel = new jswVerticalPanel("bigpanel", true, true);
        bigpanel.setBorder(jswStyle.makeLineBorder());
        bigpanel.setName("bigpanel");
        bigpanel.setPreferredSize(defdimension);
        bigpanel.setSize(defdimension);
        // free to shrink with the window (see open() for how far)
        bigpanel.setMinimumSize(new Dimension(0, 0));
        getContentPane().add(bigpanel);
        jswHorizontalPanel optionBar = new jswHorizontalPanel();
        buttonset = new jswPushButtonset(this, "mode", false, false);
        buttonset.setBorder(BorderFactory.createEmptyBorder());
        buttonset.addNewButton("Browse");
        buttonset.addNewButton("Search");
        buttonset.addNewButton("Edit");
        buttonset.addNewButton("Tools");
        infobutton = buttonset.addNewButton("Info","INFO");
        buttonset.setButtonSelection("Browse");
        // a new contact, from any mode, with the + of the Contacts viewer
        jswButton newcontact = new jswButton(this, "New contact", "NEWCONTACT");
        JButton plus = (JButton) newcontact.getComponent(0);
        plus.setIcon(new com.formdev.flatlaf.extras.FlatSVGIcon("org/lerot/mycontacts/lite/icons/plus.svg", 14, 14));
        // sized again, for the icon as well as the words
        newcontact.applyStyle();
        optionBar.add(" LEFT ", newcontact);
        optionBar.add(" RIGHT ", buttonset);
      //  optionBar.add(" RIGHT ", new jswButton(this, "Info", "INFO"));
        optionBar.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        jswTheme.tint(optionBar, jswTheme.NAVAREA);
        bigpanel.add(" FILLW ", optionBar);
        selbox = new selectorBox(this, this);
        jswTheme.tint(selbox, jswTheme.SELECTAREA);
        bigpanel.add("FILLW", selbox);
        mainpanel = new jswVerticalPanel("mainpanel", false, false);
        mainpanel.setPanelname("mainpanel");
        //mainpanel.setTrace(true);
        bigpanel.add(" FILLH FILLW", mainpanel);
        bigpanel.setBorder(jswStyle.makeLineBorder());
        bigpanel.setPadding(2); // the titled-border padding left a wasted band at the top
        abrowsepanel = new browsePanel();
        abrowsepanel.setTrace(true);
        jswTheme.tint(abrowsepanel, jswTheme.DETAILAREA);
        mainpanel.add(" FILLH FILLW ", abrowsepanel);
        asearchpanel = new searchPanel("search", false, false);
        asearchpanel.makesearchPanel(selbox, this);
        aneditpanel = new editPanel();
        selbox.setEnabled(true);
        startup();
        toolspanel = new ToolsPanel(this);
        jswTheme.tint(toolspanel, jswTheme.TOOLSAREA);
        selbox.setTaglist();
        selbox.refreshAllContacts("1");
        refreshView();
        initDragAndDrop();
        MyContacts.started = true;
    }

    // the full window, without the Contacts viewer (see Main)
    public static void main(String[] args)
    {
        String[] full = new String[args.length + 1];
        full[0] = "--full";
        System.arraycopy(args, 0, full, 1, args.length);
        Main.main(full);
    }

    // show the full window, made the first time it is wanted; with a contact
    // id, on that contact's Edit page
    public static void open(Integer cid)
    {
        if (mframe == null)
        {
            Main.setupLookAndFeel();
            Dimension size = jswScale.windowSize(1200, 900);
            mframe = new MyContacts(size.width, size.height);
            mframe.getContentPane().setLayout(
                    new BoxLayout(mframe.getContentPane(), BoxLayout.X_AXIS));
            mframe.setSize(size);
            // narrower than this, the buttons in a row would overlap
            mframe.setMinimumSize(jswScale.windowSize(700, 500));
            // centred, but moved left if need be to clear the Contacts
            // viewer at the top right, as far as the screen allows
            Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            int x = screen.x + (screen.width - mframe.getWidth()) / 2;
            Integer viewer = org.lerot.mycontacts.lite.MyContactsLite.leftEdge();
            if (viewer != null) x = Math.min(x, viewer - jswScale.px(16) - mframe.getWidth());
            x = Math.max(screen.x, x);
            int y = screen.y + Math.max(0, (screen.height - mframe.getHeight()) / 2);
            mframe.setLocation(x, y);
        }
        mframe.setVisible(true);
        mframe.toFront();
        if (cid != null) SwingUtilities.invokeLater(() -> topgui.editContact(cid));
    }

    // show the full window on a new, empty contact (the viewer's + button)
    public static void newContact()
    {
        open(null);
        SwingUtilities.invokeLater(() -> topgui.addContact());
    }

    // asks for the new contact's name first: nothing is saved without one
    public void addContact()
    {
        JTextField forename = new JTextField(18);
        JTextField surname = new JTextField(18);
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 0, 3, 8);
        c.gridy = 0;
        form.add(new JLabel("Forename"), c);
        form.add(forename, c);
        c.gridy = 1;
        form.add(new JLabel("Surname"), c);
        form.add(surname, c);
        // the cursor starts in Forename
        forename.addAncestorListener(new javax.swing.event.AncestorListener()
        {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { forename.requestFocusInWindow(); }

            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}

            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });
        int answer = JOptionPane.showConfirmDialog(this, form, "New contact", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) return;
        if (forename.getText().isBlank() && surname.getText().isBlank()) return;
        mcContact contact = mcContacts.createNewContact(forename.getText(), surname.getText());
        if (contact == null)
        {
            JOptionPane.showMessageDialog(this, "The new contact could not be saved.", "New contact",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // into the drop-down, then shown
        selbox.update();
        editContact(contact.getID());
    }

    // show a contact in the Edit view
    public void editContact(int cid)
    {
        mcContact contact = selbox.getAllcontactlist().FindbyID(cid);
        if (contact == null)
        {
            System.err.println("No contact " + cid + " to edit");
            return;
        }
        selbox.setSelcontact(contact);
        selbox.update2();
        mode = "EDIT";
        buttonset.setButtonSelection("Edit");
        refreshView();
        toFront();
    }

    // Run database work from the GUI as one transaction. If it fails nothing
    // is saved, so tell the user and reload the contact list from the database.
    public static boolean dbTransaction(String what, Runnable work)
    {
        boolean ok = topgui.currentcon.transaction(work);
        if (!ok)
        {
            JOptionPane.showMessageDialog(mframe,
                    what + " failed, so no changes were saved.\nSee the console output for details.",
                    "Database error", JOptionPane.ERROR_MESSAGE);
            if (selbox != null) selbox.refreshAllContacts("rollback");
        }
        return ok;
    }

    public static void initiateStyles()
    {
        panelstyles = jswStyles.getDefaultStyles();
        panelstyles.name = "panel";

        jswStyle jswWidgetStyles = panelstyles.makeStyle("jswWidget");
        jswWidgetStyles.putAttribute("foregroundColor", "Black");
        jswWidgetStyles.putAttribute("borderWidth", "0");
        jswWidgetStyles.putAttribute("fontsize", jswTheme.TEXT);
        jswWidgetStyles.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle jswLabelStyles = panelstyles.makeStyle("jswLabel");
        jswStyle largelabelStyle = panelstyles.makeStyle("largeLabel");
        largelabelStyle.putAttribute("fontsize", jswTheme.TITLE);
        largelabelStyle.putAttribute("foregroundColor", "Red");

        jswStyle mediumlabelStyle = panelstyles.makeStyle("mediumLabel");
        mediumlabelStyle.putAttribute("fontsize", jswTheme.TITLE);
        mediumlabelStyle.putAttribute("foregroundColor", "#2B2B2B");

        jswStyle jswButtonStyles = panelstyles.makeStyle("jswButton");
        jswButtonStyles.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle jswToggleButtonStyles = panelstyles
                .makeStyle("jswToggleButton");
        jswToggleButtonStyles.putAttribute("foregroundColor", "Red");

        jswStyle jswTextBoxStyles = panelstyles.makeStyle("jswTextBox");

        jswStyle jswDropDownBoxStyles = panelstyles.makeStyle("jswDropDownBox");

        jswStyle jswhpStyles = panelstyles.makeStyle("jswContainer");

        jswStyle jswDropDownContactBoxStyles = panelstyles
                .makeStyle("jswDropDownContactBox");
        jswDropDownContactBoxStyles.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle jswScrollPaneStyles = panelstyles
                .makeStyle("jswScrollPaneStyles");
        jswScrollPaneStyles.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle jswBorderStyle = panelstyles.makeStyle("borderstyle");
        jswBorderStyle.putAttribute("borderWidth", "1");
        jswBorderStyle.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle hpanelStyle = panelstyles.makeStyle("hpanelstyle");
        hpanelStyle.putAttribute("borderWidth", "2");
        hpanelStyle.putAttribute("borderColor", jswTheme.BORDER);
        hpanelStyle.putAttribute("height", "100");

        jswStyle pbStyle = panelstyles.makeStyle("jswPushButton");
        pbStyle.putAttribute("fontsize", jswTheme.TEXT);

        pbStyle.putAttribute("foregroundColor", "black");
        jswStyle greenfont = panelstyles.makeStyle("greenfont");
        greenfont.putAttribute("foregroundColor", "green");

    }

    public static jswStyles getTableStyles()
    {
        jswStyles tablestyles = new jswStyles();
        tablestyles.name = "defaulttable";
        jswStyle tablestyle = tablestyles.makeStyle("table");
        //tablestyle.putAttribute("backgroundColor", "#c0d6f2");
        tablestyle.putAttribute("borderwidth", "1");
        tablestyle.putAttribute("borderColor", jswTheme.BORDER);
        jswStyle cellstyle = tablestyles.makeStyle("cell");
        //	cellstyle.putAttribute("backgroundColor", "#c0d6f2");
        cellstyle.putAttribute("fontSize", jswTheme.TEXT);
        cellstyle.putAttribute("fontStyle", Font.PLAIN);
        cellstyle.putAttribute("borderColor", jswTheme.CELLBORDER);
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
        col0style.putAttribute("fontSize", jswTheme.TEXT);
        col0style.putAttribute("minwidth", true);
        col0style.setHorizontalAlign("LEFT");

        jswStyle col1style = tablestyles.makeStyle("col_1");
        //col1style.putAttribute("fontStyle", Font.BOLD);
        //col1style.putAttribute("fontSize", jswTheme.HEADING);
        //col1style.putAttribute("backgroundColor", "BLUE");

        jswStyle col2style = tablestyles.makeStyle("col_1");
        //col1style.putAttribute("fontStyle", Font.BOLD);
        //col2style.putAttribute("fontSize", jswTheme.HEADING);
        col2style.putAttribute("minwidth", true);
        return tablestyles;
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        String action = evt.getActionCommand().toUpperCase();
        System.out.println(" MyContacts action :" + action);
        if (action.equals("NEWCONTACT"))
        {
            addContact();
            return;
        } else if (action.equals("MODE:INFO"))
        {
            infoMenu();
            return;
        } else if (action.startsWith("MODE:"))
        {
            String vstr = action.substring(5);
            mode = vstr;
            if (mode.equalsIgnoreCase("tools"))
            {

            }
        } else if (action.startsWith("VIEW:"))
        {
            if (!mode.equalsIgnoreCase("EDIT")) mode = "BROWSE";
        } else
            System.out.println("action  " + action + " unrecognised in main ");
        refreshView();
    }

    // Info offers the help pages and the About popup; the mode button that
    // was lit before stays lit
    private void infoMenu()
    {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem help = new JMenuItem("Help pages");
        help.addActionListener(e -> org.lerot.mycontacts.lite.MyContactsLite.help(null));
        menu.add(help);
        JMenuItem about = new JMenuItem("About MyContacts");
        about.addActionListener(e -> showInfo());
        menu.add(about);
        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener()
        {
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}

            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e)
            {
                buttonset.setButtonSelection(mode);
            }

            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });
        menu.show(infobutton, 0, infobutton.getHeight());
    }

    // the About popup: facts about this build and the open database, then the
    // descriptive lines (and credits) from "infotext" in the properties file
    public void showInfo()
    {
        JPanel info = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        c.insets = new Insets(2, 0, 2, 12);
        String[][] facts = {
                {"Version", version},
                {"Built", buildDate()},
                {"Java", System.getProperty("java.version") + "  (" + System.getProperty("java.vendor") + ")"},
                {"Database", dbtitle + "  (" + dbsource + ")"},
                {"Contacts", String.valueOf(selbox.countAll())},
                {"Settings", propsfile}};
        for (String[] fact : facts)
        {
            c.gridx = 0;
            JLabel name = new JLabel(fact[0]);
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            info.add(name, c);
            c.gridx = 1;
            info.add(new JLabel(fact[1]), c);
            c.gridy++;
        }
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(12, 0, 0, 0);
        String text = props.getProperty("infotext", INFOTEXT);
        for (String line : text.strip().split("\\R"))
        {
            info.add(new JLabel(line.strip()), c);
            c.gridy++;
            c.insets = new Insets(2, 0, 0, 0);
        }
        JOptionPane.showMessageDialog(this, info, "About MyContacts", JOptionPane.PLAIN_MESSAGE);
    }

    // what the Info popup says, unless "infotext" in the properties file replaces it
    static final String INFOTEXT = """
            MyContacts keeps your address book on this computer: contacts, groups and tags,
            address labels, letters and correspondence.
            The small Contacts window looks people up; this window adds and edits them,
            prints labels, imports vCards (from Google, Apple or Outlook) and makes backups.
            Developed by Paul Golder, with Claude (Anthropic) as AI pair programmer.""";

    static String appVersion()
    {
        File jar = programJar();
        if (jar != null)
            try (java.util.jar.JarFile jf = new java.util.jar.JarFile(jar))
            {
                String v = jf.getManifest().getMainAttributes().getValue("Implementation-Version");
                if (v != null) return v;
            } catch (Exception e)
            {
                // not a jar we can read: a development run
            }
        return "development";
    }

    // the jar this program runs from, or null when run from class folders (an IDE)
    static File programJar()
    {
        try
        {
            File f = new File(MyContacts.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return f.isFile() ? f : null;
        } catch (Exception e)
        {
            return null;
        }
    }

    // when this build was made: the date of the program's jar (the entries in
    // it all carry a fixed date, for reproducible builds), or of this class file
    static String buildDate()
    {
        try
        {
            File jar = programJar();
            long time = jar != null ? jar.lastModified()
                    : new File(MyContacts.class.getResource("MyContacts.class").toURI()).lastModified();
            return new SimpleDateFormat("d MMM yyyy, HH:mm").format(new Date(time));
        } catch (Exception e)
        {
            return "unknown";
        }
    }

    public void startup()
    {
        System.out.println(os + " " + userhome);
        System.out.println("opening :" + dbsource);
        Map<String, String> mychecks = currentcon.checkmcdb();
        if (mychecks.get("Valid").equalsIgnoreCase("yes"))
            System.out.println("Checks ok");
        else
            System.out.println("Checks NOT ok");
        String ncon = mychecks.get("No of Contacts");
        dbtitle = mychecks.get("Title");
        System.out.println("title " + dbtitle);
        System.out.println("No of contacts = " + ncon);
        mcDataTypes.loadTypes();
        mcAttributeTypes.loadAttributeTypes();
        selbox.setBrowseFilter("all");
        mode = "BROWSE";
        mcLetter.getTemplates(mcSettings.folder());
        labeltemplates = mcPDF.readTemplates();
    }

    public void refreshView()
    {

     //   buttonset.setButtonSelection(mode);
        if (mode.equals("EDIT"))
        {
            selbox.setVisible(true);
            selbox.navVisible(true);
            selbox.filterboxVisible(false);
            mainpanel.removeAll();
            aneditpanel.makeEditPanel();
            mainpanel.add(aneditpanel);
            aneditpanel.repaint();
        } else if (mode.equals("SEARCH"))
        {
            selbox.setVisible(true);
            selbox.navVisible(false);
            mainpanel.removeAll();
            asearchpanel.makesearchPanel(selbox, this);
            mainpanel.add(" FILLH ", asearchpanel);
            asearchpanel.repaint();
        } else if (mode.equals("BROWSE"))
        {
            selbox.setVisible(true);
            selbox.navVisible(true);
            selbox.filterboxVisible(true);
            mainpanel.removeAll();
            abrowsepanel.removeAll();
            abrowsepanel.makeBrowsePanel();
           // abrowsepanel.setBorder(jswStyle.makeLineBorder(Color.green, 4));
           // abrowsepanel.applyStyle();
            mainpanel.add(" FILLW ", abrowsepanel);
            abrowsepanel.repaint();
        } else if (mode.equals("TOOLS"))
        {
            selbox.setVisible(false);
            mainpanel.removeAll();
            mainpanel.add(" FILLW ", toolspanel);
            toolspanel.refresh();
        }

        mainpanel.repaint();
        if (mframe != null) mframe.repaint();
        getContentPane().repaint();
        getContentPane().validate();
    }

    public Properties readProperties(String propsfile)
    {
        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream(propsfile))
        {
            prop.loadFromXML(in);
        } catch (FileNotFoundException e)
        {
            System.err.println("No properties file " + propsfile + ", using defaults");
        } catch (IOException e)
        {
            System.err.println("Cannot read properties " + propsfile + " : " + e.getMessage()
                    + ", using defaults");
        }
        return prop;
    }

    private void initDragAndDrop()
    {
        this.setDropTarget(new DropTarget()
        {
            private static final long serialVersionUID = 1L;

            // mcContact selcontact = MyContacts.selbox.getSelcontact();
            @Override
            public synchronized void drop(DropTargetDropEvent dtde)
            {
                try
                {
                    System.out.println("File dropped 487 ");
                    mcContact selcontact = MyContacts.selbox.getSelcontact();
                    File directory = new File(mcSettings.contactFolder(selcontact));
                    if (!directory.exists())
                    {
                        directory.mkdirs();
                    }
                    Transferable transfer = dtde.getTransferable();
                    if (transfer.isDataFlavorSupported(
                            DataFlavor.javaFileListFlavor))
                    {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List objects = (List) transfer
                                .getTransferData(DataFlavor.javaFileListFlavor);
                        for (Object object : objects)
                        {

                            if (object instanceof File source)
                            {
                                // File dest = new
                                // File(System.getProperty("user.home")+File.separator+"source.getName());"
                                File dest = new File(directory, source.getName());

                                Files.copy(Paths.get(source.getAbsolutePath()),
                                        Paths.get(dest.getAbsolutePath()),
                                        StandardCopyOption.REPLACE_EXISTING);
                                SimpleDateFormat sdf = new SimpleDateFormat(
                                        "yyyy-MM-dd");
                                String date = sdf.format(source.lastModified());


                                System.out.println("File copied from 509 "
                                        + source.getAbsolutePath() + " to "
                                        + dest.getAbsolutePath());
                            }
                        }
                    } else if (transfer
                            .isDataFlavorSupported(DataFlavor.stringFlavor))
                    {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        String type = (String) transfer
                                .getTransferData(DataFlavor.stringFlavor);
                        System.out
                                .println("Data flavor not supported: " + type);
                    } else
                    {
                        System.out.println("Data flavor not supported.");
                    }
                } catch (UnsupportedFlavorException ex)
                {
                    System.err.println("UFException " + ex.getMessage());
                } catch (IOException ex)
                {
                    System.err.println("IOException " + ex.getMessage());
                } catch (Exception ex)
                {
                    System.err.println("Exception " + ex.getMessage());
                } finally
                {
                    dtde.dropComplete(true);
                }
            }
        });
    }


}
