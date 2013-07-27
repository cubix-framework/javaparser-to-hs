package org.columba.addressbook.gui.context;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.columba.addressbook.facade.DialogFacade;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.PhoneModel;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.contact.search.ContactSearchResult;
import org.columba.core.facade.ServiceFacadeRegistry;
import org.columba.core.gui.base.RoundedBorder;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.facade.IDialogFacade;
import org.jdesktop.swingx.JXHyperlink;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ContactDetailPanel extends JPanel {

	private static final String NOT_AVAIL = "n/a";

	private JLabel pictureLabel;

	private JLabel label1;

	private JXHyperlink label2;

	private JXHyperlink label5;

	private JLabel label9;

	private JLabel label10;

	private JLabel label12;

	private JLabel label13;

	private JLabel label4;

	private JLabel label11;

	private JLabel label7;

	private JLabel label8;

	private JPopupMenu contextMenu;

	private final IContactModel model;

	private final ContactSearchResult searchResult;

	public ContactDetailPanel(final IContactModel model,
			final ContactSearchResult searchResult) {
		this.model = model;
		this.searchResult = searchResult;

		setBackground(UIManager.getColor("TextField.background"));

		Border b = BorderFactory.createCompoundBorder(new RoundedBorder(
				new Color(220, 220, 220)), BorderFactory.createEmptyBorder(4,
				4, 4, 4));
		Border b2 = BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(4, 4, 4, 4), b);
		setBorder(b2);

		initComponents();

		pictureLabel.setIcon(ImageLoader.getMiscIcon("malehead.png"));
		pictureLabel.setHorizontalAlignment(JLabel.CENTER);
		pictureLabel.setBorder(BorderFactory.createCompoundBorder(
				new RoundedBorder(new Color(220, 220, 220)), BorderFactory
						.createEmptyBorder(4, 4, 4, 4)));

		if (model.getFormattedName() != null)
			label1.setText(model.getFormattedName());
		else
			label1.setText(NOT_AVAIL);

		label1.setFont(label1.getFont().deriveFont(Font.BOLD));
		if (model.getPreferredEmail() != null) {
			label2.setText(model.getPreferredEmail());
			label2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					try {
						IDialogFacade facade = (IDialogFacade) ServiceFacadeRegistry
								.getInstance()
								.getService(
										org.columba.mail.facade.IDialogFacade.class);
						facade.openComposer(model.getPreferredEmail());
					} catch (ServiceNotFoundException e) {
						e.printStackTrace();
					}
				}
			});
		} else
			label2.setText(NOT_AVAIL);
		if (model.getHomePage() != null)
			label5.setText(model.getHomePage());
		else
			label5.setText(NOT_AVAIL);

		label9.setText("Birthday:");
		if (model.getBirthday() != null)
			label10.setText(model.getBirthday().toLocaleString());
		else
			label10.setText(NOT_AVAIL);

		label12.setText("Phone Home:");
		label4.setText("Phone Work:");
		label11.setText(NOT_AVAIL);
		label13.setText(NOT_AVAIL);
		Iterator it = model.getPhoneIterator();
		while (it.hasNext()) {
			PhoneModel phoneModel = (PhoneModel) it.next();
			if (phoneModel.getType() == PhoneModel.TYPE_HOME_PHONE)
				label13.setText(phoneModel.getNumber());
			if (phoneModel.getType() == PhoneModel.TYPE_BUSINESS_PHONE)
				label11.setText(phoneModel.getNumber());
		}
		if (model.getPreferredInstantMessaging() != null)
			label7.setText(model.getPreferredInstantMessaging() + ":");
		else
			label7.setText("No IM available");

		// TODO: real IM status here
		label8.setText(NOT_AVAIL);

		addMouseListener(new MyMouseListener());
	}

	private void initComponents() {
		pictureLabel = new JLabel();
		label1 = new JLabel();
		label2 = new JXHyperlink();
		label5 = new JXHyperlink();
		label9 = new JLabel();
		label10 = new JLabel();
		label12 = new JLabel();
		label13 = new JLabel();
		label4 = new JLabel();
		label11 = new JLabel();
		label7 = new JLabel();
		label8 = new JLabel();

		CellConstraints cc = new CellConstraints();

		// ======== this ========
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC }, new RowSpec[] {
				FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC }));
		add(pictureLabel, cc.xywh(1, 1, 1, 7, CellConstraints.FILL,
				CellConstraints.FILL));

		add(label1, cc.xy(3, 1));

		add(label2, cc.xy(3, 3));

		add(label5, cc.xy(3, 5));

		add(label9, cc.xywh(1, 9, 1, 1, CellConstraints.RIGHT,
				CellConstraints.DEFAULT));

		add(label10, cc.xywh(3, 9, 1, 1, CellConstraints.FILL,
				CellConstraints.DEFAULT));

		add(label12, cc.xywh(1, 11, 1, 1, CellConstraints.RIGHT,
				CellConstraints.DEFAULT));

		add(label13, cc.xywh(3, 11, 1, 1, CellConstraints.FILL,
				CellConstraints.DEFAULT));

		add(label4, cc.xywh(1, 13, 1, 1, CellConstraints.RIGHT,
				CellConstraints.DEFAULT));

		add(label11, cc.xywh(3, 13, 1, 1, CellConstraints.FILL,
				CellConstraints.DEFAULT));

		add(label7, cc.xywh(1, 15, 1, 1, CellConstraints.RIGHT,
				CellConstraints.DEFAULT));

		add(label8, cc.xywh(3, 15, 1, 1, CellConstraints.FILL,
				CellConstraints.DEFAULT));
	}

	private JPopupMenu getPopupMenu() {
		if (contextMenu != null)
			return contextMenu;

		contextMenu = new JPopupMenu();

		JMenuItem item = new JMenuItem("Open..");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new DialogFacade()
						.openContactDialog(searchResult.getLocation());
			}
		});
		contextMenu.add(item);

		item = new JMenuItem("Compose Message..");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				String address = model.getPreferredEmail();

				try {
					IDialogFacade facade = (IDialogFacade) ServiceFacadeRegistry
							.getInstance()
							.getService(
									org.columba.mail.facade.IDialogFacade.class);
					facade.openComposer(address);
				} catch (ServiceNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		contextMenu.add(item);
		return contextMenu;
	}

	class MyMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			handleEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			handlePopupEvent(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			handlePopupEvent(e);
		}

		/**
		 * @param e
		 */
		private void handlePopupEvent(MouseEvent e) {
			Point p = e.getPoint();
			if (e.isPopupTrigger()) {
				// show context menu
				getPopupMenu().show(e.getComponent(), p.x, p.y);
			}
		}

		/**
		 * @param e
		 */
		private void handleEvent(MouseEvent e) {
		}
	}

}
