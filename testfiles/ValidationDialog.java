/*
 * $Revision: 20173 $
 * $Date: 2011-10-30 05:38:03 -0400 (Sun, 30 Oct 2011) $
 * $Author: kerik-sf $
 *
 * Copyright (C) 2001 C. Scott Willy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

 package cswilly.spell;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.net.URL;

/**
 * Dialog to the user to determine what to do about a misspelled word.
 *<p>
 *   <p>Features of the dialog box:</p>
 *   <ul>
 *     <li>the dialog box is modal</li>
 *     <li>the title of the dialog box is set to "Spell Check"</li>
 *     <li>the user action change be determined using the getUserAction() method</li>
 *     <li>the possible user actions are modelled using the instances of the class UserAction
 *       (which is an enum-like thingy). The possible instances are give by public
 *       static text fields (e.g. ADD, and CANCEL) of type UserAction.</li>
 *     <li>the dialog box can be closed using the escape key (user action is CANCEL)</li>
 *     <li>there is a default button (<cite>Ignore</cite>)</li>
 *     <li>the buttons all have mnemonic keys</li>
 *     <li>??? todo set focus to <cite>Change to</cite> text field when dialog is
 *       opened</li>
 *     <li>??? todo add tool tips to all visual components</li>
 *     <li>??? todo move text to properties file</li>
 *   </ul>
 *<p>
 * Escape closes dialog (http://www.javaworld.com/javaworld/javatips/jw-javatip72.html)
 */
