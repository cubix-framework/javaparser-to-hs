/*
 * Gruntspud
 * 
 * Copyright (C) 2002 Brett Smith.
 * 
 * Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.authentication;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.ResourceUtil;
import gruntspud.StringUtil;
import gruntspud.ui.MultilineLabel;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.OptionDialog.Option;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

/**
 * Implementation of a <code>java.net.Authenticator</code> that uses the Gruntspud password manager. If the password isn't
 * currently known, a dialog will be displayed asking the user for it and given the oppurtunity to save the password for later use.
 * 
 * @author magicthize @created 26 May 2002
 */
public class GruntspudAuthenticator extends Authenticator {
	static ResourceBundle res = ResourceBundle.getBundle("gruntspud.authentication.ResourceBundle");

	private final static String[] REMEMBER_PASSWORD = {res.getString("gruntspudAuthenticator.rememberPassword.dontRemember"),
			res.getString("gruntspudAuthenticator.rememberPassword.rememberForSession"),
			res.getString("gruntspudAuthenticator.rememberPassword.rememberPermanently")};
	private GruntspudContext context;
	private XTextField user;
	private JPasswordField password;
	private JPanel passwordPanel;
	private JComponent parent;
	private JComboBox rememberPassword;
	private TitledBorder detailsBorder;
	private JLabel requestingSchemeLabel;
	private JLabel requestingProtocolLabel;
	private JLabel requestingSiteLabel;
	private JLabel protocolLabel;
	private JLabel schemeLabel;
	private JLabel hostLabel;
	private JLabel userLabel;
	private boolean forceAskForPassword;
	private PasswordPairList pairs;
	private String userName;
	private Throwable exception;
	private boolean emphasisePrompt;

