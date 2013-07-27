/*
 * XSearchAndReplace.java - Search and replace: derived from SearchAndReplace
 * :tabSize=2:indentSize=2:noTabs=false:
 * :folding=explicit:collapseFolds=5:
 *
 * Copyright (C) 2002 Rudolf Widmann
 * Portions copyright (C) 1999, 2000, 2001, 2002 Slava Pestov
 * Portions copyright (C) 2001 Tom Locke
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

// package org.gjt.sp.jedit.search;

//{{{ Imports
import javax.swing.text.Segment;
import javax.swing.JOptionPane;
import java.awt.Component;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.msg.SearchSettingsChanged;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.search.*;
import org.gjt.sp.util.Log;
//}}}

/**
 * Class that implements regular expression and literal search within
 * jEdit buffers.
 * @author Slava Pestov
 * @version $Id: XSearchAndReplace.java 1466 2002-08-16 10:59:15Z  $
 * derived version $Id: XSearchAndReplace.java 1466 2002-08-16 10:59:15Z  $
 */
public class XSearchAndReplace
{
	public static boolean debug=false; // debug-flag, may be modified by bsh
	//{{{ Getters and setters

	//{{{ setSearchString() method
	/**
	 * Sets the current search string.
	 * If wordpart search activ, a regexp is constructed
	 * the original string is saved in origSearch
	 * @param search The new search string
	 */
	public static void setSearchString(String search)
	{
if (debug) Log.log(Log.DEBUG, BeanShell.class,"XSearchAndReplace.60: search = "+search+", origSearch = "+origSearch+", XSearchAndReplace.search = "+XSearchAndReplace.search);
		boolean searchSettingsChanged = false;
		
		if(!search.equals(origSearch)) {
			origSearch = search;
			searchSettingsChanged = true;
		}
		if (wordPart == XSearchDialog.SEARCH_PART_NONE) {
			if(!search.equals(XSearchAndReplace.search)) {
				XSearchAndReplace.search = search;
				searchSettingsChanged = true;
			}
		} else {
			String regExpString=null;
			switch (wordPart) {
				case XSearchDialog.SEARCH_PART_WHOLE_WORD:
					regExpString = "\\<"+MiscUtilities.charsToEscapes(search)+"\\>";
					break;
				case XSearchDialog.SEARCH_PART_PREFIX:
					regExpString = "\\<"+MiscUtilities.charsToEscapes(search);
					break;
				case XSearchDialog.SEARCH_PART_SUFFIX:
					regExpString = MiscUtilities.charsToEscapes(search)+"\\>";
					break;
			}
			if(!regExpString.equals(XSearchAndReplace.search)) {
				XSearchAndReplace.search = regExpString;
				searchSettingsChanged = true;
			}
		}
if (debug) Log.log(Log.DEBUG, BeanShell.class,"XSearchAndReplace.89: origSearch = "+origSearch+", XSearchAndReplace.search = "+XSearchAndReplace.search+", searchSettingsChanged = "+searchSettingsChanged);
		if (searchSettingsChanged) {
			matcher = null;
			EditBus.send(new SearchSettingsChanged(null));
		}
	} //}}}

	//{{{ getSearchString() method
	/**
	 * Returns the current search string.
	 */
	public static String getSearchString()
	{
		return origSearch;
	} //}}}

