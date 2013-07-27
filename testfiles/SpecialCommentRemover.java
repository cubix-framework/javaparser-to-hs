/**
 * SpecialCommentRemover.java - Sql Plugin
 * :tabSize=8:indentSize=8:noTabs=false:
 *
 * Copyright (C) 2001 Sergey V. Udaltsov
 * svu@users.sourceforge.net
 *
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

package sql.preprocessors;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.util.*;

import sql.*;
import sql.options.*;

/**
 *  Description of the Class
 *
 * @author     svu
 */
public class SpecialCommentRemover extends Preprocessor
{

	protected final static String PROP_NAME =
	        SpecialCommentRemover.class.getName() + ".tokens";


	/**
	 *  Gets the OptionPane attribute of the SpecialCommentRemover object
	 *
	 * @return    The OptionPane value
	 */
	public OptionPane getOptionPane()
	{
		return new CommentOptionPane();
	}


	/**
	 *Description of the Method
	 *
	 * @param  text  Description of Parameter
	 * @return       Description of the Returned Value
	 * @since
	 */
	public String doProcess(String text)
	{

		ArrayList comments = (ArrayList) this.getAllSpecialComments();
		Iterator iter = comments.iterator();
		while (iter.hasNext())
		{
			final String comment = ((String) iter.next());

			int curPos = text.indexOf(comment);
			while (curPos > -1)
			{
				text = substituteFragment(text, curPos, comment.length());
				curPos = text.indexOf(comment, curPos + comment.length());
			}
		}

		return text;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  text              Description of Parameter
	 * @param  pos               Description of Parameter
	 * @param  substitutionSize  Description of Parameter
	 * @return                   Description of the Returned Value
	 */
	protected String substituteFragment(String text, int pos, int substitutionSize)
	{
		return text.substring(0, pos) +
		       text.substring(pos + substitutionSize);
	}


	/**
	 *  Gets the AllSpecialComments attribute of the SpecialCommentRemover class
	 *
	 * @return    The AllSpecialComments value
	 */
	public static java.util.List getAllSpecialComments()
	{
		final ArrayList lst = new ArrayList();
		try
		{
			final String prop = SqlPlugin.getGlobalProperty(PROP_NAME);
			final StringTokenizer strTkn = new StringTokenizer(prop, "?");

			while (strTkn.hasMoreTokens())
			{
				lst.add(strTkn.nextToken());
			}
		} catch (NullPointerException ex)
		{
			//!! just ignore missing property
		}
		return lst;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  comments  Description of Parameter
	 */
	public static void save(java.util.List comments)
	{
		final Iterator iter = comments.iterator();
		final StringBuffer text = new StringBuffer();
		while (iter.hasNext())
		{
			text.append((String) iter.next() + "?");
		}

		final String stext = text.toString();
		SqlPlugin.setGlobalProperty(PROP_NAME,
		                            stext.length() > 0 ?
		                            stext.substring(0,
		                                            stext.length() - 1) : "");
	}


	public static class CommentOptionPane extends SqlOptionPane
	{
		private JList allSpecialCommentsLst;

		private JButton addSpecialCommentBtn;
		private JButton editSpecialCommentBtn;
		private JButton delSpecialCommentBtn;

		private java.util.List allSpecialComments;

		private JTextField specialCommentField;

		private JFrame parentFrame = null;


		/**
		 *  Constructor for the SqlOptionPane object
		 *
		 * @since
		 */
		public CommentOptionPane()
		{
			super("sql_preprocessors_specialCommentRemover");
		}


		/**
		 *Description of the Method
		 *
		 * @since
		 */
		public void _init()
		{
			super._init();

			JPanel panel = new JPanel(new BorderLayout(5, 5));
			{
				panel.add(new JLabel(jEdit.getProperty("sql.options.specialCommentRemover.title.label")),
				          BorderLayout.NORTH);

				JPanel vp = new JPanel(new BorderLayout(5, 5));
				{
					vp.setBorder(new BevelBorder(BevelBorder.LOWERED));
					allSpecialCommentsLst = new JList();

					vp.add(allSpecialCommentsLst, BorderLayout.CENTER);
				}
				panel.add(vp, BorderLayout.CENTER);

				vp = new JPanel(new BorderLayout(5, 5));
				{

					JPanel hp = new JPanel(new BorderLayout(5, 5));
					{
						hp.add(new JLabel(jEdit.getProperty("sql.options.specialCommentRemover.label")),
						       BorderLayout.WEST);

						hp.add(specialCommentField = new JTextField(""),
						       BorderLayout.CENTER);
					}

					vp.add(hp, BorderLayout.NORTH);

					hp = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
					JPanel bp = new JPanel(new GridLayout(1, 0, 5, 5));
					{
						addSpecialCommentBtn = new JButton(jEdit.getProperty("sql.options.specialCommentRemover.addBtn.label"));
						bp.add(addSpecialCommentBtn);

						delSpecialCommentBtn = new JButton(jEdit.getProperty("sql.options.specialCommentRemover.delBtn.label"));
						bp.add(delSpecialCommentBtn);
					}
					hp.add(bp);

					vp.add(hp, BorderLayout.SOUTH);

				}
				panel.add(vp, BorderLayout.SOUTH);
			}
			add(panel, BorderLayout.NORTH);

			specialCommentField.addKeyListener(
			        new KeyAdapter()
			        {
				        public void keyReleased(KeyEvent e)
				        {
					        updateSpecialCommentsButtons();
				        }
			        });

			allSpecialCommentsLst.addListSelectionListener(
			        new ListSelectionListener()
			        {
				        public void valueChanged(ListSelectionEvent evt)
				        {
					        updateSpecialCommentsButtons();
				        }
			        });

			addSpecialCommentBtn.addActionListener(
			        new ActionListener()
			        {
				        public void actionPerformed(ActionEvent evt)
				        {
					        final String text = specialCommentField.getText();
					        allSpecialComments.add(text);
					        Collections.sort(allSpecialComments,
					                                new Comparator()
					                                {
						                                public int compare(Object o1, Object o2)
						                                {
							                                final String s1 = o1.toString();
							                                final String s2 = o2.toString();
							                                if (s1.length() > s2.length())
								                                return -1;
							                                if (s1.length() < s2.length())
								                                return 1;
							                                return s1.compareTo(s2);
						                                }
					                                });

					        updateSpecialCommentList();
				        }
			        });

			delSpecialCommentBtn.addActionListener(
			        new ActionListener()
			        {
				        public void actionPerformed(ActionEvent evt)
				        {
					        final String text = (String) allSpecialCommentsLst.getSelectedValue();
					        if (text == null)
						        return;
					        allSpecialComments.remove(text);
					        updateSpecialCommentList();
				        }
			        });

			allSpecialComments = SpecialCommentRemover.getAllSpecialComments();

			updateSpecialCommentList();

			Component cp = this;
			while (cp != null)
			{
				cp = cp.getParent();
				if (cp instanceof JFrame)
				{
					parentFrame = (JFrame) cp;
					break;
				}
			}
		}


		/**
		 *  Description of the Method
		 *
		 * @since
		 */
		public void _save()
		{
			SpecialCommentRemover.save(allSpecialComments);
		}


		/**
		 *  Description of the Method
		 */
		protected void updateSpecialCommentList()
		{
			allSpecialCommentsLst.setListData(allSpecialComments.toArray());

			updateSpecialCommentsButtons();
		}


		private void updateSpecialCommentsButtons()
		{
			final boolean isAny = allSpecialCommentsLst.getSelectedIndex() != -1;
			boolean isText = false;
			try
			{

				isText = specialCommentField.getText().indexOf("?") == -1 ? true : false;
			} catch (NullPointerException ex)
			{
				Log.log(Log.ERROR, SpecialCommentRemover.CommentOptionPane.class, ex);
			}
			delSpecialCommentBtn.setEnabled(isAny);
			addSpecialCommentBtn.setEnabled(isText);

		}
	}

}