public
class ValidationDialog
  extends JDialog
{
  private static final ImageIcon ignoredIcon;
  static{
	  URL url = ValidationDialog.class.getClassLoader().getResource("cswilly/spell/ignored-words.png");
	  if(url !=null)ignoredIcon = new ImageIcon(url);
	  else ignoredIcon = null;
  }
  private		JTextField _originalWordTextField;
  private       JTextField  _changeToTextField;
  private       JList       _suggestionsJList;
  private		DefaultListModel	_suggestionsModel;
  private       String      _title              = "Spell Check, Release R007";
  private		JLabel		_ignoredWordsLabel;
  private	    AbstractAction		_previousAction;
  private	    AbstractAction		_suggestAction;
  // ??? bad to have release hardocoded here. Fix later...right.
  private       static Point       _location           = new Point( 100, 100 );


  private boolean _done;
  private Callback _callback;
  private SpellException _exception;
  
  public ValidationDialog( Frame owner)
  {
    super( owner );
	_init();
  }
  

  public
  void dispose()
  {
    // save current location statically for next time
    _location = getLocation();
    super.dispose();
  }

  public boolean showAndGo(Result firstResult,Callback callback)throws SpellException{
	  _callback=callback;
	  refresh(firstResult);
	  _done=true;
	  setVisible(true);
	  if(_exception!=null)throw _exception;
	  return _done;
  }


  private String getSelectedWord()
  {
    return _changeToTextField.getText();
  }

  /**
  * Overridden to register {@link CloseDialogActionListener} to be called when
  * the escape key is pressed.
   */
  protected
  JRootPane createRootPane()
  {
    KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    JRootPane rootPane = new JRootPane();

    ActionListener actionListener = new CloseDialogActionListener();
    rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

	// final String ESC_ACTION_KEY = "ESC_ACTION_KEY";
    // rootPane.getActionMap().put(
    //     ESC_ACTION_KEY,
	// 	new CancelAction()
    //     );
	// rootPane.getInputMap(
    //     JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
    // ).put(
    //     KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
    //     ESC_ACTION_KEY
    // );

    return rootPane;
  }


  /**
   * Convenience method add lableled component
   */
  private
  Box _createRow( JComponent labelComponent, Component component )
  {
    Box hBox = Box.createHorizontalBox();

    Dimension labelComponentDim =
      new Dimension( 100, labelComponent.getPreferredSize().height );
    labelComponent.setPreferredSize( labelComponentDim );
    labelComponent.setMinimumSize( labelComponentDim );
    labelComponent.setMaximumSize( labelComponentDim );
    hBox.add( labelComponent );

    hBox.add( Box.createHorizontalGlue() );
    hBox.add( component );
    hBox.add( Box.createHorizontalGlue() );
	return hBox;
  }

  /**
   * Convenience method to create and config buttons from an action
   *<p>
   * After creating the JButton, sets the Mnemonic and ToolTipText by looking
   * for the AbstractAction.MNEMONIC_KEY and AbstractAction.SHORT_DESCRIPTION
   * keys in <code>action</code>.
   *<p>
   * @return a new, freshly configured JButton
   */
  private static
  JButton _configButton( AbstractAction action )
  {
    JButton retButton = new JButton( action );

    Object value;

    value = action.getValue( AbstractAction.MNEMONIC_KEY );
    if( value != null )
    {
      int MnemonicKey = ((Integer)value).intValue();
      retButton.setMnemonic( MnemonicKey );
    }

    value = action.getValue( AbstractAction.SHORT_DESCRIPTION );
    if( value != null )
    {
      String toolTip = (String)value;
      retButton.setToolTipText( toolTip );
    }

    value = action.getValue( AbstractAction.NAME );
    if( value != null )
    {
      String name = (String)value;
      retButton.setName( name );
    }

    return retButton;
  }

  /**
   * Initializes the dialog box
   */
  private
  void _init()
  {
    setModal( true );
    setTitle( _title );
	setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

	addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				if(_callback.cancel()){
					ValidationDialog.this.setVisible(false);
				}
			}
	});
    // //--
    //-- Buttons
    //--
    JButton aboutButton       = _configButton( new AboutAction() );
    JButton addButton       = _configButton( new AddAction() );
    JButton cancelButton    = _configButton( new CancelAction() );
    JButton changeButton    = _configButton( new ChangeAction() );
    JButton changeAllButton = _configButton( new ChangeAllAction() );
    JButton ignoreButton    = _configButton( new IgnoreAction() );
    JButton ignoreAllButton = _configButton( new IgnoreAllAction() );
	_suggestAction = new SuggestAction();
    JButton suggestButton    = _configButton( _suggestAction );
	_previousAction = new PreviousAction();
    JButton previousButton = _configButton( _previousAction );
    JButton doneButton    = _configButton( new DoneAction() );

    //--
    //-- Text Fields
    //--
    _changeToTextField = new JTextField();
	_changeToTextField.setName("changeTo");
    _changeToTextField.setMinimumSize( new Dimension( 100, _changeToTextField.getPreferredSize().height ) );
    _changeToTextField.setMaximumSize( new Dimension( Integer.MAX_VALUE, _changeToTextField.getPreferredSize().height ) );
	_changeToTextField.getDocument().addDocumentListener(new SuggestListener());

    Dimension textFieldDim =
      new Dimension( Integer.MAX_VALUE, _changeToTextField.getPreferredSize().height );
    _originalWordTextField = new JTextField( "ORIGINAL" );
	_originalWordTextField.setName("originalWord");
    _originalWordTextField.setMinimumSize( new Dimension( 200, _originalWordTextField.getPreferredSize().height ) );
    _originalWordTextField.setMaximumSize( new Dimension( Integer.MAX_VALUE, _originalWordTextField.getPreferredSize().height ) );
    _originalWordTextField.setEditable(false);

    //--
    //-- Other components
    //--
	
	_suggestionsModel = new DefaultListModel();
	_suggestionsModel.addElement("(no suggestion)");
	_suggestionsJList = new JList(_suggestionsModel);
	_suggestionsJList.setForeground( Color.lightGray );
    
	_suggestionsJList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    _suggestionsJList.addListSelectionListener( new MyListSelectionListener() );
    _suggestionsJList.setMinimumSize( new Dimension( 200, 300 ) );
    _suggestionsJList.setMaximumSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );
    _suggestionsJList.setPreferredSize( new Dimension( 200, 300 ) );
    JScrollPane suggestionsJScrollPane = new JScrollPane( _suggestionsJList );
    // suggestionsJScrollPane.setPreferredSize(
    //   new Dimension( suggestionsJScrollPane.getPreferredSize().width, 75 ) );

    //--
    //-- Overall Dialog box
    //--
    Box mainBox = Box.createVerticalBox();

    getContentPane().add( mainBox );

    Box hBox;
    JLabel jLabel;

    jLabel = new JLabel( "Not in Dictionary:" );
    mainBox.add(_createRow(jLabel, _originalWordTextField ));

    jLabel = new JLabel( "Change to:" );
    hBox = _createRow( jLabel, _changeToTextField );
	hBox.add(suggestButton);
	mainBox.add(hBox);

    jLabel = new JLabel( "Suggestions:" );
    hBox = Box.createHorizontalBox();

    //suggestionsJScrollPane.setMinimumSize( new Dimension( 200, 300 ) );
    //suggestionsJScrollPane.setMaximumSize( new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE ) );
    hBox.add( suggestionsJScrollPane );
    hBox.add( Box.createHorizontalGlue() );


    getRootPane().setDefaultButton( ignoreButton );
    JPanel buttonPanel = new JPanel();
    JPanel navigPanel = new JPanel();
    buttonPanel.setPreferredSize( new Dimension( 200, 130 ) );
    buttonPanel.add( ignoreButton );
    buttonPanel.add( ignoreAllButton );
    buttonPanel.add( changeButton );
    buttonPanel.add( changeAllButton );
    buttonPanel.add( addButton );
    navigPanel.add( cancelButton );
    buttonPanel.add( aboutButton );
    navigPanel.add( previousButton );
    navigPanel.add( doneButton );
    hBox.add( buttonPanel );
    hBox.add( Box.createHorizontalGlue() );

    mainBox.add(_createRow(jLabel, hBox ));

	//---- message about ignored words
	hBox = Box.createHorizontalBox();
	_ignoredWordsLabel = new JLabel("<HTML><I>There are ignored words (to clear them, use the Spell Check menu)</I></HTML>",ignoredIcon,SwingConstants.LEADING);
	hBox.add(Box.createHorizontalGlue() );
	hBox.add(_ignoredWordsLabel);
	hBox.add(Box.createHorizontalGlue() );
	mainBox.add(Box.createVerticalGlue());
	mainBox.add(hBox);

	mainBox.add(Box.createVerticalGlue());
	mainBox.add(navigPanel);
    if( _location != null )
    setLocation( _location );
    pack();

    //setSize( 750, getPreferredSize().height );
  }


  private void refresh(Result res){
	  if(res == null){
		  setVisible(false);
		  return;
	  }
	  
	  _originalWordTextField.setText(res.getOriginalWord());
	  _changeToTextField.setText(res.getOriginalWord());
	  
	  if(_callback.hasIgnored()){
		  _ignoredWordsLabel.setVisible(true);
	  }else{
		  _ignoredWordsLabel.setVisible(false);
	  }

	  if(_callback.hasPrevious()){
		  _previousAction.setEnabled(true);
	  }else{
		  _previousAction.setEnabled(false);
	  }
	  
	  refreshList(res.getSuggestions(),true);
	  
  }
  

  private void refreshList(List<String> suggestions,boolean changeSelection){
      while(_suggestionsModel.getSize()!=0)_suggestionsModel.remove(0);

	  if (suggestions == null || suggestions.isEmpty() )
	  {
		  _suggestionsModel.addElement("(no suggestion)");
		  _suggestionsJList.setForeground( Color.lightGray );
		  if(changeSelection){
		  _changeToTextField.grabFocus();
		  }
	  }
	  else
	  {
		  for(int i=0;i<suggestions.size();i++)
			  _suggestionsModel.addElement(suggestions.get(i) );

		  if(changeSelection){
			  _suggestionsJList.setSelectedIndex( 0 );
			  _changeToTextField.setText(suggestions.get(0).toString());
			  _suggestionsJList.grabFocus();
		  }

		  _suggestionsJList.setForeground( null );
	  }
	  _suggestAction.setEnabled(false);
  }

  //--
  //-- Availables Actions
  //--

  private class AboutAction
    extends AbstractAction
  {
    private AboutAction()
    {
      super( "About..." );
	  putValue(Action.SHORT_DESCRIPTION,"Brings up the About Dialog.");
    }

    public
    void actionPerformed( ActionEvent event )
    {
      String msg = "Based on interfacing Java with Aspell.\n" +
                   "Hacked by C. Scott Willy to scratch an itch.\n" +
                   "Copyright 2001 Scott Willy\n" +
                   "http://www.geocities.com/cswilly/spellcheck/";
      String title = "About " + _title;
      JOptionPane.showMessageDialog( ValidationDialog.this,
                                     msg,
                                     title,
                                     JOptionPane.INFORMATION_MESSAGE);
    }
  }

  private class SuggestAction
    extends AbstractAction
  {
    private SuggestAction()
    {
      super( "Suggest" );
	  putValue(Action.SHORT_DESCRIPTION,"Verify replacement word");
    }

    public
    void actionPerformed( ActionEvent event )
    {
		try{
			Result r = _callback.suggest(getSelectedWord());
			if(r.getType()==Result.OK){
				refreshList(Arrays.asList(new String[]{r.getOriginalWord()}),false);
			}else{
				refreshList(r.getSuggestions(),false);
			}
		}catch(SpellException spe){
			_exception = spe;
			setVisible(false);
		}
    }
  }
  
  private class DoneAction
    extends AbstractAction
  {
    private DoneAction()
    {
      super( "Done" );
	  putValue(Action.SHORT_DESCRIPTION,"exit current session and apply changes");
    }

    public
    void actionPerformed( ActionEvent event )
    {
		_callback.done();
		setVisible(false);
    }
  }

  private class AddAction
    extends AbstractAction
  {
    private AddAction()
    {
      super( "Add" );
      putValue( MNEMONIC_KEY, new Integer(KeyEvent.VK_A) );
      putValue( ACCELERATOR_KEY, new Integer(KeyEvent.VK_A) );
	  putValue(Action.SHORT_DESCRIPTION,"Add the suspect word (not the 'Change to') to the dictionary.");
    }

    public
    void actionPerformed( ActionEvent event )
    {
		try{
			refresh(_callback.add());
		}catch(SpellException spe){
			_exception = spe;
			setVisible(false);
		}
    }
  }

  private class PreviousAction
    extends AbstractAction
  {
    private PreviousAction()
    {
      super( "Previous" );
	  putValue(Action.SHORT_DESCRIPTION,"Go back to previous word.");
    }

    public
    void actionPerformed( ActionEvent event )
    {
		try{
			refresh(_callback.previous());
		}catch(SpellException spe){
			_exception = spe;
			setVisible(false);
		}
    }
  }

  private class CancelAction
    extends AbstractAction
  {
    private CancelAction()
    {
      super( "Cancel" );
	  putValue(Action.SHORT_DESCRIPTION,"Cancel whole spell-checking session.");
    }

    public
    void actionPerformed( ActionEvent event )
    {
	  if(_callback.cancel()){
		  _done = false;
		  setVisible(false);
	  }
    }
  }

  private class ChangeAction
    extends AbstractAction
  {
    private ChangeAction()
    {
      super( "Change" );
      putValue( MNEMONIC_KEY, new Integer(KeyEvent.VK_C) );
      putValue( ACCELERATOR_KEY, new Integer(KeyEvent.VK_C) );
      putValue( SHORT_DESCRIPTION, "Replaces the current word with the word in the Change to text field." );
    }

    public
    void actionPerformed( ActionEvent event )
    {
		try{
			refresh(_callback.change(getSelectedWord()));
		}catch(SpellException spe){
			_exception = spe;
			setVisible(false);
		}
    }
  }

  private class ChangeAllAction
    extends AbstractAction
  {
    private ChangeAllAction()
    {
      super( "Change All" );
      putValue( MNEMONIC_KEY, new Integer(KeyEvent.VK_L) );
      putValue( ACCELERATOR_KEY, new Integer(KeyEvent.VK_L) );
      putValue( SHORT_DESCRIPTION, "Replaces all occurences of the current word with the word in the Change to text field." );
    }

    public
    void actionPerformed( ActionEvent event )
    {
		try{
			refresh(_callback.changeAll(getSelectedWord()));
		}catch(SpellException spe){
			_exception = spe;
			setVisible(false);
		}
    }
  }

  private class IgnoreAction
    extends AbstractAction
  {
    private IgnoreAction()
    {
      super( "Ignore" );
      putValue( MNEMONIC_KEY, new Integer(KeyEvent.VK_I) );
      putValue( ACCELERATOR_KEY, new Integer(KeyEvent.VK_I) );
      putValue( SHORT_DESCRIPTION, "Ignore this instance of the current word (will not be changed)." );
    }

    public
    void actionPerformed( ActionEvent event )
    {
		try{
			refresh(_callback.ignore());
		}catch(SpellException spe){
			_exception = spe;
			setVisible(false);
		}
    }
  }

  private class IgnoreAllAction
    extends AbstractAction
  {
    private IgnoreAllAction()
    {
      super( "Ignore All" );
      putValue( MNEMONIC_KEY, new Integer(KeyEvent.VK_G) );
      putValue( ACCELERATOR_KEY, new Integer(KeyEvent.VK_G) );
      putValue( SHORT_DESCRIPTION, "Ignore all instances of the current word." );
    }

    public
    void actionPerformed( ActionEvent event )
    {
		try{
			refresh(_callback.ignoreAll());
		}catch(SpellException spe){
			_exception = spe;
			setVisible(false);
		}
    }
  }

  private class CloseDialogActionListener
    implements ActionListener
  {
    public void
    actionPerformed(ActionEvent actionEvent)
    {
	  if(_callback.cancel()){
		  _done = false;
		  setVisible(false);
	  }
   }
  }


 private class MyListSelectionListener
    implements ListSelectionListener
  {
    public void
    valueChanged( ListSelectionEvent e )
    {
	  Object selectedValue = _suggestionsJList.getSelectedValue();
      if( selectedValue !=null )
      {
		  if(!_changeToTextField.getText().equals(selectedValue)){
			_changeToTextField.setText( (String)selectedValue );
			_suggestAction.setEnabled(false);
		  }
      }
      else
      {
        _suggestionsJList.clearSelection();
      }
    }
  }

  private class SuggestListener implements DocumentListener{
	  public void changedUpdate(DocumentEvent e){}
	  public void insertUpdate(DocumentEvent e){
		  enableSuggest();
	  }
	  public void removeUpdate(DocumentEvent e){
		  enableSuggest();
	  }
	  private void enableSuggest(){
		  String text = _changeToTextField.getText();
		  if(text.length()==0){
			  _suggestAction.setEnabled(false);
			  return;
		  }
		  
		  for(int i = 0; i < _suggestionsModel.getSize(); i++) {
			  if(_suggestionsModel.getElementAt(i).equals(text)){
				  _suggestAction.setEnabled(false);
				  _suggestionsJList.setSelectedIndex(i);
				  _suggestionsJList.ensureIndexIsVisible(i);
				  return;
			  }
		  }
		  _suggestAction.setEnabled(true);
	  }
  }
  
  public static interface Callback{
	public Result add()throws SpellException;
	
	public Result change(String newWord)throws SpellException;
	public Result changeAll(String newWord)throws SpellException;
	
	public Result ignore()throws SpellException;
	public Result ignoreAll()throws SpellException;
	
	public Result suggest(String newWord)throws SpellException;

	public Result previous()throws SpellException;
	public boolean cancel();
	public void done();
	
	public boolean hasPrevious();
	public boolean hasIgnored();
}
}
