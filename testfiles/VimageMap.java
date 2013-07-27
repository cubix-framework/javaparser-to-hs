
/* 
Copyright (C) 2009 Matthew Gilbert 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package vimage;

/* Mappings shared by all VimageInputHandler's (no buffer local mappings). */

import java.lang.StringBuffer;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.io.BufferedReader;
import java.io.StringReader;

import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;

import org.gjt.sp.jedit.gui.KeyEventTranslator;
import org.gjt.sp.jedit.BeanShell;
import org.gjt.sp.jedit.bsh.BshMethod;
import org.gjt.sp.util.Log;

class VimageMap
{
    protected HashMap<String, HashMap<KeyEventTranslator.Key, VimageBshMethod>> maps;
    protected HashMap<String, VimageBshMethod> command_map;

    public VimageMap()
    {
        maps = new HashMap<String, HashMap<KeyEventTranslator.Key, VimageBshMethod>>();
        command_map = new HashMap<String, VimageBshMethod>();
        // Setup standard modes
        maps.put("nmap", new HashMap<KeyEventTranslator.Key, VimageBshMethod>());
        maps.put("omap", new HashMap<KeyEventTranslator.Key, VimageBshMethod>());
        maps.put("imap", new HashMap<KeyEventTranslator.Key, VimageBshMethod>());
        maps.put("vmap", new HashMap<KeyEventTranslator.Key, VimageBshMethod>());
    }

    public Map<KeyEventTranslator.Key, VimageBshMethod> getMode(String mode)
    {
        return maps.get(mode);
    }

    public void remove(String mode, KeyEventTranslator.Key key)
    {
        Map<KeyEventTranslator.Key, VimageBshMethod> map = getMode(mode);
        if (map == null)
            return;
        map.remove(key);
    }

    public void add(String mode, KeyEventTranslator.Key key, VimageBshMethod method)
    {
        Map<KeyEventTranslator.Key, VimageBshMethod> map = getMode(mode);
        if (map == null) {
            Log.log(Log.DEBUG, this, "Adding new mode \"" + mode + "\"");
            maps.put(mode, new HashMap<KeyEventTranslator.Key, VimageBshMethod>());
            map = getMode(mode);
        }
        map.put(key, method);
    }

    public void add(String mode, String key_string, VimageBshMethod method)
    {
        // Special case for commands, where the command doesn't get turned
        // into a key.
        if (mode.equals("command")) {
            command_map.put(key_string, method);
        } else {
            KeyEventTranslator.Key key = KeyEventTranslator.parseKey(key_string);
            if (key == null) {
                JOptionPane.showMessageDialog(null, 
                                              "Invalid key \""+key_string+"\" in mode "+mode+"; ignoring keybinding",
                                              "Vimage Error",
                                              JOptionPane.WARNING_MESSAGE);
                return;
            }
            add(mode, key, method);
        }
    }

    public void add(String mode, String key_string, String script)
    {
        String pre = " _(String s) { mode.invoke(s); } _(String action, String next_mode) { mode.invoke(action, next_mode); } __(String s) { mode.setMode(s); } exec() ";
        String post = " exec(); ";
        BshMethod method;
        try {
            method = BeanShell.cacheBlock(mode + key_string,
                                          pre + script + post, 
                                          true);
        } catch (java.lang.Exception ex) {
            JOptionPane.showMessageDialog(null, 
                                          "Invalid BeanShell for \""+key_string+"\" in mode "+mode+"; ignoring keybinding",
                                          "Vimage Error",
                                          JOptionPane.WARNING_MESSAGE);
            return;
        }
        add(mode, key_string, new VimageBshMethod(key_string, script, method));
    }

    public void add(String text)
    {
        try {
            VimageParser.parse(this, new BufferedReader(new StringReader(text)));
        } catch (java.io.IOException ex) {
            Log.log(Log.ERROR, this, ex);
        }
    }

    public BshMethod get(String mode, KeyEventTranslator.Key key)
    {
        Map<KeyEventTranslator.Key, VimageBshMethod> map = getMode(mode);
        if (map == null) {
            JOptionPane.showMessageDialog(null, 
                                          "Invalid mode \""+mode+"\"; ignoring key \""+key+"\"",
                                          "Vimage Error",
                                          JOptionPane.WARNING_MESSAGE);
            return null;
        }
        VimageBshMethod method = map.get(key);
        if (method == null)
            return null;
        return method.method;
    }

    // This is used only for the command map. All other gets must pass a Key
    public BshMethod get(String mode, String command)
    {
        VimageBshMethod method = command_map.get(command);
        if (method == null)
            return null;
        return method.method;
    }
    
    public String[] getModes()
    {
        return (String[])this.maps.keySet().toArray();
    }

    public String toString()
    {
        boolean first = true;
        StringBuffer sbuf = new StringBuffer();
        
        for (String mode : this.maps.keySet()) {
            if (first) {
                first = false;
            } else {
                sbuf.append("\n\n");
            }
            sbuf.append(mode+":\n");
            
            HashMap<KeyEventTranslator.Key, VimageBshMethod> bindings = this.maps.get(mode);
            for (KeyEventTranslator.Key k : bindings.keySet()) {
                VimageBshMethod m = bindings.get(k);
                sbuf.append("\t\""+m.id+"\" "+m.code.replace('\n', ' ').replace('\t', ' ')+"\n");
            }
        }
        return sbuf.toString();
    }
}

