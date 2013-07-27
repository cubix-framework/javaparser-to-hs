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

package gruntspud.ui.preferences;

import gruntspud.connection.ConnectionProfile;

import java.awt.BorderLayout;

import javax.swing.JLabel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class NoAdditionalConnectionOptionsPane
    extends AbstractAdditionalOptionsPane {
    /**
     *  Constructor for the GlobalOptionsTab object
     *
     *@param  host  Description of the Parameter
     */
    public NoAdditionalConnectionOptionsPane() {
        setLayout(new BorderLayout());
        add(new JLabel("This connection type has no additional options",
                       JLabel.CENTER), BorderLayout.CENTER);
    }

    /**
     * Set the connection profile
     *
     * @param profile connection profile
     */
    public void setProfile(ConnectionProfile profile) {
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean validateOptions() {
        return true;
    }

    /**
     *  Description of the Method
     */
    public void applyOptions() {
    }
}
