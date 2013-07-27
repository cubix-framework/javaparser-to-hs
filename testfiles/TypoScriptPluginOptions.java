/***************************************************************
*  Copyright notice
*
*  (c) 2005,2006 Neil Bertram (neil@tasmanstudios.co.nz)
*  All rights reserved
*
*  This plugin is part of the Typo3 project. The Typo3 project is
*  free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*
*  The GNU General Public License can be found at
*  http://www.gnu.org/copyleft/gpl.html.
*  A copy is found in the textfile GPL.txt
*
*
*  This plugin is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  This copyright notice MUST APPEAR in all copies of the source!
***************************************************************/
/**
 * $Id: TypoScriptPluginOptions.java 10411 2006-01-29 03:28:55Z n31l $
 * 
 * This file contains the entire options dialog source for the 
 * "Plugin Options" section. It allows adding, editing and removing
 * of TYPO3 sites, as well as verification of credentials etc.
 *
 * @author      Neil Bertram <neil@tasmanstudios.co.nz>
 */

package typoscript;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.xmlrpc.XmlRpcException;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.RolloverButton;

/**
 * The options dialog in the Plugin Option panel
 */
public class TypoScriptPluginOptions extends AbstractOptionPane {
	private JList siteList;
	private JButton add;
	private JButton edit;
	private JButton remove;
	
	protected Vector localSitesConfig;
	protected DefaultListModel listModel;
	
	public TypoScriptPluginOptions() {
		super(TypoScriptPlugin.NAME);	
	}
	
	protected void _init() {
		// Take a copy of the current sites config
		localSitesConfig = (Vector)TypoScriptPlugin.siteConfig.clone();

		listModel = new DefaultListModel();
		Iterator iter = localSitesConfig.iterator();
		while (iter.hasNext()) {
			listModel.addElement(iter.next());
		}

		setLayout(new BorderLayout());

		add(BorderLayout.NORTH, new JLabel("TYPO3 Sites"));

		JPanel sites = new JPanel(new BorderLayout());
		siteList = new JList(listModel);
		siteList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		siteList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateButtons();
			}
		});

		siteList.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				// Edit if doubleclick
				if (e.getClickCount() == 2) {
					new AddEditSiteDialog(TypoScriptPluginOptions.this, (T3Site)localSitesConfig.get(siteList.getSelectedIndex()));
				}
			}
			public void mouseEntered(MouseEvent e) {;}
			public void mousePressed(MouseEvent e) {;}
			public void mouseReleased(MouseEvent e) {;}
			public void mouseExited(MouseEvent e) {;}
			
		});
		JScrollPane scrollPane = new JScrollPane(siteList);
		sites.add(scrollPane);
		this.add(sites);

		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		buttons.setBorder(new EmptyBorder(6,0,0,0));

		add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
		add.setToolTipText("Add Site");
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Open the add/edit dialog in add mode
				new AddEditSiteDialog(TypoScriptPluginOptions.this, null);
			}
		});

		buttons.add(add);
		remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
		remove.setToolTipText("Remove Site");
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				localSitesConfig.remove(siteList.getSelectedIndex());
				listModel.removeElementAt(siteList.getSelectedIndex());
			}
		});

		buttons.add(remove);
		edit = new RolloverButton(GUIUtilities.loadIcon("ButtonProperties.png"));
		edit.setToolTipText("Edit Site");
		edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Open add/edit dialog in edit mode
				new AddEditSiteDialog(TypoScriptPluginOptions.this, (T3Site)localSitesConfig.get(siteList.getSelectedIndex()));
			}
		});

		buttons.add(edit);
		buttons.add(Box.createGlue());

		updateButtons();
		add(BorderLayout.SOUTH,buttons);
	}
	
	protected void _save() {
		// Copy the configuration back into the global store
		TypoScriptPlugin.siteConfig = localSitesConfig;
		
		// Save it back to disk
		TypoScriptPlugin.saveConfiguration();
	}

	/**
	 * Checks whether any buttons should be disabled/enabled
	 * based on the selection (or lack thereof) in the list
	 */
	private void updateButtons() {
		int index = siteList.getSelectedIndex();
		edit.setEnabled(index != -1 ? true : false);
		remove.setEnabled(index != -1 ? true : false);
	}
}

