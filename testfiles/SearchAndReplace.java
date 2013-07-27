package xsearch;

/*
 * xsearch.SearchAndReplace.java - Search and replace: derived from SearchAndReplace
 * :tabSize=4:indentSize=4:noTabs=false:
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

//{{{ Imports
import gnu.regexp.CharIndexed;
import gnu.regexp.RE;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.Segment;
import javax.swing.tree.DefaultMutableTreeNode;

import org.gjt.sp.jedit.BeanShell;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.TextUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.bsh.*;
import org.gjt.sp.jedit.gui.HistoryModel;
import org.gjt.sp.jedit.gui.TextAreaDialog;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.msg.SearchSettingsChanged;
import org.gjt.sp.jedit.search.AllBufferSet;
import org.gjt.sp.jedit.search.CurrentBufferSet;
import org.gjt.sp.jedit.search.DirectoryListSet;
import org.gjt.sp.jedit.search.SearchFileSet;
import org.gjt.sp.jedit.syntax.DefaultTokenHandler;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;



//}}}

/**
 * Class that implements regular expression and literal search within jEdit
 * buffers.
 * <p>
 * 
 * There are two main groups of methods in this class:
 * <ul>
 * <li>Property accessors - for changing search and replace settings.</li>
 * <li>Actions - for performing search and replace.</li>
 * </ul>
 * 
 * The "HyperSearch" and "Keep dialog" features, as reflected in checkbox
 * options in the search dialog, are not handled from within this class. If you
 * wish to have these options set before the search dialog appears, make a prior
 * call to either or both of the following:
 * 
 * <pre>
 * jEdit.setBooleanProperty(&quot;search.hypersearch.toggle&quot;, true);
 * jEdit.setBooleanProperty(&quot;search.keepDialog.toggle&quot;, true);
 * </pre>
 * 
 * If you are not using the dialog to undertake a search or replace, you may
 * call any of the search and replace methods (including
 * {@link #hyperSearch(View)}) without concern for the value of these
 * properties.
 * 
 * @author Slava Pestov
 * @author John Gellene (API documentation)
 * @author Rudi Widmann (XSearch extension)
 * @version $Id: SearchAndReplace.java 20063 2011-10-12 15:58:17Z ezust $
 */
public class SearchAndReplace
{
	public static boolean debug = false; // debug-flag, may be modified

	// by bsh

	// xsearch.SearchAndReplace.debug=true

	// {{{ Constants
	public static final boolean FIND_OPTION_SILENT = true;

	// }}}

	// }}}

	// {{{ Getters and setters

	// {{{ setSearchString() method
	/**
	 * Sets the current search string. If wordpart search activ (or
	 * tentativ), a regexp is constructed the original string is saved in
	 * origSearch
	 * 
	 * @param search
	 *                The new search string
	 */
	public static void setSearchString(String search)
	{
		if (debug)
			Log.log(Log.DEBUG, BeanShell.class, "SearchAndReplace.60: search = "
				+ search + ", origSearch = " + origSearch
				+ ", SearchAndReplace.search = " + SearchAndReplace.search);
		boolean searchSettingsChanged = false;

		if (!search.equals(origSearch))
		{
			origSearch = search;
			searchSettingsChanged = true;
		}
		// if a "word" starts with $ (due to extra word char), it won'
		// be found by wordpart search
		if (search.startsWith("$") && wordPart != XSearch.SEARCH_PART_SUFFIX)
			wordPart = XSearch.SEARCH_PART_NONE;
		if (wordPart == XSearch.SEARCH_PART_NONE && !tentativSearch)
		{
			if (!search.equals(SearchAndReplace.search))
			{
				SearchAndReplace.search = search;
				searchSettingsChanged = true;
			}
		}
		else
		{
			// manipulate search string to a regexp
			String regExpString = search;
			if (tentativSearch)
				regExpString = constructTentativSearchString(search);
			else
				regExpString = StandardUtilities.charsToEscapes(regExpString,
					"\r\t\n\\()[]{}$^*+?|.");
			// Log.log(Log.DEBUG, BeanShell.class,"+++
			// SearchAndReplace.escaped.115: regExpString =
			// "+regExpString);
			switch (wordPart)
			{
			case XSearch.SEARCH_PART_WHOLE_WORD:
				regExpString = "\\<" + regExpString + "\\>";
				break;
			case XSearch.SEARCH_PART_PREFIX:
				regExpString = "\\<" + regExpString;
				break;
			case XSearch.SEARCH_PART_SUFFIX:
				regExpString = regExpString + "\\>";
				break;
			}
			// Log.log(Log.DEBUG, BeanShell.class,"+++
			// SearchAndReplace.wordpart.129: regExpString =
			// "+regExpString);
			if (!regExpString.equals(SearchAndReplace.search))
			{
				SearchAndReplace.search = regExpString;
				searchSettingsChanged = true;
			}
		}
		if (debug)
			Log.log(Log.DEBUG, BeanShell.class, "SearchAndReplace.89: origSearch = "
				+ origSearch + ", SearchAndReplace.search = "
				+ SearchAndReplace.search + ", searchSettingsChanged = "
				+ searchSettingsChanged);
		if (searchSettingsChanged)
		{
			matcher = null;
			EditBus.send(new SearchSettingsChanged(null));
		}
	} // }}}

	// {{{ getSearchString() method
	/**
	 * Returns the current search string.
	 */
	public static String getSearchString()
	{
		return origSearch;
	} // }}}

	// {{{ setReplaceString() method
	/**
	 * Sets the current replacement string.
	 * 
	 * @param replace
	 *                The new replacement string
	 */
	public static void setReplaceString(String replace)
	{
		if (replace.equals(SearchAndReplace.replace))
			return;

		SearchAndReplace.replace = replace;
		// matcher = null;

		// EditBus.send(new SearchSettingsChanged(null));
	} // }}}

	// {{{ getReplaceString() method
	/**
	 * Returns the current replacement string.
	 */
	public static String getReplaceString()
	{
		return replace;
	} // }}}

	// {{{ setFindAll() method
	/**
	 * Sets the find all flag.
	 */
	public static void setFindAll(boolean findAllFlag)
	{
		SearchAndReplace.findAll = findAllFlag;
	} // }}}

	// {{{ setIgnoreCase() method
	/**
	 * Sets the ignore case flag.
	 * 
	 * @param ignoreCase
	 *                True if searches should be case insensitive, false
	 *                otherwise
	 */
	public static void setIgnoreCase(boolean ignoreCase)
	{
		if (ignoreCase == SearchAndReplace.ignoreCase)
			return;

		SearchAndReplace.ignoreCase = ignoreCase;
		matcher = null;

		// EditBus.send(new SearchSettingsChanged(null));
	}

	// getIgnoreCase() method
	/**
	 * Returns the state of the ignore case flag.
	 * 
	 * @return True if searches should be case insensitive, false otherwise
	 */
	public static boolean getIgnoreCase()
	{
		return ignoreCase;
	} // }}}

	// {{{ setRegexp() method
	/**
	 * Sets the state of the regular expression flag.
	 * 
	 * @param regexp
	 *                True if regular expression searches should be
	 *                performed
	 */
	public static void setRegexp(boolean regexp)
	{
		if (regexp == SearchAndReplace.regexp)
			return;

		SearchAndReplace.regexp = regexp;
		// if(regexp && reverse)
		// reverse = false;

		matcher = null;

		// EditBus.send(new SearchSettingsChanged(null));
	} // }}}

	// {{{ getRegexp() method
	/**
	 * Returns the state of the regular expression flag.
	 * 
	 * @return True if regular expression searches should be performed
	 */
	public static boolean getRegexp()
	{
		return regexp;
	} // }}}

	// {{{ setReverseSearch() method
	/**
	 * Determines whether a reverse search will conducted from the current
	 * position to the beginning of a buffer. Note that reverse search and
	 * regular expression search is mutually exclusive; enabling one will
	 * disable the other.
	 * 
	 * @param reverse
	 *                True if searches should go backwards, false otherwise
	 */
	public static void setReverseSearch(boolean reverse)
	{
		if (reverse == SearchAndReplace.reverse)
			return;
		SearchAndReplace.reverse = reverse;

		// matcher = null;

		// EditBus.send(new SearchSettingsChanged(null));
	} // }}}

	// {{{ getReverseSearch() method
	/**
	 * Returns the state of the reverse search flag.
	 * 
	 * @return True if searches should go backwards, false otherwise
	 */
	public static boolean getReverseSearch()
	{
		return reverse;
	} // }}}

	// {{{ resetIgnoreFromTop() method
	/**
	 * Resets ignoreFromTop flag Note: ignoreFromTop flag is set, when find
	 * was invoked This allowes "refind" without resetting "fromTop"
	 * manually If any search settings change, ignoreFromTop flag shall be
	 * reset If end reached (refind results in "not found"), the next
	 * "refind" shall restart from top ==> reset ignoreFromTop
	 */
	public static void resetIgnoreFromTop()
	{
		ignoreFromTop = false;
	} // }}}

	// {{{ setSearchFromTop() method
	/**
	 * Sets the search from top flag.
	 * 
	 * @param fromTop
	 *                True if searches should start from top, false
	 *                otherwise
	 */
	public static void setSearchFromTop(boolean fromTop)
	{
		if (fromTop == SearchAndReplace.fromTop)
			return;

		SearchAndReplace.fromTop = fromTop;
		if (fromTop)
			reverse = false;

		matcher = null;

		// EditBus.send(new SearchSettingsChanged(null));
	} // }}}

	// {{{ getSearchFromTop() method
	/**
	 * Returns the state of the search from top flag.
	 */
	public static boolean getSearchFromTop()
	{
		return fromTop;
	} // }}}

	// {{{ setBeanShellReplace() method
	/**
	 * Sets the state of the BeanShell replace flag.
	 * 
	 * @param beanshell
	 *                True if the replace string is a BeanShell expression
	 * @since jEdit 3.2pre2
	 */
	public static void setBeanShellReplace(boolean beanshell)
	{
		if (beanshell == SearchAndReplace.beanshell)
			return;

		SearchAndReplace.beanshell = beanshell;

		// EditBus.send(new SearchSettingsChanged(null));
	} // }}}

	// {{{ getBeanShellReplace() method
	/**
	 * Returns the state of the BeanShell replace flag.
	 * 
	 * @return True if the replace string is a BeanShell expression
	 * @since jEdit 3.2pre2
	 */
	public static boolean getBeanShellReplace()
	{
		return beanshell;
	} // }}}
	
	/**
	 * @return true if the "synchronize" button should be disabled in favor of auto-sync
	 * @since XSearch 1.0.9.2
	 */
	public static boolean isAutoSync() 
	{
		return jEdit.getBooleanProperty("xsearch.dirsearch.autosync");
	}
	
