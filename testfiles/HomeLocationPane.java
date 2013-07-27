/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.ui;

import gruntspud.Constants;
import gruntspud.Gruntspud;
import gruntspud.GruntspudContext;
import gruntspud.StringListModel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.EventListener;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.event.EventListenerList;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

/**
 * Description of the Class
 *
 * @author magicthize
 */
public class HomeLocationPane
    extends JPanel {
    //  Private instanve variables
    private JComboBox homeLocation;
    private EventListenerList listenerList;
    private StringListModel model;
    private GruntspudContext context;
    private JComboBox filters;

    /**
     * Constructor for the HomeLocationPane object
     *
     * @param context  Description of the Parameter
     */
    public HomeLocationPane(final GruntspudContext context) {
        super();

        this.context = context;

        //
        listenerList = new EventListenerList();

        //
        homeLocation = new JComboBox(model = new StringListModel(
            context.getHost().getProperty(Constants.HOME_LIST,
                                          System.getProperty("user.dir")), false));
        homeLocation.setEditable(true);
        homeLocation.setLightWeightPopupEnabled(true);
//        try {
//            Method m = homeLocation.getClass().getMethod("setPrototypeDisplayValue", new Class[] { Object.class });
//            int max = 0;
//            for(int i = 0 ; i < model.getSize(); i++) {
//                max = Math.max(max, model.getElementAt(i).toString().length());
//            }
//            StringBuffer prototype = new StringBuffer();
//            for(int i = 0 ; i < max; i++) {
//                prototype.append('W');
//            }
//            m.invoke(homeLocation, new Object[] { prototype.toString() });
//        }
//        catch(Throwable t) {
//            
//        }
        final HomeLocationEditor editor = new HomeLocationEditor();
        homeLocation.setEditor(editor);
        editor.setBorder(null);
		homeLocation.setBorder(null);
        homeLocation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if( ( evt.getActionCommand().equals("comboBoxChanged") && evt.getModifiers() != 0 ) ||
                    evt.getActionCommand().equals("comboBoxEdited")) {
                    addSelected();
                    changeToSelectedHome();
                }
            }
        });
        homeLocation.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                File f = getSelectedFile(context.getHost().isUseCanonicalPaths());
                if (!f.equals(context.getViewManager().getHome())) {
                    setSelectedFile(f);
//                        changeToSelectedHome();
                }
            }
        });
        if(Gruntspud.is14Plus()) {
            homeLocation.addPopupMenuListener(new PopupMenuListener() {
                public void popupMenuCanceled(PopupMenuEvent pme) {
                    editor.setAutoComplete(true);
                }

                public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
                    editor.setAutoComplete(true);
                }

                public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
                    editor.setAutoComplete(false);
                }

            });
        }
        homeLocation.setUI(new LargerComboBoxUI());
        init();
    }

    /**
     * DOCUMENT ME!
     */
    public void init() {
        invalidate();
        removeAll();
        setLayout(new GridBagLayout());

        if (context.getHost().getBooleanProperty(Constants.
            OPTIONS_DISPLAY_SHOW_FULL_PATH_FOR_HOME_LOCATION,
            true)) {
            homeLocation.setRenderer(new DefaultListCellRenderer());
        }
        else {
            homeLocation.setRenderer(new TooltipComboBoxRenderer());

        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 1);
        gbc.weightx = 1.0;

        if (context.getHost().getBooleanProperty(Constants.
            OPTIONS_DISPLAY_SHOW_FILTERS_ON_TOOL_BAR,
            false)) {
            UIUtil.jGridBagAdd(this, homeLocation, gbc, 1);
            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JSeparator(JSeparator.VERTICAL), gbc,
                               GridBagConstraints.RELATIVE);
            UIUtil.jGridBagAdd(this,
                               filters = new JComboBox(context.getFilterModel()),
                               gbc,
                               GridBagConstraints.REMAINDER);
            filters.setMaximumRowCount(15);
        }
        else {
            UIUtil.jGridBagAdd(this, homeLocation, gbc,
                               GridBagConstraints.REMAINDER);

        }
        validate();
        repaint();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        homeLocation.setEnabled(enabled);
        if(filters != null)
            filters.setEnabled(enabled);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getHomeCount() {
        return homeLocation.getModel().getSize();
    }

    /**
     * DOCUMENT ME!
     */
    public void addSelected() {
        File f = getSelectedFile(context.getHost().isUseCanonicalPaths());
        addFile(f, false);
    }

    /**
     * DOCUMENT ME!
     */
    public void cleanUp() {
        context.getHost().setProperty(Constants.HOME_LIST,
                                      model.getListAsPropertyString());
    }

    private void changeToSelectedHome() {
        String s = homeLocation.getEditor().getItem().toString();

        if (model.indexOfString(s) == -1) {
            model.addString(s);
            model.setSelectedItem(s);
        }

        fireHomeChanged();
    }

    /**
     * Sets the selectedFile attribute of the HomeLocationPane object
     *
     * @param f  The new selectedFile value
     */
    public void setSelectedFile(File f) {
        homeLocation.getEditor().setItem(f);
    }

    /**
     * Description of the Method
     */
    public void chooseHomeLocation() {
        String loc = null;
        JFileChooser chooser = new JFileChooser( (model.getSelectedItem() == null)
                                                ?
                                                System.getProperty("user.home")
                                                :
                                                model.getSelectedItem().
                                                toString());
        chooser.setDialogTitle("Choose home location");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonText("Select");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            addFile(chooser.getSelectedFile(), false);
            fireHomeChanged();
        }
    }

    /**
     * Gets the selectedFile attribute of the HomeLocationPane object
     *
     * @return The selectedFile value
     */
    public File getSelectedFile() {
        return getSelectedFile(false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param f DOCUMENT ME!
     * @param noSelect DOCUMENT ME!
     */
    public void addFile(File f, boolean noSelect) {
        String s = f.getAbsolutePath();
        model.addString(s, noSelect);

        if (!noSelect) {
            model.setSelectedItem(s);
        }
    }

    /**
     * Gets the selectedFile attribute of the HomeLocationPane object
     *
     * @param canonical  Description of the Parameter
     *
     * @return The selectedFile value
     */
    public File getSelectedFile(boolean canonical) {
        String s = model.getSelectedItem().toString();
        File f = null;

        if (s != null) {
            if (canonical) {
                try {
                    f = new File(s).getCanonicalFile();
                }
                catch (IOException ioe) {
                }
            }
            else {
                f = new File(s);
            }
        }

        return f;
    }

    /**
     * DOCUMENT ME!
     */
    public void removeCurrentLocation() {
        model.removeString(model.getSelectedItem().toString());
    }

    /**
     * Description of the Method
     */
    private void fireHomeChanged() {
        EventListener[] l = listenerList.getListeners(ActionListener.class);

        for (int i = l.length - 1; i >= 0; i--) {
            ( (ActionListener) l[i]).actionPerformed(new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED,
                model.getSelectedItem().toString()));
        }
    }

    /**
     * Adds a feature to the ActionListener attribute of the HomeLocationPane
     * object
     *
     * @param l  The feature to be added to the ActionListener attribute
     */
    public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }

    /**
     * Description of the Method
     *
     * @param l  Description of the Parameter
     */
    public void removeActionListener(ActionListener l) {
        listenerList.remove(ActionListener.class, l);
    }
    
    public class LargerComboBoxUI extends MetalComboBoxUI{
        protected ComboPopup createPopup() {
            return new LargerComboPopup(comboBox);
        }
        public class LargerComboPopup extends BasicComboPopup{
             public LargerComboPopup(JComboBox comboBox) {
                 super(comboBox);
             }
             public void show() {
                int selectedIndex = comboBox.getSelectedIndex();
                if(selectedIndex == -1){
                   list.clearSelection();
                }
                else{
                   list.setSelectedIndex(selectedIndex);
                   list.ensureIndexIsVisible(selectedIndex);
                }

                Insets insets = getInsets();
                Dimension listDim = list.getPreferredSize();
                boolean hasScrollBar = scroller.getViewport().getViewSize().height != listDim.height;
                if(hasScrollBar){
                   JScrollBar scrollBar = scroller.getVerticalScrollBar();
                   listDim.width += scrollBar.getPreferredSize().getWidth();
                }

                int width = Math.max(listDim.width,comboBox.getWidth() - (insets.right + insets.left));
                int height = getPopupHeightForRowCount( comboBox.getMaximumRowCount());
                Rectangle popupBounds = computePopupBounds(0,comboBox.getHeight(),width,height);

                Dimension scrollSize = popupBounds.getSize();
                scroller.setMaximumSize( scrollSize );
                scroller.setPreferredSize( scrollSize );
                scroller.setMinimumSize( scrollSize );

                list.revalidate();
                show(comboBox, popupBounds.x, popupBounds.y);
             }
        }
     }



    //  Supporting classes
    class TooltipComboBoxRenderer
        extends DefaultListCellRenderer {
        /**
         * DOCUMENT ME!
         *
         * @param list DOCUMENT ME!
         * @param value DOCUMENT ME!
         * @param index DOCUMENT ME!
         * @param isSelected DOCUMENT ME!
         * @param cellHasFocus DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                                               cellHasFocus);

            String val = String.valueOf(value);
            list.setToolTipText(val);

            int idx = val.lastIndexOf(File.separator);

            if (idx != -1) {
                setText(".." + val.substring(idx));

            }
            return this;
        }
    }

    class HomeLocationEditor
        extends FileNameTextField
        implements ComboBoxEditor {

        public HomeLocationEditor() {
            super(null, null, 0, Gruntspud.is14Plus(), false, null, false);
            
            Constants.UI_LOG.debug("Using filename completion for home location " + isAutoComplete());
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Component getEditorComponent() {
            return this;
        }

        /**
         * DOCUMENT ME!
         *
         * @param o DOCUMENT ME!
         */
        public void setItem(Object o) {
            setText(String.valueOf(o));
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Object getItem() {
            return getText();
        }
    }
}