/**
 * The dialog for adding or editing a site
 */
class AddEditSiteDialog extends EnhancedDialog {
	private T3Site curSite;
	private TypoScriptPluginOptions parent;
	private T3Site origSite;
	private boolean isNew;
	protected JTextField txtURL;
	protected JTextField txtUser;
	protected JPasswordField txtPass;
	protected JCheckBox chkClearCache;
	protected JButton btnSave;
	protected JButton btnCancel;
	protected JButton btnTest;
	
	/**
	 * Construcs the dialog to add or edit a site
	 * If a site object is passed in, we are editing
	 * otherwise a null site means new
	 * 
	 * @param parent A reference to the option pane that opened this dialog
	 * @param site A T3Site object to edit, or null to create a new one
	 */
	public AddEditSiteDialog(Component ourParent, T3Site site) {
		super(JOptionPane.getFrameForComponent(ourParent), (site == null) ? "New Site" : "Edit Site", true);
		parent = (TypoScriptPluginOptions)ourParent;
		
		
		if (site == null) {
			curSite = new T3Site(); // temporary
			isNew = true;
			origSite = null;
		} else {
			curSite = site;
			isNew = false;
			origSite = site;
		}
		
		JPanel formPanel = new JPanel(new GridBagLayout());
		
		JLabel lblURL = new JLabel("Site root URL");
		JLabel lblUser = new JLabel("Backend Login");
		JLabel lblPass = new JLabel("Password");

		txtURL = new JTextField(curSite.getUrlBase().toString(), 20);
		txtUser = new JTextField(curSite.getUsername(), 20);
		txtPass = new JPasswordField(curSite.getPassword(), 20);
		chkClearCache = new JCheckBox("Clear TYPO3 cache on template save", curSite.clearCacheOnSave()); // defaults to true
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		btnTest = new JButton("Test settings");
		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				test(true);
			}
		});

		formPanel.add(lblURL, GridBagHelper.getConstraint(0, 0, 1, 1, "right"));
		formPanel.add(txtURL, GridBagHelper.getConstraintWithInsets(1, 0, 1, 1, "left", 1, 5, 1, 0));
		formPanel.add(lblUser, GridBagHelper.getConstraint(0, 1, 1, 1, "right"));
		formPanel.add(txtUser, GridBagHelper.getConstraintWithInsets(1, 1, 1, 1, "left", 1, 5, 1, 0));
		formPanel.add(lblPass, GridBagHelper.getConstraint(0, 2, 1, 1, "right"));
		formPanel.add(txtPass, GridBagHelper.getConstraintWithInsets(1, 2, 1, 1, "left", 1, 5, 3, 0));
		formPanel.add(chkClearCache, GridBagHelper.getConstraint(0, 3, 2, 1, "centre"));
		
		JPanel buttons = new JPanel();
		buttons.add(btnSave);
		buttons.add(btnCancel);
		buttons.add(btnTest);
		formPanel.add(buttons, GridBagHelper.getConstraint(0, 5, 2, 1, "centre"));
		
		this.getContentPane().add(formPanel);
		this.pack();
		this.setLocationRelativeTo(GUIUtilities.getParentDialog(parent));
		this.setVisible(true);
		
		txtURL.requestFocus();
	}
	
	public void cancel() {
		dispose();
	}
	
	public void ok() {
		if (!test(false)) return; // test() will report any errors
		
		// Commit curSite
		if (isNew) {
			// Append
			parent.localSitesConfig.add(curSite);
			Collections.sort(parent.localSitesConfig);
			parent.listModel.addElement(curSite);
		} else {
			// Replace the old version by inserting the new one into the same index
			int index = parent.localSitesConfig.indexOf(origSite);
			parent.localSitesConfig.remove(origSite);
			parent.listModel.removeElement(origSite);
			parent.localSitesConfig.insertElementAt(curSite, index);
			parent.listModel.add(index, curSite);
		}
		dispose();
	}
	
	/**
	 * Checks to ensure all fields are filled out and that login credentials check out
	 * 
	 * @param reportOK Whether to report on success (false if this is a passive check before adding)
	 * @return true if everything checks out
	 */
	private boolean test(boolean reportOK) {
		URL urlFull = null;
		URL urlBase = null;
		
		if (txtURL.getText().length() == 0) {
			JOptionPane.showMessageDialog(this, "You must specify a URL", "URL Field Blank", JOptionPane.ERROR_MESSAGE);
			return false;
		} else {
			try {
				String typeNum = jEdit.getProperty(TypoScriptPlugin.PROPERTY_PREFIX + "typenum");
				urlFull = new URL(txtURL.getText() + "index.php?type=" + typeNum);
				urlBase = new URL(txtURL.getText());
			} catch(MalformedURLException e) {
				JOptionPane.showMessageDialog(this, "The URL you entered was not valid.\nPlease ensure it starts with http:// or https:// and contains no spaces or other characters that would normally be disallowed in a URL", "Invalid URL", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if (!txtURL.getText().endsWith("/")) {
				JOptionPane.showMessageDialog(this, "The URL you entered did not end with a '/' character. This is required.\nIf you would normally login at http://typo3.example.com/typo3/, then you need to enter http://typo3.example.com/", "Invalid URL", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if (txtUser.getText().length() == 0) {
			JOptionPane.showMessageDialog(this, "You must specify a username, even if you have some alternative authentication scheme (make one up)", "Username Field Blank", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (txtPass.getPassword().length == 0) {
			JOptionPane.showMessageDialog(this, "You must specify a password", "Password Field Blank", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// First stage checks out. Disable the buttons and check the provided information
		disableButtons();

		this.pack(); // resize window

		try {
			HttpClient client = new HttpClient();
			HeadMethod head = new HeadMethod(urlFull.toString());
			client.executeMethod(head);
			
			//head.execute()
			if (head.getStatusCode() != HttpStatus.SC_OK) {
				JOptionPane.showMessageDialog(this, "Received a non-200 response code from the server: " + head.getStatusCode() + "(" + head.getStatusText() + ")\nPlease ensure there are no rewrite rules or redirects in the way.\nI tried URL: " + urlFull.toString(), "Non-200 response", JOptionPane.ERROR_MESSAGE);
				resetButtonState();
				return false;
			}
			if (head.getResponseHeader("X-jeditvfs-present") == null) {
				JOptionPane.showMessageDialog(this, "The jEdit VFS extension doesn't appear to be installed on the remote site.\nPlease install the 'jeditvfs' plugin from the TYPO3 extension repository and try again.\nIt's also possible the URL didn't point to the right place.\nYou  shoud be pointing to the root of the FRONTEND of your site.\nI tried: " + urlFull.toString(), "jeditvfs extension not located", JOptionPane.ERROR_MESSAGE);
				resetButtonState();
				return false;
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "An I/O error occured while testing the site.\nPlease check your internet connection. Details below:\n" + e.toString(), "IOException", JOptionPane.ERROR_MESSAGE);
			resetButtonState();
			return false;
		}
		
		// That worked. Now we need to construct a real T3Site object and perform an authentication check (and get the site name in the process)
		curSite = new T3Site(urlBase, urlFull, txtUser.getText(), new String(txtPass.getPassword()), chkClearCache.isSelected());
		
		try {
			curSite.setName(curSite.getWorker().getSiteTitle());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "An I/O error occured while testing the site.\nPlease check your internet connection. Details below:\n" + e.toString(), "IOException", JOptionPane.ERROR_MESSAGE);
			resetButtonState();
			return false;
		} catch (XmlRpcException e) {
			// Probably an authentication failure.
			if (e.code == RemoteCallWorker.JEDITVFS_ERROR_AUTHFAIL) {
				JOptionPane.showMessageDialog(this, "Authentication failed.\nPlease make sure you typed your username and password correctly.\nNote also that you require an admin-level backend account to use this system.\nIf you get this failure and are using an exotic authentication scheme, please contact the developer of this plugin to report a bug.\nServer said: " + e.getMessage(), "Backend authentication failed", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "The server returned an unexpected response in reply to an authentication test\nServer said: " + e.getMessage(), "Unexpected XML-RPC exception", JOptionPane.ERROR_MESSAGE);
			}
			resetButtonState();
			return false;
		}
		
		if (reportOK) {
			JOptionPane.showMessageDialog(this, "All OK", "All tests passed", JOptionPane.INFORMATION_MESSAGE);
		}
		resetButtonState();
		return true;
		
	}
	
	private void disableButtons() {
		btnSave.setEnabled(false);
		btnTest.setEnabled(false);
	}
	
	private void resetButtonState() {
		btnSave.setEnabled(true);
		btnTest.setEnabled(true);
	}
}

/**
 * This class returns GridBagConstraints, optionally with inset padding. It speeds up the use of
 * the GridBag layout manager!
 * 
 * (borrowed from an earlier project of mine)
 * @author Neil Bertram <neil@tasmanstudios.co.nz>
 *
 */
class GridBagHelper {
        /**
         * This method is STATIC and will give you a GridBagConstraints with the required attributes
         * @param gridx Horizontal grid position of the element
         * @param gridy Vertical grid position of the element
         * @param gridwidth How many cells the element should span
         * @param gridheight How many rows the element should span
         * @param position either left, centre, right, top-left, top-right, bottom-left, bottom-right
         * @return a GridBagConstraints object with the desired attributes.
         */
        public static GridBagConstraints getConstraint(int gridx, int gridy, int gridwidth, int gridheight, String position) {
                GridBagConstraints tempGbc = new GridBagConstraints();
                tempGbc.gridx = gridx;
                tempGbc.gridy = gridy;
                tempGbc.gridwidth = gridwidth;
                tempGbc.gridheight = gridheight;

                if (position.equals("left")) {
                        tempGbc.anchor = GridBagConstraints.LINE_START;
                }
                else if (position.equals("centre")) {
                        tempGbc.anchor = GridBagConstraints.CENTER;
                }
                else if (position.equals("right")) {
                        tempGbc.anchor = GridBagConstraints.LINE_END;
                }
                else if (position.equals("top-left")) {
                        tempGbc.anchor = GridBagConstraints.FIRST_LINE_START;
                }
                else if (position.equals("top-right")) {
                        tempGbc.anchor = GridBagConstraints.FIRST_LINE_END;
                }
                else if (position.equals("bottom-left")) {
                        tempGbc.anchor = GridBagConstraints.LAST_LINE_START;
                }
                else if (position.equals("bottom-right")) {
                        tempGbc.anchor = GridBagConstraints.LAST_LINE_END;
                }
                else {
                        // error
                        System.out.println("getConstraint was provided with an invalid position '" + position + "', returning null");
                        return null;
                }

                return tempGbc;
        }

        /**
         * This method is STATIC and will give you a GridBagConstraints with the required attributes
         * @param gridx Horizontal grid position of the element
         * @param gridy Vertical grid position of the element
         * @param gridwidth How many cells the element should span
         * @param gridheight How many rows the element should span
         * @param position either left, centre, right, top-left, top-right, bottom-left, bottom-right
         * @param insetTop How many pixels to pad the top of the element by
         * @param insetLeft How many pixels to pad the left of the element by
         * @param insetBottom How many pixels to pad the bottom of the element by
         * @param insetRight How many pixels to pad the right of the element by
         * @return a GridBagConstraints object with the desired attributes.
         */
        public static GridBagConstraints getConstraintWithInsets(int gridx, int gridy, int gridwidth, int gridheight, String position, int insetTop, int insetLeft, int insetBottom, int insetRight) {
                GridBagConstraints tempGbc = getConstraint(gridx, gridy, gridwidth, gridheight, position);
                // add the insets
                tempGbc.insets = new Insets(insetTop, insetLeft, insetBottom, insetRight);
                return tempGbc;
        }
}



