package com.lipstikLF.theme;

import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class LightGrayTheme extends LipstikColorTheme
{
	public String getName()
	{
		return "Light Gray";
	}

	public String toString()
	{
		return getName();
	}

	private static final ColorUIResource borderDisabled = new ColorUIResource(204, 199, 194);
	private static final ColorUIResource borderNormal = new ColorUIResource(186, 183, 178);
	private static final ColorUIResource borderSelected = new ColorUIResource(183, 181, 173);
	private static final ColorUIResource borderBrighter = new ColorUIResource(196, 193, 188);

	// controls background
	private static final ColorUIResource control = new ColorUIResource(232, 229, 222);
	private static final ColorUIResource controlDarkShadow = new ColorUIResource(203, 201, 195);
	private static final ColorUIResource controlShadow  = new ColorUIResource(227, 224, 217);
	private static final ColorUIResource controlHilight = new ColorUIResource(242, 239, 232);

	// menu bar
	private static final ColorUIResource menuBarBackground = new ColorUIResource(227, 224, 217);

	// menu item
	private static final ColorUIResource menuItemSelectedBackground = new ColorUIResource(225, 222, 215);
	private static final ColorUIResource menuItemSelectedFrame = new ColorUIResource(223, 219, 201);

	// popup menu
	private static final ColorUIResource menuBackground = new ColorUIResource(248, 244, 236);
	private static final ColorUIResource menuGradient = new ColorUIResource(218, 214, 196);

	// internal frame
	private static final ColorUIResource internalActiveBackground = new ColorUIResource(150, 186, 196);
	private static final ColorUIResource internalInactiveBackground = new ColorUIResource(133, 159, 184);
	private static final ColorUIResource internalButtonBackground = new ColorUIResource(190, 206, 206);
	private static final ColorUIResource internalButtonInactive = new ColorUIResource(180, 192, 194);
	private static final ColorUIResource internalButtonHighlight = new ColorUIResource(200, 216, 216);

	private static final ColorUIResource progressForeground = new ColorUIResource(62,113,160);
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
