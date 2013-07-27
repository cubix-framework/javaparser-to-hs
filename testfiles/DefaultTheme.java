package com.lipstikLF.theme;

import java.awt.Font;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class DefaultTheme extends LipstikColorTheme
{
	public String getName()
	{
		return "Default Dark";
	}

	public String toString()
	{
		return getName();
	}

	private static final ColorUIResource borderDisabled = new ColorUIResource(134, 144, 150);
	private static final ColorUIResource borderNormal = new ColorUIResource(135, 132, 127);
	private static final ColorUIResource borderSelected = new ColorUIResource(202, 198, 190);
	private static final ColorUIResource borderBrighter = new ColorUIResource(162, 159, 154);

	// controls background
	private static final ColorUIResource control = new ColorUIResource(212, 208, 200);
	private static final ColorUIResource controlDarkShadow = new ColorUIResource(166, 173, 156);
	private static final ColorUIResource controlShadow  = new ColorUIResource(199, 195, 189);
	private static final ColorUIResource controlHilight = new ColorUIResource(234, 230, 221);

	// menu bar
	private static final ColorUIResource menuBarBackground = new ColorUIResource(206, 203, 194);

	// menu item
	private static final ColorUIResource menuItemSelectedBackground = new ColorUIResource(206, 202, 194);
	private static final ColorUIResource menuItemSelectedFrame = new ColorUIResource(200, 197, 188);

	// popup menu
	private static final ColorUIResource menuBackground = new ColorUIResource(228, 224, 216);
	private static final ColorUIResource menuGradient = new ColorUIResource(194, 190, 182);

	// internal frame
	private static final ColorUIResource internalActiveBackground = new ColorUIResource(121, 132, 109);
	private static final ColorUIResource internalInactiveBackground = borderDisabled;
	private static final ColorUIResource internalButtonBackground = new ColorUIResource(160, 168, 151);
	private static final ColorUIResource internalButtonInactive = new ColorUIResource(154, 164, 170);
	private static final ColorUIResource internalButtonHighlight = new ColorUIResource(170, 177, 163);

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
	public ColorUIResource getMenuSelectedBackground()		{ return menuItemSelectedBackground; }
	public ColorUIResource getMenuGradient()				{ return menuGradient;   }
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

  // public ColorUIResource getProgressBackground()             { return progressBackground; }
  // primary1 - active internal frame contour
  // primary2 - internal pane background
  // primary3 - active internal frame title bar
  // secondary1 - inactive internal frame contour
  // secondary2 - disabled menu item text
  // secondary3 - inactive internal frame title bar
}