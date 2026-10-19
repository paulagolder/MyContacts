package org.lerot.mycontacts;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBluer;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
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
    public static String version = "_20251205";
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
    public mcContactSet templist;
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
    public String docs;
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
    private final jswLabel title;
    private final jswLabel source;

    public MyContacts(int w, int h)
    {
        super("MyContacts " + version);
        defdimension = new Dimension(w, h);
        userdir = System.getProperty("user.dir");
        userhome = System.getProperty("user.home");
        user = System.getProperty("user.name");
        osversion = System.getProperty("os.version");
        os = System.getProperty("os.name");
        String home = userhome.replace('\\', '/');
        if (os.startsWith("Windows"))
        {
            dotcontacts = home + "/.mccontacts/";
            letterfolder = home + "/Documents/correspondance";
            docsfolder = home + "/Documents/correspondance";
        } else
        {
            dotcontacts = home + "/.mccontacts/";
            desktop = home + "/Desktop/";
            letterfolder = desktop + "Labels and Letters/";
            docsfolder = home + "/Documents/correspondance";
        }
        java.net.URL jstatIconURL = ClassLoader.getSystemClassLoader()
                .getResource("mccontacts.png");

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
        propsfile = dotcontacts + "properties.xml";
        props = readProperties(propsfile);
        dbsource = props.getProperty("database", "mcdb.sqlite");
        budir = props.getProperty("backupdir", dotcontacts + "backups");
        if (!new File(budir).isAbsolute()) budir = home + "/" + budir;
        budir = budir.replaceAll("/+$", "");
        File budirfile = new File(budir);
        if (!budirfile.isDirectory() && !budirfile.mkdirs())
            System.err.println("Cannot create backup directory " + budir);
        docs = props.getProperty("docs", "Documents/correspondance/");
        currentcon = new mcDataSource(dotcontacts + dbsource);
        templist = new mcContactSet();
        topgui = this;
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        initiateStyles();
        bigpanel = new jswVerticalPanel("bigpanel", true, true);
        bigpanel.setBorder(BorderFactory.createLineBorder(Color.blue));
        bigpanel.setName("bigpanel");
        bigpanel.setPreferredSize(defdimension);
        bigpanel.setSize(defdimension);
        bigpanel.setMinimumSize(defdimension);
        getContentPane().add(bigpanel);
        jswHorizontalPanel optionBar = new jswHorizontalPanel();
        buttonset = new jswPushButtonset(this, "mode", false, false);
        buttonset.setBorder(jswStyle.makeLineBorder(Color.red, 1));
        buttonset.addNewButton("Browse");
        buttonset.addNewButton("Search");
        buttonset.addNewButton("Edit");
        buttonset.addNewButton("Tools");
        buttonset.setButtonSelection("Browse");
        optionBar.add(" RIGHT ", buttonset);
        optionBar.setBorder(jswStyle.makeLineBorder(Color.pink, 1));
        bigpanel.add(" FILLW ", optionBar);
        jswHorizontalPanel sourceBar = new jswHorizontalPanel();
        title = new jswLabel(dbtitle);
        sourceBar.add(" ", title);
        source = new jswLabel(dbsource);
        sourceBar.add(" ", source);
        sourceBar.setBorder(jswStyle.makeLineBorder(Color.GREEN, 1));
        bigpanel.add(" height=40 ", sourceBar);
        sourceBar.setStyleAttribute(" height", 50);
        sourceBar.applyStyle();
        selbox = new selectorBox(this, this);
        bigpanel.add("FILLW", selbox);
        mainpanel = new jswVerticalPanel("mainpanel", false, false);
        mainpanel.setPanelname("mainpanel");
        //mainpanel.setTrace(true);
        bigpanel.add(" FILLH FILLW", mainpanel);
        bigpanel.setBorder(jswStyle.makeLineBorder(Color.GRAY, 3));
        abrowsepanel = new browsePanel();
        abrowsepanel.setTrace(true);
        mainpanel.add(" FILLH FILLW ", abrowsepanel);
        asearchpanel = new searchPanel("search", false, false);
        asearchpanel.makesearchPanel(selbox, this);
        aneditpanel = new editPanel();
        selbox.setEnabled(true);
        startup();
        toolspanel = new ToolsPanel(this);
        selbox.setTaglist();
        selbox.refreshAllContacts("1");
        refreshView();
        initDragAndDrop();
        MyContacts.started = true;
    }

    public static void main(String[] args)
    {
        try
        {
            PlasticLookAndFeel.setPlasticTheme(new DesertBluer());
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (Exception e)
        {
        }
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        mframe = new MyContacts(1200, 900);
        mframe.setVisible(true);
        mframe.addWindowListener(new WindowAdapter()
        {

            @Override
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        mframe.getContentPane().setLayout(
                new BoxLayout(mframe.getContentPane(), BoxLayout.X_AXIS));
        Dimension actual = new Dimension();
        actual.width = 1200;
        actual.height = 900;
        mframe.setSize(actual);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - mframe.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - mframe.getHeight()) / 2);
        mframe.setLocation(x, y);
    }

    public static void initiateStyles()
    {
        panelstyles = jswStyles.getDefaultStyles();
        panelstyles.name = "panel";

        jswStyle jswWidgetStyles = panelstyles.makeStyle("jswWidget");
        jswWidgetStyles.putAttribute("backgroundColor", "#e0dcdf");
        jswWidgetStyles.putAttribute("boxbackgroundColor", "GREEN");
        jswWidgetStyles.putAttribute("foregroundColor", "Black");
        jswWidgetStyles.putAttribute("borderWidth", "0");
        jswWidgetStyles.putAttribute("fontsize", "14");
        jswWidgetStyles.putAttribute("borderColor", "blue");

        jswStyle jswLabelStyles = panelstyles.makeStyle("jswLabel");
        jswStyle largelabelStyle = panelstyles.makeStyle("largeLabel");
        largelabelStyle.putAttribute("fontsize", "30");
        largelabelStyle.putAttribute("foregroundColor", "Red");

        jswStyle mediumlabelStyle = panelstyles.makeStyle("mediumLabel");
        mediumlabelStyle.putAttribute("fontsize", "19");
        mediumlabelStyle.putAttribute("foregroundColor", "Blue");

        jswStyle jswButtonStyles = panelstyles.makeStyle("jswButton");
        jswButtonStyles.putAttribute("fontsize", "10");

        jswStyle jswToggleButtonStyles = panelstyles
                .makeStyle("jswToggleButton");
        jswToggleButtonStyles.putAttribute("foregroundColor", "Red");

        jswStyle jswTextBoxStyles = panelstyles.makeStyle("jswTextBox");

        jswTextBoxStyles.putAttribute("backgroundColor", "#e0dcdf");

        jswStyle jswDropDownBoxStyles = panelstyles.makeStyle("jswDropDownBox");

        jswStyle jswhpStyles = panelstyles.makeStyle("jswContainer");
        jswhpStyles.putAttribute("backgroundColor", "#C0C0C0");

        jswStyle jswDropDownContactBoxStyles = panelstyles
                .makeStyle("jswDropDownContactBox");
        jswDropDownContactBoxStyles.putAttribute("backgroundColor", "#C0C0C0");
        jswDropDownContactBoxStyles.putAttribute("fontsize", "10");

        jswStyle jswScrollPaneStyles = panelstyles
                .makeStyle("jswScrollPaneStyles");
        jswScrollPaneStyles.putAttribute("backgroundColor", "#C0C0C0");
        jswScrollPaneStyles.putAttribute("fontsize", "10");

        jswStyle jswBorderStyle = panelstyles.makeStyle("borderstyle");
        jswBorderStyle.putAttribute("borderWidth", "1");
        jswBorderStyle.putAttribute("borderColor", "black");

        jswStyle hpanelStyle = panelstyles.makeStyle("hpanelstyle");
        hpanelStyle.putAttribute("borderWidth", "2");
        hpanelStyle.putAttribute("borderColor", "blue");
        hpanelStyle.putAttribute("height", "100");

        jswStyle pbStyle = panelstyles.makeStyle("jswPushButton");
        pbStyle.putAttribute("backgroundColor", "#C0C0C0");
        pbStyle.putAttribute("fontsize", "10");

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

        jswStyle col2style = tablestyles.makeStyle("col_1");
        //col1style.putAttribute("fontStyle", Font.BOLD);
        //col2style.putAttribute("fontSize", "16");
        col2style.putAttribute("minwidth", true);
        col2style.putAttribute("backgroundColor", "GREEN");

        return tablestyles;
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        String action = evt.getActionCommand().toUpperCase();
        System.out.println(" MyContacts action :" + action);
        if (action.startsWith("MODE:"))
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
        title.setText(dbtitle);
        source.setText(" (" + dbsource + ")");
        System.out.println("No of contacts = " + ncon);
        mcDataTypes.loadTypes();
        mcAttributeTypes.loadAttributeTypes();
        selbox.setBrowseFilter("all");
        mode = "BROWSE";
        mcLetter.getTemplates(dotcontacts);
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
                    File directory = new File(MyContacts.docsfolder + File.separator
                            + selcontact.getID());
                    if (!directory.exists())
                    {
                        directory.mkdir();
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
                                File dest = new File(MyContacts.docsfolder
                                        + File.separator + selcontact.getID()
                                        + File.separator + source.getName());

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
