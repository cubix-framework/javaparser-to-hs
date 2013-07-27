/*
 * BackgroundHighlight.java
 * Copyright (c) 2002, 2003 Andre Kaplan
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


package background;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Image;

import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextAreaExtension;
import org.gjt.sp.jedit.textarea.TextAreaPainter;
import org.gjt.sp.util.Log;


public class BackgroundHighlight extends TextAreaExtension
{
    private static final int ICON_CENTER = 0;
    private static final int ICON_STRECH = 1;
    private static final int ICON_TILE = 2;
    private static int getIconPositionFromString(String s) {
        for (int i =0 ; i < BackgroundOptionPane.IMAGE_POSITIONS.length; i++) {
            if (BackgroundOptionPane.IMAGE_POSITIONS[i].equals(s)) {
                return i;
            }
        }
        return ICON_TILE;
    }
    
    // (EditPane, BackgroundHighlight) association
    private static Hashtable highlights = new Hashtable();

    private static String    iconName   = null;
    private static ImageIcon icon       = null;
    private static boolean   blend      = false;
    private static Color     blendColor = null;
    private static int       blendAlpha = 127;
    private static Color     alphaColor = null;
    private static int       iconPosition = ICON_TILE;

    private static AlphaComposite alphaComposite = AlphaComposite.getInstance(
        AlphaComposite.SRC_OVER
    );

    private boolean enabled = true;

    private JEditTextArea textArea;


    static {
        iconName = jEdit.getProperty("background.file", (String) null);

        if ("".equals(iconName)) { iconName = null; }

        try {
            if (iconName != null) {
                icon = new ImageIcon(iconName);
            }
        } catch (Exception e) {
            iconName = null;
            icon     = null;
            Log.log(Log.DEBUG, BackgroundHighlight.class, e);
        }
        iconPosition = getIconPositionFromString(
            jEdit.getProperty("background.position", "tile"));

        blend      = jEdit.getBooleanProperty("background.blend", false);
        blendColor = jEdit.getColorProperty(
            "background.blend-color",
            jEdit.getColorProperty("view.bgColor", Color.white)
        );
        blendAlpha = jEdit.getIntegerProperty(
            "background.blend-alpha", 127
        );
        if (blendAlpha < 0)   { blendAlpha = 0; }
        if (blendAlpha > 255) { blendAlpha = 255; }

        alphaColor = new Color(
            blendColor.getRed(), blendColor.getGreen(), blendColor.getBlue(), blendAlpha
        );
    }


    private BackgroundHighlight(JEditTextArea textArea) {
        this.textArea = textArea;
    }


    public boolean isEnabled() {
        return this.enabled;
    }


    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }


    public void paintScreenLineRange(Graphics2D gfx, int firstLine,
        int lastLine, int[] physicalLines, int[] start, int[] end,
        int lineY, int lineHeight
    ) {
        if (!isEnabled() || icon == null || icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            return;
        }

        TextAreaPainter painter = this.textArea.getPainter();
        int painterWidth = painter.getWidth();
        int painterHeight = painter.getHeight();
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        Image iconImage = icon.getImage();
        
        Rectangle rect = gfx.getClipBounds();

        if (iconPosition == ICON_TILE) {
            int x0 = rect.x;
            int x1 = x0 + rect.width;
            int y0 = rect.y;
            int y1 = y0 + rect.height;
            for (int x = 0; x <= x1; x += iconWidth) {
                if (x + iconWidth > x0) {
                    for (int y = 0; y <= y1; y += iconHeight) {
                        if (y + iconHeight > y0) {
                            gfx.drawImage(iconImage, x, y, textArea);
                        }
                    }
                }
            }
        }
        else if (iconPosition == ICON_STRECH) {
            gfx.drawImage(iconImage,
                0, 0, painterWidth, painterHeight,
                textArea);
        }
        else if (iconPosition == ICON_CENTER) {
            gfx.drawImage(iconImage,
                (painterWidth - iconWidth) / 2,
                (painterHeight - iconHeight) / 2,
                textArea);
        }

        if (blend) {
            // Remember the original color and composite
            Color     color     = gfx.getColor();
            Composite composite = gfx.getComposite();

            gfx.setColor(alphaColor);
            gfx.setComposite(alphaComposite);

            gfx.fillRect(rect.x, rect.y, rect.width, rect.height);

            // Restore the original color and composite
            gfx.setColor(color);
            gfx.setComposite(composite);
        }
    }


    private void updateTextArea() {
        if (this.textArea == null) { return; }

        this.textArea.repaint();
    }


    public static TextAreaExtension addHighlightTo(EditPane editPane) {
        JEditTextArea textArea = editPane.getTextArea();
        TextAreaPainter painter = textArea.getPainter();
        BackgroundHighlight highlight = new BackgroundHighlight(textArea);
        painter.addExtension(TextAreaPainter.BACKGROUND_LAYER, highlight);
        highlights.put(editPane, highlight);
        return highlight;
    }


    public static void removeHighlightFrom(EditPane editPane) {
        BackgroundHighlight highlight = (BackgroundHighlight) highlights.remove(editPane);

        if (highlight == null) { return; }

        editPane.getTextArea().getPainter().removeExtension(highlight);
    }


    public static BackgroundHighlight getHighlightFor(EditPane editPane) {
        return (BackgroundHighlight) highlights.get(editPane);
    }


    public static boolean isHighlightEnabledFor(EditPane editPane) {
        BackgroundHighlight highlight = getHighlightFor(editPane);

        if (highlight == null) { return false; }

        return highlight.isEnabled();
    }


    public static void toggleHighlightEnabledFor(EditPane editPane) {
        BackgroundHighlight highlight = getHighlightFor(editPane);

        if (highlight == null) { return; }

        highlight.toggleEnabled();
        highlight.updateTextArea();
    }


    public static void propertiesChanged() {
        String    newIconName = jEdit.getProperty("background.file", (String) null);
        ImageIcon newIcon     = null;

        if ("".equals(newIconName)) { newIconName = null; }

        try {
            if (newIconName != null) {
                newIcon = new ImageIcon(newIconName);
            }
        } catch (Exception e) {
            newIconName = null;
            newIcon     = null;
            Log.log(Log.DEBUG, BackgroundHighlight.class, e);
        }

        iconName = newIconName;
        icon     = newIcon;
        iconPosition = getIconPositionFromString(
            jEdit.getProperty("background.position", "tile"));

        blend      = jEdit.getBooleanProperty("background.blend", false);
        blendColor = jEdit.getColorProperty(
            "background.blend-color",
            jEdit.getColorProperty("view.bgColor", Color.white)
        );
        blendAlpha = jEdit.getIntegerProperty(
            "background.blend-alpha", 127
        );
        if (blendAlpha < 0)   { blendAlpha = 0; }
        if (blendAlpha > 255) { blendAlpha = 255; }

        alphaColor = new Color(
            blendColor.getRed(), blendColor.getGreen(), blendColor.getBlue(), blendAlpha
        );

        // Propagate the changes to all textareas
        View[] views = jEdit.getViews();
        for (int i = 0; i < views.length; i++) {
            EditPane[] editPanes = views[i].getEditPanes();
            BackgroundHighlight highlight;

            for (int j = 0; j < editPanes.length; j++) {
                highlight = (BackgroundHighlight) highlights.get(editPanes[j]);
                if (highlight == null) { continue; }

                if (highlight.isEnabled()) {
                    highlight.updateTextArea();
                }
            }
        }
    }
}

