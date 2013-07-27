/*
 * AutoComplete.java
 * $$id$$
 * author Jakub (Kuba) Holy, 2005
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

 ///////////////////////////////////////////////////////////////////////////////
 // TO BE DONE:
 // - handle backspace (=> a new list of completions if appropriate, ...)
 // - detach calls attach
 // - +fce rememberWord( word ) ^ uzij v update, +get/setTheListener,
 //   +get/setWordList
 // - Pref.Mng. - upozorni ze getIsWordFilter se uziva jen pri inic.
 //   Add Pref.Mngr.isParseBufferEnabled()
 // - +option: case-insensitive completion search (podpora toho uz existuje)
 // - enhancement: let the user define regions that are ignored / only considered
 //		( such as comments in a source code )
 // - log start, attach (+buffer), detach(-"-)
 // - enable the user to remove a word from the list directly via the current pop-up window
 // - add 'word blacklist' - words to never remember
package net.jakubholy.jedit.autocomplete;

//{{{ Imports

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.jakubholy.jedit.autocomplete.WordTypedListener.Filter;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.syntax.KeywordMap;
import org.gjt.sp.jedit.syntax.ParserRuleSet;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;


//}}}

/**
 * Try to complete a word typed in the associated buffer.
 * Automatically try to find any completions for the current
 * word being typed and if there're any, present them to the user
 * in a pop-up list. A list of possible completions is
 * constructed from words typed so far in the buffer that
 * satisfy some conditions.
 *
 * The class observers an {@link WordTypedListener}
 * to get notified of word events such as a word
 * started/finished/invalidated (when the user
 * jumps to another location etc.).
 *
 * @see net.jakubholy.jedit.autocomplete.PreferencesManager
 * @see net.jakubholy.jedit.autocomplete.WordTypedListener
 * @see net.jakubholy.jedit.autocomplete.CompletionPopup
 *
 * Based on jEdit's CompleteWord feature.
*/
public final class AutoComplete //{{{ // TODO: Rename to AutoCompleteController
implements java.util.Observer
{

	/////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Holds an instance of AutoComplete for each buffer that has got one.
	 */
	static Map<Buffer, AutoComplete> bufferToAutoComplete =
		new IdentityHashMap<Buffer, AutoComplete>();

	/**
	 * Create a new AutoComplete that starts working for the given buffer if the buffer has none.
	 * If the buffer already has an AutoComplete, it's only returned.
	 */
	public static AutoComplete createAutoCompleteAction( final Buffer buffer )
	{
		Pattern filter = PreferencesManager.getPreferencesManager().getFilenameFilterPattern();
		if (filter != null) {
			String path = buffer.getPath();
			boolean match = filter.matcher(path).matches();
			if (match == PreferencesManager.getPreferencesManager().isExclusionFilter())
				return null;
		}
		AutoComplete autoComplete = getAutoCompleteOfBuffer(buffer);
		if(autoComplete == null)
		{
			autoComplete = new AutoComplete(buffer);
			bufferToAutoComplete.put(buffer, autoComplete);
		}
		else
		{
			if( autoComplete.getBuffer() == null || autoComplete.getBuffer() != buffer )
			{ autoComplete.attach(buffer); }
		}
		return autoComplete;
	}

	/**
	 * Attach an AutoComplete to the buffer.
	 * It either re-attaches an existing detached one ore creates a new one.
	 * If the buffer already has an AutoComplete attached to it, it's detached and re-attached.
	 * @return The [new] buffer's AutoComplete
	 */
	public static AutoComplete attachAction( Buffer buffer )
	{ return createAutoCompleteAction(buffer); }

	/**
	 * Detach the AutoCompletion from the given buffer if it has one.
	 * But we keep the AutoComplete for the buffer and it can be later re-attached.
     * Word list is cleared.
     */
	public static void detachAction( Buffer buffer )
	{
		AutoComplete autoComplete = getAutoCompleteOfBuffer(buffer);
		if(autoComplete == null || autoComplete.getBuffer() == null)
		{
			String bufferName = (buffer == null)? "null" : buffer.getName();
			Log.log(Log.DEBUG, TextAutocompletePlugin.class, "DETACH action: doing nothing - no AutoComplete " +
					"attached to the current buffer ["+bufferName+"].");
		}
		else
		{ autoComplete.detach(); }
	}

	/**
	 * Parse the current buffer and add all words in it to the list.
	 * @throws ActionException When the action cannot be performed for some reason
	 */
	public static void parseAction( Buffer buffer ) throws ActionException
	{
		AutoComplete autoComplete = getAutoCompleteOfBuffer(buffer);
		if(autoComplete == null || autoComplete.getBuffer() == null)
		{
			String title = jEdit.getProperty("textautocomplete-buffer-parse.label", "Parse buffer");
			if( askToAttach(buffer, title) )
			{ parseAction( buffer ); }
		}
		else
		{ autoComplete.parseBuffer(); }
	}

	/**
	 * Forget all words in the completions list including those from the buffer
	 * and those from other sources such as the default word list.
	 * @throws ActionException When the action cannot be performed for some reason
	 */
	public static void forgetAllWordsAction( Buffer buffer ) throws ActionException
	{
		AutoComplete autoComplete = getAutoCompleteOfBuffer(buffer);
		if(autoComplete != null && autoComplete.getBuffer() != null)
		{ autoComplete.wordList.clear(); }
	}

	/**
	 * Show the WordList Editor user interface to edit words remembered for the current buffer.
	 * @throws ActionException When the action cannot be performed for some reason
	 */
	public static void showWordsAction( Buffer buffer ) throws ActionException
	{
		final AutoComplete autoComplete = getAutoCompleteOfBuffer(buffer);
		if(autoComplete == null || autoComplete.getBuffer() == null)
		{
			String title = jEdit.getProperty("textautocomplete-show_words.label", "Show remembered words");
			if( askToAttach(buffer, title) )
			{ showWordsAction( buffer ); }
		}
		else
		{
			java.awt.EventQueue.invokeLater(new Runnable() {
	            public void run() {
	                new WordListEditorUI(autoComplete).setVisible(true);
	            }
	        });
		}
	}

	/**
	 * Return the AutoComplete associated with the buffer or null if
	 * no AutoComplete has been attached to the buffer so far.
	 * The AutoComplete doesn't need to be currently attached to the
	 * buffer if it has been detached recently - check that its method
	 * {@link #getBuffer()} doesn't return null and that it matches the
	 * buffer.
	 *
	 * @param buffer The buffer whose AutoComplete you want to access.
	 * @return AutoComplete associated (not necessarily attached)
	 * with the buffer or null.
	 */
	public static AutoComplete getAutoCompleteOfBuffer(Buffer buffer)
	{ return (AutoComplete) bufferToAutoComplete.get(buffer); }


	/**
	 * Ask the user whether an Autocomplete should be attached to the
	 * buffer and if yes, do it.
	 *
	 * @param buffer The buffer to which to attach
	 * @param title Appended to the title of the dialog box
	 * @return True if the user has agreed to start an Autoc. for the buffer
	 * and the Autoc. has been started.
	 */
	private static boolean askToAttach(Buffer buffer, String title)
	{
		int answer = JOptionPane.showConfirmDialog(
				jEdit.getActiveView(),
				"No Autocomplete attached to the buffer.\n" +
					"Should I start one first?",
				"TextAutocomplete - " + title,
		        JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);
		if(answer == JOptionPane.OK_OPTION)
		{
			attachAction( buffer );	// attach to the buffer
			return true;
		}
		return false;
	}

	/**
	 * Attach AutoComplete to all opened buffers.
	 * @throws ActionException When the action cannot be performed for some reason
	 */
	public static void attachToAllBuffersAction() throws ActionException
	{
		Log.log(Log.DEBUG, TextAutocompletePlugin.class, "Attaching to all buffers.");
		Buffer[] openedBuffers = jEdit.getBuffers();
		for (int i = 0; i < openedBuffers.length; i++) {
			attachAction( openedBuffers[i] );
		}
	}

	/**
	 * Save the list of remembered words to the user supplied file.
	 * @throws IOException
	 */
	public void exportWordList() throws IOException {
		// exportWordList
		// Get the destination file
		VFSFileChooserDialog fileDialog = new VFSFileChooserDialog(
				jEdit.getActiveView(),
				buffer.getDirectory(),
				VFSBrowser.SAVE_DIALOG,
				false); // false for non-multiple selections

		String[] files = fileDialog.getSelectedFiles();

		if (files != null && files.length == 1)
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(files[0]));
			Completion[] words = getWordList().getAllWords();
			for (int i = 0; i < words.length; i++)
			{
				out.write( words[i].getWord() );
				out.newLine();
			}
			out.close();
		}
		else
		{
			Log.log(Log.DEBUG, TextAutocompletePlugin.class, "exportWordList: Doing " +
					"nothing, no/too many input file(s) selected. ["+files+"]");
		}
	}

	/**
	 * Read words from a file supplied
	 * by the user and add them to those
	 * remembered for this buffer.
	 * <p>
	 * See {@link #importWordList(Reader)} for details
	 * on the file format.
	 *
	 * @return Count of the imported words.
	 * @throws MaxWordsExceededException When the maximal number of
     * remembered words have already been reached.
     * @throws FileNotFoundException The import file couldn't be found
	 * @throws IOException Exception when trying to read the file
	 */
	public int importWordList() throws MaxWordsExceededException, FileNotFoundException, IOException
	{
		// Get the file with words
		VFSFileChooserDialog fileDialog = new VFSFileChooserDialog(
				jEdit.getActiveView(),
				buffer.getDirectory(),
				VFSBrowser.OPEN_DIALOG,
				false); // false for non-multiple selections

		String[] files = fileDialog.getSelectedFiles();
		int lineCount = 0;

		if(files != null && files.length == 1)
		{

			final Reader wordListReader = new FileReader(files[0]);
			lineCount = importWordList(wordListReader);
		}
		else
		{
			Log.log(Log.DEBUG, TextAutocompletePlugin.class, "importWordList: Doing " +
				"nothing, no/too many input file(s) selected. ["+files+"]");
		}

		return lineCount;
	}


	/**
	 * Read words via the provided
	 * Reader and add them to those
	 * remembered for this buffer.
	 * <p>
	 * The file must contain one word
	 * per line. We do not check that
	 * they're words, remove white
	 * spaces or anything like that - we
	 * import them as they are. Only rows that contain
	 * nothing but whitespaces are ignored.
	 * <p>
	 * Note: The <code>wordListReader</code> is closed at the end.
	 *
	 * @param wordListReader A reader (e.g. FileReader) for reading the word list.
	 * Note: it's closed at the end.
	 * @return Count of the imported words.
	 * @throws MaxWordsExceededException When the maximal number of
     * remembered words have already been reached.
	 * @throws IOException Exception when trying to read the file
	 */
	public int importWordList(final Reader wordListReader) throws IOException,
			MaxWordsExceededException {
		int lineCount = 0;
		final BufferedReader in = new BufferedReader(wordListReader);
		String line;
		while ((line = in.readLine()) != null)
		{
			if(line.trim().length() != 0)	// ignore empty lines
			{
				rememberWord(line);
				lineCount++;
			}
		}
		in.close();

		Log.log(Log.DEBUG, TextAutocompletePlugin.class, "importWordList: Number of lines ('words') read: " + lineCount);
		return lineCount;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Detach and destroy (forget) all instance of AutoComplete.
	 */
	public static void destroyAllAutoCompletes()
	{
		Log.log(Log.DEBUG, TextAutocompletePlugin.class, "Detaching and destroying all autocompletes...");
		for (Iterator<AutoComplete> acIter =
				bufferToAutoComplete.values().iterator(); acIter.hasNext();)
		{
			AutoComplete autoComplete = (AutoComplete) acIter.next();
			autoComplete.detach();
			if(autoComplete.getBuffer() != null)
			{ Log.log(Log.DEBUG, TextAutocompletePlugin.class, "DESTROY ALL: removing autocomplete for the buffer "
					+ autoComplete.getBuffer().getPath() ); }
			acIter.remove();
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////

    // {{{ constructor
	/** Create a new AutoComplete that start's working for the given buffer. */
    AutoComplete( Buffer buffer )
    {
        Log.log(Log.DEBUG, TextAutocompletePlugin.class, "CREATED ");
//        this.thePopup 	= new CompletionPopup( jEdit.getActiveView() );
        //this.view 		= view;
        //this.buffer 	= buffer;
        //this.textArea 	= view.getTextArea();
        m_wordTypedListener = new WordTypedListener();
        m_wordTypedListener.addObserver( this );
        m_wordTypedListener.setCheckIsWord( prefManager.getIsWordFilter() );	// pass reference to Pref.Mngr's word filter, don't copy!
        //this.buffer.addBufferChangeListener( m_wordTypedListener );

        this.attach(buffer);
    } // constructor }}}

	///////////////////////////////////////////////////////////////////////////
    // {{{ detach
    /** Detach the AutoCompletion from the buffer it's bound to == stop working.
     * Word list is emptied. */
    public void detach()
    {
    	if (buffer != null) {
			Log.log(Log.DEBUG, TextAutocompletePlugin.class, "Detaching from the buffer: " + buffer);
			buffer.removeBufferListener(m_wordTypedListener);
			disposePopupIfVisible();
			wordList.clear();
			buffer = null;
		}
    	else
    	{ Log.log(Log.DEBUG, TextAutocompletePlugin.class, "detach: Doing nothing - not attached (the buffer is null)"); }
    } // detach }}}

	///////////////////////////////////////////////////////////////////////////
    // {{{ attach
    /** Attach the AutoCompletion to the buffer == start completing there.
     * The buffer is parsed for words to remember as possible completions. */
    public void attach( Buffer buffer )
    {
        // If attached already detach first
        if ( this.buffer != null ) { detach(); }

        Log.log(Log.DEBUG, TextAutocompletePlugin.class, "Attaching to the buffer: " + buffer);
    	this.buffer = buffer;
    	buffer.addBufferListener( m_wordTypedListener );

    	// Initialize the word list
        parseBuffer();
        importBufferDefaultWordList(buffer);
        loadBufferModeKeywords(buffer);

    } // attach }}}

	/**
	 * Import words from a default word list appropriate for the give buffer if
	 * there is any such list.
	 * <p>
	 * It's error-safe: Exceptions are catched and just logged not to make this plugin unusable
	 * because of problems with default word lists.
	 *
	 * @param buffer The buffer to try to read default words for.
	 * @return Number of imported words.
	 */
	int importBufferDefaultWordList(Buffer buffer) {
		final URL defaultWordListUrl = prefManager.getDefaultWordListForBuffer(buffer.getName(), true);
		int countImportedWords = 0;

        if (defaultWordListUrl != null)
        {
        	try {
				countImportedWords = importWordList(new InputStreamReader( defaultWordListUrl.openStream() ));
				Log.log(Log.NOTICE, TextAutocompletePlugin.class
						, countImportedWords + " words imported from the default word list '" +
						defaultWordListUrl + "' for the buffer '" + buffer + "'");
        	} catch (FileNotFoundException e) {
				Log.log(Log.DEBUG, TextAutocompletePlugin.class, "importBufferDefaultWordList('" + buffer +
						"'): The default word list '" + defaultWordListUrl +
						"' doesn't exist, not loading anything.");
        	} catch (Exception e) {
				Log.log(Log.ERROR, TextAutocompletePlugin.class, "importBufferDefaultWordList('" + buffer +
						"'): Failed to import the default word list '" + defaultWordListUrl +
						"' because of: " + e);
			}
        }

        return countImportedWords;
	} /* importBufferDefaultWordList */

	///////////////////////////////////////////////////////////////////////////
	// {{{ update
	/* Process word events w.r.t. their types.
	 * @param o = sender of the message (arg). The observer may observe more objects. */
    /**
     * @inheritDoc
     * @see WordTypedListener
     */
	public void update(java.util.Observable observable, Object arg )
	{
		if(!(arg instanceof WordTypedEvent))
		{ return; }

		WordTypedEvent event = (WordTypedEvent)arg;
		int type = event.type;
		this.thePrefix = event.getWord().toString();

		switch(type)
		{
			case WordTypedEvent.AT_START:
				// Nothing, 1-letter words are too short
				break;
			case WordTypedEvent.INSIDE:
			    // Offer a completition
			    if ( thePrefix.length() >= prefManager.minPrefixLength() ) { displayCompletionPopup(); }
				break;
			case WordTypedEvent.AT_END:
			    // Remember the word & hide the popup
			    if ( prefManager.isWordToRemember(thePrefix) )
			    {
					// TODO: (?) assert that prefix == the word before the caret(we have: "prefix|")
                    // wordList.add( new Completion(thePrefix) );
			    	rememberWordSilent(thePrefix);
			    }
			    disposePopupIfVisible();
				break;
			case WordTypedEvent.RESET:
			    // Hide the popup
				disposePopupIfVisible();
				break;
			case WordTypedEvent.TRUNCATED:
			    // Offer a completion
			    if ( thePrefix.length() >= prefManager.minPrefixLength() )
			    { displayCompletionPopup(); }
			    else
			    { disposePopupIfVisible(); }
				break;
			default:
				Log.log( Log.ERROR, TextAutocompletePlugin.class, "AutoComplete.update: " +
						"unknown WordTypedEvent type: " +type + ", the event:" + event );
			// unknown type == an error           break;
		}//switch
	}//update }}}

	/**
	 * Hide/destroy the popup if it is visible.
	 */
	private void disposePopupIfVisible() {
		if ( thePopup != null ) {
	    	thePopup.dispose();
	    	thePopup = null;
	    }
	}

	//////////////////////////////////////////////////////////////////////
    //	{{{ completeWord() method
	/** Display a pop-up window with possible completions for
	 * the word being typed.
	 */
	protected void displayCompletionPopup()
	{
		// TODO: (?) assert jEdit.getActiveView().getBuffer() == myBuffer
        final JEditTextArea textArea = jEdit.getActiveView().getTextArea();

        Log.log(Log.DEBUG, TextAutocompletePlugin.class, "displayCompletionPopup: entry");

		/* If the buffer isn't editable the user doesn't enter
		 * a character and thus the pop-up will never show up.
		if(!buffer.isEditable())
		{
			textArea.getToolkit().beep();
			return;
		}
		*/

		// Get all possible completions
		Completion[] completions = getCompletions( thePrefix );

		//
		// Handle the number of completions
		//
		if ( completions.length >= 1 )
		{

			if ( thePopup == null ) {
				// Create

				textArea.scrollToCaret(false);
				Point location = textArea.offsetToXY(
						textArea.getCaretPosition() - thePrefix.length());
				location.y += textArea.getPainter().getFontMetrics()
					.getHeight();

				SwingUtilities.convertPointToScreen(location,
					textArea.getPainter());

				this.thePopup 	= new CompletionPopup( jEdit.getActiveView() , location );
				//  Display the popup

				thePopup.setWord( thePrefix );
			    thePopup.showCompletions( getCompletions( thePrefix ) );
			    Log.log(Log.DEBUG, TextAutocompletePlugin.class, "displayCompletionPopup: popup displayed");
			} else {
			    // The pop-up is already visible => update it only
				thePopup.setWord( thePrefix );
			    thePopup.showCompletions( getCompletions( thePrefix ) );
			    Log.log(Log.DEBUG, TextAutocompletePlugin.class, "displayCompletionPopup: an already displayed popup updated");
			} // if-else pop-up visible

		}
		else
		{
			disposePopupIfVisible();
		}
	} // displayCompletionPopup }}}

	//////////////////////////////////////////////////////////////////////////////////
	/** Get all possible completion for the given prefix. */
	protected Completion[] getCompletions( String prefix )
	{
	    // TODO: (low) If the pop-up is already showing completions and only
        // the user has typed 1 more char we don't need to acces
        // the word list, instead we could work on the list we have already
        // TODO: (low) Add 'greatest common part' completion if the user desires it
        //  Ex.: prefix = pr, completions = prachovka, prachy => create the completion
        // 'prach' and add it to the front of the list. Use System.arraycopy.
        return wordList.getCompletions(prefix);
	    /*
        return new Completion[]{ new Completion("dummy10"), new Completion("dummy20"),
	    		new Completion("dum"), new Completion("dudak") };
        */
	}

	////////////////////////////////////////////////////////////////////////////////////
	//			FIELDS
    ////////////////////////////////////////////////////////////////////////////////////
	/** The prefix to complete/typed so far. */
	private String thePrefix = "";
	/** A completion pop-up window; it's only set when displayed (otherwise null). */
	private ITextAutoCompletionPopup thePopup;
	//private View view;
	/** The buffer this AutoComplete is attached to. */
	private Buffer buffer = null;
	//private JEditTextArea textArea;
    private WordTypedListener m_wordTypedListener;
    private PreferencesManager prefManager = PreferencesManager.getPreferencesManager();
    /** Place to store words typed so far & to use as completions. */
    private WordList wordList = new WordListTreeSet();

	//////////////////////////////////////////
    /** The prefix to complete/typed so far. */
    public String getThePrefix() {
        return thePrefix;
    }

    /** The prefix to complete/typed so far. */
    public void setThePrefix(String thePrefix) {
        this.thePrefix = thePrefix;
    }
    /** Set the list of words used to find completions. */
    public WordList getWordList() {
        return wordList;
    }
    /** Get the list of words used to find completions. */
    public void setWordList(WordList wordList) {
        this.wordList = wordList;
    }
    /** Remove the given word from the list of remembered words.
     * @return True if the word was in the list & is removed. */
    public boolean forgetWord(String word2remove) {
        return wordList.remove( new Completion(word2remove) );
    }

    /**
     * Add the given word to the list of remembered words.
     * @param word2remember The word/string to remember
     * @param silent If true don't throw MaxWordsExceededException,
     * 	only do nothing and return false
     * @return True if the word has been added to the list, false if it
     * hasn't been added because either it was already there or the max.
     * number of words to remember is already reached and silent is true.
     * @throws MaxWordsExceededException When the maximal number of
     * remembered words have already been reached - unless silent is true.
     */
    public boolean rememberWord(String word2remember, boolean silent)
    throws MaxWordsExceededException
    {
    	int maxCount = prefManager.getMaxCountOfWords();
    	if( wordList.size() < maxCount )
        {
    		boolean isAdded = wordList.add( new Completion(word2remember) );
    		// Log
    		if(isAdded && wordList.size() == maxCount)
    		{
    			Log.log(Log.NOTICE, TextAutocompletePlugin.class,
    					"AutoComplete.rememberWord: the max. number of remembered words" +
    					"("+maxCount+") has just been reached for the buffer '"+
    					buffer.getName() + "'. No more words will be" +
    					"further remembered unless some is deleted/the limit changes.");
    		}
    		return isAdded;
    	}
    	else if( !silent )
    	{ throw new MaxWordsExceededException(word2remember); }
    	else
    	{ return false; }
    }

    /**
     * As {@link #rememberWord(String, boolean)} but silent == false.
     * (Throw an exception when the max. number of words is exceeded.)
     */
    public boolean rememberWord(String word2remember)
    throws MaxWordsExceededException
    { return rememberWord(word2remember, false); }

    /**
     * As {@link #rememberWord(String, boolean)} but silent == true.
     * (Never throw an exception when the max. number of words is exceeded.)
     */
    public boolean rememberWordSilent(String word2remember) {
		try
		{ return rememberWord(word2remember, true); }
		catch (MaxWordsExceededException e)
		{ return false; }
	}

    /** A concatenation of all rememberd words.
     * @return A concatenation of all rememberd words separated by ','. */
    public String getWords() {
        Completion[] ca = wordList.getAllWords();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ca.length; i++) {
            sb.append( ca[i] + ",");
        }
        return sb.toString();
    }

    /** Parse the current buffer and add all words in it to the list. */
    public void parseBuffer()
    {
        int length = buffer.getLength();
        Filter checkIsWord = m_wordTypedListener.getCheckIsWord();
        String insertion = null;
        StringBuffer word = new StringBuffer(15);

        // Simulate typing the buffer char by char
        for (int offset = 0; offset < length; offset++) {

            insertion = buffer.getText( offset, 1 );
            if ( checkIsWord.accept( word, insertion.charAt(0) ) )
            { word.append(insertion); }
            else
            {
                if ( prefManager.isWordToRemember(word.toString()) )
                { rememberWordSilent( word.toString() ); }
                word.setLength(0);
            } // if-else is a word character

        } // for all characters of the buffer

        // Remember the last word followed by EOF if there is any.
        if ( prefManager.isWordToRemember(word.toString()) )
        { rememberWordSilent( word.toString() ); }
    }

    /**
     * Return the buffer this AutoComplete is attached to.
     * @return The buffer this AutoComplete is attached to or null if not attached.
     */
	public Buffer getBuffer() {
		return buffer;
	}

	/**
	 * Load keywords from the buffer's syntax highlighting mode into
	 * the buffer's word list. Does nothing unless {@link PreferencesManager#isLoadModeKeywords()}
	 * true.
	 * This is useful when using TextAutocomplete for writing source code.
	 * The mode are stored in files like $JEDIT_HOME/modes/php.xml.
	 * @param buffer (required) the target buffer
	 * @since 0.9.9
	 */
	private void loadBufferModeKeywords(Buffer buffer)
	{
		if (this.prefManager.isLoadModeKeywords()) {
			final Collection<String> collectedKeywords = new LinkedList<String>();
			final StringBuilder noWordSeparators = new StringBuilder();

			// Extract keyword sources
			final ParserRuleSet mainRuleSet = buffer.getTokenMarker().getMainRuleSet();
			final ParserRuleSet[] modeRuleSets =
				this.prefManager.isLoadMainModeOnly()?
					((mainRuleSet != null)? new ParserRuleSet[]{ mainRuleSet } : new ParserRuleSet[0])
					: buffer.getTokenMarker().getRuleSets();
			final int editModesCount = modeRuleSets.length;
			final int mainModeKeywordsCount =
				(mainRuleSet != null && mainRuleSet.getKeywords() != null)?
					mainRuleSet.getKeywords().getKeywords().length : 0;

			// Extract keywords, non-word separators
			for(ParserRuleSet ruleSet: modeRuleSets)
			{
				final KeywordMap keywordMap = ruleSet.getKeywords();
				if (keywordMap != null)
				{
					Collections.addAll(collectedKeywords, keywordMap.getKeywords());
					if (ruleSet.getNoWordSep() != null)
					{
						noWordSeparators.append( ruleSet.getNoWordSep() );
					}
				}
			}

			// Sanitize noWordSeparators
			{
				final String originalNoWordSeparators = noWordSeparators.toString();
				noWordSeparators.setLength(0);
				for (int i = 0; i < originalNoWordSeparators.length(); i++) {
					final String noSeparator = originalNoWordSeparators.substring(i, i+1);
					if (noWordSeparators.indexOf(noSeparator) == -1) {
						noWordSeparators.append( noSeparator );
					}
				}
			}

			// Store them
			{
				this.prefManager.setNoWordSeparators( noWordSeparators.toString() );

				// Log
				StringBuilder keywordList = new StringBuilder();
				for (String keyword : collectedKeywords)
				{
					rememberWordSilent(keyword);
					keywordList.append(keyword).append(" ");
				}

				final String modeName = (buffer.getMode() != null)? buffer.getMode().getName() : "<undefined>";
				Log.log(Log.DEBUG, TextAutocompletePlugin.class, "loadBufferModeKeywords: " + collectedKeywords.size() +
						" keywords extracted from " + editModesCount + " edit sub-modes with " +
						mainModeKeywordsCount + " from the main mode for " +
						"the buffer '" + buffer.getName() + "': " + keywordList);
				Log.log(Log.DEBUG, TextAutocompletePlugin.class, "loadBufferModeKeywords: no word separators extracted from the " +
						"mode " + modeName + " of the buffer '" + buffer.getName() + "': " + noWordSeparators);
			}
		} else {
			Log.log(Log.DEBUG, TextAutocompletePlugin.class, "loadBufferModeKeywords: doing nothing, loading mode's keywords " +
					"disabled in preferences.");
		}

	} /* loadBufferModeKeywords */

} // AutoComplete }}} **********************************************************