	/**
	 * @since XSearch 1.0.9.2
	 * @param newSync
	 */
	public static void setAutoSync(boolean newSync) {
		jEdit.setBooleanProperty("xsearch.dirsearch.autosync", newSync);
	}

	// {{{ setAutoWrap() method
	/**
	 * Sets the state of the auto wrap around flag.
	 * 
	 * @param wrap
	 *                If true, the 'continue search from start' dialog will
	 *                not be displayed
	 * @since jEdit 3.2pre2
	 */
	public static void setAutoWrapAround(boolean wrap)
	{
		if (wrap == SearchAndReplace.wrap)
			return;

		SearchAndReplace.wrap = wrap;

		// EditBus.send(new SearchSettingsChanged(null));
	} // }}}

	
	// {{{ getAutoWrap() method
	
	/**
	 * Returns the state of the auto wrap around flag.
	 * 
	 * @since jEdit 3.2pre2
	 */
	public static boolean getAutoWrapAround()
	{
		return wrap;
	} // }}}

	// {{{ setSearchMatcher() method
	/**
	 * Sets a custom search string matcher. Note that calling
	 * {@link #setSearchString(String)}, {@link #setIgnoreCase(boolean)},
	 * or {@link #setRegexp(boolean)} will reset the matcher to the default.
	 */
	public static void setSearchMatcher(SearchMatcher matcher)
	{
		SearchAndReplace.matcher = matcher;

		// EditBus.send(new SearchSettingsChanged(null));
	} // }}}

	// {{{ getSearchMatcher() method
	/**
	 * Returns the current search string matcher.
	 * 
	 * @exception IllegalArgumentException
	 *                    if regular expression search is enabled, the
	 *                    search string or replacement string is invalid
	 * @since jEdit 4.1pre7
	 */
	public static SearchMatcher getSearchMatcher() throws Exception
	{
		if (matcher != null)
			return matcher;

		if (search == null || "".equals(search))
			return null;

		/*
		 * if(regexp || wordPart != XSearchPanel.SEARCH_PART_NONE ||
		 * tentativSearch) matcher = new
		 * RESearchMatcher(search,replace,ignoreCase,
		 * beanshell,replaceMethod);
		 */
		// often, users have regexp activ, but the search expression is
		// an ordinary expression
		// to improve performance, a BoyerMooreSearchMatcher is returned
		// in this case
		// (I tested 100 searches in a big buffer: RE = 32240ms,
		// BoyerMoore = 1760ms)
		// this optimization is applied for search, NOT for
		// search-replace
		boolean createREMatcher = false;
		if ((regexp && replace.length() != 0)
			|| wordPart != XSearch.SEARCH_PART_NONE
			|| tentativSearch
			|| (regexp && // RE compiler passes ".", "^" and "$"
			// transparent, and it ignores grouping
			(search.indexOf('\\') != -1 || search.indexOf('.') != -1
				|| search.indexOf('^') != -1 || search.indexOf('$') != -1 || search
				.indexOf('(') != -1)))
			createREMatcher = true;
		else if (regexp)
		{
			RE re = new RE(search);
			// /Log.log(Log.DEBUG, BeanShell.class,"+++
			// SearchAndReplace.420: re.toString() =
			// "+re.toString());
			if (!re.toString().equals("(?:" + search + ")"))
				createREMatcher = true;
		}
		Log.log(Log.DEBUG, SearchAndReplace.class,
			"+++ SearchAndReplace.419 create matcher: search = " + search
				+ ", createREMatcher = " + createREMatcher);
		if (createREMatcher)
			matcher = new RESearchMatcher(search, ignoreCase);
		else
		{
			matcher = new BoyerMooreSearchMatcher(search, ignoreCase);
		}

		return matcher;
	} // }}}

	// {{{ getMatchedSubexpressions() method
	/**
	 * Return the found subexpressions ico regexp
	 * 
	 * @param match
	 *                the String which has been matched
	 */
	public static String[] getMatchedSubexpressions(String match)
	{
		if (debug)
			testMatcher();
		if (matcher instanceof RESearchMatcher)
		{
			return ((RESearchMatcher) matcher).getSubexpressions(match);
		}
		else
			return null;
	}

	public static String getMatchedSubexpression(String match, int matchNumber)
	{
		if (matcher instanceof RESearchMatcher)
		{
			String[] subExp = ((RESearchMatcher) matcher).getSubexpressions(match);
			if (subExp != null && subExp.length > matchNumber)
				return subExp[matchNumber];
		}
		return null;
	}// }}}

	// {{{ getLastMatchedSelection() method
	/**
	 * Return the last matched selection used for silent search option
	 */
	public static Selection getLastMatchedSelection()
	{
		return lastMatchedSelection;
	}// }}}

	// {{{ setSearchFileSet() method
	/**
	 * Sets the current search file set.
	 * 
	 * @param fileset
	 *                The file set to perform searches in
	 * @see AllBufferSet
	 * @see CurrentBufferSet
	 * @see DirectoryListSet
	 */
	public static void setSearchFileSet(org.gjt.sp.jedit.search.SearchFileSet fileset)
	{
		SearchAndReplace.fileset = fileset;
		EditBus.send(new SearchSettingsChanged(null));
	} // }}}

	// {{{ getSearchFileSet() method
	/**
	 * Returns the current search file set.
	 */
	public static SearchFileSet getSearchFileSet()
	{
		return fileset;
	} // }}}

	// {{{ getSmartCaseReplace() method
	/**
	 * Returns if the replacement string will assume the same case as each
	 * specific occurrence of the search string.
	 * 
	 * @since jEdit 4.2pre10
	 */
	public static boolean getSmartCaseReplace()
	{
		return (replace != null && TextUtilities.getStringCase(replace) == TextUtilities.LOWER_CASE);
	} // }}}

	// {{{ setColumnSearchOptions() method
	/**
	 * Sets the column search parameters
	 * 
	 * @param expandTabs
	 * @param leftColumn
	 * @param rightColumn
	 */
	public static void setColumnSearchOptions(boolean expandTabs, int leftCol, int rightCol)
	{
		SearchAndReplace.columnSearchEnabled = true;
		SearchAndReplace.columnSearchExpandTabs = expandTabs;
		SearchAndReplace.columnSearchLeftCol = leftCol;
		SearchAndReplace.columnSearchRightCol = rightCol;
	} // }}}

	// {{{ methodes for Column Options
	/**
	 * gets the column option
	 */
	public static boolean getColumnOption()
	{
		return columnSearchEnabled;
	}

	public static boolean getColumnExpandTabsOption()
	{
		return columnSearchExpandTabs;
	}

	public static int getColumnLeftCol()
	{
		return columnSearchLeftCol;
	}

	public static int getColumnRightCol()
	{
		return columnSearchRightCol;
	}

	// resetColumnSearch() method
	/**
	 * Resets the column search parameters
	 */
	public static void resetColumnSearch()
	{
		columnSearchEnabled = false;
	} // }}}

	// {{{ setRowSearchOptions() method
	/**
	 * Sets the row search parameters
	 * 
	 * @param expandTabs
	 * @param leftRow
	 * @param rightRow
	 */
	public static void setRowSearchOptions(int leftRow, int rightRow)
	{
		SearchAndReplace.rowSearchEnabled = true;
		SearchAndReplace.rowSearchLeftRow = leftRow;
		SearchAndReplace.rowSearchRightRow = rightRow;
	} // }}}

	// {{{ methodes for Row Options
	/**
	 * gets the row option
	 */
	public static boolean getRowOption()
	{
		return rowSearchEnabled;
	}

	public static int getRowLeftRow()
	{
		return rowSearchLeftRow;
	}

	public static int getRowRightRow()
	{
		return rowSearchRightRow;
	}

	// resetRowSearch() method
	/**
	 * Resets the row search parameters
	 */
	public static void resetRowSearch()
	{
		rowSearchEnabled = false;
	} // }}}