	//{{{ setReplaceString() method
	/**
	 * Sets the current replacement string.
	 * @param replace The new replacement string
	 */
	public static void setReplaceString(String replace)
	{
		if(replace.equals(XSearchAndReplace.replace))
			return;

		XSearchAndReplace.replace = replace;
		matcher = null;

		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ getReplaceString() method
	/**
	 * Returns the current replacement string.
	 */
	public static String getReplaceString()
	{
		return replace;
	} //}}}

	//{{{ setIgnoreCase() method
	/**
	 * Sets the ignore case flag.
	 * @param ignoreCase True if searches should be case insensitive,
	 * false otherwise
	 */
	public static void setIgnoreCase(boolean ignoreCase)
	{
		if(ignoreCase == XSearchAndReplace.ignoreCase)
			return;

		XSearchAndReplace.ignoreCase = ignoreCase;
		matcher = null;

		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ getIgnoreCase() method
	/**
	 * Returns the state of the ignore case flag.
	 * @return True if searches should be case insensitive,
	 * false otherwise
	 */
	public static boolean getIgnoreCase()
	{
		return ignoreCase;
	} //}}}

	//{{{ setRegexp() method
	/**
	 * Sets the state of the regular expression flag.
	 * @param regexp True if regular expression searches should be
	 * performed
	 */
	public static void setRegexp(boolean regexp)
	{
		if(regexp == XSearchAndReplace.regexp)
			return;

		XSearchAndReplace.regexp = regexp;
		if(regexp && reverse)
			reverse = false;

		matcher = null;

		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ getRegexp() method
	/**
	 * Returns the state of the regular expression flag.
	 * @return True if regular expression searches should be performed
	 */
	public static boolean getRegexp()
	{
		return regexp;
	} //}}}

	//{{{ setReverseSearch() method
	/**
	 * Sets the reverse search flag. Note that currently, only literal
	 * reverse searches are supported.
	 * @param reverse True if searches should go backwards,
	 * false otherwise
	 */
	public static void setReverseSearch(boolean reverse)
	{
		if(reverse == XSearchAndReplace.reverse)
			return;

		XSearchAndReplace.reverse = (regexp ? false : reverse);

		matcher = null;

		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ getReverseSearch() method
	/**
	 * Returns the state of the reverse search flag.
	 * @return True if searches should go backwards,
	 * false otherwise
	 */
	public static boolean getReverseSearch()
	{
		return reverse;
	} //}}}

	//{{{ resetIgnoreFromTop() method
	/**
	 * Resets ignoreFromTop flag
		 Note: ignoreFromTop flag is set, when find was invoked
		       This allowes "refind" without resetting "fromTop" manually
					 if any search settings change, ignoreFromTop flag shall be reset
	 */
	public static void resetIgnoreFromTop()
	{
		ignoreFromTop = false;
	} //}}}

	//{{{ setSearchFromTop() method
	/**
	 * Sets the reverse search flag. Note that currently, only literal
	 * reverse searches are supported.
	 * @param reverse True if searches should go backwards,
	 * false otherwise
	 */
	public static void setSearchFromTop(boolean fromTop)
	{
		if(fromTop == XSearchAndReplace.fromTop)
			return;

		XSearchAndReplace.fromTop = fromTop;

		matcher = null;

		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ getSearchFromTop() method
	/**
	 * Returns the state of the reverse search flag.
	 * @return True if searches should go backwards,
	 * false otherwise
	 */
	public static boolean getSearchFromTop()
	{
		return fromTop;
	} //}}}

	//{{{ setBeanShellReplace() method
	/**
	 * Sets the state of the BeanShell replace flag.
	 * @param regexp True if the replace string is a BeanShell expression
	 * @since jEdit 3.2pre2
	 */
	public static void setBeanShellReplace(boolean beanshell)
	{
		if(beanshell == XSearchAndReplace.beanshell)
			return;

		XSearchAndReplace.beanshell = beanshell;
		matcher = null;

		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ getBeanShellReplace() method
	/**
	 * Returns the state of the BeanShell replace flag.
	 * @return True if the replace string is a BeanShell expression
	 * @since jEdit 3.2pre2
	 */
	public static boolean getBeanShellReplace()
	{
		return beanshell;
	} //}}}

	//{{{ setAutoWrap() method
	/**
	 * Sets the state of the auto wrap around flag.
	 * @param wrap If true, the 'continue search from start' dialog
	 * will not be displayed
	 * @since jEdit 3.2pre2
	 */
	public static void setAutoWrapAround(boolean wrap)
	{
		if(wrap == XSearchAndReplace.wrap)
			return;

		XSearchAndReplace.wrap = wrap;

		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ getAutoWrap() method
	/**
	 * Returns the state of the auto wrap around flag.
	 * @param wrap If true, the 'continue search from start' dialog
	 * will not be displayed
	 * @since jEdit 3.2pre2
	 */
	public static boolean getAutoWrapAround()
	{
		return wrap;
	} //}}}

	//{{{ setSearchMatcher() method
	/**
	 * Sets the current search string matcher. Note that calling
	 * <code>setSearchString</code>, <code>setReplaceString</code>,
	 * <code>setIgnoreCase</code> or <code>setRegExp</code> will
	 * reset the matcher to the default.
	 */
	public static void setSearchMatcher(SearchMatcher matcher)
	{
		XSearchAndReplace.matcher = matcher;

		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ getSearchMatcher() method
	/**
	 * Returns the current search string matcher.
	 * @exception IllegalArgumentException if regular expression search
	 * is enabled, the search string or replacement string is invalid
	 */
	public static SearchMatcher getSearchMatcher()
		throws Exception
	{
		return getSearchMatcher(true);
	} //}}}

	//{{{ getSearchMatcher() method
	/**
	 * Returns the current search string matcher.
	 * @param reverseOK Replacement commands need a non-reversed matcher,
	 * so they set this to false
	 * @exception IllegalArgumentException if regular expression search
	 * is enabled, the search string or replacement string is invalid
	 */
	public static SearchMatcher getSearchMatcher(boolean reverseOK)
		throws Exception
	{
		reverseOK &= (fileset instanceof CurrentBufferSet);

//Log.log(Log.DEBUG, BeanShell.class,"tp325: matcher = "+matcher);
		if(matcher != null && (reverseOK || !reverse))
			return matcher;

		if(search == null || "".equals(search))
			return null;

		// replace must not be null
		String replace = (XSearchAndReplace.replace == null ? "" : XSearchAndReplace.replace);

		String replaceMethod;
		if(beanshell && replace.length() != 0)
		{
			replaceMethod = BeanShell.cacheBlock("replace","return ("
				+ replace + ");",true);
		}
		else
			replaceMethod = null;

		if(regexp || wordPart != XSearchDialog.SEARCH_PART_NONE)
			matcher = new RESearchMatcher(search,replace,ignoreCase,
				beanshell,replaceMethod);
		else
		{
			matcher = new BoyerMooreSearchMatcher(search,replace,
				ignoreCase,reverse && reverseOK,beanshell,
				replaceMethod);
		}

		return matcher;
	} //}}}

	//{{{ getMatchedSubexpressions() method
	/**
	 * Return the found subexpressions ico regexp
	 */
	public static String[] getMatchedSubexpressions(String txt) {
		if (debug) Log.log(Log.DEBUG, BeanShell.class,"tp362: matcher = "+matcher);
		if (debug) testMatcher();
		if (matcher instanceof RESearchMatcher) {
			if (debug) Log.log(Log.DEBUG, BeanShell.class,"tp361: ");
			return ((RESearchMatcher)matcher).getSubexpressions(txt);
		}
		else
			return null;
	}

	//{{{ setSearchFileSet() method
	/**
	 * Sets the current search file set.
	 * @param fileset The file set to perform searches in
	 */
	public static void setSearchFileSet(SearchFileSet fileset)
	{
		XSearchAndReplace.fileset = fileset;

		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ setColumnSearchOptions() method
	/**
	 * Sets the column search parameters
	 * @param expandTabs
	 * @param leftColumn
	 * @param rightColumn
	 */
	public static void setColumnSearchOptions(boolean expandTabs, int leftCol, int rightCol)
	{
		XSearchAndReplace.columnSearchEnabled = true;
		XSearchAndReplace.columnSearchExpandTabs = expandTabs;
		XSearchAndReplace.columnSearchLeftCol = leftCol;
		XSearchAndReplace.columnSearchRightCol = rightCol;
	} //}}}
	//{{{ methodes for Column Options
	/**
	 * gets the column option
	 */
	public static boolean getColumnOption()	{
		return columnSearchEnabled;
	} 
	public static boolean getColumnExpandTabsOption()	{
		return columnSearchExpandTabs;
	} 
	public static int getColumnLeftCol()	{
		return columnSearchLeftCol;
	} 
	public static int getColumnRightCol()	{
		return columnSearchRightCol;
	} 
	// resetColumnSearch() method
	/**
	 * Resets the column search parameters
	 */
	public static void resetColumnSearch()	{
		columnSearchEnabled = false;
	} //}}}

	//{{{ set/get CommentOption() method
	/**
	 * Sets the CommentOption search parameters
	 */
	public static void setCommentOption(int commentOption)
	{
		commentSearch = commentOption;
	} 
	// getCommentOption() method
	/**
	 * gets the CommentOption search parameters
	 */
	public static int getCommentOption()
	{
		return commentSearch;
	} //}}}

	//{{{ set/get FoldOption() method
	/**
	 * Sets the FoldOption search parameters
	 */
	public static void setFoldOption(int foldOption)
	{
		XSearchAndReplace.foldSearch = foldOption;
	} 
	// getFoldOption() method
	/**
	 * gets the FoldOption search parameters
	 */
	public static int getFoldOption()
	{
		return foldSearch;
	} //}}}

	//{{{ set/get WordPartOption() method
	/**
	 * Sets the WordPartOption search parameters
	 */
	public static void setWordPartOption(int wordPartOption)
	{
		XSearchAndReplace.wordPart = wordPartOption;
	}
	// getWordPartOption() method
	/**
	 * Sets the WordPartOption search parameters
	 */
	public static int getWordPartOption()
	{
		return wordPart;
	} //}}}

	//{{{ getSearchFileSet() method
	/**
	 * Returns the current search file set.
	 */
	public static SearchFileSet getSearchFileSet()
	{
		return fileset;
	} //}}}

	//}}}

	//{{{ Actions

	//{{{ hyperSearch() method
	/**
	 * Performs a HyperSearch.
	 * @param view The view
	 * @since jEdit 2.7pre3
	 */
	public static boolean hyperSearch(View view)
	{
		return hyperSearch(view,false);
	} //}}}

	//{{{ hyperSearch() method
	/**
	 * Performs a HyperSearch.
	 * @param view The view
	 * @param selection If true, will only search in the current selection.
	 * Note that the file set must be the current buffer file set for this
	 * to work.
	 * @since jEdit 4.0pre1
	 */
	public static boolean hyperSearch(View view, boolean selection)
	{
		record(view,"hyperSearch(view," + selection + ")",false,
			!selection);

		view.getDockableWindowManager().addDockableWindow(
			HyperSearchResults.NAME);
		final HyperSearchResults results = (HyperSearchResults)
			view.getDockableWindowManager()
			.getDockable(HyperSearchResults.NAME);
		results.searchStarted();

		try
		{
			SearchMatcher matcher = getSearchMatcher(false);
			testMatcher();
			if(matcher == null)
			{
				view.getToolkit().beep();
				results.searchDone(0,0);
				return false;
			}

			Selection[] s;
			if(selection)
			{
				s = view.getTextArea().getSelection();
				if(s == null)
				{
					results.searchDone(0,0);
					return false;
				}
			}
			else
				s = null;
			VFSManager.runInWorkThread(new HyperSearchRequest(view,
				matcher,results,s));
			return true;
		}
		catch(Exception e)
		{
			Log.log(Log.ERROR,XSearchAndReplace.class,e);
			Object[] args = { e.getMessage() };
			if(args[0] == null)
				args[0] = e.toString();
			GUIUtilities.error(view,"searcherror",args);
			return false;
		}
	} //}}}

	//{{{ find() method
	/**
	 * Finds the next occurance of the search string.
	 * @param view The view
	 * @return True if the operation was successful, false otherwise
	 */
	public static boolean find(View view)
	{
		// this is the entry for all find calls 
		boolean repeat = false;
		String path = fileset.getNextFile(view,null);
		if(path == null)
		{
			GUIUtilities.error(view,"empty-fileset",null);
			return false;
		}

		try
		{
			SearchMatcher matcher = getSearchMatcher(true);
if (debug) Log.log(Log.DEBUG, BeanShell.class,"XSearchAndReplace.609: matcher = "+matcher+", search = "+search);
			if(matcher == null)
			{
				view.getToolkit().beep();
				return false;
			}

			record(view,"find(view)",false,true);

			view.showWaitCursor();

			Selection[] lastSelection=null;
			int lastCaret = 0;
loop:			for(;;)
			{
				while(path != null)
				{
					Buffer buffer = jEdit.openTemporary(
						view,null,path,false);

					/* this is stupid and misleading.
					 * but 'path' is not used anywhere except
					 * the above line, and if this is done
					 * after the 'continue', then we will
					 * either hang, or be forced to duplicate
					 * it inside the buffer == null, or add
					 * a 'finally' clause. you decide which one's
					 * worse. */
					path = fileset.getNextFile(view,path);

					if(buffer == null)
						continue loop;

					// Wait for the buffer to load
					if(!buffer.isLoaded())
						VFSManager.waitForRequests();

					int start;
					JEditTextArea textArea = view.getTextArea();
					if(view.getBuffer() == buffer && !repeat)
					{
						// rwchg: eval findFromTop
						if (fromTop && !ignoreFromTop) {
							start=0;
							if (debug) Log.log(Log.DEBUG, BeanShell.class,"fromTop invoked");
						}
						else {
							Selection s = textArea.getSelectionAtOffset(
								textArea.getCaretPosition());
							if(s == null)
								start = textArea.getCaretPosition();
							else if(reverse)
								start = s.getStart();
							else
								start = s.getEnd();
						}
						if (debug) Log.log(Log.DEBUG, BeanShell.class,"tp508: start = "+start);
					}
					else if(reverse)
						start = buffer.getLength();
					else
						start = 0;
					if(find(view,buffer,start,repeat)) {
if (debug) Log.log(Log.DEBUG, BeanShell.class,"XSearchAndReplace.674: found at start = "+start+", search = "+search);
						testMatcher();
						ignoreFromTop = true;
						// if "find" was restarted, ask user if wrapping allowed
						if (debug) Log.log(Log.DEBUG, BeanShell.class,"tp591: lastSelection = "+lastSelection);
						if (lastSelection != null) {
							if (textArea.getCaretPosition() != lastCaret) {
								Integer[] args = { new Integer(reverse ? 1 : 0) };
								if (GUIUtilities.confirm(view,
								"keepsearching",args,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
									lastSelection = null;
									return true;
								}
							}
							// reset Selection to former state
							 textArea.setSelection(lastSelection);
							 textArea.moveCaretPosition(lastCaret,true);
						} else return true;
					}
				}

				if(repeat)
				{
					if(!BeanShell.isScriptRunning())
					{
						view.getStatus().setMessageAndClear(
							jEdit.getProperty("view.status.search-not-found"));

						view.getToolkit().beep();
					}
					return false;
				}

				boolean restart;

				if(BeanShell.isScriptRunning())
				{
					restart = true;  // this leads to endless loops in bs-scripts when scanning a file
				}
				else if(wrap)
				{
					view.getStatus().setMessageAndClear(
						jEdit.getProperty("view.status.auto-wrap"));
					// beep if beep property set
					if(jEdit.getBooleanProperty("search.beepOnSearchAutoWrap"))
					{
						view.getToolkit().beep();
					}
					restart = true;
				}
				else
				{
					if (fromTop) {
						// rwchg: don't ask for wrap if search from top. Give a message instead
						if(!BeanShell.isScriptRunning()) {
							view.getStatus().setMessageAndClear(jEdit.getProperty(
								"view.status.no-further-search-string-found"));
						}
						restart = false;
					} else {
						/* {{{ it is better to first check if there are further matches when
						 * keeping searching.
						 * therefore, the current selection has to be memorized, and the
						 * user dialog done after ignoreFromTop
						 
						Integer[] args = { new Integer(reverse ? 1 : 0) };
						// rwchg: error in GUIUtilities.confirm !!!
						int result = GUIUtilities.confirm(view,
							"keepsearching",args,
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
						restart = (result == JOptionPane.YES_OPTION);
						}}} */
						lastSelection = view.getTextArea().getSelection();
						lastCaret = view.getTextArea().getCaretPosition();
						if (lastSelection.length > 0 && debug) Log.log(Log.DEBUG, BeanShell.class,"tp662: lastSelection = "+
							view.getTextArea().getSelectedText(lastSelection[0]));
						restart = true;
					}
				}

				if(restart)
				{
					// start search from beginning
					path = fileset.getFirstFile(view);
					repeat = true;
				}
				else
					break loop;
			}
		}
		catch(Exception e)
		{
			Log.log(Log.ERROR,XSearchAndReplace.class,e);
			Object[] args = { e.getMessage() };
			if(args[0] == null)
				args[0] = e.toString();
			GUIUtilities.error(view,"searcherror",args);
		}
		finally
		{
			view.hideWaitCursor();
		}

		return false;
	} //}}}

	//{{{ find() method
	/**
	 * Finds the next instance of the search string in the specified
	 * buffer.
	 * @param view The view
	 * @param buffer The buffer
	 * @param start Location where to start the search
	 */
	public static boolean find(View view, Buffer buffer, int start)
		throws Exception
	{
		return find(view,buffer,start,false);
	} //}}}

	//{{{ find() method
	/**
	 * Finds the next instance of the search string in the specified
	 * buffer.
	 * @param view The view
	 * @param buffer The buffer
	 * @param start Location where to start the search
	 * @param firstTime See <code>SearchMatcher.nextMatch()</code>
	 * @since jEdit 4.0pre7
	 */
	public static boolean find(View view, Buffer buffer, int start,
		boolean firstTime) throws Exception {
			// Log.log(Log.DEBUG, BeanShell.class,"find(...): start = "+start+", firstTime = "+firstTime);
		SearchMatcher matcher = getSearchMatcher(true);
if (debug) Log.log(Log.DEBUG, BeanShell.class,"XSearchAndReplace.812: matcher = "+matcher);
		if(matcher == null)
		{
			view.getToolkit().beep();
			return false;
		}

		Segment text = new Segment();
		// remark: Segment is like a String reference without copying
		//         ==> buffer.getText doesn't copy the whole buffer
		//         Therefore, multiple assignment of "text" shouldn't be a problem
		boolean xFound = false;
		// rwchg loop for xsearch-check
		while (!xFound) {
			if(reverse)
				buffer.getText(0,start,text);
			else
				buffer.getText(start,buffer.getLength() - start,text);
	
			// the start and end flags will be wrong with reverse search enabled,
			// but they are only used by the regexp matcher, which doesn't
			// support reverse search yet.
			//
			// REMIND: fix flags when adding reverse regexp search.
			int[] match = matcher.nextMatch(new CharIndexedSegment(text,reverse),
				start == 0,true,firstTime);
	
			if(match != null)
			{
				int matchOffsetBegin, matchOffsetEnd;  // offset inside buffer
				if(reverse)				{
					matchOffsetBegin = start - match[1];
					matchOffsetEnd = start - match[0];
				} else {
					matchOffsetBegin = start + match[0];
					matchOffsetEnd = start + match[1];
				}

							
				if (checkXSearchParameters(view.getTextArea(), buffer, matchOffsetBegin, matchOffsetEnd, false)) {
					jEdit.commitTemporary(buffer);
					view.setBuffer(buffer);
					JEditTextArea textArea = view.getTextArea();
		
					if(reverse)
					{
						textArea.setSelection(new Selection.Range(
							start - match[1],
							start - match[0]));
						textArea.moveCaretPosition(start - match[1]);
					}
					else
					{
						textArea.setSelection(new Selection.Range(
							start + match[0],
							start + match[1]));
						textArea.moveCaretPosition(start + match[1]);
					}
					xFound = true;
				} else {
					// target matched, but nok because of xParameters
					// find next
					if (reverse) start -= match[1];
					else start += match[1];
				}
			}
			else
				return false;
		}
		return xFound;  // always true
	} //}}}

	//{{{ replace() method
	/**
	 * Replaces the current selection with the replacement string.
	 * @param view The view
	 * @return True if the operation was successful, false otherwise
	 */
	public static boolean replace(View view)
	{
		JEditTextArea textArea = view.getTextArea();

		Buffer buffer = view.getBuffer();
		if(!buffer.isEditable())
			return false;

		boolean smartCaseReplace = (replace != null
			&& TextUtilities.getStringCase(replace)
			== TextUtilities.LOWER_CASE);

		Selection[] selection = textArea.getSelection();
		if(selection.length == 0)
		{
			view.getToolkit().beep();
			return false;
		}

		record(view,"replace(view)",true,false);

		try
		{
			buffer.beginCompoundEdit();

			SearchMatcher matcher = getSearchMatcher(false);
			if(matcher == null)
				return false;

			int retVal = 0;

			for(int i = 0; i < selection.length; i++)
			{
				Selection s = selection[i];

				/* if an occurence occurs at the
				beginning of the selection, the
				selection start will get moved.
				this sucks, so we hack to avoid it. */
				int start = s.getStart();

				if(s instanceof Selection.Range)
				{
					retVal += _replace(view,buffer,matcher,
						s.getStart(),s.getEnd(),
						smartCaseReplace);

					textArea.removeFromSelection(s);
					textArea.addToSelection(new Selection.Range(
						start,s.getEnd()));
				}
				else if(s instanceof Selection.Rect)
				{
					for(int j = s.getStartLine(); j <= s.getEndLine(); j++)
					{
						retVal += _replace(view,buffer,matcher,
							s.getStart(buffer,j),s.getEnd(buffer,j),
							smartCaseReplace);
					}
					textArea.addToSelection(new Selection.Rect(
						start,s.getEnd()));
				}
			}

			Selection s = textArea.getSelectionAtOffset(
				textArea.getCaretPosition());
			if(s != null)
				textArea.moveCaretPosition(s.getEnd());

			if(retVal == 0)
			{
				view.getToolkit().beep();
				return false;
			}

			return true;
		}
		catch(Exception e)
		{
			Log.log(Log.ERROR,XSearchAndReplace.class,e);
			Object[] args = { e.getMessage() };
			if(args[0] == null)
				args[0] = e.toString();
			GUIUtilities.error(view,"searcherror",args);
		}
		finally
		{
			buffer.endCompoundEdit();
		}

		return false;
	} //}}}

	//{{{ replace() method
	/**
	 * Replaces text in the specified range with the replacement string.
	 * @param view The view
	 * @param buffer The buffer
	 * @param start The start offset
	 * @param end The end offset
	 * @return True if the operation was successful, false otherwise
	 */
	public static boolean replace(View view, Buffer buffer, int start, int end)
	{
		if(!buffer.isEditable())
			return false;

		boolean smartCaseReplace = (replace != null
			&& TextUtilities.getStringCase(replace)
			== TextUtilities.LOWER_CASE);

		JEditTextArea textArea = view.getTextArea();

		try
		{
			buffer.beginCompoundEdit();

			SearchMatcher matcher = getSearchMatcher(false);
			if(matcher == null)
				return false;

			int retVal = 0;

			retVal += _replace(view,buffer,matcher,start,end,
				smartCaseReplace);

			if(retVal != 0)
				return true;
		}
		catch(Exception e)
		{
			Log.log(Log.ERROR,XSearchAndReplace.class,e);
			Object[] args = { e.getMessage() };
			if(args[0] == null)
				args[0] = e.toString();
			GUIUtilities.error(view,"searcherror",args);
		}
		finally
		{
			buffer.endCompoundEdit();
		}

		return false;
	} //}}}

	//{{{ replaceAll() method
	/**
	 * Replaces all occurances of the search string with the replacement
	 * string.
	 * @param view The view
	 */
	public static boolean replaceAll(View view)
	{
		int fileCount = 0;
		int occurCount = 0;
/*
		if(fileset.getFileCount(view) == 0)
		{
			GUIUtilities.error(view,"empty-fileset",null);
			return false;
		}
*/
		record(view,"replaceAll(view)",true,true);

		view.showWaitCursor();

		boolean smartCaseReplace = (replace != null
			&& TextUtilities.getStringCase(replace)
			== TextUtilities.LOWER_CASE);

		try
		{
			SearchMatcher matcher = getSearchMatcher(false);
			if(matcher == null)
				return false;

			String path = fileset.getFirstFile(view);
loop:			while(path != null)
			{
				Buffer buffer = jEdit.openTemporary(
					view,null,path,false);

				/* this is stupid and misleading.
				 * but 'path' is not used anywhere except
				 * the above line, and if this is done
				 * after the 'continue', then we will
				 * either hang, or be forced to duplicate
				 * it inside the buffer == null, or add
				 * a 'finally' clause. you decide which one's
				 * worse. */
				path = fileset.getNextFile(view,path);

				if(buffer == null)
					continue loop;

				// Wait for buffer to finish loading
				if(buffer.isPerformingIO())
					VFSManager.waitForRequests();

				if(!buffer.isEditable())
					continue loop;

				// Leave buffer in a consistent state if
				// an error occurs
				int retVal = 0;

				try
				{
					buffer.beginCompoundEdit();
					retVal = _replace(view,buffer,matcher,
						0,buffer.getLength(),
						smartCaseReplace);
				}
				finally
				{
					buffer.endCompoundEdit();
				}

				if(retVal != 0)
				{
					fileCount++;
					occurCount += retVal;
					jEdit.commitTemporary(buffer);
				}
			}
		}
		catch(Exception e)
		{
			Log.log(Log.ERROR,XSearchAndReplace.class,e);
			Object[] args = { e.getMessage() };
			if(args[0] == null)
				args[0] = e.toString();
			GUIUtilities.error(view,"searcherror",args);
		}
		finally
		{
			view.hideWaitCursor();
		}

		/* Don't do this when playing a macro, cos it's annoying */
		if(!BeanShell.isScriptRunning())
		{
			Object[] args = { new Integer(occurCount),
				new Integer(fileCount) };
			view.getStatus().setMessageAndClear(jEdit.getProperty(
				"view.status.replace-all",args));
			if(occurCount == 0)
				view.getToolkit().beep();
		}

		return (fileCount != 0);
	} //}}}

	//}}}

	//{{{ load() method
	/**
	 * Loads search and replace state from the properties.
	 */
	public static void load()
	{
		search = jEdit.getProperty("search.find.value");
		origSearch = search;
		replace = jEdit.getProperty("search.replace.value");
		ignoreCase = jEdit.getBooleanProperty("search.ignoreCase.toggle");
		regexp = jEdit.getBooleanProperty("search.regexp.toggle");
		beanshell = jEdit.getBooleanProperty("search.beanshell.toggle");
		wrap = jEdit.getBooleanProperty("search.wrap.toggle");
		// load extended properties
		fromTop = jEdit.getBooleanProperty("search.ext.fromTop.toggle");
		columnSearchEnabled = jEdit.getBooleanProperty("search.ext.column.toggle");
		columnSearchExpandTabs = jEdit.getBooleanProperty("search.ext.column.expand-tabs.toggle");
		columnSearchLeftCol = jEdit.getIntegerProperty("search.ext.column.left.value",0);
		columnSearchRightCol = jEdit.getIntegerProperty("search.ext.column.right.value",0);
		wordPart = jEdit.getIntegerProperty("search.ext.wordpart.value", XSearchDialog.SEARCH_PART_NONE);
		foldSearch = 
			jEdit.getIntegerProperty("search.ext.foldSearch.value", XSearchDialog.SEARCH_IN_OUT_NONE);
		commentSearch = 
			jEdit.getIntegerProperty("search.ext.commentSearch.value", XSearchDialog.SEARCH_IN_OUT_NONE);

		fileset = new CurrentBufferSet();

		// Tags plugin likes to call this method at times other than
		// startup; so we need to fire a SearchSettingsChanged to
		// notify the search bar and so on.
		matcher = null;
		EditBus.send(new SearchSettingsChanged(null));
	} //}}}

	//{{{ save() method
	/**
	 * Saves search and replace state to the properties.
	 */
	public static void save()
	{
		jEdit.setProperty("search.find.value",search);
		jEdit.setProperty("search.replace.value",replace);
		jEdit.setBooleanProperty("search.ignoreCase.toggle",ignoreCase);
		jEdit.setBooleanProperty("search.regexp.toggle",regexp);
		jEdit.setBooleanProperty("search.beanshell.toggle",beanshell);
		jEdit.setBooleanProperty("search.wrap.toggle",wrap);
		// save extended properties
		jEdit.setBooleanProperty("search.ext.fromTop.toggle",fromTop);
		jEdit.setBooleanProperty("search.ext.column.toggle",columnSearchEnabled);
		jEdit.setBooleanProperty("search.ext.column.expand-tabs.toggle",columnSearchExpandTabs);
		jEdit.setIntegerProperty("search.ext.column.left.value",columnSearchLeftCol);
		jEdit.setIntegerProperty("search.ext.column.right.value",columnSearchRightCol);
		jEdit.setIntegerProperty("search.ext.wordpart.value",wordPart);
		jEdit.setIntegerProperty("search.ext.foldSearch.value",foldSearch);
		jEdit.setIntegerProperty("search.ext.commentSearch.value",commentSearch);
		
/*
		jEdit.setBooleanProperty("search.show-settings.toggle",showSettings);		
		jEdit.setBooleanProperty("search.show-replace.toggle",showReplace);
		jEdit.setBooleanProperty("search.show-extended.toggle",showExtended);
*/
	} //}}}

	//{{{ Private members

	//{{{ Instance variables
	private static String search;
	private static String origSearch;
	private static String replace;
	private static boolean regexp;
	private static boolean ignoreCase;
	private static boolean reverse;     // search backward
	private static boolean fromTop;     // search from top
	private static boolean beanshell;   // use bean shell snippet
	private static boolean wrap;	    // wrap search
	private static boolean ignoreFromTop;	  // rwchg: ignore "fromTop", when multiple "find" invoked

	private static SearchMatcher matcher;
	private static SearchFileSet fileset;
	
	/* selectiv display
	private static boolean showSettings;  // display the standart search settings
	private static boolean showReplace;   // display the replace options and buttons
	private static boolean showExtended;  // display the extended options and buttons
*/
	// column search
	private static boolean columnSearchEnabled;
	private static boolean columnSearchExpandTabs;
	private static int columnSearchLeftCol;
	private static int columnSearchRightCol;

	// comment search
	private static int commentSearch = XSearchDialog.SEARCH_IN_OUT_NONE;

	// folding search
	private static int foldSearch = XSearchDialog.SEARCH_IN_OUT_NONE;

	// word part search
	private static int wordPart = XSearchDialog.SEARCH_PART_NONE;
	
	//}}}

	//{{{ checkXSearchParameters method
	/**
	 * Checks if the search result matches the extended parameters, too
	 * @param textArea
	 * @param buffer The buffer
	 * @param matchBegin Location where match starts
	 * @param matchBegin Location where match ends
	 */
	static boolean checkXSearchParameters(JEditTextArea textArea, Buffer buffer, 
		int matchBegin, int matchEnd, boolean hyperSearchFlag) {
		if (debug) Log.log(Log.DEBUG, BeanShell.class,
			"checkXsp: matchBegin = "+matchBegin+", matchEnd = "+matchEnd);
		boolean xMatchOk = true;
		int matchLine = buffer.getLineOfOffset(matchBegin);
		int startMatchLine = buffer.getLineStartOffset(matchLine);
//		int startMatchEndLine = buffer.getLineStartOffset(buffer.getLineOfOffset(matchEnd));
		if (debug) Log.log(Log.DEBUG, BeanShell.class,"tp1111: matchLine = "+matchLine+
			", startMatchLine = "+startMatchLine);
		/************************************************************************************
		 * check fold status
		 ************************************************************************************/
		if (xMatchOk && foldSearch != XSearchDialog.SEARCH_IN_OUT_NONE) {
			if (foldSearch == XSearchDialog.SEARCH_IN_OUT_OUTSIDE  
			^ textArea.getFoldVisibilityManager().isLineVisible(matchLine)) {
				xMatchOk = false;
			}
		}
		/************************************************************************************
		 * check comment status
		 ************************************************************************************/
		if (xMatchOk && commentSearch != XSearchDialog.SEARCH_IN_OUT_NONE) {
			// now we know if we are inside / outside of a comment ==> evaluate request
			if (commentSearch == XSearchDialog.SEARCH_IN_OUT_OUTSIDE ^	// xor
				  isOutsideComments(textArea, buffer, matchBegin, hyperSearchFlag))
//				outsideCmt) 
				xMatchOk = false;
		}
		/************************************************************************************
		 * check columns
		 ************************************************************************************/
		if (columnSearchEnabled) {
			// reject if match on multiple lines
			if (buffer.getLineOfOffset(matchEnd) != matchLine) xMatchOk = false;
			else {
			// Log.log(Log.DEBUG, BeanShell.class,"matchBegin = "+matchBegin+", matchEnd = "+matchEnd);
//			BeanShellUtility bsu = new BeanShellUtility(view);
				if (columnSearchExpandTabs) {
					int matchRowStart = matchBegin - startMatchLine;
					int matchRowEnd   = matchEnd   - startMatchLine;
					matchBegin = startMatchLine + BeanShellUtility.getVisiblePosition(buffer.getTabSize(),
						matchRowStart, buffer.getLineText(matchLine));
					matchEnd = startMatchLine + BeanShellUtility.getVisiblePosition(buffer.getTabSize(), 
						matchRowEnd, buffer.getLineText(matchLine));
				}
				// check match inside columns ( add +1 to ranges, because visible row starts with 1)
				if (debug) Log.log(Log.DEBUG, BeanShell.class,"tp1197: matchBegin = "+matchBegin+", matchLine = "+matchLine+
					", buffer.getLineStartOffset(matchLine) = "+buffer.getLineStartOffset(matchLine)+
				", columnSearchLeftCol = "+columnSearchLeftCol);
				if (matchBegin - buffer.getLineStartOffset(matchLine) < columnSearchLeftCol-1) {
					xMatchOk = false;
				}
				if (debug) Log.log(Log.DEBUG, BeanShell.class,"tp1203: matchEnd = "+matchEnd+"matchLine = "+matchLine+
				"buffer.getLineStartOffset(matchLine) = "+buffer.getLineStartOffset(matchLine)+
				"columnSearchRightCol = "+columnSearchRightCol);
				if (matchEnd - buffer.getLineStartOffset(matchLine) > columnSearchRightCol) {
					xMatchOk = false;
				}
			}
		}
		/************************************************************************************
		 ************************************************************************************/
		if (debug) Log.log(Log.DEBUG, BeanShell.class,"checkXsp: xMatchOk = "+xMatchOk);
		return xMatchOk;
	}
	//}}}
	
	//{{{ isOutsideComments(int currPos) method
	/**
	 * Checks if the passed buffer position is inside comments
	 * Note: in case of hypersearch, the token methode doesn't work (Edit Mode not parsed yet ?!)
	 *       therefore, a simplified methode with default comment signs is used
	 * @param textArea
	 * @param buffer
	 * @param currPos  Position in buffer to be checked
	 * @param hyperSearchFlag  Indicates if normal or hypersearch
	 */
	
	private static boolean isOutsideComments(JEditTextArea textArea, Buffer buffer, 
		int currPos, boolean hyperSearchFlag) {
		// Note: as getToken doesn't work for blanks, we have to find the first nonblank
		// int caretPos = textArea.getCaretPosition();
		boolean outsideCmt;
		if (debug) Log.log(Log.DEBUG, BeanShell.class,"tp1280: currPos = "+currPos);
		if (true) {
		// {{{ hypersearch
			int matchLine = buffer.getLineOfOffset(currPos);
			outsideCmt = true;
			// check line comment
			String currLine = buffer.getLineText(matchLine);
//			if (currLine.lastIndexOf("//",currPos-buffer.getLineStartOffset(matchLine)) != -1)
			if (currLine.lastIndexOf(jEdit.getProperty("search.comment.line"),
				currPos-buffer.getLineStartOffset(matchLine)) != -1)
				outsideCmt = false;
			// check block comment
			if (outsideCmt) {
				// search for "start-comment" before match
				SearchMatcher cmtMatcher = new BoyerMooreSearchMatcher(
					jEdit.getProperty("search.comment.blockbegin"),		// search,
					"",		    //replace,
					false,		//ignoreCase,
					true,		  //reverse
					false,		//beanshell,
					null		  //replaceMethod);
				);
				Segment textBeforeMatch = new Segment();
				buffer.getText(0,currPos,textBeforeMatch);
	
				int[] openCmtMatch = cmtMatcher.nextMatch(new CharIndexedSegment(textBeforeMatch,true),
				false,true,true);
	
				if(openCmtMatch != null) {
					// we found an open comment before match ==> check if alreday closed 
					if (debug) Log.log(Log.DEBUG, BeanShell.class,"found open cmt at = "
					+openCmtMatch[0]+"-"+openCmtMatch[1]);
					cmtMatcher = new BoyerMooreSearchMatcher(
						jEdit.getProperty("search.comment.blockend"),		// search,
						"",		    //replace,
						false,		//ignoreCase,
						true,	  	//reverse
						false,		//beanshell,
						null		  //replaceMethod);
					);
					int[] closeCmtMatch = cmtMatcher.nextMatch(new CharIndexedSegment(textBeforeMatch,true),
					false,true,true);
					if(closeCmtMatch == null) {
						// no close found ==> inside comment
						outsideCmt = false;
					} else {
						if (debug) Log.log(Log.DEBUG, BeanShell.class,"found close cmt at = "
						+closeCmtMatch[0]+"-"+closeCmtMatch[1]);
						// we found a close comment ==> check which was earlier
						if (openCmtMatch[0] < closeCmtMatch[0]) outsideCmt = false;
					}
				}
			}
		// }}}
			
			
		}   //}}}
		return outsideCmt;
	}
	//}}}
	

	//{{{ record() method
	private static void record(View view, String action,
		boolean replaceAction, boolean recordFileSet)
	{
		Macros.Recorder recorder = view.getMacroRecorder();

		if(recorder != null)
		{
			recorder.record("XSearchAndReplace.setSearchString(\""
				+ MiscUtilities.charsToEscapes(search) + "\");");

			if(replaceAction)
			{
				recorder.record("XSearchAndReplace.setReplaceString(\""
					+ MiscUtilities.charsToEscapes(replace) + "\");");
				recorder.record("XSearchAndReplace.setBeanShellReplace("
					+ beanshell + ");");
			}
			else
			{
				// only record this if doing a find next
				recorder.record("XSearchAndReplace.setAutoWrapAround("
					+ wrap + ");");
				recorder.record("XSearchAndReplace.setReverseSearch("
					+ reverse + ");");
			}

			recorder.record("XSearchAndReplace.setIgnoreCase("
				+ ignoreCase + ");");
			recorder.record("XSearchAndReplace.setRegexp("
				+ regexp + ");");

			if(recordFileSet)
			{
				recorder.record("XSearchAndReplace.setSearchFileSet("
					+ fileset.getCode() + ");");
			}

			recorder.record("XSearchAndReplace." + action + ";");
		}
	} //}}}

	//{{{ _replace() method
	/**
	 * Replaces all occurances of the search string with the replacement
	 * string.
	 * @param view The view
	 * @param buffer The buffer
	 * @param start The start offset
	 * @param end The end offset
	 * @param matcher The search matcher to use
	 * @param smartCaseReplace See user's guide
	 * @return The number of occurrences replaced
	 */
	private static int _replace(View view, Buffer buffer,
		SearchMatcher matcher, int start, int end,
		boolean smartCaseReplace)
		throws Exception
	{
		int occurCount = 0;

		boolean endOfLine = (buffer.getLineEndOffset(
			buffer.getLineOfOffset(end)) - 1 == end);

		Segment text = new Segment();
		int offset = start;
loop:		for(int counter = 0; ; counter++)
		{
			buffer.getText(offset,end - offset,text);

			boolean startOfLine = (buffer.getLineStartOffset(
				buffer.getLineOfOffset(offset)) == offset);

			int[] occur = matcher.nextMatch(
				new CharIndexedSegment(text,false),
				startOfLine,endOfLine,counter == 0);
			if(occur == null)
				break loop;
			int _start = occur[0];
			int _length = occur[1] - occur[0];
			// check xsearch parameters
			if (!checkXSearchParameters(view.getTextArea(), buffer,
				offset + _start, offset + _start + _length, false)) {
				// current match was not ok ==> skip replacement
				offset += _start + _length;
				if (debug) Log.log(Log.DEBUG, BeanShell.class,"offset = "+offset);
			} else {
				String found = new String(text.array,text.offset + _start,_length);
				String subst = matcher.substitute(found);
				if(smartCaseReplace && ignoreCase)
				{
					int strCase = TextUtilities.getStringCase(found);
					if(strCase == TextUtilities.LOWER_CASE)
						subst = subst.toLowerCase();
					else if(strCase == TextUtilities.UPPER_CASE)
						subst = subst.toUpperCase();
					else if(strCase == TextUtilities.TITLE_CASE)
						subst = TextUtilities.toTitleCase(subst);
				}
	
				if(subst != null)
				{
					buffer.remove(offset + _start,_length);
					buffer.insert(offset + _start,subst);
					occurCount++;
					offset += _start + subst.length();
	
					end += (subst.length() - found.length());
				}
				else
					offset += _start + _length;
			}
		}
		return occurCount;
	} //}}}
  private static void testMatcher() {
		if (debug) Log.log(Log.DEBUG, BeanShell.class,"tp1433: matcher = "+ matcher +
			" is " + matcher.getClass().getName());
	}
	//}}}
}
