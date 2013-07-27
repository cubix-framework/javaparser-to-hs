package com.lipstikLF.theme;

import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class KlearlooksTheme extends LipstikColorTheme
{
	public String getName()
	{
		return "Klearlooks Lucidity";
	}

	public String toString()
	{
		return getName();
	}

	private static final ColorUIResource borderDisabled = new ColorUIResource(187, 191, 170);
	private static final ColorUIResource borderNormal = new ColorUIResource(167, 171, 150);
	private static final ColorUIResource borderSelected = new ColorUIResource(183, 181, 173);
	private static final ColorUIResource borderBrighter = new ColorUIResource(184, 188, 167);
	
	// controls background
	private static final ColorUIResource control = new ColorUIResource(208, 212, 184);
	private static final ColorUIResource controlDarkShadow = new ColorUIResource(186, 190, 169);
	private static final ColorUIResource controlShadow  = new ColorUIResource(206, 210, 184);
	private static final ColorUIResource controlHilight = new ColorUIResource(229, 233, 205);

	// menu bar
	private static final ColorUIResource menuBarBackground = new ColorUIResource(202, 206, 178);

	// menu item
	private static final ColorUIResource menuItemSelectedBackground = new ColorUIResource(202, 206, 178);
	private static final ColorUIResource menuItemSelectedFrame = new ColorUIResource(199, 200, 161);

	// popup menu
	private static final ColorUIResource menuBackground = new ColorUIResource(218, 222, 192);
	private static final ColorUIResource menuGradient = new ColorUIResource(190, 194, 173);

	// internal frame
	private static final ColorUIResource internalActiveBackground = new ColorUIResource(161, 162, 128);
	private static final ColorUIResource internalInactiveBackground = borderDisabled;
	private static final ColorUIResource internalButtonBackground = new ColorUIResource(187, 191, 170);
	private static final ColorUIResource internalButtonInactive = new ColorUIResource(193, 194, 162);
	private static final ColorUIResource internalButtonHighlight = new ColorUIResource(201, 202, 170);

	private static final ColorUIResource progressForeground = new ColorUIResource(157, 161, 140);
	private static final ColorUIResource tooltipBackground = new ColorUIResource(255, 255, 220);

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
	public ColorUIResource getMenuGradient()				{ return menuGradient;   }
	public ColorUIResource getMenuSelectedBackground()		{ return menuItemSelectedBackground; }

	public ColorUIResource getControl()						{ return control;           }
	public ColorUIResource getControlDarkShadow()			{ return controlDarkShadow; }
	public ColorUIResource getControlShadow()				{ return controlShadow;     }
	public ColorUIResource getControlHighlight()			{ return controlHilight;    }
	public ColorUIResource getMenuItemSelectedBackground()	{ return menuItemSelectedBackground; }

	public ColorUIResource getProgressForeground()			{ return progressForeground; }
	public ColorUIResource getTextHighlightColor()			{ return menuItemSelectedBackground; }
	public ColorUIResource getTooltipBackground()			{ return tooltipBackground; }

	public ColorUIResource getPrimary1()					{ return internalActiveBackground; }
	public ColorUIResource getPrimary3()					{ return internalActiveBackground; }
	public ColorUIResource getSecondary1()					{ return internalInactiveBackground; }
	public ColorUIResource getSecondary3()					{ return internalInactiveBackground; }

	public ColorUIResource getInternalButtonBackground()	{ return internalButtonBackground; }
	public ColorUIResource getInternalButtonInactive()		{ return internalButtonInactive; }
	public ColorUIResource getInternalButtonHighlight()		{ return internalButtonHighlight; }

	public FontUIResource getStdFont()						{ return stdFont; }
	public FontUIResource getStdBoldFont()					{ return stdBoldFont; }
	public FontUIResource getSubTextFont()					{ return accelFont; }

}