	// {{{ set/get CommentOption() method
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
	} // }}}

	// {{{ set/get FoldOption() method
	/**
	 * Sets the FoldOption search parameters
	 */
	public static void setFoldOption(int foldOption)
	{
		foldSearch = foldOption;
	}

	// getFoldOption() method
	/**
	 * gets the FoldOption search parameters
	 */
	public static int getFoldOption()
	{
		return foldSearch;
	} // }}}

	// {{{ set/get WordPartOption() method
	/**
	 * Sets the WordPartOption search parameters
	 */
	public static void setWordPartOption(int wordPartOption)
	{
		SearchAndReplace.wordPart = wordPartOption;
	}

	// getWordPartOption() method
	/**
	 * Sets the WordPartOption search parameters
	 */
	public static int getWordPartOption()
	{
		return wordPart;
	} // }}}

	// {{{ set/get TentativOption() method
	/**
	 * Sets the TentativOption
	 */
	public static void setTentativOption(boolean tentOption)
	{
		tentativSearch = tentOption;
	}

	/**
	 * Gets the TentativOption
	 */
	public static boolean getTentativOption()
	{
		return tentativSearch;
	} // }}}

	// {{{ set/get hyperRange method
	/**
	 * Sets the hyperRange
	 */
	public static void setHyperRange(int upper, int lower)
	{
		hyperRangeUpper = upper;
		hyperRangeLower = lower;
	}

	public static int getHyperRangeUpper()
	{
		return hyperRangeUpper;
	}

	public static int getHyperRangeLower()
	{
		return hyperRangeLower;
	} // }}}

	// {{{ staticToString() method
	// defined static because this is a pure static class
	/**
	 * Returns the current settings
	 */
	public static String staticToString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("search = " + SearchAndReplace.search);
		sb.append(", origSearch = " + SearchAndReplace.origSearch);
		sb.append(", replace = " + SearchAndReplace.replace);
		sb.append(", getRegexp() = " + SearchAndReplace.getRegexp());
		sb.append(", getIgnoreCase() = " + SearchAndReplace.getIgnoreCase());
		sb.append(", getReverseSearch() = " + SearchAndReplace.getReverseSearch());
		sb.append(", getSearchFromTop() = " + SearchAndReplace.getSearchFromTop());
		sb.append(", getBeanShellReplace() = " + SearchAndReplace.getBeanShellReplace());
		sb.append(", getAutoWrapAround() = " + SearchAndReplace.getAutoWrapAround());
		sb.append(", getIgnoreCase() = " + SearchAndReplace.getIgnoreCase());
		sb.append(", matcher = " + SearchAndReplace.matcher);
		sb.append(", fileset = " + SearchAndReplace.fileset);
		sb.append(", getColumnOption() = " + SearchAndReplace.getColumnOption());
		sb.append(", getColumnExpandTabsOption() = "
			+ SearchAndReplace.getColumnExpandTabsOption());
		sb.append(", getColumnLeftCol() = " + SearchAndReplace.getColumnLeftCol());
		sb.append(", getColumnRightCol() = " + SearchAndReplace.getColumnRightCol());
		sb.append(", getRowOption() = " + SearchAndReplace.getRowOption());
		sb.append(", getRowLeftRow() = " + SearchAndReplace.getRowLeftRow());
		sb.append(", getRowRightRow() = " + SearchAndReplace.getRowRightRow());
		sb.append(", getCommentOption() = " + SearchAndReplace.getCommentOption());
		sb.append(", getFoldOption() = " + SearchAndReplace.getFoldOption());
		sb.append(", getWordPartOption() = " + SearchAndReplace.getWordPartOption());
		return sb.toString();
	} // }}}

	// }}}

	// {{{ Actions

	// {{{ hyperSearch() method
	/**
	 * Performs a HyperSearch.
	 * 
	 * @param view
	 *                The view
	 * @since jEdit 2.7pre3
	 */
	public static boolean hyperSearch(View view)
	{
		return hyperSearch(view, false);
	} // }}}

	// {{{ hyperSearch() method
	/**
	 * Performs a HyperSearch.
	 * 
	 * @param view
	 *                The view
	 * @param selection
	 *                If true, will only search in the current selection.
	 *                Note that the file set must be the current buffer file
	 *                set for this to work.
	 * @since jEdit 4.0pre1
	 */
	public static boolean hyperSearch(View view, boolean selection)
	{

		record(view, "hyperSearch(view," + selection + ")", false, !selection);

		view.getDockableWindowManager().addDockableWindow(HyperSearchResults.NAME);
		final HyperSearchResults results = (HyperSearchResults) view
			.getDockableWindowManager().getDockable(HyperSearchResults.NAME);
		results.searchStarted();

		try
		{
			SearchMatcher matcher = getSearchMatcher();
			if (matcher == null)
			{
				view.getToolkit().beep();
				results.searchFailed();
				return false;
			}

			Selection[] s;
			if (selection)
			{
				s = view.getTextArea().getSelection();
				if (s == null)
				{
					results.searchFailed();
					return false;
				}
			}
			else
				s = null;
			if (hyperRangeUpper == -1 && hyperRangeLower == -1)
				VFSManager.runInWorkThread(new HyperSearchRequest(view, matcher,
					results, s));
			else
			{
				int hyUp = hyperRangeUpper == -1 ? hyperRangeLower
					: hyperRangeUpper;
				int hyLo = hyperRangeLower == -1 ? hyperRangeUpper
					: hyperRangeLower;
				VFSManager.runInWorkThread(new HyperSearchRequest(view, matcher,
					results, s, hyUp, hyLo));
			}
			return true;
		}
		catch (Exception e)
		{
			results.searchFailed();
			handleError(view, e);
			return false;
		}
	} // }}}

	// {{{ assignSelectionOrCaretwordToSearchString() method
	/**
	 * assignSelectionToSearchString
	 */
	public static String assignSelectionToSearchString(JEditTextArea textArea,
		boolean selectWord)
	{
		String text = textArea.getSelectedText();
		int caretPos = textArea.getCaretPosition();
		String currentChar = " ";
		try {
			currentChar = textArea.getText(caretPos, 1);
		}
		catch (Exception e) {}
		if (selectWord && text == null && !Character.isWhitespace(currentChar.charAt(0)))
		{
			textArea.selectWord();
			text = textArea.getSelectedText();
		}

		if (text != null && text.indexOf('\n') == -1)
		{
			setSearchString(text);
			return text;
		}
		else
			return null;
	}

	// {{{ quickXfind() method
	/**
	 * Quick Xfind - pops up a dialog , but also attempts to start searching for the current
	 * selection, so it can show something in the hypersearch results.
	 * 
	 */
	public static void quickXfind(View view, JEditTextArea textArea, int searchType)
	{
		
		String text = assignSelectionToSearchString(textArea, true);
		if (text != null)
		{
			HistoryModel.getModel("find").addItem(text);
		}
		

		// RW: will this work when text == null ?
		switch (searchType)
		{
		case XSearch.SEARCH_TYPE_SINGLE:
			setSearchFileSet(new org.gjt.sp.jedit.search.CurrentBufferSet());
			find(view);
			break;
		case XSearch.SEARCH_TYPE_CURRENT_BUFFER:
			setSearchFileSet(new org.gjt.sp.jedit.search.CurrentBufferSet());
			hyperSearch(view, false);
			break;
		case XSearch.SEARCH_TYPE_ALL_BUFFERS:
			setSearchFileSet(new org.gjt.sp.jedit.search.AllBufferSet("*"));
			hyperSearch(view, false);
			break;
		case XSearch.SEARCH_TYPE_PROJECT:
			ProjectViewerListSet pvl = new ProjectViewerListSet(view);
			if (pvl.isProjectViewerPresent())
			{
				setSearchFileSet(pvl);
				hyperSearch(view, false);
			}
			break;
		case XSearch.SEARCH_TYPE_DIRECTORY:
			xsearch.XSearchPanel.showSearchPanel(view, text,
				XSearch.SEARCH_TYPE_DIRECTORY);
			break;
		default:
			XSearchPanel.showSearchPanel(view, textArea.getSelectedText(),
				XSearch.SEARCH_TYPE_CURRENT_BUFFER);
			break;
		}
	} // }}}

	// {{{ getSelectionOrWord() method
	/**
	 * Return selected text or word under caret
	 */
	public static String getSelectionOrWord(View view, JEditTextArea textArea)
	{
		String text = textArea.getSelectedText();
		if (text == null)
		{
			textArea.selectWord();
			text = textArea.getSelectedText();
		}

		if (text != null && text.indexOf('\n') == -1)
			return text;
		else
			return null;
	} // }}}

	// {{{ find() method
	/**
	 * Finds the next occurance of the search string.
	 * 
	 * @param view
	 *                The view
	 * @return True if the operation was successful, false otherwise
	 */
	public static boolean find(View view)
	{

		// Log.log(Log.DEBUG, BeanShell.class,"+++ SearchAndReplace.822:
		// "+staticToString());
		// component that will parent any dialog boxes
		// Component comp = XSearchPanel.getSearchPanel(view);
//		if (comp == null || !comp.isShowing())
//			comp = view;

		boolean repeat = false;
		if (findAll)
		{
			resetIgnoreFromTop(); // findAll always from top
			setReverseSearch(false);
			setSearchFromTop(true);
		}
		// check search settings
		if (!areSearchSettingsOk())
		{
			view.getToolkit().beep();
			view.getStatus().setMessageAndClear(
				jEdit.getProperty("view.status.illegal-search-settings"));

			return false;
		}
		if (fileset == null)
		{
			fileset = new org.gjt.sp.jedit.search.CurrentBufferSet();
		}
		String path = fileset.getNextFile(view, null);
		if (path == null)
		{
			GUIUtilities.error(view, "empty-fileset", null);
			return false;
		}

		boolean _reverse = reverse
			&& fileset instanceof org.gjt.sp.jedit.search.CurrentBufferSet;
		try
		{
			view.showWaitCursor();

			SearchMatcher matcher = getSearchMatcher();
			if (debug)
				Log.log(Log.DEBUG, BeanShell.class,
					"SearchAndReplace.609: matcher = " + matcher
						+ ", search = " + search);
			if (matcher == null)
			{
				view.getToolkit().beep();
				return false;
			}

			record(view, "find(view)", false, true);

			Selection[] lastSelection = null;
			int lastCaret = 0;

			loop: for (;;) // {{{
			{
				while (path != null)
				{
					if (debug)
						Log.log(Log.DEBUG, BeanShell.class,
							"+++ SearchAndReplace.866: path = " + path);
					Buffer buffer = jEdit
						.openTemporary(view, null, path, false);

					/*
					 * this is stupid and misleading. but
					 * 'path' is not used anywhere except
					 * the above line, and if this is done
					 * after the 'continue', then we will
					 * either hang, or be forced to
					 * duplicate it inside the buffer ==
					 * null, or add a 'finally' clause. you
					 * decide which one's worse.
					 */
					path = fileset.getNextFile(view, path);
					if (debug)
						Log.log(Log.DEBUG, BeanShell.class,
							"+++ SearchAndReplace.880: path = " + path);

					if (buffer == null)
						continue loop;

					// Wait for the buffer to load
					if (!buffer.isLoaded())
						VFSManager.waitForRequests();

					int start, end = 0;
					JEditTextArea textArea = view.getTextArea();
					if (view.getBuffer() == buffer && !repeat)
					{
						// eval findFromTop
						if (fromTop && !ignoreFromTop)
						{
							if (rowSearchEnabled)
								start = buffer
									.getLineStartOffset(rowSearchLeftRow);
							else
								start = 0;
							if (debug)
								Log.log(Log.DEBUG, BeanShell.class,
									"fromTop invoked");
						}
						else
						{
							Selection s = textArea
								.getSelectionAtOffset(textArea
									.getCaretPosition());
							if (s == null)
								start = textArea.getCaretPosition();
							else if (_reverse)
								start = s.getStart();
							else
								start = s.getEnd();
						}
						if (debug)
							Log.log(Log.DEBUG, BeanShell.class,
								"tp508: start = " + start);
					}
					else if (_reverse)
						start = buffer.getLength();
					else
					{
						if (rowSearchEnabled)
							start = buffer
								.getLineStartOffset(rowSearchLeftRow);
						else
							start = 0;
					}
					if (rowSearchEnabled
						&& rowSearchRightRow < buffer.getLineCount())
						end = buffer.getLineEndOffset(rowSearchRightRow);
					else
						end = buffer.getLength();
					if (find(view, buffer, start, end, repeat, _reverse, false))
					{
						if (debug)
							Log.log(Log.DEBUG, BeanShell.class,
								"SearchAndReplace.674: found at start = "
									+ start + ", search = "
									+ search);
						testMatcher();
						ignoreFromTop = true;
						// if "find" was restarted, ask
						// user if wrapping allowed
						if (debug)
							Log.log(Log.DEBUG, BeanShell.class,
								"tp591: lastSelection = "
									+ lastSelection);
						if (lastSelection != null)
						{
							if (textArea.getCaretPosition() != lastCaret)
							{
								Integer[] args = { new Integer(
									_reverse ? 1 : 0) };
								if (GUIUtilities
									.confirm(
										view,
										"keepsearching",
										args,
										JOptionPane.YES_NO_OPTION,
										JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
								{
									lastSelection = null;
									return true;
								}
							}
							// reset Selection to
							// former state
							textArea.setSelection(lastSelection);
							textArea.moveCaretPosition(lastCaret, true);
						}
						else
						{
							if (findAll)
							{
								/* find all was selected:
								 * mark all matches display status line
								 * setSelection move cursor to each Selection to
								 * set it visible reset findAll flag */
								Selection[] foundSelections = (Selection[]) findAllSelections
									.toArray(new Selection[1]);
								view.getStatus().setMessageAndClear(jEdit.getProperty(
									"view.status.findAll.number-of-occurance-found",
									new Object[] { new Integer(foundSelections.length) }));
								if (debug)
									for (int i = 0; i < foundSelections.length; i++)
										Log.log(Log.DEBUG, BeanShell.class,
											"SearchAndReplace.706: foundSelections[i].getEnd() = "
											+ foundSelections[i].getEnd()
											+ ", foundSelections[i].getStart() = "
											+ foundSelections[i].getStart());
								textArea.setSelection(foundSelections);
								for (int i = foundSelections.length - 1; i >= 0; i--)
									textArea.moveCaretPosition(foundSelections[i].getEnd());
								findAll = false;
							}
							return true;
						}
					}
				}

				if (repeat)
				{
					if (!BeanShell.isScriptRunning())
					{
						view.getStatus().setMessageAndClear(
								jEdit.getProperty("view.status.search-not-found")
								+ (SearchAndReplace.search.endsWith(" ") ?
								     jEdit.getProperty("view.status.search-ends-with-blank-warning") : ""));

						view.getToolkit().beep();
					}
					return false;
				}

				boolean restart;

				// if auto wrap is on, always restart search.
				// if auto wrap is off, and we're called from
				// a macro, stop search. If we're called
				// interactively, ask the user what to do.
				if (wrap)
				{
					if (!BeanShell.isScriptRunning())
					{
						view.getStatus().setMessageAndClear(
							jEdit.getProperty("view.status.auto-wrap"));
						// beep if beep property set
						if (jEdit.getBooleanProperty("search.beepOnSearchAutoWrap"))
						{
							view.getToolkit().beep();
						}
					}
					restart = true;
				}
				else if (BeanShell.isScriptRunning())
				{
					restart = false;
				}
				else
				{
					if (fromTop)
					{
						// rwchg: don't ask for wrap if
						// search from top. Give a
						// message instead
						if (!BeanShell.isScriptRunning())
						{
							view.getStatus().setMessageAndClear(
									jEdit.getProperty("view.status.no-further-search-string-found")
										+ (SearchAndReplace.search.endsWith(" ") ? jEdit
										   .getProperty("view.status.search-ends-with-blank-warning"): ""));
						}
						ignoreFromTop = false; // v0.7:
						// enable
						// re-find
						// after
						// "no
						// further
						// found"
						restart = false;
					}
					else
					{
						/*
						 * {{{ it is better to first
						 * check if there are further
						 * matches when keeping
						 * searching. therefore, the
						 * current selection has to be
						 * memorized, and the user
						 * dialog done after
						 * ignoreFromTop
						 * 
						 * Integer[] args = { new
						 * Integer(reverse ? 1 : 0) }; //
						 * rwchg: error in
						 * GUIUtilities.confirm !!! int
						 * result =
						 * GUIUtilities.confirm(comp,
						 * "keepsearching",args,
						 * JOptionPane.YES_NO_OPTION,
						 * JOptionPane.QUESTION_MESSAGE);
						 * restart = (result ==
						 * JOptionPane.YES_OPTION); }}}
						 */
						lastSelection = view.getTextArea().getSelection();
						lastCaret = view.getTextArea().getCaretPosition();
						if (lastSelection.length > 0 && debug)
							Log.log(Log.DEBUG, BeanShell.class,
								"tp662: lastSelection = "
								+ view.getTextArea().getSelectedText(lastSelection[0]));
						restart = true;
					}
				}

				if (restart)
				{
					// start search from beginning
					path = fileset.getFirstFile(view);
					repeat = true;
				}
				else
					break loop;
			} // }}}
		}
		catch (Exception e)
		{
			handleError(view, e);
		}
		finally
		{
			view.hideWaitCursor();
		}

		return false;
	} // }}}

	// {{{ find() method
	/**
	 * Finds the next instance of the search string in the specified buffer.
	 * 
	 * @param view
	 *                The view
	 * @param buffer
	 *                The buffer
	 * @param start
	 *                Location where to start the search
	 */
	public static boolean find(View view, Buffer buffer, int start) throws Exception
	{
		return find(view, buffer, start, buffer.getLength(), false, false, false);
	} // }}}

	// {{{ find() method
	/**
	 * Finds the next instance of the search string in the specified buffer.
	 * 
	 * @param view
	 *                The view
	 * @param buffer
	 *                The buffer
	 * @param start
	 *                Location where to start the search
	 * @param end
	 *                Location where to end the search
	 * @param firstTime
	 *                See {@link SearchMatcher#nextMatch(CharIndexed,
	 *                boolean,boolean,boolean,boolean)}.
	 * @param silent
	 *                Enables silent search, without highlighting the
	 *                matched string the result is to be retrieved via
	 *                getLastMatchedSelection
	 * @since jEdit 4.1pre7
	 */
	public static boolean find(View view, Buffer buffer, int start, int end, boolean firstTime,
		boolean reverse, boolean silent) throws Exception
	{
		SearchMatcher matcher = getSearchMatcher();
		if (matcher == null)
		{
			view.getToolkit().beep();
			return false;
		}

		Segment text = new Segment();
		// remark: Segment is like a String reference without copying
		// ==> buffer.getText doesn't copy the whole buffer
		// Therefore, multiple assignment of "text" shouldn't be a
		// performance problem
		boolean xFound = false; // matched extended options, too
		if (findAll)
			findAllSelections = new ArrayList<Selection>();
		// rwchg loop for xsearch-check
		int secCnt = 1000000;
		if (debug)
		{
			secCnt = 100;
			Log.log(Log.DEBUG, BeanShell.class, "+++ SearchAndReplace.1138: secCnt = "
				+ secCnt);
		}
		while (!xFound && secCnt-- > 0)
		{
			if (reverse)
				buffer.getText(0, start, text);
			else
				// buffer.getText(start,buffer.getLength() -
				// start,text);
				buffer.getText(start, end - start > 0 ? end - start : 0, // maybe
					// start
					// >
					// end
					text);

			// the start and end flags will be wrong with reverse
			// search enabled,
			// but they are only used by the regexp matcher, which
			// doesn't
			// support reverse search yet.
			//
			// REMIND: fix flags when adding reverse regexp search.
			// int[] match = matcher.nextMatch(new
			// CharIndexedSegment(text,reverse),
			// start == 0,true,firstTime,reverse);
			// even if regexp reverse, we do implicitly a forward
			// search
			// ==> create CharIndexedSegment as forward
			SearchMatcher.Match match = matcher.nextMatch(new CharIndexedSegment(text,
				(matcher instanceof RESearchMatcher) ? false : reverse),
				start == 0, end == buffer.getLength(), firstTime, reverse);

			if (match != null)
			{
				int matchOffsetBegin, matchOffsetEnd; // offset
				// inside
				// buffer
				if (reverse)
				{
					if (matcher instanceof RESearchMatcher)
					{
						// regexp backward search always
						// starts from 0
						// ==> no match calculation
						// neccessary
						matchOffsetBegin = match.start;
						matchOffsetEnd = match.end;
					}
					else
					{
						matchOffsetBegin = start - match.end;
						matchOffsetEnd = start - match.start;
					}
				}
				else
				{
					matchOffsetBegin = start + match.start;
					matchOffsetEnd = start + match.end;
				}

				if (debug)
					Log.log(Log.DEBUG, BeanShell.class,
						"SearchAndReplace.1181: matched at matchOffsetBegin = "
							+ matchOffsetBegin);

				if (checkXSearchParameters(view.getTextArea(), buffer,
					matchOffsetBegin, matchOffsetEnd, false))
				{
					jEdit.commitTemporary(buffer);
					view.setBuffer(buffer);
					JEditTextArea textArea = view.getTextArea();

					lastMatchedSelection = new Selection.Range(
						matchOffsetBegin, matchOffsetEnd);
					if (reverse)
					{
						// lastMatchedSelection = new
						// Selection.Range(matchOffsetBegin,
						// matchOffsetEnd);
						// start - match.start,
						// start - match[0]);
						if (!silent)
						{
							textArea.setSelection(lastMatchedSelection);
							// make sure end of
							// match is visible
							textArea.scrollTo(matchOffsetEnd, false);
							textArea
								.moveCaretPosition(matchOffsetBegin);
						}
					}
					else
					{
						// lastMatchedSelection = new
						// Selection.Range(matchOffsetBegin,
						// matchOffsetEnd);
						// start + match[0],
						// start + match.start);
						if (!silent)
						{
							if (findAll)
							{
								findAllSelections.add(lastMatchedSelection);
								start += match.end;
							}
							else
							{
								textArea.setSelection(lastMatchedSelection);
								// make sure start of match is visible
								textArea.scrollTo(matchOffsetBegin, false);
								textArea.moveCaretPosition(matchOffsetEnd);
							}
						}
					}
					if (!findAll)
						xFound = true;
				}
				else
				{
					// target matched, but nok because of
					// xParameters
					// find next
					if (reverse)
						start -= match.end;
					else
						start += match.end;
					// Log.log(Log.DEBUG,
					// BeanShell.class,"SearchAndReplace.901:
					// start = "+start);
				}
			}
			else
			{
				if (findAll && !findAllSelections.isEmpty())
				{
					// previously we did find anything ==>
					// set xFound
					xFound = true;
				}
				else
					return false;
			}
		}
		return xFound; // always true
	} // }}}

	// {{{ findSilent() method
	/**
	 * Finds the next instance of the search string in the specified buffer,
	 * without highlighting the match The match can be obtained via
	 * "getLastMatchedSelection"
	 * 
	 * @param view
	 *                The view
	 * @param buffer
	 *                The buffer
	 * @param start
	 *                Location where to start the search
	 */
	public static boolean findSilent(View view, Buffer buffer, int start)
	{
		try
		{
			return find(view, buffer, start, buffer.getLength(), false, false,
				FIND_OPTION_SILENT);
		}
		catch (Exception e)
		{
			Log.log(Log.ERROR, SearchAndReplace.class, e);
			Object[] args = { e.getMessage() };
			if (args[0] == null)
				args[0] = e.toString();
			GUIUtilities.error(view, "searcherror", args);
		}
		return false;
	}

	// {{{ replace() method
	/**
	 * Replaces the current selection with the replacement string.
	 * 
	 * @param view
	 *                The view
	 * @return True if the operation was successful, false otherwise
	 */
	public static boolean replace(View view)
	{
		// component that will parent any dialog boxes
		XSearchPanel panel = XSearchPanel.getSearchPanel(view);
		// panel.setCurrentSelection();

		Component comp = panel;
		if (comp == null)
			comp = view;

		JEditTextArea textArea = view.getTextArea();

		Buffer buffer = view.getBuffer();
		if (!buffer.isEditable())
		{
			view.getStatus().setMessageAndClear(
				jEdit.getProperty("view.status.replace.buffer-is-not-editable"));
			return false;
		}

		boolean smartCaseReplace = getSmartCaseReplace();

		Selection[] selection = textArea.getSelection();
		if (selection.length == 0)
		{
			if (find(view))
			{
				if (debug)
					Log.log(Log.DEBUG, BeanShell.class,
						"SearchAndReplace.1058");
				selection = textArea.getSelection();
			}
			if (selection.length == 0)
			{
				view.getToolkit().beep();
				return false;
			}
		}

		record(view, "replace(view)", true, false);

		// a little hack for reverse replace and find
		int caret = textArea.getCaretPosition();
		Selection s = textArea.getSelectionAtOffset(caret);
		if (s != null)
			caret = s.getStart();

		try
		{
			buffer.beginCompoundEdit();

			SearchMatcher matcher = getSearchMatcher();
			if (matcher == null)
				return false;

			initReplace();

			int retVal = 0;

			for (int i = 0; i < selection.length; i++)
			{
				s = selection[i];

				retVal += replaceInSelection(view, textArea, buffer, matcher,
					smartCaseReplace, s);
			}
			boolean _reverse = reverse
				&& fileset instanceof org.gjt.sp.jedit.search.CurrentBufferSet;
			if (_reverse)
			{
				// so that Replace and Find continues from
				// the right location
				textArea.moveCaretPosition(caret);
			}
			else
			{
				s = textArea.getSelectionAtOffset(textArea.getCaretPosition());
				if (s != null)
					textArea.moveCaretPosition(s.getEnd());
			}

			if (retVal == 0)
			{
				view.getToolkit().beep();
				return false;
			}

			return true;
		}
		catch (Exception e)
		{
			handleError(comp, e);
		}
		finally
		{
			buffer.endCompoundEdit();
		}

		return false;
	} // }}}

	// {{{ replace() method
	/**
	 * Replaces text in the specified range with the replacement string.
	 * 
	 * @param view
	 *                The view
	 * @param buffer
	 *                The buffer
	 * @param start
	 *                The start offset
	 * @param end
	 *                The end offset
	 * @return True if the operation was successful, false otherwise
	 */
	public static boolean replace(View view, Buffer buffer, int start, int end)
	{
		if (!buffer.isEditable())
			return false;

		// component that will parent any dialog boxes
		XSearchPanel panel = XSearchPanel.getSearchPanel(view);

		Component comp = panel;
		if (comp == null)
			comp = view;

		boolean smartCaseReplace = getSmartCaseReplace();

		try
		{
			buffer.beginCompoundEdit();

			SearchMatcher matcher = getSearchMatcher();
			if (matcher == null)
				return false;

			int retVal = 0;

			retVal += _replace(view, buffer, matcher, start, end, smartCaseReplace,
				null);

			if (retVal != 0)
				return true;
		}
		catch (Exception e)
		{
			handleError(comp, e);
		}
		finally
		{
			buffer.endCompoundEdit();
		}

		return false;
	} // }}}

	// {{{ replaceAll() method
	public static boolean replaceAll(View view)
	{
		return replaceAll(view, false);
	}

	/**
	 * Replaces all occurances of the search string with the replacement
	 * string.
	 * 
	 * @param view
	 *                The view
	 * @param showChanges
	 *                Flag indicating that all replacements are displayed in
	 *                hyper window
	 */
	public static boolean replaceAll(View view, boolean showChanges)
	{
		// component that will parent any dialog boxes
		Component comp = XSearchPanel.getSearchPanel(view);
		if (comp == null)
			comp = view;

		int fileCount = 0;
		int occurCount = 0;

		if (fileset.getFileCount(view) == 0)
		{
			GUIUtilities.error(comp, "empty-fileset", null);
			return false;
		}

		record(view, "replaceAll(view)", true, true);

		view.showWaitCursor();

		boolean smartCaseReplace = (replace != null && TextUtilities.getStringCase(replace) == TextUtilities.LOWER_CASE);

		/***************************************************************
		 * allocate tree for display of replacements
		 **************************************************************/
		DefaultMutableTreeNode rootSearchNode = null;
		HyperSearchResults results = null;
		if (showChanges)
		{
			view.getDockableWindowManager().addDockableWindow(HyperSearchResults.NAME);
			results = (HyperSearchResults) view.getDockableWindowManager().getDockable(
				HyperSearchResults.NAME);
			results.searchStarted();

			rootSearchNode = new DefaultMutableTreeNode("replaceAll \"" + search
				+ "\" with \"" + replace + "\"");
		}

		try
		{
			SearchMatcher matcher = getSearchMatcher();
			if (matcher == null)
				return false;

			initReplace();

			String path = fileset.getFirstFile(view);
			loop: while (path != null)
			{
				Buffer buffer = jEdit.openTemporary(view, null, path, false);

				/*
				 * this is stupid and misleading. but 'path' is
				 * not used anywhere except the above line, and
				 * if this is done after the 'continue', then we
				 * will either hang, or be forced to duplicate
				 * it inside the buffer == null, or add a
				 * 'finally' clause. you decide which one's
				 * worse.
				 */
				path = fileset.getNextFile(view, path);

				if (buffer == null)
					continue loop;

				// Wait for buffer to finish loading
				if (buffer.isPerformingIO())
					VFSManager.waitForRequests();

				if (!buffer.isEditable())
					continue loop;

				// register replacements of this buffer in
				// bufferNode
				final DefaultMutableTreeNode bufferNode = new DefaultMutableTreeNode(
				// buffer.getPath());
					new HyperSearchPath(buffer, 0, 0, 0));
				// Leave buffer in a consistent state if
				// an error occurs
				int retVal = 0;

				int start, end;
				if (rowSearchEnabled)
				{
					if (rowSearchLeftRow <= buffer.getLineCount())
						start = buffer.getLineStartOffset(rowSearchLeftRow);
					else
						// this may occur cause settings
						// apply to all buffers
						start = buffer.getLength();
					if (rowSearchRightRow < buffer.getLineCount())
						end = buffer.getLineEndOffset(rowSearchRightRow);
					else
						end = buffer.getLength();
				}
				else
				{
					start = 0;
					end = buffer.getLength();
				}
				try
				{
					buffer.beginCompoundEdit();
					retVal = _replace(view, buffer, matcher,
					// 0, buffer.getLength(),
						start, end, smartCaseReplace,
						showChanges ? bufferNode : null);
				}
				finally
				{
					buffer.endCompoundEdit();
				}

				if (retVal != 0)
				{
					if (showChanges)
						rootSearchNode.insert(bufferNode, rootSearchNode
							.getChildCount());
					fileCount++;
					occurCount += retVal;
					jEdit.commitTemporary(buffer);
				}
			}
		}
		catch (Exception e)
		{
			handleError(comp, e);
		}
		finally
		{
			view.hideWaitCursor();
		}

		/* Don't do this when playing a macro, cos it's annoying */
		if (!BeanShell.isScriptRunning())
		{
			Object[] args = { new Integer(occurCount), new Integer(fileCount) };
			view.getStatus().setMessageAndClear(
				jEdit.getProperty("view.status.replace-all", args));
			if (occurCount == 0)
				view.getToolkit().beep();
			else if (showChanges)
			{
				results.searchDone(rootSearchNode);
			}
		}

		return (fileCount != 0);
	} // }}}

	// }}}

	// {{{ load() method
	/**
	 * Loads search and replace state from the properties.
	 */
	public static void load()
	{
		Log.log(Log.DEBUG, SearchAndReplace.class, "+++ SearchAndReplace.1594");
		search = jEdit.getProperty("search.ext.find.value");
		origSearch = search;
		replace = jEdit.getProperty("search.ext.replace.value");
		ignoreCase = jEdit.getBooleanProperty("search.ext.ignoreCase.toggle");
		regexp = jEdit.getBooleanProperty("search.ext.regexp.toggle");
		beanshell = jEdit.getBooleanProperty("search.ext.beanshell.toggle");
		wrap = jEdit.getBooleanProperty("search.ext.wrap.toggle");

		// load extended properties
		fromTop = jEdit.getBooleanProperty("search.ext.fromTop.toggle");
		tentativSearch = jEdit.getBooleanProperty("search.ext.tentativ.toggle");
		columnSearchEnabled = jEdit.getBooleanProperty("search.ext.column.toggle");
		columnSearchExpandTabs = jEdit
			.getBooleanProperty("search.ext.column.expand-tabs.toggle");
		columnSearchLeftCol = jEdit.getIntegerProperty("search.ext.column.left.value", 0);
		columnSearchRightCol = jEdit.getIntegerProperty("search.ext.column.right.value", 0);
		rowSearchEnabled = jEdit.getBooleanProperty("search.ext.row.toggle");
		rowSearchLeftRow = jEdit.getIntegerProperty("search.ext.row.left.value", 0);
		rowSearchRightRow = jEdit.getIntegerProperty("search.ext.row.right.value", 0);
		wordPart = jEdit.getIntegerProperty("search.ext.wordpart.value",
			XSearch.SEARCH_PART_NONE);
		foldSearch = jEdit.getIntegerProperty("search.ext.foldSearch.value",
			XSearch.SEARCH_IN_OUT_NONE);
		commentSearch = jEdit.getIntegerProperty("search.ext.commentSearch.value",
			XSearch.SEARCH_IN_OUT_NONE);
		hyperRangeUpper = -1;
		hyperRangeLower = -1;
		findAll = false;
		fileset = new org.gjt.sp.jedit.search.CurrentBufferSet();

		// Tags plugin likes to call this method at times other than
		// startup; so we need to fire a SearchSettingsChanged to
		// notify the search bar and so on.
		matcher = null;
		// EditBus.send(new SearchSettingsChanged(null));
	} // }}}

	// {{{ save() method
	/**
	 * Saves search and replace state to the properties.
	 */
	public static void save()
	{
		jEdit.setProperty("search.ext.find.value", search);
		jEdit.setProperty("search.ext.replace.value", replace);
		jEdit.setBooleanProperty("search.ext.ignoreCase.toggle", ignoreCase);
		jEdit.setBooleanProperty("search.ext.regexp.toggle", regexp);
		jEdit.setBooleanProperty("search.ext.beanshell.toggle", beanshell);
		jEdit.setBooleanProperty("search.ext.wrap.toggle", wrap);
		// save extended properties
		jEdit.setBooleanProperty("search.ext.fromTop.toggle", fromTop);
		jEdit.setBooleanProperty("search.ext.tentativ.toggle", tentativSearch);
		jEdit.setBooleanProperty("search.ext.column.toggle", columnSearchEnabled);
		jEdit.setBooleanProperty("search.ext.column.expand-tabs.toggle",
			columnSearchExpandTabs);
		jEdit.setIntegerProperty("search.ext.column.left.value", columnSearchLeftCol);
		jEdit.setIntegerProperty("search.ext.column.right.value", columnSearchRightCol);
		jEdit.setBooleanProperty("search.ext.row.toggle", rowSearchEnabled);
		jEdit.setIntegerProperty("search.ext.row.left.value", rowSearchLeftRow);
		jEdit.setIntegerProperty("search.ext.row.right.value", rowSearchRightRow);
		jEdit.setIntegerProperty("search.ext.wordpart.value", wordPart);
		jEdit.setIntegerProperty("search.ext.foldSearch.value", foldSearch);
		jEdit.setIntegerProperty("search.ext.commentSearch.value", commentSearch);

		/*
		 * jEdit.setBooleanProperty("search.show-settings.toggle",showSettings);
		 * jEdit.setBooleanProperty("search.show-replace.toggle",showReplace);
		 * jEdit.setBooleanProperty("search.show-extended.toggle",showExtended);
		 */
	} // }}}

	// {{{ handleError() method
	static void handleError(Component comp, Exception e)
	{
		Log.log(Log.ERROR, SearchAndReplace.class, e);
		if (comp instanceof Dialog)
		{
			new TextAreaDialog((Dialog) comp, beanshell ? "searcherror-bsh"
				: "searcherror", e);
		}
		if (comp instanceof JPanel)
		{
			JFrame frame = new JFrame();
			frame.add(comp);
			new TextAreaDialog(frame, beanshell ? "searcherror-bsh" : "searcherror", e);
		}
		else
		{
			new TextAreaDialog((Frame) comp, beanshell ? "searcherror-bsh"
				: "searcherror", e);
		}
	} // }}}

	// {{{ Private members

	// {{{ Instance variables
	private static String search;

	private static String origSearch;

	private static String replace;

	private static BshMethod replaceMethod;

	private static NameSpace replaceNS = new NameSpace(BeanShell.getNameSpace(), BeanShell
		.getNameSpace().getClassManager(), "search and replace");

	private static boolean regexp;

	private static boolean ignoreCase;

	private static boolean reverse; // search backward

	private static boolean fromTop; // search from top

	private static boolean beanshell; // use bean shell snippet

	private static boolean wrap; // wrap search

	private static boolean ignoreFromTop; // rwchg: ignore "fromTop", when

	// multiple "find" invoked

	private static boolean findAll;

	public static boolean tentativSearch; // bs:

	// SearchAndReplace.tentativSearch=true

	private static SearchMatcher matcher;

	private static org.gjt.sp.jedit.search.SearchFileSet fileset;

	private static ArrayList<Selection> findAllSelections;

	private static Selection lastMatchedSelection;

	private static Map<Mode, CommentStruct> modeToCommentsMap;

	/*
	 * selectiv display private static boolean showSettings; // display the
	 * standart search settings private static boolean showReplace; //
	 * display the replace options and buttons private static boolean
	 * showExtended; // display the extended options and buttons
	 */
	// column search
	private static boolean columnSearchEnabled;

	private static boolean columnSearchExpandTabs;

	private static int columnSearchLeftCol;

	private static int columnSearchRightCol;

	// row search
	private static boolean rowSearchEnabled;

	private static int rowSearchLeftRow;

	private static int rowSearchRightRow;

	// hyperRange
	private static int hyperRangeUpper;

	private static int hyperRangeLower;

	// comment search
	private static int commentSearch = XSearch.SEARCH_IN_OUT_NONE;

	// folding search
	private static int foldSearch = XSearch.SEARCH_IN_OUT_NONE;

	// word part search
	private static int wordPart = XSearch.SEARCH_PART_NONE;

	private static final String keyboard = "12345567890� qwertzuiop�+ asdfghjkl��# <yxcvbnm,.-";

	// }}}

	// {{{ initReplace() method
	/**
	 * Set up BeanShell replace if necessary.
	 */
	private static void initReplace() throws Exception
	{
		if (beanshell && replace.length() != 0)
		{
			replaceMethod = BeanShell.cacheBlock("replace",
				"return (" + replace + ");", true);
		}
		else
			replaceMethod = null;
	} // }}}

	// {{{ areSearchSettingsOk
	/**
	 * checks if the search settings are consistent. not allowed
	 * combinations: - searchback and regexp (wordpart and tentativ are
	 * regexp, too)
	 */
	private static boolean areSearchSettingsOk()
	{
		// if (reverse && (regexp || wordPart !=
		// XSearchPanel.SEARCH_PART_NONE || tentativSearch))
		// return false;

		return true;
	} // }}}

	// {{{ constructTentativSearchString method
	/**
	 * Translates a String into a regexp which shall find following
	 * mistypings: - typed 'dash' instead of 'underscore' - double typed
	 * char (toown ico town)
	 * 
	 * @param searchString
	 */
	private static String constructTentativSearchString(String searchString)
	{
		Log.log(Log.DEBUG, BeanShell.class, "+++ SearchAndReplace.1724: searchString = "
			+ searchString);
		StringBuffer dest = new StringBuffer();
		// iterate over searchString. As the loop works one char ahead,
		// we add a blank
		StringCharacterIterator it = new StringCharacterIterator(new String(searchString
			+ " "));
		char prevChar = ' ';
		// char pre2Char = ' ';
		boolean skipFirst = true; // we need the following
		// character, so process one
		// char later
		// read characters which will be included always in search
		// characters
		// String tendCharacters =
		// jEdit.getProperty("search.ext.tentativ.addition");
		boolean doubledChar = false;
		for (char currChar = it.first(); currChar != CharacterIterator.DONE; currChar = it
			.next())
		{
			if (skipFirst)
				skipFirst = false;
			else
			{
				if (currChar != prevChar)
				{
					if (prevChar == '-')
						dest.append("[-_]");
					else
						dest
							.append(charsToEscapes(String
								.valueOf(prevChar)));
					if (doubledChar)
						dest.append("+");
					doubledChar = false;
				}
				else
					doubledChar = true;
			}
			prevChar = currChar;
		}
		Log.log(Log.DEBUG, BeanShell.class, "+++ SearchAndReplace.1752: dest = " + dest);
		return dest.toString();
	} // }}}

	// {{{ constructTentativSearchStringOld method
	/**
	 * Translates a String into a regexp which shall find following
	 * mistypings: - double typed char (toown ico town) - few char (ton ico
	 * toon) - wrong char typed (Rzdi ico Rudi) - wrong sequence (Rdui ico
	 * Rudi)
	 * 
	 * @param searchString
	 */
	private static String constructTentativSearchStringOld(String searchString)
	{
		StringBuffer dest = new StringBuffer();
		// iterate over searchString. As the loop works one char ahead,
		// we add a blank
		StringCharacterIterator it = new StringCharacterIterator(new String(searchString
			+ " "));
		char prevChar = ' ';
		char pre2Char = ' ';
		boolean skipFirst = true; // we need the following
		// character, so process one
		// char later
		// read characters which will be included always in search
		// characters
		String tendCharacters = jEdit.getProperty("search.ext.tentativ.addition");
		for (char c = it.first(); c != CharacterIterator.DONE; c = it.next())
		{
			char currChar = Character.toLowerCase(c);
			if (skipFirst)
				skipFirst = false;
			else
			{
				if (pre2Char != prevChar)
				{
					// skip identical characters
					StringBuffer charBuff = new StringBuffer();
					int keyBoardIdx = keyboard.indexOf(prevChar);
					if (keyBoardIdx != -1 && !Character.isWhitespace(prevChar))
					{
						int leftIdx = keyBoardIdx - 1;
						if (leftIdx < 0)
							leftIdx = 0;
						int rightIdx = keyBoardIdx + 2;
						if (rightIdx > keyboard.length())
							rightIdx = keyboard.length();
						charBuff.append(keyboard.substring(leftIdx,
							rightIdx).trim());
					}
					else
					{
						charBuff.append(prevChar);
					}
					// add previous and following char to
					// list, skip if already in charBuff
					if (!Character.isWhitespace(pre2Char)
						&& charBuff.toString().indexOf(pre2Char) == -1)
						charBuff.append(pre2Char);
					if (!Character.isWhitespace(currChar)
						&& charBuff.toString().indexOf(currChar) == -1)
						charBuff.append(currChar);
					// add uppercases
					String lcString = charBuff.toString();
					StringCharacterIterator lcIter = new StringCharacterIterator(
						lcString);
					for (char lc = lcIter.first(); lc != CharacterIterator.DONE; lc = lcIter
						.next())
					{
						if (Character.isLowerCase(lc))
							charBuff.append(Character.toUpperCase(lc));
					}
					// add tend characters in list, skip if
					// already in charBuff
					StringCharacterIterator tendIter = new StringCharacterIterator(
						tendCharacters);
					for (char tc = tendIter.first(); tc != CharacterIterator.DONE; tc = tendIter
						.next())
					{
						if (charBuff.toString().indexOf(tc) == -1)
							charBuff.append(tc);
					}
					// Log.log(Log.DEBUG,
					// BeanShell.class,"SearchAndReplace.1351:
					// charBuff = "+charBuff);
					dest.append("[" + charsToEscapes(charBuff.toString())
						+ "]+");
					// Log.log(Log.DEBUG,
					// BeanShell.class,"SearchAndReplace.1353:
					// dest = "+dest);
				}
			}
			pre2Char = prevChar;
			prevChar = currChar;
		}
		return dest.toString();
	} // }}}

	// {{{ charsToEscapes() method
	/**
	 * Escapes all regexp special characters in the specified string.
	 * 
	 * @param str
	 *                The string
	 */
	public static String charsToEscapes(String str)
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			if ("+*()[]./{}^$?|\"\\".indexOf(c) != -1)
			{
				buf.append("\\");
			}
			buf.append(c);
		}
		return buf.toString();
	} // }}}

	// {{{ checkXSearchParameters method
	/**
	 * Checks if the search result matches the extended parameters, too
	 * 
	 * @param textArea
	 * @param buffer
	 *                The buffer
	 * @param matchBegin
	 *                Location where match starts
	 * @param matchBegin
	 *                Location where match ends
	 */
	static boolean checkXSearchParameters(JEditTextArea textArea, Buffer buffer,
		int matchBegin, int matchEnd, boolean hyperSearchFlag)
	{
		if (debug)
			Log.log(Log.DEBUG, BeanShell.class, "checkXsp: matchBegin = " + matchBegin
				+ ", matchEnd = " + matchEnd);
		boolean xMatchOk = true;
		int matchLine = buffer.getLineOfOffset(matchBegin);
		int startMatchLine = buffer.getLineStartOffset(matchLine);
		// int startMatchEndLine =
		// buffer.getLineStartOffset(buffer.getLineOfOffset(matchEnd));
		if (debug)
			Log.log(Log.DEBUG, BeanShell.class, "tp1111: matchLine = " + matchLine
				+ ", startMatchLine = " + startMatchLine);
		/***************************************************************
		 * check fold status
		 **************************************************************/
		if (xMatchOk && foldSearch != XSearch.SEARCH_IN_OUT_NONE)
		{
			if (foldSearch == XSearch.SEARCH_IN_OUT_OUTSIDE
				^ textArea.getDisplayManager().isLineVisible(matchLine))
			{
				xMatchOk = false;
			}
		}
		/***************************************************************
		 * check comment status
		 **************************************************************/
		if (xMatchOk && commentSearch != XSearch.SEARCH_IN_OUT_NONE)
		{
			// now we know if we are inside / outside of a comment
			// ==> evaluate request
			if (commentSearch == XSearch.SEARCH_IN_OUT_OUTSIDE ^ // xor
				isOutsideComments(textArea, buffer, matchBegin, hyperSearchFlag))
				// outsideCmt)
				xMatchOk = false;
		}
		/***************************************************************
		 * check columns
		 **************************************************************/
		if (columnSearchEnabled)
		{
			// reject if match on multiple lines
			if (buffer.getLineOfOffset(matchEnd) != matchLine)
				xMatchOk = false;
			else
			{
				// Log.log(Log.DEBUG,
				// BeanShell.class,"matchBegin = "+matchBegin+",
				// matchEnd = "+matchEnd);
				// BeanShellUtility bsu = new
				// BeanShellUtility(view);
				if (columnSearchExpandTabs)
				{
					int matchRowStart = matchBegin - startMatchLine;
					int matchRowEnd = matchEnd - startMatchLine;
					matchBegin = startMatchLine
						+ XSearchBeanShellUtility.getVisiblePosition(buffer
							.getTabSize(), matchRowStart, buffer
							.getLineText(matchLine));
					matchEnd = startMatchLine
						+ XSearchBeanShellUtility.getVisiblePosition(buffer
							.getTabSize(), matchRowEnd, buffer
							.getLineText(matchLine));
				}
				// check match inside columns ( add +1 to
				// ranges, because visible row starts with 1)
				if (debug)
					Log.log(Log.DEBUG, BeanShell.class, "tp1197: matchBegin = "
						+ matchBegin + ", matchLine = " + matchLine
						+ ", buffer.getLineStartOffset(matchLine) = "
						+ buffer.getLineStartOffset(matchLine)
						+ ", columnSearchLeftCol = " + columnSearchLeftCol);
				if (matchBegin - buffer.getLineStartOffset(matchLine) < columnSearchLeftCol - 1)
				{
					xMatchOk = false;
				}
				if (debug)
					Log.log(Log.DEBUG, BeanShell.class, "tp1203: matchEnd = "
						+ matchEnd + "matchLine = " + matchLine
						+ "buffer.getLineStartOffset(matchLine) = "
						+ buffer.getLineStartOffset(matchLine)
						+ "columnSearchRightCol = " + columnSearchRightCol);
				if (matchEnd - buffer.getLineStartOffset(matchLine) > columnSearchRightCol)
				{
					xMatchOk = false;
				}
			}
		}
		/************************************************************************************
		 ************************************************************************************/
		if (debug)
			Log.log(Log.DEBUG, BeanShell.class, "checkXsp: xMatchOk = " + xMatchOk);
		return xMatchOk;
	}

	// }}}

	/**
	 * Gets the mode attribute of the SearchAndReplace class
	 * 
	 * @param buffer
	 *                Description of the Parameter
	 * @return The mode value
	 */
	private static Mode getMode(Buffer buffer)
	{
		Mode buffermode = buffer.getMode();
		if (buffermode == null)
		{
			Mode[] modes = jEdit.getModes();
			// this must be in reverse order so that modes from the
			// user
			// catalog get checked first!
			for (int i = modes.length - 1; i >= 0; i--)
			{
				if (modes[i].accept(buffer.getName(), ""))
				{
					return modes[i];
				}
			}
		}
		return buffermode;
	}

	// {{{ isOutsideComments(int currPos) method
	/**
	 * Checks if the passed buffer position is inside comments Note: in case
	 * of hypersearch, the token methode doesn't work (Edit Mode not parsed
	 * yet ?!) therefore, a simplified methode with default comment signs is
	 * used Improvement: arr=jEdit.getModes()
	 * arr[buffer.getMode().toString()].loadIfNecessary()
	 * arr[20].getProperty("commentStart") arr[20].getProperty("commentEnd")
	 * arr[20].getProperty("lineComment")
	 * 
	 * @param textArea
	 * @param buffer
	 * @param currPos
	 *                Position in buffer to be checked
	 * @param hyperSearchFlag
	 *                Indicates if normal or hypersearch
	 */

	private static boolean isOutsideComments(JEditTextArea textArea, Buffer buffer,
		int currPos, boolean hyperSearchFlag)
	{
		// Note: as getToken doesn't work for blanks, we have to find
		// the first nonblank
		// int caretPos = textArea.getCaretPosition();

		// RW: initialized outsideCmt because too difficult to cover all
		// else-cases
		boolean outsideCmt = true;
		if (debug)
			Log.log(Log.DEBUG, BeanShell.class,
				"+++ SearchAndReplace.isOutsideCmt.1868: currPos = " + currPos
					+ ", buffer.getMode() = " + buffer.getMode()
					+ ", buffer.getName() = " + buffer.getName()
					+ ", hyperSearchFlag = " + hyperSearchFlag
					+ ", SwingUtilities.isEventDispatchThread() = "
					+ javax.swing.SwingUtilities.isEventDispatchThread());
		// if (hyperSearchFlag) {
		// if (textArea.getBuffer() != buffer) {
		if (!javax.swing.SwingUtilities.isEventDispatchThread() || buffer.getMode() == null)
		{
			// {{{ for hypersearch, token method doesn't work ==>
			// explicit comment checking
			Mode buffermode = getMode(buffer);
			if (debug)
				org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.DEBUG,
					SearchAndReplace.class, "+++ .2023: buffermode = "
						+ buffermode);
			if (buffermode != null)
			{
				CommentStruct cs;
				// get values from map
				if (modeToCommentsMap == null)
					modeToCommentsMap = new HashMap<Mode, CommentStruct>();
				if (modeToCommentsMap.containsKey(buffermode))
					cs = (CommentStruct) modeToCommentsMap.get(buffermode);
				else
				{
					buffermode.loadIfNecessary();
					cs = new CommentStruct((String) buffermode.getProperty("lineComment"), 
						(String) buffermode.getProperty("commentStart"),
						(String) buffermode.getProperty("commentEnd"));
					modeToCommentsMap.put(buffermode, cs);
				}

				// check line comment
				if (cs.lineComment != null && cs.lineComment.length() > 0)

				{
					int matchLine = buffer.getLineOfOffset(currPos);
					// check line comment
					String currLine = buffer.getLineText(matchLine);
					// if
					// (currLine.lastIndexOf("//",currPos-buffer.getLineStartOffset(matchLine))
					// != -1)
					if (currLine.lastIndexOf(cs.lineComment, currPos -
						buffer.getLineStartOffset(matchLine))  != -1)
						outsideCmt = false;
				}
				// check block comment
				if (outsideCmt && cs.hasBlockComment())
				{
					// search for "start-comment" before
					// match
					SearchMatcher cmtMatcher = new BoyerMooreSearchMatcher(
						cs.blockCommentStart, false );
					Segment textBeforeMatch = new Segment();
					buffer.getText(0, currPos, textBeforeMatch);

					SearchMatcher.Match openCmtMatch = cmtMatcher.nextMatch(
						new CharIndexedSegment(textBeforeMatch, true),
						false, true, true, true // reverse
						);

					if (openCmtMatch != null)
					{
						/* we found an open comment before match:
						      check if already closed */
						if (debug)
							Log.log(Log.DEBUG, BeanShell.class,
								"found open cmt at = "
									+ openCmtMatch.start + "-"
									+ openCmtMatch.end);
						cmtMatcher = new BoyerMooreSearchMatcher(
							cs.blockCommentEnd, false );
						SearchMatcher.Match closeCmtMatch = cmtMatcher
							.nextMatch(new CharIndexedSegment(
								textBeforeMatch, true), false,
								true, true, true // reverse
							);
						if (closeCmtMatch == null)
						{
							/* no close found ==>
  							    inside comment */
							outsideCmt = false;
						}
						else
						{
							if (debug)
								Log.log(Log.DEBUG, BeanShell.class, 
									"found close cmt at = " + closeCmtMatch.start +
									"-" + closeCmtMatch.end);
							/*  we found a close comment ==>
							     check which was earlier */
							if (openCmtMatch.start < closeCmtMatch.start)
								outsideCmt = false;
						}
					}
				}
			}
			// }}}

		}
		else
		{ // {{{ normal search, edit mode depending
			boolean blankChar = true;
			for (int i = currPos; blankChar && i < buffer.getLength(); i++)
			{
				if (!Character.isWhitespace(buffer.getText(i, 1).charAt(0)))
				{
					/*
					 * String currChar =
					 * buffer.getText(i,1); if ("
					 * \r\n\t".indexOf(currChar) == -1) {
					 */
					blankChar = false;
					currPos = i;
				}
			}
			int line = buffer.getLineOfOffset(currPos);
			if (debug)
				Log.log(Log.DEBUG, BeanShell.class, "tp1293: lineNr = " + line);
			int position = currPos - buffer.getLineStartOffset(line);
			if (debug)
				Log.log(Log.DEBUG, BeanShell.class, "tp1296: position = " + position);

			DefaultTokenHandler tokens = new DefaultTokenHandler();
			buffer.markTokens(line, tokens);
			org.gjt.sp.jedit.syntax.Token token = TextUtilities.getTokenAtOffset(tokens
				.getTokens(), position);
			// Log.log(Log.DEBUG, BeanShell.class,"+++
			// SearchAndReplace.1952: token.id = "+token.id);
			if (token.id == org.gjt.sp.jedit.syntax.Token.COMMENT1
				|| token.id == org.gjt.sp.jedit.syntax.Token.COMMENT2
				|| token.id == org.gjt.sp.jedit.syntax.Token.COMMENT3
				|| token.id == org.gjt.sp.jedit.syntax.Token.COMMENT4)
				outsideCmt = false;
		} // }}}
		// Log.log(Log.DEBUG, BeanShell.class,"+++
		// SearchAndReplace.1954: outsideCmt = "+outsideCmt);
		return outsideCmt;
	}

	// }}}

	// {{{ record() method
	private static void record(View view, String action, boolean replaceAction,
		boolean recordFileSet)
	{
		Macros.Recorder recorder = view.getMacroRecorder();

		if (recorder != null)
		{
			recorder.record("xsearch.SearchSettings.push();");
			recorder.record("xsearch.SearchSettings.resetSettings();");

			recorder.record("xsearch.SearchAndReplace.setSearchString(\""
				+ StandardUtilities.charsToEscapes(search) + "\");");

			if (replaceAction)
			{
				recorder.record("xsearch.SearchAndReplace.setReplaceString(\""
					+ StandardUtilities.charsToEscapes(replace) + "\");");
				if (beanshell)
					recorder.record("xsearch.SearchAndReplace.setBeanShellReplace("
							+ beanshell + ");");
			}
			else
			{
				// only record this if doing a find next
				if (wrap)
					recorder.record("xsearch.SearchAndReplace.setAutoWrapAround("
							+ wrap + ");");
				if (reverse)
					recorder.record("xsearch.SearchAndReplace.setReverseSearch("
							+ reverse + ");");
			}

			if (ignoreCase)
				recorder.record("xsearch.SearchAndReplace.setIgnoreCase("
					+ ignoreCase + ");");
			if (regexp)
				recorder.record("xsearch.SearchAndReplace.setRegexp(" + regexp
					+ ");");
			// add extended options
			if (fromTop && !findAll)
				recorder.record("xsearch.SearchAndReplace.setSearchFromTop("
					+ fromTop + ");");
			if (columnSearchEnabled)
				recorder.record("xsearch.SearchAndReplace.setColumnSearchOptions("
					+ columnSearchExpandTabs + ", " + columnSearchLeftCol
					+ ", " + columnSearchRightCol + ");");
			if (rowSearchEnabled)
				recorder.record("xsearch.SearchAndReplace.setRowSearchOptions("
					+ rowSearchLeftRow + ", " + rowSearchRightRow + ");");
			if (commentSearch != XSearch.SEARCH_IN_OUT_NONE)
				recorder.record("xsearch.SearchAndReplace.setCommentOption("
					+ commentSearch + ");");
			if (foldSearch != XSearch.SEARCH_IN_OUT_NONE)
				recorder.record("xsearch.SearchAndReplace.setFoldOption("
					+ foldSearch + ");");
			if (wordPart != XSearch.SEARCH_PART_NONE)
				recorder.record("xsearch.SearchAndReplace.setWordPartOption("
					+ wordPart + ");");

			if (recordFileSet)
			{
				recorder.record("xsearch.SearchAndReplace.setSearchFileSet("
					+ fileset.getCode() + ");");
			}
			if (findAll)
				recorder.record("xsearch.SearchAndReplace.setFindAll(true);");

			recorder.record("xsearch.SearchAndReplace." + action + ";");
			recorder.record("xsearch.SearchSettings.pop();");
		}
	} // }}}

	// {{{ replaceInSelection() method
	private static int replaceInSelection(View view, JEditTextArea textArea, Buffer buffer,
		SearchMatcher matcher, boolean smartCaseReplace, Selection s) throws Exception
	{
		/*
		 * if an occurence occurs at the beginning of the selection, the
		 * selection start will get moved. this sucks, so we hack to
		 * avoid it.
		 */
		int start = s.getStart();

		int returnValue;

		if (s instanceof Selection.Range)
		{
			returnValue = _replace(view, buffer, matcher, s.getStart(), s.getEnd(),
				smartCaseReplace, null);

			textArea.removeFromSelection(s);
			textArea.addToSelection(new Selection.Range(start, s.getEnd()));
		}
		else if (s instanceof Selection.Rect)
		{
			Selection.Rect rect = (Selection.Rect) s;
			int startCol = rect.getStartColumn(buffer);
			int endCol = rect.getEndColumn(buffer);

			returnValue = 0;
			for (int j = s.getStartLine(); j <= s.getEndLine(); j++)
			{
				returnValue += _replace(view, buffer, matcher,
					getColumnOnOtherLine(buffer, j, startCol),
					getColumnOnOtherLine(buffer, j, endCol), smartCaseReplace,
					null);
			}
			textArea.addToSelection(new Selection.Rect(start, s.getEnd()));
		}
		else
			throw new RuntimeException("Unsupported: " + s);

		return returnValue;
	} // }}}

	// {{{ _replace() method
	/**
	 * Replaces all occurances of the search string with the replacement
	 * string.
	 * 
	 * @param view
	 *                The view
	 * @param buffer
	 *                The buffer
	 * @param start
	 *                The start offset
	 * @param end
	 *                The end offset
	 * @param matcher
	 *                The search matcher to use
	 * @param smartCaseReplace
	 *                See user's guide
	 * @param node
	 *                Node where to register replacements
	 * @return The number of occurrences replaced
	 */
	private static int _replace(View view, Buffer buffer, SearchMatcher matcher, int start,
		int end, boolean smartCaseReplace, DefaultMutableTreeNode node) throws Exception
	{
		// make smart case replace optional
		smartCaseReplace = smartCaseReplace
			&& jEdit.getBooleanProperty("xsearch.replaceCaseSensitiv", true);
		int occurCount = 0;

		boolean endOfLine = (buffer.getLineEndOffset(buffer.getLineOfOffset(end)) - 1 == end);

		Segment text = new Segment();
		int offset = start;
		int line = -1;
		loop: for (int counter = 0;; counter++)
		{
			buffer.getText(offset, end - offset, text);

			boolean startOfLine = (buffer.getLineStartOffset(buffer
				.getLineOfOffset(offset)) == offset);

			SearchMatcher.Match occur = matcher.nextMatch(new CharIndexedSegment(text,
				false), startOfLine, endOfLine, counter == 0, false);
			if (occur == null)
				break loop;
			int _start = occur.start;
			int _length = occur.end - occur.start;
			// check xsearch parameters
			if (!checkXSearchParameters(view.getTextArea(), buffer, offset + _start,
				offset + _start + _length, true))
			{
				// current match was not ok ==> skip replacement
				offset += _start + _length;
				if (debug)
					Log.log(Log.DEBUG, BeanShell.class, "offset = " + offset);
			}
			else
			{
				String found = new String(text.array, text.offset + _start, _length);
				String subst = replaceOne(occur, found);
				if (smartCaseReplace && ignoreCase)
				{
					int strCase = TextUtilities.getStringCase(found);
					if (strCase == TextUtilities.LOWER_CASE)
						subst = subst.toLowerCase();
					else if (strCase == TextUtilities.UPPER_CASE)
						subst = subst.toUpperCase();
					else if (strCase == TextUtilities.TITLE_CASE)
						subst = TextUtilities.toTitleCase(subst);
				}

				if (subst != null)
				{
					buffer.remove(offset + _start, _length);
					buffer.insert(offset + _start, subst);
					occurCount++;
					offset += _start;

					int newLine = buffer.getLineOfOffset(offset);
					if (node != null && line < newLine)
					{
						HyperSearchResult substResult = new HyperSearchResult(
							buffer, newLine);
						// Log.log(Log.DEBUG,
						// BeanShell.class,"+++
						// SearchAndReplace.2065: add
						// node");
						node.add(new DefaultMutableTreeNode(substResult,
						// offset,
							// offset+subst.length()),false));
							false));
						substResult.addOccur(offset, offset
							+ subst.length());
					}

					offset += subst.length();

					end += (subst.length() - found.length());
					line = newLine;
				}
				else
					offset += _start + _length;
			}
		}
		return occurCount;
	} // }}}

	// {{{ replaceOne() method
	private static String replaceOne(SearchMatcher.Match occur, String found) throws Exception
	{
		if (regexp)
		{
			if (replaceMethod != null)
				return regexpBeanShellReplace(occur, found);
			else
				return regexpReplace(occur, found);
		}
		else
		{
			if (replaceMethod != null)
				return literalBeanShellReplace(occur, found);
			else
				return replace;
		}
	} // }}}

	// {{{ regexpBeanShellReplace() method
	private static String regexpBeanShellReplace(SearchMatcher.Match occur, String found)
		throws Exception
	{
		for (int i = 0; i < occur.substitutions.length; i++)
		{
			replaceNS.setVariable("_" + i, occur.substitutions[i]);
		}

		Object obj = BeanShell.runCachedBlock(replaceMethod, null, replaceNS);
		if (obj == null)
			return "";
		else
			return obj.toString();
	} // }}}

	// {{{ regexpReplace() method
	private static String regexpReplace(SearchMatcher.Match occur, String found)
		throws Exception
	{
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < replace.length(); i++)
		{
			char ch = replace.charAt(i);
			switch (ch)
			{
			case '$':
				if (i == replace.length() - 1)
				{
					buf.append(ch);
					break;
				}

				ch = replace.charAt(++i);
				if (ch == '$')
					buf.append('$');
				else if (ch == '0')
					buf.append(found);
				else if (Character.isDigit(ch))
				{
					int n = ch - '0';
					if (n < occur.substitutions.length)
					{
						buf.append(occur.substitutions[n]);
					}
				}
				break;
			case '\\':
				if (i == replace.length() - 1)
				{
					buf.append('\\');
					break;
				}
				ch = replace.charAt(++i);
				switch (ch)
				{
				case 'n':
					buf.append('\n');
					break;
				case 't':
					buf.append('\t');
					break;
				default:
					buf.append(ch);
					break;
				}
				break;
			default:
				buf.append(ch);
				break;
			}
		}

		return buf.toString();
	} // }}}

	// {{{ literalBeanShellReplace() method
	private static String literalBeanShellReplace(SearchMatcher.Match occur, String found)
		throws Exception
	{
		replaceNS.setVariable("_0", found);
		Object obj = BeanShell.runCachedBlock(replaceMethod, null, replaceNS);
		if (obj == null)
			return "";
		else
			return obj.toString();
	} // }}}

	// {{{ getColumnOnOtherLine() method
	/**
	 * Should be somewhere else...
	 */
	private static int getColumnOnOtherLine(Buffer buffer, int line, int col)
	{
		int returnValue = buffer.getOffsetOfVirtualColumn(line, col, null);
		if (returnValue == -1)
			return buffer.getLineEndOffset(line) - 1;
		else
			return buffer.getLineStartOffset(line) + returnValue;
	} // }}}

	private static void testMatcher()
	{
		if (debug)
			Log.log(Log.DEBUG, BeanShell.class, "tp1433: matcher = " + matcher + " is "
				+ matcher.getClass().getName());
	}

	// }}}
	private static class CommentStruct
	{
		String lineComment;

		String blockCommentStart;

		String blockCommentEnd;

		/**
		 * Constructor for the CommentStruct object
		 * 
		 * @param lineComment
		 *                Description of the Parameter
		 * @param blockCommentStart
		 *                Description of the Parameter
		 * @param blockCommentEnd
		 *                Description of the Parameter
		 */
		private CommentStruct(String lineComment, String blockCommentStart,
			String blockCommentEnd)
		{
			this.lineComment = lineComment;
			this.blockCommentStart = blockCommentStart;
			this.blockCommentEnd = blockCommentEnd;
		}

		boolean hasBlockComment()
		{
			return blockCommentStart != null && blockCommentEnd != null;
			// && blockCommentStart.length() > 0 &&
			// blockCommentEnd.length() > 0;
		}
	}
	// }}}
}
