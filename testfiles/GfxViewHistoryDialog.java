//{{{ imports
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.net.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.browser.*;
import org.gjt.sp.util.*;
//}}}

public class GfxViewHistoryDialog extends JDialog
		implements ListSelectionListener,ActionListener {
	private JButton buttonFile_local,buttonFile_remote;
	private PropertyChangeSupport changes;
	private Object oldURL;
	private String oldURL_display;
	private GfxViewListModel model;
	private JList list;
	private JButton butOk,butCancel;
	private int previousIndex;
	private View view;

	//{{{ +GfxViewHistoryDialog(View) : <init>
	public GfxViewHistoryDialog(View view) {
		super(view,"Choose picture from GfxView history",true);
		this.view = view;

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		changes = new PropertyChangeSupport(this);
		model = new GfxViewListModel();

		list = new JList(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.addListSelectionListener(this);
		JScrollPane pane = new JScrollPane(list);
		getContentPane().add(pane, BorderLayout.CENTER);

		JPanel panel = new JPanel(new FlowLayout());
		butOk = new JButton("Pick this entry");
		butOk.addActionListener(this);
		butCancel = new JButton("Cancel");
		butCancel.addActionListener(this);
		panel.add(butOk);
		panel.add(butCancel);
		getContentPane().add(panel, BorderLayout.SOUTH);

		pack();
	} //}}}

	//{{{ +show() : void
	public void show() {
		previousIndex=list.getSelectedIndex(); // -1 also
		setLocation((view.getWidth()-this.getWidth())/2,
			(view.getHeight()-this.getHeight())/2);
		super.show();
	}//}}}

	//{{{ +actionPerformed(ActionEvent) : void
	public void actionPerformed(ActionEvent evt)	{
		if (evt.getSource()==butCancel) {
			selectValue(previousIndex);
		}
		dispose(); // also for butOk
	}//}}}

	//{{{ +valueChanged(ListSelectionEvent) : void
	public void valueChanged(ListSelectionEvent evt) {
		if (!evt.getValueIsAdjusting()) {
				selectValue(list.getSelectedIndex());
		}
	} //}}}


	//{{{ -selectValue(int) : void
	private void selectValue(int index) {
		String newURL_display;
		Object newURL;

		if (index==-1) {
			list.getSelectionModel().clearSelection();
			newURL = null;
			newURL_display = "";
		}
		else {
			newURL = model.getElementAt(index);
			newURL_display = buildURL_name(newURL);
		}
		changes.firePropertyChange("UrlGfxView-path",oldURL,newURL);
		oldURL = newURL;
		changes.firePropertyChange("UrlGfxView-display",oldURL_display,newURL_display);
		oldURL_display = newURL_display;
	}//}}}

	//{{{ -buildURL_name(String) : String
	private String buildURL_name(Object newURL) {
			String  newURL_display = newURL.toString();
			boolean result = newURL instanceof URL;
			newURL_display = (result ?
				newURL_display.substring(newURL.toString().lastIndexOf('/')+1) :
				newURL_display.substring(newURL.toString().lastIndexOf(File.separator)+1));

			return newURL_display;
	}//}}}


	//{{{ +loadNext() : void
	public void loadNext() {
		int size=list.getModel().getSize();
		int index = list.getSelectedIndex();
		if (size > 0 && index < (size-1)) { //  index=-1 is also matched !
			list.setSelectedIndex(index+1);
		}
	}//}}}

	//{{{ +loadPrev() : void
	public void loadPrev() {
		int index = list.getSelectedIndex();
		if (index > 0) {
			list.setSelectedIndex(index-1);
		}
	}//}}}

	//{{{ +addEntry(Object) : void
	public void addEntry(Object urlPath) {
		if (urlPath!=null) {
			model.addEntry(urlPath);
			changes.firePropertyChange("UrlGfxView-display",oldURL,buildURL_name(urlPath));
		}
	}//}}}

	//{{{ +removeEntry(Object) : void
	public void removeEntry(Object urlPath) {
		model.removeEntry(urlPath);
	}//}}}


	//{{{ +addPropertyChangeListener(PropertyChangeListener) : void
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
			changes.addPropertyChangeListener(listener);
	} //}}}

	//{{{ +removePropertyChangeListener(PropertyChangeListener) : void
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
			changes.removePropertyChangeListener(listener);
	} //}}}
}

/* :folding=explicit:tabSize=2:indentSize=2: */