	/**
	 * Creates a new GruntspudAuthenticator.
	 * 
	 * @param context context
	 * @throws IOException
	 */
	public GruntspudAuthenticator(GruntspudContext context) {
		//  Initialise
		this.context = context;

		this.context = context;

		//  Create the password panel
		JPanel s = new JPanel(new GridBagLayout());
		s.setBorder(detailsBorder = new TitledBorder(""));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(s, protocolLabel = new JLabel(res.getString("gruntspudAuthenticator.protocol.text")), gbc,
				GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(s, requestingProtocolLabel = new JLabel(), gbc, GridBagConstraints.REMAINDER);

		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(s, schemeLabel = new JLabel(res.getString("gruntspudAuthenticator.scheme.text")), gbc,
				GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(s, requestingSchemeLabel = new JLabel(), gbc, GridBagConstraints.REMAINDER);

		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(s, hostLabel = new JLabel(res.getString("gruntspudAuthenticator.host.text")), gbc,
				GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(s, requestingSiteLabel = new JLabel(), gbc, GridBagConstraints.REMAINDER);

		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(s, userLabel = new JLabel(res.getString("gruntspudAuthenticator.user.text")), gbc,
				GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(s, user = new XTextField(10), gbc, GridBagConstraints.REMAINDER);

		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(s, new JLabel(res.getString("gruntspudAuthenticator.password.text")), gbc, GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(s, password = new JPasswordField(10), gbc, GridBagConstraints.REMAINDER);
		gbc.weightx = 0.0;

		gbc.weightx = 2.0;
		UIUtil.jGridBagAdd(s, rememberPassword = new JComboBox(REMEMBER_PASSWORD), gbc, GridBagConstraints.REMAINDER);

		//
		//  Icon panel
		JLabel i = new JLabel(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_LOCK));
		i.setVerticalAlignment(JLabel.NORTH);
		i.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 4));

		//
		passwordPanel = new JPanel(new BorderLayout());
		passwordPanel.add(i, BorderLayout.WEST);
		passwordPanel.add(s, BorderLayout.CENTER);
	}

	/**
	 * Return a list of all the password pairs
	 * 
	 * @return password pair list
	 */
	public PasswordPairList getPasswordPairList() {
		return pairs;
	}

	/**
	 * Initiailise the autenticator. Should be called just after instantiation
	 * 
	 * @param context context
	 * @throws IOException if password list cannot be loaded
	 */
	public void init(GruntspudContext context) throws IOException {
		//
		File passwordFile = GruntspudUtil.getPreferenceFile("gruntspud.passwords", false);
		pairs = new PasswordPairList(passwordFile, context);

		//
		//  If the password file exists, check the first line to see if it is
		//  in the old format. If it is, offer to encrypt the file
		if (passwordFile.exists()) {
			InputStream in = null;

			try {
				//  Check the first line to see if it is encrypted or not
				in = new FileInputStream(passwordFile);

				BufferedReader r = new BufferedReader(new InputStreamReader(in));
				String format = r.readLine();
				in.close();
				in = null;

				//  Now load the password file again
				pairs.loadPasswordFile();

				if (((format == null) || !format.startsWith("#" + PasswordPairList.FORMAT_KEY)) && context.getEncrypter() != null) {
					askEncrypt();
				}
			}
			finally {
				if (in != null) {
					in.close();
				}
			}
		}
		else
			if (context.getEncrypter() != null) {
				askEncrypt();
			}
	}

	/**
	 * Show a dialog asking the use for a new master password. The user must enter the same password twice, then an 2 element array
	 * of the old password and new password as a char[] array is returned. If the user cancels <code>null</code> will be
	 * returned.
	 * 
	 * @param context context
	 * @param parent parent component
	 * @param text help text
	 * @param title dialog title
	 * @param checkAgainstOld make sure the old password matches first
	 * 
	 * @return 2 element array of char[], first element is old password, second is new
	 */
	public char[][] showMasterPasswordConfirmationDialog(GruntspudContext context, JComponent parent, String text, String title,
			final boolean checkAgainstOld) {
		//
		JPanel t = new JPanel(new BorderLayout());

		if (text != null) {
			t.add(new MultilineLabel(text), BorderLayout.NORTH);

		}
		t.setBorder(BorderFactory
				.createTitledBorder(res.getString("gruntspudAuthenticator.masterPasswordConfirmation.borderTitle")));

		//
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.weightx = 0.0;

		final JPasswordField oldPassword = checkAgainstOld ? new JPasswordField(15) : null;

		if (checkAgainstOld) {
			UIUtil.jGridBagAdd(p, new JLabel(res.getString("gruntspudAuthenticator.masterPasswordConfirmation.oldPassword.text")),
					gbc, GridBagConstraints.RELATIVE);
			gbc.weightx = 1.0;
			UIUtil.jGridBagAdd(p, oldPassword, gbc, GridBagConstraints.REMAINDER);
			gbc.weightx = 0.0;
		}

		UIUtil.jGridBagAdd(p, new JLabel(res.getString("gruntspudAuthenticator.masterPasswordConfirmation.newPassword.text")), gbc,
				GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;

		final JPasswordField newPassword = new JPasswordField(15);
		UIUtil.jGridBagAdd(p, newPassword, gbc, GridBagConstraints.REMAINDER);
		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(p, new JLabel(res.getString("gruntspudAuthenticator.masterPasswordConfirmation.confirmPassword.text")),
				gbc, GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;

		final JPasswordField confirmPassword = new JPasswordField(15);
		UIUtil.jGridBagAdd(p, confirmPassword, gbc, GridBagConstraints.REMAINDER);
		gbc.weightx = 0.0;
		t.add(p, BorderLayout.CENTER);

		//
		final OptionDialog.Option ok = new OptionDialog.Option(res.getString(
				"gruntspudAuthenticator.masterPasswordConfirmation.optionDialog.option.ok.text"), res.getString(
				"gruntspudAuthenticator.masterPasswordConfirmation.optionDialog.option.ok.toolTipText"), ResourceUtil.getResourceMnemonic(
				res, "gruntspudAuthenticator.masterPasswordConfirmation.optionDialog.option.ok.mnemonic"));
		final OptionDialog.Option cancel = new OptionDialog.Option(res.getString(
				"gruntspudAuthenticator.masterPasswordConfirmation.optionDialog.option.cancel.text"), res.getString(
				"gruntspudAuthenticator.masterPasswordConfirmation.optionDialog.option.cancel.toolTipText"), ResourceUtil
				.getResourceMnemonic(res, "gruntspudAuthenticator.masterPasswordConfirmation.optionDialog.option.cancel.mnemonic"));
		OptionDialog.Option opt = OptionDialog.showOptionDialog("passwordConfirm", context, parent, new OptionDialog.Option[]{ok,
				cancel}, t,
				title, ok, new OptionDialog.Callback() {
					public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
						if (option == cancel) {
							return true;
						}

						char[] p1 = newPassword.getPassword();
						char[] p2 = confirmPassword.getPassword();

						if (checkAgainstOld) {
							char[] p3 = oldPassword.getPassword();

							try {
								if (!getPasswordPairList().checkMasterPassword(p3)) {
									JOptionPane.showMessageDialog(dialog, res.getString(
											"gruntspudAuthenticator.masterPasswordConfirmation.error.oldPasswordIncorrect.text"), res.getString(
											"gruntspudAuthenticator.masterPasswordConfirmation.error.oldPasswordIncorrect.title"),
											JOptionPane.ERROR_MESSAGE);

									return false;
								}
							}
							catch (IOException ioe) {
								JOptionPane.showMessageDialog(dialog, ioe.getMessage(), res.getString(
										"gruntspudAuthenticator.masterPasswordConfirmation.error.ioException.title"), JOptionPane.ERROR_MESSAGE);

								return false;
							}
						}

						if (!(new String(p1).equals(new String(p2)))) {
							JOptionPane.showMessageDialog(dialog, res.getString(
									"gruntspudAuthenticator.masterPasswordConfirmation.error.mismatched.text"), res.getString(
									"gruntspudAuthenticator.masterPasswordConfirmation.error.mismatched.title"), JOptionPane.ERROR_MESSAGE);

							return false;
						}

						return true;
					}

                    public void close(OptionDialog dialog, Option option) {
                        // TODO Auto-generated method stub
                        
                    }
				}

				, false, true, null, UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_LOCK));

		if (opt != ok) {
			return null;
		}

		return new char[][]{checkAgainstOld ? oldPassword.getPassword() : null, newPassword.getPassword()};
	}

	private void askEncrypt() throws IOException {
		final OptionDialog.Option yes = new OptionDialog.Option(res.getString(
				"gruntspudAuthenticator.askEncrypt.optionDialog.option.ok.text"), res.getString(
				"gruntspudAuthenticator.askEncrypt.optionDialog.option.ok.toolTipText"), ResourceUtil
				.getResourceMnemonic(res, "gruntspudAuthenticator.askEncrypt.optionDialog.option.ok.mnemonic"));
		final OptionDialog.Option no = new OptionDialog.Option(res.getString(
				"gruntspudAuthenticator.askEncrypt.optionDialog.option.cancel.text"), res.getString(
				"gruntspudAuthenticator.askEncrypt.optionDialog.option.cancel.toolTipText"), ResourceUtil.getResourceMnemonic(
				res, "gruntspudAuthenticator.askEncrypt.optionDialog.option.cancel.mnemonic"));

		OptionDialog.Option opt = OptionDialog.showOptionDialog("encryptPasswordFile", context, context.getHost()
				.getMainComponent(),
				new OptionDialog.Option[]{yes, no}, res.getString("gruntspudAuthenticator.askEncrypt.optionDialog.text"), res
						.getString("gruntspudAuthenticator.askEncrypt.optionDialog.title"), yes,
				null, false, true);

		//`Encrypt the passwords if required
		if (opt == yes) {
			OptionDialog.Option next = new OptionDialog.Option("Next", "Provide the master password to use.", 'n');
			OptionDialog.showOptionDialog("nextQuestionIsPassword", context, context.getHost().getMainComponent(),
					new OptionDialog.Option[]{next}, res.getString("gruntspudAuthenticator.encrypt.optionDialog.text"), res
							.getString("gruntspudAuthenticator.encrypt.optionDialog.title"), yes,
					null, false, true);

			String extraMessage = null;

			while (true) {
				char[] pw = getMasterPassword(extraMessage, context.getHost().getMainComponent());

				if (pw == null) {
					JOptionPane.showMessageDialog(context.getHost().getMainComponent(), res.getString(
							"gruntspudAuthenticator.encrypt.getMasswordPassword.aborted.text"), res.getString(
							"gruntspudAuthenticator.encrypt.getMasswordPassword.aborted.title"),
							JOptionPane.INFORMATION_MESSAGE);
					opt = no;

					break;
				}
				else {
					if (pw.length < 6) {
						extraMessage = res.getString("gruntspudAuthenticator.encrypt.getMasswordPassword.extraMessage.text");
					}
					else {
						pairs.setMasterPassword(pw);
						pairs.encryptAllPairs();

						break;
					}
				}
			}
		}

		//
		pairs.setEncrypted(opt == yes);
		pairs.savePasswordFile();
	}

	/**
	 * Show a dialog asking for the master password
	 * 
	 * @param message message to show
	 * @param parent parent component
	 * 
	 * @return master password (or <code>null</code> if aborted)
	 */
	public char[] getMasterPassword(String message, JComponent parent) {
		//
		JPanel t = new JPanel(new BorderLayout());
		t.add(new MultilineLabel(res.getString("gruntspudAuthenticator.getMasterPassword.text")), BorderLayout.CENTER);

		if (message != null) {
			JLabel mesg = new JLabel(message, JLabel.CENTER);
			mesg.setForeground(Color.red);
			t.add(mesg, BorderLayout.SOUTH);
		}

		t.setBorder(BorderFactory.createTitledBorder(res.getString("gruntspudAuthenticator.getMasterPassword.borderTitle")));

		//
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(p, new JLabel(res.getString("gruntspudAuthenticator.getMasterPassword.password")), gbc,
				GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;

		JPasswordField password = new JPasswordField(15);
		password.grabFocus();
		UIUtil.jGridBagAdd(p, password, gbc, GridBagConstraints.REMAINDER);
		t.add(p, BorderLayout.CENTER);

		//
		final OptionDialog.Option ok = new OptionDialog.Option(res.getString(
				"gruntspudAuthenticator.getMasterPassword.optionDialog.option.ok.text"), res.getString(
				"gruntspudAuthenticator.getMasterPassword.optionDialog.option.ok.toolTipText"), ResourceUtil.getResourceMnemonic(
				res, "gruntspudAuthenticator.getMasterPassword.optionDialog.option.ok.mnemonic"));
		final OptionDialog.Option cancel = new OptionDialog.Option(res.getString(
				"gruntspudAuthenticator.getMasterPassword.optionDialog.option.cancel.text"), res.getString(
				"gruntspudAuthenticator.getMasterPassword.optionDialog.option.cancel.toolTipText"), ResourceUtil
				.getResourceMnemonic(res, "gruntspudAuthenticator.getMasterPassword.optionDialog.option.cancel.mnemonic"));

		OptionDialog.Option opt = OptionDialog.showOptionDialog("masterPassword", context, parent, new OptionDialog.Option[]{ok,
				cancel}, t,
				res.getString("gruntspudAuthenticator.getMasterPassword.optionDialog.title"), ok, null, false, true, null, UIUtil
						.getCachedIcon(Constants.ICON_TOOL_LARGE_LOCK));

		if (opt != ok) {
			return null;
		}

		return password.getPassword();
	}

	/**
	 * Set whether the master password should be asked for if it is needed. This should be set just before any operation that might
	 * require the use of a password is run.
	 * 
	 * @param forceAskForPassword always ask for the master password next time it is needed
	 */
	public void setForceAskForPassword(boolean forceAskForPassword) {
		this.forceAskForPassword = forceAskForPassword;
	}

	/**
	 * Set if the next request for a password should emphasise the 'prompt' field by colouring it red. This would normally be used
	 * if this is the second request for a password as the first one was incorrect in some way.
	 * 
	 * @param emphasisePrompt emphasis prompt
	 */
	public void setEmphasisePrompt(boolean emphasisePrompt) {
		this.emphasisePrompt = emphasisePrompt;
	}

	/**
	 * Set the parent component to use as a parent for the modal authentication dialog (should it be displayed).
	 * 
	 * @param parent parent component
	 */
	public void setParentComponent(JComponent parent) {
		this.parent = parent;
	}

	/**
	 * Set the user name to use next time a password is requested
	 * 
	 * @param userName user name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void updateUI() {
		SwingUtilities.updateComponentTreeUI(passwordPanel);
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		exception = null;
		try {
			Constants.SYSTEM_LOG.info("Some network access requested authentication");

			String host = (getRequestingSite() == null)
					? res.getString("gruntspudAuthenticator.auth.unknownSite")
					: getRequestingSite().getHostName();
			String scheme = getRequestingScheme();
			String prompt = getRequestingPrompt();

			requestingSchemeLabel.setText(scheme);
			requestingSchemeLabel.setToolTipText(scheme);
			requestingProtocolLabel.setText(getRequestingProtocol());
			requestingSiteLabel.setText(host);
			detailsBorder.setTitle(prompt);
			detailsBorder.setTitleColor(emphasisePrompt ? Color.red : UIManager.getColor("TitledBorder.titleColor"));

			PasswordKey key = new PasswordKey(getRequestingProtocol(), getRequestingScheme(), host);

			//  First check the session passwords, then the persistant ones for a
			//  match on the protocol / scheme / host name
			String userPassword = null;
			PasswordPair pair = pairs.getPair(key);

			if (pair == null) {
				rememberPassword.setSelectedIndex(1);
			}
			else {
				userPassword = pair.getUserPassword();

				if (pair.isPersistant()) {
					rememberPassword.setSelectedIndex(2);
				}
				else {
					rememberPassword.setSelectedIndex(0);
				}
			}

			//  Decrypt the password if necessary
			if ((userPassword != null) && pairs.isEncrypted()) {
				String message = null;

				while (true) {
					if (pairs.getMasterPassword() == null) {
						pairs.setMasterPassword(getMasterPassword(message, parent));

					}
					if (pairs.getMasterPassword() == null) {
						return null;
					}

					if (pairs.checkMasterPassword(pairs.getMasterPassword())) {
						break;
					}
					else {
						message = res.getString("gruntspudAuthenticator.auth.incorrectPassword.text");
						pairs.setMasterPassword(null);
					}
				}

				if (context.getEncrypter() == null) {
					throw new IOException(res.getString("gruntspudAuthenticator.auth.error.encrypterNotAvailable.text"));
				}

				userPassword = context.getEncrypter().decryptString(userPassword, pairs.getMasterPassword());
			}

			//  Get the user and password
			char[] passwordChars = null;

			if ((userPassword != null) && (userPassword.length() != 0)) {
				String[] s = StringUtil.splitString(userPassword, '@');

				if (userName == null) {
					userName = s[0];

				}
				if (s.length > 1) {
					passwordChars = s[1].toCharArray();
				}
			}

			boolean askForPassword = forceAskForPassword || (passwordChars == null) || (userName == null);

			//  Only ask for the password if we do not already know it
			if (askForPassword) {
				user.setEnabled(userName == null);
				user.setText((userName == null) ? "" : userName);
				password.setText((passwordChars == null) ? "" : new String(passwordChars));

				//  Set the focus
				if (userName == null) {
					password.grabFocus();
				}
				else {
					user.grabFocus();

					//  I really don't know why this is necessary ...... If this is not
					//  done the password caret sits at about position 8 and throws exceptions
					//  and stuff .. ?
				}
				password.setCaretPosition(0);

				//  Show the dialog
				final OptionDialog.Option ok = new OptionDialog.Option(res.getString(
						"gruntspudAuthenticator.auth.optionDialog.option.ok.text"), res.getString(
						"gruntspudAuthenticator.auth.optionDialog.option.ok.toolTipText"), ResourceUtil
						.getResourceMnemonic(res, "gruntspudAuthenticator.auth.optionDialog.option.ok.mnemonic"));
				final OptionDialog.Option cancel = new OptionDialog.Option(res.getString(
						"gruntspudAuthenticator.auth.optionDialog.option.cancel.text"), res.getString(
						"gruntspudAuthenticator.auth.optionDialog.option.cancel.toolTipText"), ResourceUtil
						.getResourceMnemonic(res, "gruntspudAuthenticator.auth.optionDialog.option.cancel.mnemonic"));

				OptionDialog.Option opt = OptionDialog.showOptionDialog("password", context, parent, new OptionDialog.Option[]{ok,
						cancel},
						passwordPanel, res.getString("gruntspudAuthenticator.auth.optionDialog.title"), ok, null, false, true);

				if (opt != ok) {
					return null;
				}

				userName = user.getText();
				passwordChars = password.getPassword();

				//  If we are to rememeber the password ..
				if (rememberPassword.getSelectedIndex() != 0) {
					if (pairs.isEncrypted() && (pairs.getMasterPassword() == null)) {
						String message = null;

						while (true) {
							if (pairs.getMasterPassword() == null) {
								pairs.setMasterPassword(getMasterPassword(message, parent));

							}
							if (pairs.getMasterPassword() == null) {
								throw new IOException(res.getString(
										"gruntspudAuthenticator.auth.error.masterPasswordNotSupplie.text"));
							}

							if (pairs.checkMasterPassword(pairs.getMasterPassword())) {
								break;
							}
							else {
								message = res.getString("gruntspudAuthenticator.auth.incorrectPassword.text");
								pairs.setMasterPassword(null);
							}
						}
					}

					String upw = userName + "@" + new String(passwordChars);
					String epw = context.getEncrypter() != null && pairs.isEncrypted() ? context.getEncrypter()
							.encryptString(upw, pairs.getMasterPassword()) : upw;
					PasswordPair p = new PasswordPair(key, epw);
					pairs.addPair(p);

					if (rememberPassword.getSelectedIndex() == 2) {
						p.setPersistant(true);
						pairs.savePasswordFile();
					}
				}
			}

			//
			return new PasswordAuthentication(userName, passwordChars);
		}
		catch (Throwable t) {
			exception = t;
			Constants.SYSTEM_LOG.error("Password authentication failed.", t);
			return null;
		}
		finally {
			userName = null;
			forceAskForPassword = false;
		}
	}

	/**
	 * Return the last exception
	 * 
	 * @return last exceptoion
	 */
	public Throwable getException() {
		return exception;
	}
}