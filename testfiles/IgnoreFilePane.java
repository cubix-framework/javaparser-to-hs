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

package gruntspud.ui.ignore;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.AbstractTab;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JLabel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class IgnoreFilePane
    extends Tabber {
    private DirectoryIgnoreTab directoryIgnoreTab;
    private GlobalIgnoreTab globalIgnoreTab;
    private CVSIgnoreTab cvsIgnoreTab;
    private GruntspudContext context;

    /**
     *  Constructor for the DiffOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public IgnoreFilePane(GruntspudContext context, File dir) {
        super();

        this.context = context;

        if (dir != null) {
            directoryIgnoreTab = new DirectoryIgnoreTab(dir);
            addTab(directoryIgnoreTab);
        }

        globalIgnoreTab = new GlobalIgnoreTab();
        addTab(globalIgnoreTab);

        File f = new File(new File(new File(System.getProperty("user.home")),
                                   ".gruntspud"), "cvsignore");

        if (f.exists()) {
            cvsIgnoreTab = new CVSIgnoreTab(f);
            addTab(cvsIgnoreTab);
        }
    }

    class DirectoryIgnoreTab
        extends AbstractTab {
        private IgnoreFileEditorPane editor;

        /**
         *  Constructor for the DiffGeneralTab object
         */
        DirectoryIgnoreTab(File dir) {
            super("Directory",
                  UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_IGNORE));
            setTabToolTipText("Ignore list for this directory");
            setLayout(new BorderLayout());
            add(new JLabel("Ignore list goes here"));
            add(editor = new IgnoreFileEditorPane(new File(dir, ".cvsignore"),
                                                  context), BorderLayout.CENTER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            editor.save();
        }

        public void tabSelected() {
        }
    }

    class GlobalIgnoreTab
        extends AbstractTab {
        private IgnoreFileEditorPane editor;

        /**
         *  Constructor for the DiffGeneralTab object
         */
        GlobalIgnoreTab() {
            super("Global",
                  UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_IGNORE));
            setTabToolTipText("Globally ignored");
            setLayout(new BorderLayout());

            String ig = context.getHost().getProperty(Constants.
                OPTIONS_SYSTEM_USER_IGNORE_FILE,
                System.getProperty("user.home") + File.separator +
                ".cvsignore");
            add(editor = new IgnoreFileEditorPane(new File(ig), context),
                BorderLayout.CENTER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            editor.save();
        }

        public void tabSelected() {
        }
    }

    class CVSIgnoreTab
        extends AbstractTab {
        private IgnoreFileEditorPane editor;

        /**
         *  Constructor for the DiffGeneralTab object
         */
        CVSIgnoreTab(File f) {
            super("CVS", UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_IGNORE));
            setTabToolTipText("Default files ignored by CVS ");
            setLayout(new BorderLayout());
            add(editor = new IgnoreFileEditorPane(f, context),
                BorderLayout.CENTER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            editor.save();
        }

        public void tabSelected() {
        }
    }
}
