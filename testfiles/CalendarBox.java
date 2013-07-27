package org.columba.calendar.ui.box;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.columba.calendar.model.api.IEventInfo;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.StoreException;
import org.columba.calendar.ui.dialog.EditEventDialog;
import org.columba.core.gui.base.DoubleClickListener;
import org.columba.core.gui.base.IconTextField;
import org.columba.core.gui.frame.api.IComponentBox;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class CalendarBox extends JPanel implements IComponentBox {

	private final static ImageIcon icon = ImageLoader
			.getSmallIcon(IconKeys.EDIT_FIND);

	private IconTextField textField;

	private JLabel label;

	private CalendarList list;

	private JPopupMenu contextMenu;

	public CalendarBox() {

		setLayout(new BorderLayout());

		label = new JLabel("Quick Find:");
		label.setDisplayedMnemonic('F');

		textField = new IconTextField(icon, 10);
		label.setLabelFor(textField);

		list = new CalendarList();
		list.setModel(new FilteringModel());
		List<IEventInfo> eventList = populateListModel("work");
		list.addAll(eventList);

		list.installJTextField(textField);

		CalendarMenu popup = new CalendarMenu(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String calendarId = event.getActionCommand();

				List<IEventInfo> eventList = populateListModel(calendarId);
				list.setModel(new FilteringModel());
				list.addAll(eventList);
				list.installJTextField(textField);
				textField.setText(textField.getText());
			}
		});
		textField.setPopupMenu(popup);

		list.addMouseListener(new DoubleClickListener() {

			@Override
			public void doubleClick(MouseEvent event) {
				IEventInfo selected = (IEventInfo) list.getSelectedValue();
				openEditCalendarEventDialog(selected);
			}
		});


		list.add(getPopupMenu());
		list.addMouseListener(new MyMouseListener());

		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		FormLayout layout = new FormLayout("pref, 2dlu, fill:default:grow",
		// 2 columns
				"fill:default:grow");

		// create a form builder
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, p);
		builder.append(label);
		builder.append(textField);
		add(p, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(list);
		add(scrollPane, BorderLayout.CENTER);
	}

	private JPopupMenu getPopupMenu() {
		if ( contextMenu != null) return contextMenu;

		contextMenu = new JPopupMenu();

		JMenuItem item = new JMenuItem("Open..");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				IEventInfo selected = (IEventInfo) list.getSelectedValue();
				openEditCalendarEventDialog(selected);
			}
		});

		contextMenu.add(item);
		return contextMenu;
	}

	private List<IEventInfo> populateListModel(String calendarId) {
//		ICalendarStore store = CalendarStoreFactory.getInstance()
//				.getLocaleStore();

//		IComponentInfoList infoList = store.getComponentInfoList(calendarId);
		List<IEventInfo> eventList = new ArrayList<IEventInfo>();
/*		Iterator<IComponentInfo> it = infoList.iterator();
		while (it.hasNext()) {
			IComponentInfo info = it.next();
			if (info.getType().equals(IComponent.TYPE.EVENT)) {
				eventList.add((IEventInfo) info);
			}
		}*/
		return eventList;
	}

	public String getDescription() {
		return "Calendar";
	}

	public ImageIcon getIcon() {
		return ImageLoader.getSmallIcon(IconKeys.CALENDAR);
	}

	public String getTechnicalName() {
		return "calendar_box";
	}

	public String getName() {
		return "Calendar";
	}

	public JComponent getView() {
		return this;
	}

	private void openEditCalendarEventDialog(IEventInfo selected) {

		ICalendarStore store = org.columba.calendar.config.CalendarList.getInstance().get(
				selected.getCalendar()).getStore();

		if (store == null)
			return;

		// retrieve event from store
		try {
			IEventInfo model = (IEventInfo) store.get(selected.getId());

			EditEventDialog dialog = new EditEventDialog(null, model, store.isReadOnly(selected.getId()));
			if (dialog.success()) {
				IEventInfo updatedModel = dialog.getModel();

				// update store
				store.modify(selected.getId(), updatedModel);
			}

		} catch (StoreException e1) {
			JOptionPane.showMessageDialog(this, e1.getMessage());
			e1.printStackTrace();
		}
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
				// check if a single entry is selected
				if ( list.getSelectedIndices().length <= 1 ) {
					// select new item
					int index = list.locationToIndex(p);
					list.setSelectedIndex(index);
				}
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
