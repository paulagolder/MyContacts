package org.lerot.mycontacts.lite;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.layout.PlatformDefaults;
import net.miginfocom.swing.MigLayout;
import org.lerot.mycontacts.Main;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mycontacts.gui.addressDialog;
import org.lerot.mycontacts.mcAttribute;
import org.lerot.mycontacts.mcContact;
import org.lerot.mycontacts.mcDataObject;
import org.lerot.mywidgets.jswScale;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;
import java.util.Set;

/*
 * myContactsLite: a small, phone-shaped window (titled MyContacts, like the
 * full window, with "Contacts" as its heading) for looking
 * up a contact while doing something else. Search, pick from the list, and
 * the contact's name (with their photo, if they have one), address, phone,
 * mobile, WhatsApp, email and birthday appear as panels of selectable text. The email opens the mail program; "Edit in
 * MyContacts" opens the full window on that contact, in the same program
 * (see Main). In the title bar, + opens it on a new contact and the window
 * button opens it as it was. The only change it makes to the database is
 * saving a WhatsApp number from the phone box. Stays on top unless unpinned.
 *
 *   ./gradlew run
 *   java -jar MyContacts.jar
 */
public class MyContactsLite extends JFrame
{
	// a soft warm off-white page, with a slightly darker shade for the
	// search box, the list panel and the contact's tiles
	static final Color PAGE = Color.decode("#F4F1EA");
	static final Color SHADE = Color.decode("#E9E4DA");
	static final String SHADEHEX = "#E9E4DA";
	static final Color MUTED = Color.decode("#8A8A8E");
	static final String ACCENT = "#2F6FDB";
	static final Color HIGHLIGHT = Color.decode("#DCE6F8");

	final ContactStore store;
	private final CardLayout pages = new CardLayout();
	private final JPanel content = new JPanel(pages);
	final JTextField search = new JTextField();
	private final DefaultListModel<ContactCard> model = new DefaultListModel<>();
	private final JList<ContactCard> list = new JList<>(model);
	private final JLabel count = new JLabel();
	private final JButton gettingStarted = new JButton("Getting started", icon("help.svg", 14));
	private final JPanel tiles = new TilePanel();
	private ContactCard showing;

	// the whole program, starting with this viewer (see Main)
	public static void main(String[] args)
	{
		Main.main(args);
	}

	private static MyContactsLite window;

	// show the viewer, made the first time it is wanted
	public static void open()
	{
		if (window == null)
		{
			setupLookAndFeel();
			window = new MyContactsLite(new ContactStore());
		}
		window.setVisible(true);
		window.toFront();
	}

	// the viewer's left edge on screen, or null if it is not showing
	public static Integer leftEdge()
	{
		return window != null && window.isVisible() ? window.getX() : null;
	}

	static void setupLookAndFeel()
	{
		Main.setupLookAndFeel();
		// MigLayout sizes below are for 100%; scale them like FlatLaf's fonts
		PlatformDefaults.setHorizontalScaleFactor(jswScale.factor());
		PlatformDefaults.setVerticalScaleFactor(jswScale.factor());
	}

	public MyContactsLite(ContactStore store)
	{
		super("MyContacts");
		this.store = store;
		Main.closeHides(this);
		// FlatLaf draws this window's title bar, in the page colour, so the
		// buttons can sit in it
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		getRootPane().putClientProperty("JRootPane.titleBarBackground", PAGE);
		Main.roundCorners(this);
		setIcon();
		setJMenuBar(titleBar());
		content.add(listPage(), "list");
		content.add(cardPage(), "card");
		setContentPane(content);
		setAlwaysOnTop(true);
		setSize(jswScale.windowSize(380, 720));
		placeTopRight();

		load();
		// show contacts saved in the full window straight away; a save can be
		// several commits, so wait for them to finish before reloading
		Timer reload = new Timer(300, e -> { if (store.refresh()) reshow(); });
		reload.setRepeats(false);
		mcDataObject.addContactsListener(() -> SwingUtilities.invokeLater(reload::restart));
		bindKey(content, "ESCAPE", this::escape);
	}

	// ---- the list page: heading, search, contacts ----

