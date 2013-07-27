package org.columba.calendar.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.columba.calendar.model.Recurrence;
import org.columba.calendar.model.api.IEventInfo;
import org.columba.calendar.model.api.IRecurrence;
import org.columba.calendar.ui.comp.DatePicker;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.SingleSideEtchedBorder;
import org.columba.core.gui.util.DialogHeaderPanel;
import org.columba.core.help.HelpManager;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RecurrenceDialog extends JDialog implements ActionListener {
	
	private static final String RECURRENCE_DAILY = "Daily";

	private static final String RECURRENCE_WEEKLY = "Weekly";

	private static final String RECURRENCE_MONTHLY = "Monthly";

	private static final String RECURRENCE_ANNUALLY = "Annually";

	private static final IRecurrence DEFAULT_RECURRENCE = new Recurrence(Recurrence.RECURRENCE_DAILY);

	private IEventInfo model;
	
	private boolean readOnly;
	
	private boolean success = false;
	
	// frequency
	JComboBox freqComboBox = new JComboBox();
	
	// no end
	JRadioButton rNoEnd = new JRadioButton();

	// maximum occurrences
	JRadioButton rMaxOccurrences = new JRadioButton();
	JTextField maxOccurrences = new JTextField("0");
	
	// end date
	JRadioButton rUntilDate = new JRadioButton();
	DatePicker endDatePicker;

	public RecurrenceDialog(Frame parentFrame, IEventInfo model, boolean readOnly) {
		super(parentFrame, true);

		endDatePicker = new DatePicker();
		
		this.model = model;
		this.readOnly = readOnly;

		setLayout(new BorderLayout());
		getContentPane().add(
				new DialogHeaderPanel("Recurrence",
						"Edit Recurrence Properties", ImageLoader
								.getIcon(IconKeys.USER)),
				BorderLayout.NORTH);
		getContentPane().add(createPanel(), BorderLayout.CENTER);
		getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);

		getRootPane().registerKeyboardAction(this, "CANCEL",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		updateComponents(true);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);

	}


	private Component createPanel() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"3dlu,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),3dlu",
				"3dlu,CENTER:DEFAULT:NONE,3dlu,FILL:DEFAULT:NONE,6dlu,FILL:DEFAULT:NONE,3dlu,FILL:DEFAULT:NONE,3dlu,FILL:DEFAULT:NONE,3dlu,FILL:DEFAULT:NONE,3dlu");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		PanelBuilder b = new PanelBuilder(formlayout1);
		b.setDefaultDialogBorder();

		b.addSeparator("Recurrence", cc.xywh(2, 2, 2, 1));
		
		freqComboBox.addItem(RECURRENCE_DAILY);
		freqComboBox.addItem(RECURRENCE_WEEKLY);
		freqComboBox.addItem(RECURRENCE_MONTHLY);
		freqComboBox.addItem(RECURRENCE_ANNUALLY);

		b.add(freqComboBox, cc.xy(3, 4));

		b.addSeparator("End", cc.xywh(2, 6, 2, 1));
		
		rNoEnd.setActionCommand("forever");
		rNoEnd.addActionListener(this);
		rNoEnd.setText("Forever");
		b.add(rNoEnd, cc.xy(3, 8));
		
		b.add(createMaxOccurrencesPanel(), cc.xy(3, 10));

		b.add(createUntilDatePanel(), cc.xy(3,12));
		
		// enable all radio buttons
		rNoEnd.setEnabled(true);
		rMaxOccurrences.setEnabled(true);
		rUntilDate.setEnabled(true);

		ButtonGroup group = new ButtonGroup();
		group.add(rNoEnd);
		group.add(rMaxOccurrences);
		group.add(rUntilDate);

		rNoEnd.setSelected(true);
		
		// disable all
		endDatePicker.setEnabled(false);
		maxOccurrences.setEnabled(false);

		return b.getPanel();
	}
	
	private JPanel createMaxOccurrencesPanel() {
		
		JPanel panel = new JPanel();

		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:NONE,FILL:DEFAULT:NONE",
				"CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		panel.setLayout(formlayout1);

		rMaxOccurrences.setText("Maximum recurrences");
		rMaxOccurrences.setActionCommand("maximum");
		rMaxOccurrences.addActionListener(this);
		panel.add(rMaxOccurrences, cc.xy(1, 1));
		
		maxOccurrences.setColumns(2);
		panel.add(maxOccurrences, cc.xy(2, 1));
		
		return panel;
	}
	
	private JPanel createUntilDatePanel() {
		JPanel panel = new JPanel();

		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:NONE,FILL:DEFAULT:NONE",
				"CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		panel.setLayout(formlayout1);

		rUntilDate.setText("Until ");
		rUntilDate.setActionCommand("until");
		rUntilDate.addActionListener(this);
		panel.add(rUntilDate, cc.xy(1, 1));
		panel.add(endDatePicker, cc.xy(2, 1));
		
		return panel;

	}


	private void updateComponents(boolean b) {
		IRecurrence r = model.getEvent().getRecurrence();
		if (b) {
			if (r == null) {
				r = DEFAULT_RECURRENCE;
				model.getEvent().setRecurrence(r);
			}
			if (r.getType() == IRecurrence.RECURRENCE_DAILY) {
				freqComboBox.setSelectedItem(RECURRENCE_DAILY);
			} else if (r.getType() == IRecurrence.RECURRENCE_WEEKLY) {
				freqComboBox.setSelectedItem(RECURRENCE_WEEKLY);
			} else if (r.getType() == IRecurrence.RECURRENCE_MONTHLY) {
				freqComboBox.setSelectedItem(RECURRENCE_MONTHLY);
			} else if (r.getType() == IRecurrence.RECURRENCE_ANNUALLY) {
				freqComboBox.setSelectedItem(RECURRENCE_ANNUALLY);
			} else {
				// it has to be set
				freqComboBox.setSelectedItem(RECURRENCE_DAILY);
				r.setType(IRecurrence.RECURRENCE_DAILY);
			}
			
			if (r.getEndType() == IRecurrence.RECURRENCE_END_FOREVER) {
				rNoEnd.setSelected(true);
				maxOccurrences.setEnabled(false);
				endDatePicker.setEnabled(false);
			} else if (r.getEndType() == IRecurrence.RECURRENCE_END_MAXOCCURRENCES) {
				rMaxOccurrences.setSelected(true);
				maxOccurrences.setEnabled(true);
				endDatePicker.setEnabled(false);
				maxOccurrences.setText((new Integer(r.getEndMaxOccurrences()).toString()));
			} else if (r.getEndType() == IRecurrence.RECURRENCE_END_ENDDATE) {
				rUntilDate.setSelected(true);
				endDatePicker.setDate(r.getEndDate());
				maxOccurrences.setEnabled(false);
				endDatePicker.setEnabled(true);
			} else {
				// it has to be set
				rNoEnd.setSelected(true);
				maxOccurrences.setEnabled(false);
				endDatePicker.setEnabled(false);
				r.setEndType(IRecurrence.RECURRENCE_END_FOREVER);
			}
		} else if (!readOnly) {
			if (freqComboBox.getSelectedItem().equals(RECURRENCE_DAILY)) {
				r.setType(IRecurrence.RECURRENCE_DAILY);
			} else if (freqComboBox.getSelectedItem().equals(RECURRENCE_WEEKLY)) {
				r.setType(IRecurrence.RECURRENCE_WEEKLY);
			} else if (freqComboBox.getSelectedItem().equals(RECURRENCE_MONTHLY)) {
				r.setType(IRecurrence.RECURRENCE_MONTHLY);
			} else if (freqComboBox.getSelectedItem().equals(RECURRENCE_ANNUALLY)) {
				r.setType(IRecurrence.RECURRENCE_ANNUALLY);
			}
			
			if (rNoEnd.isSelected()) {
				r.setEndType(IRecurrence.RECURRENCE_END_FOREVER);
			} else if (rMaxOccurrences.isSelected()) {
				r.setEndType(IRecurrence.RECURRENCE_END_MAXOCCURRENCES);
				try {
					r.setEndMaxOccurrences(Integer.parseInt(maxOccurrences.getText()));
				} catch (NumberFormatException e) {
					// LOG.severe("no number!")
					r.setEndMaxOccurrences(1);
				}
			} else if (rUntilDate.isSelected()) {
				r.setEndType(IRecurrence.RECURRENCE_END_ENDDATE);
				r.setEndDate(endDatePicker.getDate());
			}
		}
	}
	
	private JPanel createButtonPanel() {
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		ButtonWithMnemonic cancelButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "cancel"));

		//$NON-NLS-1$ //$NON-NLS-2$
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$

		ButtonWithMnemonic okButton = new ButtonWithMnemonic(MailResourceLoader
				.getString("global", "ok"));

		//$NON-NLS-1$ //$NON-NLS-2$
		okButton.addActionListener(this);
		okButton.setActionCommand("OK"); //$NON-NLS-1$
		okButton.setDefaultCapable(true);
		if (readOnly)
			okButton.setEnabled(false);
		getRootPane().setDefaultButton(okButton);

		ButtonWithMnemonic helpButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "help"));

		// associate with JavaHelp
		HelpManager.getInstance().enableHelpOnButton(helpButton,
				"configuring_columba");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		buttonPanel.setLayout(new GridLayout(1, 3, 6, 0));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(helpButton);

		bottom.add(buttonPanel, BorderLayout.EAST);

		return bottom;
	}

	public boolean success() {
		return success;
	}

	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();
		
		if (action.equals("OK")) //$NON-NLS-1$
		{
			// check if the user entered valid data
			success = true;

			updateComponents(false);

			setVisible(false);
		} else if (action.equals("CANCEL")) {
			success = false;
			setVisible(false);
		} else if (action.equals("maximum")) {
			// disable date picker
			endDatePicker.setEnabled(false);
			// enable max occurrences
			maxOccurrences.setEnabled(true);
		} else if (action.equals("until")) {
			// disable max occurrences
			maxOccurrences.setEnabled(false);
			// enable until
			endDatePicker.setEnabled(true);
		} else if (action.equals("forever")) {
			// disable other
			endDatePicker.setEnabled(false);
			maxOccurrences.setEnabled(false);
		}
	}

	/**
	 * @return Returns the model.
	 */
	public IEventInfo getModel() {
		return model;
	}

}
