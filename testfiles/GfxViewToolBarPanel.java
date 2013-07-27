/* 
 * Parts of this code are inspired and/or copied from QuickNotePadToolPanel / Slava Pestov
 */

//{{{ imports
import java.beans.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.textarea.*;
 //}}}

public class GfxViewToolBarPanel extends JPanel implements PropertyChangeListener {
	private JLabel labelZoom;
	private JLabel labelTitle;
	private PropertyChangeSupport changes;
	private int oldZoomButton;
	
	//{{{ +GfxViewToolBarPanel() : <init>
	public GfxViewToolBarPanel() {
		changes = new PropertyChangeSupport(this);

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.putClientProperty("JToolBar.isRollover",Boolean.TRUE);

		toolBar.add(makeCustomButton("gfxview.zoom",
			new ActionListener() {
				//{{{ +actionPerformed(ActionEvent) : void
				public void actionPerformed(ActionEvent evt) {
					int newZoomButton = GfxViewImagePanel.ZOOM;
					int oldZoomButton = GfxViewImagePanel.UNZOOM;
					changes.firePropertyChange("zoomValueButton",new java.lang.Integer(oldZoomButton),new java.lang.Integer(newZoomButton));
				} //}}}
			}));
		toolBar.add(makeCustomButton("gfxview.unzoom",
			new ActionListener() {
				//{{{ +actionPerformed(ActionEvent) : void
				public void actionPerformed(ActionEvent evt) {
					int newZoomButton = GfxViewImagePanel.UNZOOM;
					int oldZoomButton = GfxViewImagePanel.ZOOM;
					changes.firePropertyChange("zoomValueButton",new java.lang.Integer(oldZoomButton),new java.lang.Integer(newZoomButton));
				} //}}}
			}));
		toolBar.add(makeCustomButton("gfxview.unzoom-all",
			new ActionListener() {
				//{{{ +actionPerformed(ActionEvent) : void
				public void actionPerformed(ActionEvent evt) {
					int newZoomButton = GfxViewImagePanel.UNZOOM_FULL;
					int oldZoomButton = GfxViewImagePanel.ZOOM;
					changes.firePropertyChange("zoomValueButton",new java.lang.Integer(oldZoomButton),new java.lang.Integer(newZoomButton));
				} //}}}
			}));
		labelZoom = new JLabel();
		labelTitle = new JLabel();
		this.setLayout(new BorderLayout(10, 10));
		this.add(BorderLayout.WEST, toolBar);
		this.add(BorderLayout.CENTER, labelTitle);
		this.add(BorderLayout.EAST, labelZoom);
//		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 10));
		this.setBorder(LineBorder.createBlackLineBorder());
	} //}}}

	//{{{ +propertyChange(PropertyChangeEvent) : void
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().compareTo("zoomValue-label")==0) {
			labelZoom.setText(evt.getNewValue().toString()+"%");
		}
		if (evt.getPropertyName().compareTo("UrlGfxView-display")==0) {
			labelTitle.setText(evt.getNewValue().toString());
		}
	} //}}}

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

	//{{{ -makeCustomButton(String, ActionListener) : AbstractButton
	private AbstractButton makeCustomButton(String name, ActionListener listener) {
		String toolTip = jEdit.getProperty(name.concat(".label"));
		AbstractButton b = new JButton(GUIUtilities.loadIcon(
			jEdit.getProperty(name + ".icon")));
		if(listener != null) {
			b.addActionListener(listener);
			b.setEnabled(true);
		}
		else {
			b.setEnabled(false);
		}
		b.setToolTipText(toolTip);
		b.setMargin(new Insets(0,0,0,0));
		b.setAlignmentY(0.0f);
		b.setRequestFocusEnabled(false);
		return b;
	} //}}}
}

/* :folding=explicit:tabSize=2:indentSize=2: */