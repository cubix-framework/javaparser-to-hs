package com.lipstikLF.theme;

import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class ZenburnTheme extends LipstikColorTheme
{
    public String getName()
    {
        return "Zenburn";
    }

    public String toString()
    {
        return getName();
    }

    // borders
    private static final ColorUIResource borderDisabled = new ColorUIResource(187, 191, 170);
    private static final ColorUIResource borderNormal = new ColorUIResource(0xa7ab96);
    private static final ColorUIResource borderSelected = new ColorUIResource(0xb7b5ad);
    private static final ColorUIResource borderBrighter = new ColorUIResource(0x486648);

    // controls
    private static final ColorUIResource control = new ColorUIResource(0x3f3f3f);
    private static final ColorUIResource controlDarkShadow = new ColorUIResource(0x486648);
    private static final ColorUIResource controlShadow  = new ColorUIResource(0x7fb47f);
    private static final ColorUIResource controlHilight = new ColorUIResource(0x4f4f4f);
    private static final ColorUIResource controlText = new ColorUIResource(0xf0dfaf);
    private static final ColorUIResource controlInactiveText = new ColorUIResource(0x8f8f8f);

    // menu bar
    private static final ColorUIResource menuBarBackground = new ColorUIResource(0x3f3f3f);

    // menu item
    private static final ColorUIResource menuItemSelectedBackground = new ColorUIResource(0x4f4f4f);
    private static final ColorUIResource menuItemSelectedFrame = new ColorUIResource(0xc7c8a1);

    private static final ColorUIResource textHighlight = new ColorUIResource(0x4f4f4f);
    private static final ColorUIResource highlightedText = new ColorUIResource(0xffffff);

    // popup menu
    private static final ColorUIResource menuBackground = new ColorUIResource(0x3f3f3f);
    private static final ColorUIResource menuForeground = new ColorUIResource(0xdcdccc);
    private static final ColorUIResource menuGradient = new ColorUIResource(190, 194, 173);

    // internal frame
    private static final ColorUIResource internalActiveBackground = new ColorUIResource(161, 162, 128);
    private static final ColorUIResource internalInactiveBackground = new ColorUIResource(187, 191, 170);
    private static final ColorUIResource internalButtonBackground = new ColorUIResource(187, 191, 170);
    private static final ColorUIResource internalButtonInactive = new ColorUIResource(193, 194, 162);
    private static final ColorUIResource internalButtonHighlight = new ColorUIResource(201, 202, 170);

    // progress bar
    private static final ColorUIResource progressForeground = new ColorUIResource(157, 161, 140);

    // tool tips
    private static final ColorUIResource tooltipBackground = new ColorUIResource(0xf0dfaf);

    // window
    private static final ColorUIResource windowBackground = new ColorUIResource(0x484848);//0x3f3f3f);
    private static final ColorUIResource windowForeground = new ColorUIResource(0xdcdccc);
    private static final ColorUIResource textColor = new ColorUIResource(0xdcdccc);

    /** Standard font */
    private static FontUIResource stdFont = new FontUIResource("Tahoma", Font.PLAIN, 11);
    private static FontUIResource stdBoldFont = new FontUIResource("Tahoma", Font.BOLD, 11);
    private static FontUIResource accelFont = new FontUIResource("Tahoma", Font.PLAIN, 10);


    public ColorUIResource getBorderDisabled()				{ return borderDisabled;   }
    public ColorUIResource getBorderNormal()				{ return borderNormal;     }
    public ColorUIResource getBorderSelected()				{ return borderSelected;   }
    public ColorUIResource getBorderBrighter()				{ return borderBrighter;   }

    public ColorUIResource getMenuItemSelectedFrame()		{ return menuItemSelectedFrame; }
    public ColorUIResource getMenuBarBackground()			{ return menuBarBackground; }
    public ColorUIResource getMenuBackground()				{ return menuBackground; }
    public ColorUIResource getMenuText()                    { return menuForeground; }
    public ColorUIResource getMenuGradient()				{ return menuGradient;   }
    public ColorUIResource getMenuSelectedBackground()		{ return menuItemSelectedBackground; }
    public ColorUIResource getMenuItemSelectedBackground()	{ return menuItemSelectedBackground; }

    public ColorUIResource getControl()						{ return control;           }
    public ColorUIResource getControlDarkShadow()			{ return controlDarkShadow; }
    public ColorUIResource getControlShadow()				{ return controlShadow;     }
    public ColorUIResource getControlHighlight()			{ return controlHilight;    }
    public ColorUIResource getControlText()                 { return controlText; }
    public ColorUIResource getInactiveControlTextColor()    { return controlInactiveText; }

    public ColorUIResource getSystemTextColor()             { return controlText; }
    public ColorUIResource getInactiveSystemTextColor()     { return controlInactiveText; }

    public ColorUIResource getProgressForeground()			{ return progressForeground; }
    public ColorUIResource getTextHighlightColor()			{ return textHighlight; }
    public ColorUIResource getHighlightedTextColor()        { return highlightedText; };
    public ColorUIResource getTooltipBackground()			{ return tooltipBackground; }

    public ColorUIResource getInternalButtonBackground()	{ return internalButtonBackground; }
    public ColorUIResource getInternalButtonInactive()		{ return internalButtonInactive; }
    public ColorUIResource getInternalButtonHighlight()		{ return internalButtonHighlight; }

    public ColorUIResource getPrimary1()					{ return internalActiveBackground; }
    public ColorUIResource getPrimary3()					{ return internalActiveBackground; }
    public ColorUIResource getSecondary1()					{ return internalInactiveBackground; }
    public ColorUIResource getSecondary3()					{ return internalInactiveBackground; }

    public FontUIResource getStdFont()						{ return stdFont; }
    public FontUIResource getStdBoldFont()					{ return stdBoldFont; }
    public FontUIResource getSubTextFont()					{ return accelFont; }

    public ColorUIResource getWindowBackground()            { return windowBackground; }
    public ColorUIResource getWindowTextColor()             { return windowForeground; }
    public ColorUIResource getTextColor()                   { return textColor; }
}