//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.main;

import java.awt.BorderLayout;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.columa.core.config.IDefaultItem;
import org.columba.api.plugin.PluginLoadingFailedException;
import org.columba.core.backgroundtask.BackgroundTaskManager;
import org.columba.core.component.api.IComponentPlugin;
import org.columba.core.config.DefaultItem;
import org.columba.core.connectionstate.ConnectionStateImpl;
import org.columba.core.facade.ServiceFacadeRegistry;
import org.columba.core.gui.base.MultiLineLabel;
import org.columba.core.gui.frame.DefaultContainer;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.main.ColumbaCmdLineParser;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.mail.config.IncomingItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.facade.DialogFacade;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.folder.virtual.ActivateVirtualFolderCommand;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.config.accountwizard.AccountWizardLauncher;
import org.columba.mail.gui.tree.FolderTreeModel;
import org.columba.mail.nativ.defaultmailclient.SystemDefaultMailClientHandler;
import org.columba.mail.parser.MailUrlParser;
import org.columba.mail.pgp.MultipartEncryptedRenderer;
import org.columba.mail.pgp.MultipartSignedRenderer;
import org.columba.mail.shutdown.ClearRecentFlagPlugin;
import org.columba.mail.shutdown.SaveAllFoldersPlugin;
import org.columba.mail.shutdown.SavePOP3CachePlugin;
import org.columba.mail.spam.SaveSpamDBPlugin;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.composer.MimeTreeRenderer;
import org.columba.ristretto.parser.ParserException;

/**
 * Main entrypoint for mail component.
 *
 * @author fdietz
 */
public class MailMain implements IComponentPlugin {
	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger.getLogger("org.columba.mail.main");

	public MailMain() {
	}

	/**
	 *
	 */
	public void init() {
		// Init PGP
		MimeTreeRenderer renderer = MimeTreeRenderer.getInstance();
		renderer.addMimePartRenderer(new MultipartSignedRenderer());
		renderer.addMimePartRenderer(new MultipartEncryptedRenderer());

		// Init Plugins
		/*PluginManager.getInstance().addHandlers(
				"org/columba/mail/plugin/pluginhandler.xml");

		try {
			InputStream is = this.getClass().getResourceAsStream(
					"/org/columba/mail/action/action.xml");
			PluginManager.getInstance().getHandler(IExtensionHandlerKeys.ORG_COLUMBA_CORE_ACTION)
					.loadExtensionsFromStream(is);
		} catch (PluginHandlerNotFoundException ex) {
			ex.printStackTrace();
		}*/

		// instantiate FolderTreeModel here to make sure the folders
		// are saved after ClearRecentFlagPlugin is executed
		FolderTreeModel.getInstance();

		Runnable plugin = new SaveAllFoldersPlugin();
		BackgroundTaskManager.getInstance().register(plugin);

		plugin = new SavePOP3CachePlugin();
		BackgroundTaskManager.getInstance().register(plugin);
		ShutdownManager.getInstance().register(plugin);

		plugin = new SaveSpamDBPlugin();
		BackgroundTaskManager.getInstance().register(plugin);
		ShutdownManager.getInstance().register(plugin);

		plugin = new ClearRecentFlagPlugin();
		ShutdownManager.getInstance().register(plugin);

		ServiceFacadeRegistry.getInstance().register(
				org.columba.mail.facade.IDialogFacade.class,
				new DialogFacade());
	}

	public void registerCommandLineArguments() {
		ColumbaCmdLineParser parser = ColumbaCmdLineParser.getInstance();

		Option composeOption = new Option("compose", true, "compose message");
		parser.addOption(composeOption);

		Option mailOption = OptionBuilder.
				hasOptionalArg().
				withLongOpt("mailurl").
				create("mail");

		parser.addOption(mailOption);
	}