	private JPanel listPage()
	{
		// the bottom inset keeps the list panel clear of the window's edge
		JPanel page = new JPanel(new MigLayout("insets 4 14 12 14, wrap, fill", "[grow, fill]", "[][]6[]4[grow, fill]"));
		page.setBackground(PAGE);

		JLabel heading = new JLabel("Contacts");
		heading.putClientProperty("FlatLaf.styleClass", "h1");
		page.add(heading, "split 2");
		// for a new address book (just the author's entry, say): shown by filter()
		gettingStarted.setToolTipText("How to get started, on the MyContacts help pages");
		gettingStarted.putClientProperty("JButton.buttonType", "toolBarButton");
		gettingStarted.setFocusable(false);
		gettingStarted.addActionListener(e -> help(GETTINGSTARTED));
		gettingStarted.setVisible(false);
		page.add(gettingStarted, "gapleft push, aligny center, growx 0");

		search.putClientProperty("JTextField.placeholderText", "Search");
		search.putClientProperty("JTextField.leadingIcon", icon("search.svg", 14));
		search.putClientProperty("JTextField.showClearButton", true);
		search.putClientProperty("JComponent.roundRect", true);
		search.putClientProperty("FlatLaf.style", "background: " + SHADEHEX);
		search.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e) { filter(); }
			public void removeUpdate(DocumentEvent e) { filter(); }
			public void changedUpdate(DocumentEvent e) { filter(); }
		});
		bindKey(search, "DOWN", () -> move(1));
		bindKey(search, "UP", () -> move(-1));
		bindKey(search, "ENTER", this::openSelected);
		page.add(search, "h 30!");

		count.putClientProperty("FlatLaf.styleClass", "small");
		count.setForeground(MUTED);
		page.add(count, "gapleft 4");

		list.setCellRenderer(new ContactRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setOpaque(false);
		// the renderer draws the selection itself
		list.putClientProperty("FlatLaf.style",
				"selectionBackground: $List.background; selectionInactiveBackground: $List.background");
		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int i = list.locationToIndex(e.getPoint());
				if (i >= 0 && list.getCellBounds(i, i).contains(e.getPoint())) open(model.get(i));
			}
		});
		bindKey(list, "ENTER", this::openSelected);
		JScrollPane scroll = slimScroll(list);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		// the list in a rounded panel, outlined like the search box
		JPanel panel = new RoundedPanel(SHADE, UIManager.getColor("Component.borderColor"), 14);
		panel.setLayout(new MigLayout("insets 6 4 6 4, fill"));
		panel.add(scroll, "grow");
		page.add(panel);
		return page;
	}

	private void load()
	{
		try
		{
			store.refresh();
			filter();
		} catch (RuntimeException e)
		{
			count.setText("Cannot read the contacts: " + e.getMessage());
		}
	}

	private void filter()
	{
		List<ContactCard> found = store.search(search.getText());
		model.clear();
		model.addAll(found);
		if (!found.isEmpty()) list.setSelectedIndex(0);
		int all = store.all().size();
		gettingStarted.setVisible(all <= 2);
		count.setText(search.getText().isBlank() ? all + (all == 1 ? " contact" : " contacts")
				: found.isEmpty() ? "No matches" : found.size() + " of " + all);
	}

	private void move(int step)
	{
		if (model.isEmpty()) return;
		int i = Math.max(0, Math.min(model.size() - 1, list.getSelectedIndex() + step));
		list.setSelectedIndex(i);
		list.ensureIndexIsVisible(i);
	}

	private void openSelected()
	{
		ContactCard selected = list.getSelectedValue();
		if (selected != null) open(selected);
	}

	// ---- the card page: one contact ----

	private JPanel cardPage()
	{
		JPanel page = new JPanel(new BorderLayout());
		page.setBackground(PAGE);

		JButton back = new JButton("Contacts", icon("back.svg", 14));
		back.putClientProperty("JButton.buttonType", "toolBarButton");
		back.setForeground(Color.decode(ACCENT));
		back.addActionListener(e -> showList());
		JPanel top = new JPanel(new MigLayout("insets 4 6 0 6", "[]push"));
		top.setOpaque(false);
		top.add(back);
		page.add(top, BorderLayout.NORTH);

		JScrollPane scroll = slimScroll(tiles);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(PAGE);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		page.add(scroll, BorderLayout.CENTER);
		return page;
	}

	void open(ContactCard card)
	{
		showing = card;
		tiles.removeAll();
		Tile name = textTile(card.name(), "h3");
		BufferedImage photo = store.photo(card.id());
		if (photo != null) name.add(new JLabel(new PhotoIcon(photo, 72)), "cell 1 0, aligny top");
		tiles.add(name);
		// the details ticked for Lite in Tools > Other > Attribute Types
		for (ContactCard.Detail detail : card.details())
		{
			Tile tile = detailTile(card, detail);
			// the usual ones speak for themselves; others say what they are
			if (!UNLABELLED.contains(detail.key())) noted(tile, detail.label());
			tiles.add(tile);
		}

		JButton edit = new JButton("Edit in MyContacts");
		edit.putClientProperty("JButton.buttonType", "borderless");
		edit.putClientProperty("FlatLaf.styleClass", "small");
		edit.setForeground(Color.decode(ACCENT));
		edit.addActionListener(e -> MyContacts.open(card.id()));
		tiles.add(edit, "right, gaptop 6");

		tiles.revalidate();
		tiles.repaint();
		pages.show(content, "card");
		SwingUtilities.invokeLater(() -> tiles.scrollRectToVisible(new Rectangle(0, 0, 1, 1)));
	}

	// a letter, label or copy of the address, with the name above it; a
	// borrowed group address keeps the contact's own name
	private void useAddress(ContactCard card)
	{
		mcContact contact = mcContact.getContact(String.valueOf(card.id()));
		if (contact == null) return;
		String address = card.addressFrom() == null ? contact.makeBlockAddress("\n", true)
				: card.name() + "\n" + card.address();
		addressDialog.show(this, contact, address);
	}

	// the details the window always showed, which need no label
	private static final Set<String> UNLABELLED = Set.of("address", "phone", "mobile", "whatsapp", "email", "birthday");

	// a detail's tile, with the icon for its kind of data
	private Tile detailTile(ContactCard card, ContactCard.Detail detail)
	{
		String text = detail.text();
		Tile tile = textTile(text, null);
		if (detail.key().equals("address"))
		{
			// the envelope opens the full window's "Use this address" box
			tile.add(iconButton("envelope.svg", "Write a letter or print a label",
					() -> useAddress(card)), "cell 1 0, aligny top");
			if (card.addressFrom() != null) noted(tile, "from " + card.addressFrom());
		}
		else if (detail.key().equals("whatsapp"))
		{
			// the WhatsApp icon opens the chat, with the greeting if ticked last time
			PhoneNumber number = PhoneNumber.of(text);
			tile.add(iconButton("whatsapp.svg", "Open a WhatsApp chat",
					() -> openWeb(number.whatsApp(greetOnWhatsApp ? greeting(card) : null))),
					"cell 1 0, aligny center, hmax 12pt");
		}
		else if (detail.datatype().equals("phone") || detail.datatype().equals("cellphone"))
			// the telephone offers the number to copy, as dialled in the UK or
			// with its country code; for a contact with no WhatsApp number yet,
			// also a WhatsApp chat with it, or to save it as their WhatsApp number
			tile.add(iconButton("phone.svg", "Copy this number, or WhatsApp it",
					() -> usePhone(card, text)), "cell 1 0, aligny center, hmax 12pt"); // no taller than the text
		else if (detail.datatype().equals("email"))
			// the @ offers the address to copy, or an email to it in the mail program
			tile.add(iconButton("at.svg", "Copy this address, or write an email",
					() -> useEmail(text)), "cell 1 0, aligny center, hmax 12pt");
		return tile;
	}

	private void usePhone(ContactCard card, String stored)
	{
		PhoneNumber number = PhoneNumber.of(stored);
		String text = "Copy this number\n\nUK:    " + number.uk()
				+ "\nInt:    " + number.international() + "\n ";
		// a contact with a WhatsApp number has its own tile for that
		if (card.whatsapp() != null)
		{
			String[] options = { "Copy UK", "Copy Int", "Cancel" };
			int n = JOptionPane.showOptionDialog(this, text, "Phone",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (n == 0) copy(number.uk());
			else if (n == 1) copy(number.international());
			return;
		}
		String[] options = { "Copy UK", "Copy Int", "WhatsApp", "Add WhatsApp", "Cancel" };
		// unticked, WhatsApp opens the chat empty, as for a call
		String greeting = greeting(card);
		JCheckBox withGreeting = new JCheckBox("WhatsApp with \"" + greeting.trim() + "\"", greetOnWhatsApp);
		int n = JOptionPane.showOptionDialog(this, new Object[] { text, withGreeting }, "Phone",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		greetOnWhatsApp = withGreeting.isSelected();
		if (n == 0) copy(number.uk());
		else if (n == 1) copy(number.international());
		else if (n == 2) openWeb(number.whatsApp(greetOnWhatsApp ? greeting : null));
		else if (n == 3) addWhatsApp(card, number);
	}

	private void addWhatsApp(ContactCard card, PhoneNumber number)
	{
		if (!store.setWhatsApp(card.id(), number.international()))
			JOptionPane.showMessageDialog(this, "Could not save the WhatsApp number.", "WhatsApp",
					JOptionPane.WARNING_MESSAGE);
	}

	// the tick box's last setting, while the program runs
	private static boolean greetOnWhatsApp = false;

	// "Hi Irene, ", or "Hi, " when the first name is not known
	private static String greeting(ContactCard card)
	{
		mcContact contact = mcContact.getContact(String.valueOf(card.id()));
		mcAttribute name = contact == null ? null : contact.getAttribute("name", "");
		String first = name == null ? null : name.getFormattedValue("fn");
		return first == null || first.isBlank() ? "Hi, " : "Hi " + first.trim().replaceAll("\\s+", " ") + ", ";
	}

	private static void copy(String text)
	{
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
	}

	private static JButton iconButton(String icon, String tip, Runnable action)
	{
		JButton button = new JButton(icon(icon, 16));
		button.putClientProperty("JButton.buttonType", "toolBarButton");
		button.setToolTipText(tip);
		button.setFocusable(false);
		button.addActionListener(e -> action.run());
		return button;
	}

	private void showList()
	{
		showing = null;
		pages.show(content, "list");
		search.requestFocusInWindow();
	}

	// after the database changed: refresh the list and the open card
	private void reshow()
	{
		filter();
		if (showing != null)
			store.find(showing.id()).ifPresentOrElse(this::open, this::showList);
	}

	private void escape()
	{
		if (showing != null) showList();
		else search.setText("");
	}

	// ---- tiles: rounded panels of selectable text ----

	private static Tile textTile(String text, String styleClass)
	{
		JTextArea area = new JTextArea(text);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setOpaque(false);
		area.setBorder(BorderFactory.createEmptyBorder());
		if (styleClass != null) area.putClientProperty("FlatLaf.styleClass", styleClass);
		return new Tile(area);
	}

	// a small muted note under a tile's text, outside the selectable text
	private static Tile noted(Tile tile, String note)
	{
		JLabel label = new JLabel(note);
		label.putClientProperty("FlatLaf.styleClass", "small");
		label.setForeground(MUTED);
		tile.add(label, "newline, gaptop 4");
		return tile;
	}

	private void useEmail(String email)
	{
		String[] options = { "Copy", "Write email", "Cancel" };
		int n = JOptionPane.showOptionDialog(this, email + "\n ", "Email",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (n == 0) copy(email);
		else if (n == 1) openMail(email);
	}

	static class Tile extends RoundedPanel
	{
		Tile(JComponent text)
		{
			super(SHADE, null, 16);
			setLayout(new MigLayout("insets 10 14 10 14, fill", "[grow, fill]"));
			add(text, "wmin 0");
		}
	}

	// a photo drawn size px high (as wide as its shape needs), with rounded corners
	static class PhotoIcon implements Icon
	{
		private final BufferedImage photo;
		private final int width, height;

		PhotoIcon(BufferedImage photo, int size)
		{
			this.photo = photo;
			height = jswScale.px(size);
			width = Math.max(1, Math.round(height * (float) photo.getWidth() / photo.getHeight()));
		}

		public int getIconWidth() { return width; }
		public int getIconHeight() { return height; }

		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.clip(new RoundRectangle2D.Float(x, y, width, height, jswScale.px(10), jswScale.px(10)));
			g2.drawImage(photo, x, y, width, height, null);
			g2.dispose();
		}
	}

	// a panel with a rounded background, and optionally a 1px outline
	static class RoundedPanel extends JPanel
	{
		private final Color fill, outline;
		private final int arc;

		RoundedPanel(Color fill, Color outline, int arc)
		{
			this.fill = fill;
			this.outline = outline;
			this.arc = jswScale.px(arc);
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(fill);
			g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
			if (outline != null)
			{
				g2.setColor(outline);
				g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, arc, arc));
			}
			g2.dispose();
		}
	}

	// the column of tiles: as wide as the window, scrolling vertically
	static class TilePanel extends JPanel implements Scrollable
	{
		TilePanel()
		{
			super(new MigLayout("wrap, insets 6 14 14 14, gapy 8", "[grow, fill]"));
			setBackground(PAGE);
		}

		public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
		public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
		public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.height; }
		public boolean getScrollableTracksViewportWidth() { return true; }
		public boolean getScrollableTracksViewportHeight() { return false; }
	}

	// ---- a list row: the contact's name on one line ----

	static class ContactRenderer extends JPanel implements ListCellRenderer<ContactCard>
	{
		private final JLabel name = new JLabel();
		private boolean selected;

		ContactRenderer()
		{
			super(new MigLayout("insets 3 10 3 10", "[grow]"));
			setOpaque(false);
			add(name);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends ContactCard> list, ContactCard card,
				int index, boolean selected, boolean focused)
		{
			name.setText(card.listName());
			name.setForeground(list.getForeground());
			this.selected = selected;
			return this;
		}

		// the selected row (the one Enter opens) gets a soft rounded highlight,
		// visible even while typing in the search box
		@Override
		protected void paintComponent(Graphics g)
		{
			if (!selected) return;
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(HIGHLIGHT);
			g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), jswScale.px(12), jswScale.px(12)));
			g2.dispose();
		}
	}

	// a scroll pane with a thin rounded scrollbar and no arrow buttons
	private static JScrollPane slimScroll(JComponent view)
	{
		JScrollPane scroll = new JScrollPane(view);
		scroll.getVerticalScrollBar().putClientProperty("FlatLaf.style",
				"width: 8; thumbArc: 999; thumbInsets: 2,2,2,2; showButtons: false");
		return scroll;
	}

	// ---- title bar, window and actions ----

	private JMenuBar titleBar()
	{
		JMenuBar bar = new JMenuBar();
		bar.add(Box.createHorizontalGlue());
		JButton help = new JButton(icon("help.svg", 14));
		help.setToolTipText("Help: the MyContacts help pages");
		help.putClientProperty("JButton.buttonType", "toolBarButton");
		help.setFocusable(false);
		help.addActionListener(e -> help(null));
		bar.add(help);
		JButton add = new JButton(icon("plus.svg", 14));
		add.setToolTipText("Add a new contact");
		add.putClientProperty("JButton.buttonType", "toolBarButton");
		add.setFocusable(false);
		add.addActionListener(e -> MyContacts.newContact());
		bar.add(add);
		JButton full = new JButton(icon("window.svg", 14));
		full.setToolTipText("Open the full MyContacts window");
		full.putClientProperty("JButton.buttonType", "toolBarButton");
		full.setFocusable(false);
		full.addActionListener(e -> MyContacts.open(null));
		bar.add(full);
		JToggleButton pin = new JToggleButton(icon("pin.svg", 14), true);
		pin.setToolTipText("Keep on top of other windows");
		pin.putClientProperty("JButton.buttonType", "toolBarButton");
		pin.setFocusable(false);
		pin.addActionListener(e -> setAlwaysOnTop(pin.isSelected()));
		bar.add(pin);
		return bar;
	}

	private void placeTopRight()
	{
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setLocation(screen.x + screen.width - getWidth() - 24, screen.y + 48);
	}

	private void setIcon()
	{
		java.net.URL icon = MyContacts.class.getResource(MyContacts.ICON);
		if (icon != null) setIconImage(new ImageIcon(icon).getImage());
	}

	static Icon icon(String name, int size)
	{
		return new FlatSVGIcon("org/lerot/mycontacts/lite/icons/" + name, size, size);
	}

	private static void bindKey(JComponent c, String key, Runnable action)
	{
		c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key), key);
		c.getActionMap().put(key, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e) { action.run(); }
		});
	}

	// the help pages are the GitHub wiki, which anyone can read
	public static final String HELPPAGES = "https://github.com/paulagolder/MyContacts/wiki";
	public static final String GETTINGSTARTED = "Getting-started";

	// a help page (null for the front page) in the default browser
	public static void help(String page)
	{
		openWeb(URI.create(page == null ? HELPPAGES : HELPPAGES + "/" + page));
	}

	// in the default browser
	static void openWeb(URI uri)
	{
		new Thread(() ->
		{
			try
			{
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
					Desktop.getDesktop().browse(uri);
				else
					new ProcessBuilder("xdg-open", uri.toString()).start();
			} catch (Exception e)
			{
				System.err.println("Cannot open " + uri + ": " + e.getMessage());
			}
		}).start();
	}

	static void openMail(String email)
	{
		new Thread(() ->
		{
			try
			{
				URI uri = new URI("mailto", email, null);
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL))
					Desktop.getDesktop().mail(uri);
				else
					new ProcessBuilder("xdg-open", uri.toString()).start();
			} catch (Exception e)
			{
				System.err.println("Cannot open mail for " + email + ": " + e.getMessage());
			}
		}).start();
	}
}