	/**
	 * @see org.columba.core.component.api.IComponentPlugin#handleCommandLineParameters(java.lang.String[])
	 */
	public void handleCommandLineParameters(CommandLine commandLine) {

		if (commandLine.hasOption("mail")) {
			// check if the optional value exists
			if (commandLine.getOptionValue("mail") != null) {
				try {
					ComposerModel model = new ComposerModel(MailUrlParser
							.parse(commandLine.getOptionValue("mail")));

					// new NewMessageAction().actionPerformed(null);
					ComposerController controller = new ComposerController();
					new DefaultContainer(controller);

					controller.setComposerModel(model);

					ColumbaCmdLineParser.getInstance().setRestoreLastSession(false);
				} catch (ParserException e1) {
					LOG.warning(e1.getLocalizedMessage());
				}
			} else {
				try {
					FrameManager.getInstance().openView("ThreePaneMail");

					ColumbaCmdLineParser.getInstance().setRestoreLastSession(false);
				} catch (PluginLoadingFailedException e) {
					LOG.severe(e.getLocalizedMessage());
				}
			}
		}

		if (commandLine.hasOption("compose")) {
			String option = commandLine.getOptionValue("compose");
			ComposerModel model = new ComposerModel(MessageOptionParser
					.parse(option));

			ComposerController controller = new ComposerController();
			new DefaultContainer(controller);

			controller.setComposerModel(model);

			ColumbaCmdLineParser.getInstance().setRestoreLastSession(false);
		}
	}

	/**
	 *
	 */
	public void postStartup() {
		// Check default mail client
		checkDefaultClient();

		// Show first time Account Wizard
		if (MailConfig.getInstance().getAccountList().count() == 0) {
			new AccountWizardLauncher().launchWizard(true);
		}

		// Check Internet Connection
		if (MailConfig.getInstance().getAccountList().count() > 0) {
			try {
				IncomingItem testConnection = MailConfig.getInstance()
						.getAccountList().getDefaultAccount().getIncomingItem();
				ConnectionStateImpl.getInstance().setTestConnection(
						testConnection.get("host"),
						testConnection.getInteger("port"));
				ConnectionStateImpl.getInstance().checkPhysicalState();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}

		// Activate all Virtual Folders
		ActivateVirtualFolderCommand.activateAll((IMailFolder) FolderTreeModel
				.getInstance().getRoot());
	}
	
	private void checkDefaultClient() {
		// Check if Columba is the default mail client
		SystemDefaultMailClientHandler defaultClientHandler = new SystemDefaultMailClientHandler();
		IDefaultItem item = new DefaultItem(MailConfig.getInstance().get(
				"options"));

		boolean checkDefault = item.getBooleanWithDefault(
				"options/defaultclient", "check", true);
		
		if (checkDefault
				&& defaultClientHandler.platfromSupportsDefaultMailClient()) {
			if (!defaultClientHandler.isDefaultMailClient()) {

				JPanel panel = new JPanel(new BorderLayout(0, 10));

				panel.add(new MultiLineLabel(MailResourceLoader.getString(
						"dialog", "defaultclient", "make_default")),
						BorderLayout.NORTH);

				JCheckBox askAgain = new JCheckBox(MailResourceLoader
						.getString("dialog", "defaultclient", "ask_no_more"));
				panel.add(askAgain, BorderLayout.CENTER);

				// Some error in the client/server communication
				// --> fall back to default login process
				
				// @author hubms: wait until the first frame is displayable
				// fix for bug [#CA-177]
				while (!(FrameManager.getInstance().getActiveFrame().isDisplayable())) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						LOG.severe(e.getMessage());
					}
				}				
				
				// fix for bug [#CA-199] java 1.6.0-b105 trick
				if (!FrameManager.getInstance().getActiveFrame().isValid())
					FrameManager.getInstance().getActiveFrame().doLayout();

				int result = JOptionPane.showConfirmDialog(FrameManager
						.getInstance().getActiveFrame(), panel,
						MailResourceLoader.getString("dialog", "defaultclient",
								"title"), JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					defaultClientHandler.setDefaultMailClient();
				}

				item.setBoolean("options/defaultclient", "check", !askAgain
						.isSelected());
			}
		}
	}

}