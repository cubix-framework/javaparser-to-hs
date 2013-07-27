package com.lipstikLF;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.DefaultEditorKit;

import com.lipstikLF.theme.DefaultTheme;
import com.lipstikLF.theme.LipstikColorTheme;
import com.lipstikLF.util.LipstikBorderFactory;
import com.lipstikLF.util.LipstikIconFactory;

public class LipstikLookAndFeel extends BasicLookAndFeel
{
	private static List installedThemes;
	private static LipstikColorTheme myCurrentTheme;

	private static final String[] themeNames = { "KlearlooksTheme", "LightGrayTheme", "DefaultTheme", "ZenburnTheme" };

	public LipstikLookAndFeel()
	{
		if (getMyCurrentTheme() == null)
		{
			String theme = getSystemProperty("lipstikLF.theme");
			if (theme != null)
			{
				for (int i=0; i<themeNames.length; i++)
					if (theme.equals(themeNames[i]))
					{
						setMyCurrentTheme(createTheme(theme));
						return;
					}
			}
			setMyCurrentTheme(new DefaultTheme());
		}
	}

	public final String getID()
	{
		return "Lipstik";
	}

	public final String getName()
	{
		return "Lipstik";
	}

	public final String getDescription()
	{
		return "Lipstik Look And Feel: Based on original KDE Lipstik theme";
	}

	public boolean isNativeLookAndFeel()
	{
		return false;
	}

	public final boolean isSupportedLookAndFeel()
	{
		return true;
	}

    public final boolean getSupportsWindowDecorations()
    {
        return true;
    }

	protected void initClassDefaults(UIDefaults table)
	{
		super.initClassDefaults(table);
		table.putDefaults(new Object[] {
				"DesktopIconUI", "com.lipstikLF.delegate.LipstikDesktopIconUI",
				"ButtonUI", "com.lipstikLF.delegate.LipstikButtonUI",
				"CheckBoxUI", "com.lipstikLF.delegate.LipstikCheckBoxUI",
				"CheckBoxMenuItemUI", "com.lipstikLF.delegate.LipstikCheckBoxMenuItemUI",
				"MenuItemUI", "com.lipstikLF.delegate.LipstikMenuItemUI",
				"MenuUI", "com.lipstikLF.delegate.LipstikMenuUI",
				"PopupMenuSeparatorUI",	"com.lipstikLF.delegate.LipstikPopupMenuSeparatorUI",
				"RadioButtonUI", "com.lipstikLF.delegate.LipstikRadioButtonUI",
				"RadioButtonMenuItemUI", "com.lipstikLF.delegate.LipstikRadioButtonMenuItemUI",
				"ScrollBarUI", "com.lipstikLF.delegate.LipstikScrollBarUI",
				"ComboBoxUI", "com.lipstikLF.delegate.LipstikComboBoxUI",
				"TabbedPaneUI", "com.lipstikLF.delegate.LipstikTabbedPaneUI",
				"ProgressBarUI", "com.lipstikLF.delegate.LipstikProgressBarUI",
				"TreeUI", "com.lipstikLF.delegate.LipstikTreeUI",
				"PasswordFieldUI", "com.lipstikLF.delegate.LipstikPasswordFieldUI",
				"SeparatorUI", "com.lipstikLF.delegate.LipstikSeparatorUI",
				"SpinnerUI", "com.lipstikLF.delegate.LipstikSpinnerUI",
				"TableHeaderUI", "com.lipstikLF.delegate.LipstikTableHeaderUI",
				"ToggleButtonUI", "com.lipstikLF.delegate.LipstikButtonUI",
				"ToolBarUI", "com.lipstikLF.delegate.LipstikToolBarUI",
				"SliderUI", "com.lipstikLF.delegate.LipstikSliderUI",
				"SplitPaneUI", "com.lipstikLF.delegate.LipstikSplitPaneUI",
				"FileChooserUI", "com.lipstikLF.delegate.LipstikFileChooserUI",
				"InternalFrameUI", "com.lipstikLF.delegate.LipstikInternalFrameUI",
				"RootPaneUI", "com.lipstikLF.delegate.LipstikRootPaneUI",
				"OptionPaneUI", "com.lipstikLF.delegate.LipstikOptionPaneUI"
				});

	}

    protected void initSystemColorDefaults(UIDefaults table)
    {
    	LipstikColorTheme theme = getMyCurrentTheme();

        Object[] systemColors = {
                      "desktop", theme.getDesktopColor(), /* Color of the desktop background */
                "activeCaption", theme.getWindowTitleBackground(), /* Color for captions (title bars) when they are active. */
            "activeCaptionText", theme.getWindowTitleForeground(), /* Text color for text in captions (title bars). */
          "activeCaptionBorder", theme.getPrimaryControlShadow(), /* Border color for caption (title bar) window borders. */
              "inactiveCaption", theme.getWindowTitleInactiveBackground(), /* Color for captions (title bars) when not active. */
          "inactiveCaptionText", theme.getWindowTitleInactiveForeground(), /* Text color for text in inactive captions (title bars). */
        "inactiveCaptionBorder", theme.getControlShadow(), /* Border color for inactive caption (title bar) window borders. */
                       "window", theme.getWindowBackground(), /* Default color for the interior of windows */
                 "windowBorder", theme.getWindowBorder(),  /* ??? */
                   "windowText", theme.getWindowText(),  /* ??? */
                         "menu", theme.getMenuBackground(), /* Background color for menus */
                     "menuText", theme.getMenuText(),  /* Text color for menus  */
                         "text", theme.getWindowBackground(), /* Text background color */
                     "textText", theme.getTextColor(),  /* Text foreground color */
                "textHighlight", theme.getTextHighlightColor(), /* Text background color when selected */
            "textHighlightText", theme.getHighlightedTextColor(), /* Text color when selected */
             "textInactiveText", theme.getInactiveSystemTextColor(), /* Text color when disabled */
                      "control", theme.getControl(), /* Default color for controls (buttons, sliders, etc) */
                  "controlText", theme.getControlText(),  /* Default color for text in controls */
             "controlHighlight", theme.getControlHighlight(), /* Specular highlight (opposite of the shadow) */
           "controlLtHighlight", theme.getControlHighlight(), /* Highlight color for controls */
                "controlShadow", theme.getControlShadow(), /* Shadow color for controls */
              "controlDkShadow", theme.getControlDarkShadow(),  /* Dark shadow color for controls */
                    "scrollbar", theme.getControl(), /* Scrollbar background (usually the "track") */
                         "info", theme.getPrimaryControl(), /* ToolTip Background */
                     "infoText", theme.getPrimaryControlInfo()  /* ToolTip Text */
        };
        table.putDefaults(systemColors);
    }


	/**
	 * Sets the current color theme.
	 *
	 * @param theme The theme to install.
	 */
	public static void setMyCurrentTheme(LipstikColorTheme theme)
	{
		myCurrentTheme = theme;
		MetalLookAndFeel.setCurrentTheme(theme);
	}

	public static LipstikColorTheme getMyCurrentTheme()
	{
		return myCurrentTheme;
	}

    private void initResourceBundle(UIDefaults table) {
        table.addResourceBundle( "com.sun.swing.internal.plaf.metal.resources.metal" );
    }

	protected void initComponentDefaults(UIDefaults table)
	{
		FontUIResource menuFontResource;
		FontUIResource menuItemFontResource;
        Object tabbedPaneTabAreaInsets = new InsetsUIResource(2, 2, 0, 3);
        Object tabbedPaneTabInsets = new InsetsUIResource(1, 10, 1, 8);
        Object tabbedPaneContentInsets = new InsetsUIResource(3, 3, 2, 2);

        super.initComponentDefaults(table);
        initResourceBundle(table);

		final int barStyle;
		String menuBar = getSystemProperty("lipstikLF.menuBar");
		String menuFont = getSystemProperty("lipstikLF.menuFont");

		if (menuBar != null)
		{
			if ("solid".equals(menuBar))
				barStyle = LipstikBorderFactory.MENUBAR_SHARP;
			else
			if ("none".equals(menuBar))
				barStyle = LipstikBorderFactory.MENUBAR_NONE;
			else
				barStyle = LipstikBorderFactory.MENUBAR_SHADOW;
		}
		else
			barStyle = LipstikBorderFactory.MENUBAR_SHADOW;

		LipstikColorTheme theme = getMyCurrentTheme();

		if (menuFont != null && !"".equals(menuFont))
		{
			menuFontResource = new FontUIResource(menuFont, Font.PLAIN, 11);
			menuItemFontResource = menuFontResource;
		}
		else
		{
			menuFontResource = theme.getStdBoldFont();
			menuItemFontResource = theme.getStdFont();
		}

		UIDefaults.LazyValue scrollPaneBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getScrollPaneBorder();
			}
		};

		UIDefaults.LazyValue flatRoundBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getFlatControlBorder();
			}
		};

		UIDefaults.LazyValue flatComboBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getFlatComboBorder();
			}
		};

		UIDefaults.LazyValue roundBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory
						.getRoundControlBorder();
			}
		};

		UIDefaults.LazyValue menuBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getButtonMenuBorder();
			}
		};
		
		UIDefaults.LazyValue menuItemBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getMenuItemBorder();
			}
		};

		UIDefaults.LazyValue popupMenuBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getPopupMenuBorder(barStyle);
			}
		};
		
		UIDefaults.LazyValue toolBarBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getToolBarBorder();
			}
		};

		UIDefaults.LazyValue toolBarButtonBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getButtonToolBorder();
			}
		};

		UIDefaults.LazyValue pushButtonBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getButtonPushBorder();
			}
		};

		UIDefaults.LazyValue arrowButtonBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getArrowBorder();
			}
		};

		UIDefaults.LazyValue spinButtonBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getSpinBorder();
			}
		};

		UIDefaults.LazyValue focusBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getFocusBorder();
			}
		};

		UIDefaults.LazyValue tableHeaderCellBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getTableHeaderCellBorder();
			}
		};

		UIDefaults.LazyValue internalFrameBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getInternalFrameBorder(false);
			}
		};

		UIDefaults.LazyValue internalFrameBorderResizable = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getInternalFrameBorder(true);
			}
		};

		UIDefaults.LazyValue tooltipBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getToolTipBorder();
			}
		};

		UIDefaults.LazyValue menuBarBorder = new UIDefaults.LazyValue()
		{
			public Object createValue(UIDefaults table)
			{
				return LipstikBorderFactory.getMenuBarBorder();
			}
		};

		Object menuArrowIcon =
			 new UIDefaults.ProxyLazyValue("javax.swing.plaf.metal.MetalIconFactory", "getMenuArrowIcon");

		Object menuItemArrowIcon =
			 new UIDefaults.ProxyLazyValue("javax.swing.plaf.metal.MetalIconFactory", "getMenuItemArrowIcon");

		Object fieldInputMap = new UIDefaults.LazyInputMap(new Object[] {
			"ctrl C", DefaultEditorKit.copyAction,
			"ctrl V", DefaultEditorKit.pasteAction,
			"ctrl X", DefaultEditorKit.cutAction,
			"COPY", DefaultEditorKit.copyAction,
			"PASTE", DefaultEditorKit.pasteAction,
			"CUT", DefaultEditorKit.cutAction,
			"shift LEFT", DefaultEditorKit.selectionBackwardAction,
			"shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
			"shift RIGHT", DefaultEditorKit.selectionForwardAction,
			"shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
			"ctrl LEFT", DefaultEditorKit.previousWordAction,
			"ctrl KP_LEFT", DefaultEditorKit.previousWordAction,
			"ctrl RIGHT", DefaultEditorKit.nextWordAction,
			"ctrl KP_RIGHT", DefaultEditorKit.nextWordAction,
			"ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
			"ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
			"ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction,
			"ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
			"ctrl A", DefaultEditorKit.selectAllAction,
			"HOME", DefaultEditorKit.beginLineAction,
			"END", DefaultEditorKit.endLineAction,
			"shift HOME", DefaultEditorKit.selectionBeginLineAction,
			"shift END", DefaultEditorKit.selectionEndLineAction,
			"BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
			"ctrl H", DefaultEditorKit.deletePrevCharAction,
			"DELETE", DefaultEditorKit.deleteNextCharAction,
			"RIGHT", DefaultEditorKit.forwardAction,
			"LEFT", DefaultEditorKit.backwardAction,
			"KP_RIGHT", DefaultEditorKit.forwardAction,
			"KP_LEFT", DefaultEditorKit.backwardAction,
			"ENTER", JTextField.notifyAction,
			"ctrl BACK_SLASH", "unselect"/*DefaultEditorKit.unselectAction*/,
			"control shift O", "toggle-componentOrientation"/*DefaultEditorKit.toggleComponentOrientation*/ });

		Object formattedFieldInputMap = new UIDefaults.LazyInputMap(new Object[] {
			"ctrl C", DefaultEditorKit.copyAction,
			"ctrl V", DefaultEditorKit.pasteAction,
			"ctrl X", DefaultEditorKit.cutAction,
			"COPY", DefaultEditorKit.copyAction,
			"PASTE", DefaultEditorKit.pasteAction,
			"CUT", DefaultEditorKit.cutAction,
			"shift LEFT", DefaultEditorKit.selectionBackwardAction,
			"shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
			"shift RIGHT", DefaultEditorKit.selectionForwardAction,
			"shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
			"ctrl LEFT", DefaultEditorKit.previousWordAction,
			"ctrl KP_LEFT", DefaultEditorKit.previousWordAction,
			"ctrl RIGHT", DefaultEditorKit.nextWordAction,
			"ctrl KP_RIGHT", DefaultEditorKit.nextWordAction,
			"ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
			"ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
			"ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction,
			"ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
			"ctrl A", DefaultEditorKit.selectAllAction,
			"HOME", DefaultEditorKit.beginLineAction,
			"END", DefaultEditorKit.endLineAction,
			"shift HOME", DefaultEditorKit.selectionBeginLineAction,
			"shift END", DefaultEditorKit.selectionEndLineAction,
			"BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
			"ctrl H", DefaultEditorKit.deletePrevCharAction,
			"DELETE", DefaultEditorKit.deleteNextCharAction,
			"RIGHT", DefaultEditorKit.forwardAction,
			"LEFT", DefaultEditorKit.backwardAction,
			"KP_RIGHT", DefaultEditorKit.forwardAction,
			"KP_LEFT", DefaultEditorKit.backwardAction,
			"ENTER", JTextField.notifyAction,
			"ctrl BACK_SLASH", "unselect",
			"control shift O", "toggle-componentOrientation",
			"ESCAPE", "reset-field-edit",
			"UP", "increment",
			"KP_UP", "increment",
			"DOWN", "decrement",
			"KP_DOWN", "decrement"	});

	        Object passwordInputMap = new UIDefaults.LazyInputMap(new Object[] {
			"ctrl C", DefaultEditorKit.copyAction,
			"ctrl V", DefaultEditorKit.pasteAction,
			"ctrl X", DefaultEditorKit.cutAction,
			"COPY", DefaultEditorKit.copyAction,
			"PASTE", DefaultEditorKit.pasteAction,
			"CUT", DefaultEditorKit.cutAction,
			"shift LEFT", DefaultEditorKit.selectionBackwardAction,
			"shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
			"shift RIGHT", DefaultEditorKit.selectionForwardAction,
			"shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
			"ctrl LEFT", DefaultEditorKit.beginLineAction,
			"ctrl KP_LEFT", DefaultEditorKit.beginLineAction,
			"ctrl RIGHT", DefaultEditorKit.endLineAction,
			"ctrl KP_RIGHT", DefaultEditorKit.endLineAction,
			"ctrl shift LEFT", DefaultEditorKit.selectionBeginLineAction,
			"ctrl shift KP_LEFT", DefaultEditorKit.selectionBeginLineAction,
			"ctrl shift RIGHT", DefaultEditorKit.selectionEndLineAction,
			"ctrl shift KP_RIGHT", DefaultEditorKit.selectionEndLineAction,
			"ctrl A", DefaultEditorKit.selectAllAction,
			"HOME", DefaultEditorKit.beginLineAction,
			"END", DefaultEditorKit.endLineAction,
			"shift HOME", DefaultEditorKit.selectionBeginLineAction,
			"shift END", DefaultEditorKit.selectionEndLineAction,
			"BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
			"ctrl H", DefaultEditorKit.deletePrevCharAction,
			"DELETE", DefaultEditorKit.deleteNextCharAction,
			"RIGHT", DefaultEditorKit.forwardAction,
			"LEFT", DefaultEditorKit.backwardAction,
			"KP_RIGHT", DefaultEditorKit.forwardAction,
			"KP_LEFT", DefaultEditorKit.backwardAction,
			"ENTER", JTextField.notifyAction,
			"ctrl BACK_SLASH", "unselect"/*DefaultEditorKit.unselectAction*/,
			"control shift O", "toggle-componentOrientation"/*DefaultEditorKit.toggleComponentOrientation*/ });

	        Object multilineInputMap = new UIDefaults.LazyInputMap(new Object[] {
			"ctrl C", DefaultEditorKit.copyAction,
			"ctrl V", DefaultEditorKit.pasteAction,
			"ctrl X", DefaultEditorKit.cutAction,
			"COPY", DefaultEditorKit.copyAction,
			"PASTE", DefaultEditorKit.pasteAction,
			"CUT", DefaultEditorKit.cutAction,
			"shift LEFT", DefaultEditorKit.selectionBackwardAction,
			"shift KP_LEFT", DefaultEditorKit.selectionBackwardAction,
			"shift RIGHT", DefaultEditorKit.selectionForwardAction,
			"shift KP_RIGHT", DefaultEditorKit.selectionForwardAction,
			"ctrl LEFT", DefaultEditorKit.previousWordAction,
			"ctrl KP_LEFT", DefaultEditorKit.previousWordAction,
			"ctrl RIGHT", DefaultEditorKit.nextWordAction,
			"ctrl KP_RIGHT", DefaultEditorKit.nextWordAction,
			"ctrl shift LEFT", DefaultEditorKit.selectionPreviousWordAction,
			"ctrl shift KP_LEFT", DefaultEditorKit.selectionPreviousWordAction,
			"ctrl shift RIGHT", DefaultEditorKit.selectionNextWordAction,
			"ctrl shift KP_RIGHT", DefaultEditorKit.selectionNextWordAction,
			"ctrl A", DefaultEditorKit.selectAllAction,
			"HOME", DefaultEditorKit.beginLineAction,
			"END", DefaultEditorKit.endLineAction,
			"shift HOME", DefaultEditorKit.selectionBeginLineAction,
			"shift END", DefaultEditorKit.selectionEndLineAction,

			"UP", DefaultEditorKit.upAction,
			"KP_UP", DefaultEditorKit.upAction,
			"DOWN", DefaultEditorKit.downAction,
			"KP_DOWN", DefaultEditorKit.downAction,
			"PAGE_UP", DefaultEditorKit.pageUpAction,
			"PAGE_DOWN", DefaultEditorKit.pageDownAction,
			"shift PAGE_UP", "selection-page-up",
			"shift PAGE_DOWN", "selection-page-down",
			"ctrl shift PAGE_UP", "selection-page-left",
			"ctrl shift PAGE_DOWN", "selection-page-right",
			"shift UP", DefaultEditorKit.selectionUpAction,
			"shift KP_UP", DefaultEditorKit.selectionUpAction,
			"shift DOWN", DefaultEditorKit.selectionDownAction,
			"shift KP_DOWN", DefaultEditorKit.selectionDownAction,
			"ENTER", DefaultEditorKit.insertBreakAction,
			"BACK_SPACE", DefaultEditorKit.deletePrevCharAction,
			"ctrl H", DefaultEditorKit.deletePrevCharAction,
			"DELETE", DefaultEditorKit.deleteNextCharAction,
			"RIGHT", DefaultEditorKit.forwardAction,
			"LEFT", DefaultEditorKit.backwardAction,
			"KP_RIGHT", DefaultEditorKit.forwardAction,
			"KP_LEFT", DefaultEditorKit.backwardAction,
			"TAB", DefaultEditorKit.insertTabAction,
			"ctrl BACK_SLASH", "unselect"/*DefaultEditorKit.unselectAction*/,
			"ctrl HOME", DefaultEditorKit.beginAction,
			"ctrl END", DefaultEditorKit.endAction,
			"ctrl shift HOME", DefaultEditorKit.selectionBeginAction,
			"ctrl shift END", DefaultEditorKit.selectionEndAction,
			"ctrl T", "next-link-action",
			"ctrl shift T", "previous-link-action",
			"ctrl SPACE", "activate-link-action",
			"control shift O", "toggle-componentOrientation"/*DefaultEditorKit.toggleComponentOrientation*/ });

		InsetsUIResource textInsets = new InsetsUIResource(1, 2, 1, 1);
		InsetsUIResource menuItemMargin = new InsetsUIResource(3, 0, 3, 0);

		table.put("Button.textShiftOffset", new Integer(5));
		table.put("Button.border", pushButtonBorder);
		table.put("Button.borderColor", theme.getBorderNormal());
		table.put("Button.font", theme.getStdFont());
        table.put("Button.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
                "SPACE", "pressed",
                "released SPACE", "released"}));

		table.put("CheckBox.font", theme.getStdFont());
		table.put("CheckBox.unselectedEnabledIcon", LookAndFeel.makeIcon(getClass(), "icons/checkbox0.gif"));
		table.put("CheckBox.selectedEnabledIcon", LookAndFeel.makeIcon(getClass(), "icons/checkbox1.gif"));
		table.put("CheckBox.unselectedDisabledIcon", LookAndFeel.makeIcon(getClass(), "icons/checkbox0_disabled.gif"));
		table.put("CheckBox.selectedDisabledIcon", LookAndFeel.makeIcon(getClass(), "icons/checkbox1_disabled.gif"));
	    table.put("CheckBox.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
	            "SPACE", "pressed",
                "released SPACE", "released"}));

		table.put("CheckBoxMenuItem.opaque", Boolean.FALSE);
		table.put("CheckBoxMenuItem.background", theme.getMenuBackground());
		table.put("CheckBoxMenuItem.selectionBackground", theme.getMenuItemSelectedBackground());
		table.put("CheckBoxMenuItem.border", menuItemBorder);
		table.put("CheckBoxMenuItem.borderPainted", Boolean.FALSE);
		table.put("CheckBoxMenuItem.checkIcon",	LipstikIconFactory.checkBoxMenuIcon);
		table.put("CheckBoxMenuItem.font", theme.getStdFont());
		table.put("CheckBoxMenuItem.margin", menuItemMargin);

		table.put("ComboBox.arrowButtonBorder", arrowButtonBorder);
		table.put("ComboBox.background", theme.getMenuBackground());
		table.put("ComboBox.selectionBackground", theme.getMenuItemSelectedBackground());
		table.put("ComboBox.editorBorder", flatComboBorder);
		table.put("ComboBox.font", theme.getStdFont());
	    table.put("ComboBox.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {
			     "ESCAPE", "hidePopup",
			    "PAGE_UP", "pageUpPassThrough",
			  "PAGE_DOWN", "pageDownPassThrough",
			       "HOME", "homePassThrough",
			        "END", "endPassThrough",
			       "DOWN", "selectNext",
			    "KP_DOWN", "selectNext",
			   "alt DOWN", "togglePopup",
			"alt KP_DOWN", "togglePopup",
			     "alt UP", "togglePopup",
			  "alt KP_UP", "togglePopup",
			      "SPACE", "spacePopup",
			      "ENTER", "enterPressed",
			         "UP", "selectPrevious",
			      "KP_UP", "selectPrevious"}));
	    
		table.put("DesktopIcon.font", theme.getSubTextFont());
		table.put("DesktopIcon.width", new Integer(160));
		table.put("DesktopIcon.background", theme.getControl());
		table.put("Desktop.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {
				"ctrl F5", "restore",
				"ctrl F4", "close",
				"ctrl F7", "move",
				"ctrl F8", "resize",
				"RIGHT", "right",
				"KP_RIGHT", "right",
				"shift RIGHT", "shrinkRight",
				"shift KP_RIGHT", "shrinkRight",
				"LEFT", "left",
				"KP_LEFT", "left",
				"shift LEFT", "shrinkLeft",
				"shift KP_LEFT", "shrinkLeft",
				"UP", "up",
				"KP_UP", "up",
                "shift UP", "shrinkUp",
                "shift KP_UP", "shrinkUp",
                "DOWN", "down",
                "KP_DOWN", "down",
                "shift DOWN", "shrinkDown",
                "shift KP_DOWN", "shrinkDown",
                "ESCAPE", "escape",
                "ctrl F9", "minimize",
                "ctrl F10", "maximize",
                "ctrl F6", "selectNextFrame",
                "ctrl TAB", "selectNextFrame",
                "ctrl alt F6", "selectNextFrame",
                "shift ctrl alt F6", "selectPreviousFrame",
                "ctrl F12", "navigateNext",
                "shift ctrl F12", "navigatePrevious"}));

		table.put("EditorPane.focusInputMap", multilineInputMap);

		table.put("FormattedTextField.border", flatRoundBorder);
		table.put("FormattedTextField.font", theme.getStdFont());
		table.put("FormattedTextField.selectionBackground", theme.getMenuItemSelectedBackground());

		table.put("FormattedTextField.border", flatRoundBorder);

		table.put("FileChooser.newFolderIcon", makeIcon(getClass(),	"icons/file_new.gif"));
		table.put("FileChooser.upFolderIcon", makeIcon(getClass(), "icons/file_back.gif"));
		table.put("FileChooser.homeFolderIcon", makeIcon(getClass(), "icons/file_home.gif"));
		table.put("FileChooser.newFolderIconDisabled", makeIcon(getClass(),	"icons/file_new_disabled.gif"));
		table.put("FileChooser.upFolderIconDisabled", makeIcon(getClass(),	"icons/file_back_disabled.gif"));

		table.put("FileChooser.detailsViewIcon", makeIcon(getClass(), "icons/file_viewlist.gif"));
		table.put("FileChooser.listViewIcon", makeIcon(getClass(),	"icons/file_multicolumn.gif"));
		table.put("FileChooser.folderIcon", makeIcon(getClass(), "icons/folder_closed.gif"));
	    table.put("FileChooser.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {
		     "ESCAPE", "cancelSelection",
		     "F2", "editFileName",
		     "F5", "refresh",
		     "BACK_SPACE", "Go Up",
		     "ENTER", "approveSelection"}));

		table.put("FileView.fileIcon", makeIcon(getClass(), "icons/leaf.gif"));
		table.put("FileView.directoryIcon", makeIcon(getClass(), "icons/folder_closed.gif"));

		table.put("FormattedTextField.focusInputMap", formattedFieldInputMap);

		table.put("InternalFrame.border", internalFrameBorderResizable);
		table.put("InternalFrame.paletteBorder", internalFrameBorder);
		table.put("InternalFrame.optionDialogBorder", internalFrameBorder);

		table.put("InternalFrame.font", MetalLookAndFeel.getWindowTitleFont());
		table.put("InternalFrame.closeIcon", LookAndFeel.makeIcon(getClass(), "icons/frame_close.gif"));
		table.put("InternalFrame.icon", LookAndFeel.makeIcon(getClass(), "icons/frame_icon.gif"));
		table.put("InternalFrame.maximizeIcon", LookAndFeel.makeIcon(getClass(), "icons/frame_max.gif"));
		table.put("InternalFrame.minimizeIcon", LookAndFeel.makeIcon(getClass(), "icons/frame_res.gif"));
		table.put("InternalFrame.iconifyIcon", LookAndFeel.makeIcon(getClass(),	"icons/frame_min.gif"));
		table.put("InternalFrame.activeTitleForeground", Color.WHITE);
		table.put("InternalFrame.inactiveTitleForeground", Color.BLACK);
		table.put("InternalFrame.paletteTitleHeight", new Integer(11));
		table.put("InternalFrame.paletteCloseIcon", LookAndFeel.makeIcon(getClass(), "icons/palette_close.gif"));

		table.put("Label.font", theme.getStdFont());

		table.put("List.focusCellHighlightBorder", focusBorder);
		table.put("List.font", theme.getStdFont());
		table.put("List.selectionBackground", theme.getMenuItemSelectedBackground());
		table.put("List.scrollPaneBorder", scrollPaneBorder);
	    table.put("List.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
	    		"ctrl C", "copy",
	    		"ctrl V", "paste",
	    		"ctrl X", "cut",
                "COPY", "copy",
                "PASTE", "paste",
                "CUT", "cut",
                "UP", "selectPreviousRow",
                "KP_UP", "selectPreviousRow",
                "shift UP", "selectPreviousRowExtendSelection",
                "shift KP_UP", "selectPreviousRowExtendSelection",
                "ctrl shift UP", "selectPreviousRowExtendSelection",
                "ctrl shift KP_UP", "selectPreviousRowExtendSelection",
                "ctrl UP", "selectPreviousRowChangeLead",
                "ctrl KP_UP", "selectPreviousRowChangeLead",
                "DOWN", "selectNextRow",
                "KP_DOWN", "selectNextRow",
                "shift DOWN", "selectNextRowExtendSelection",
                "shift KP_DOWN", "selectNextRowExtendSelection",
                "ctrl shift DOWN", "selectNextRowExtendSelection",
                "ctrl shift KP_DOWN", "selectNextRowExtendSelection",
                "ctrl DOWN", "selectNextRowChangeLead",
                "ctrl KP_DOWN", "selectNextRowChangeLead",
                "LEFT", "selectPreviousColumn",
                "KP_LEFT", "selectPreviousColumn",
                "shift LEFT", "selectPreviousColumnExtendSelection",
                "shift KP_LEFT", "selectPreviousColumnExtendSelection",
                "ctrl shift LEFT", "selectPreviousColumnExtendSelection",
                "ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection",
                "ctrl LEFT", "selectPreviousColumnChangeLead",
                "ctrl KP_LEFT", "selectPreviousColumnChangeLead",
            	"RIGHT", "selectNextColumn",
            	"KP_RIGHT", "selectNextColumn",
            	"shift RIGHT", "selectNextColumnExtendSelection",
            	"shift KP_RIGHT", "selectNextColumnExtendSelection",
            	"ctrl shift RIGHT", "selectNextColumnExtendSelection",
            	"ctrl shift KP_RIGHT", "selectNextColumnExtendSelection",
            	"ctrl RIGHT", "selectNextColumnChangeLead",
            	"ctrl KP_RIGHT", "selectNextColumnChangeLead",
            	"HOME", "selectFirstRow",
            	"shift HOME", "selectFirstRowExtendSelection",
            	"ctrl shift HOME", "selectFirstRowExtendSelection",
            	"ctrl HOME", "selectFirstRowChangeLead",
            	"END", "selectLastRow",
            	"shift END", "selectLastRowExtendSelection",
            	"ctrl shift END", "selectLastRowExtendSelection",
            	"ctrl END", "selectLastRowChangeLead",
            	"PAGE_UP", "scrollUp",
            	"shift PAGE_UP", "scrollUpExtendSelection",
            	"ctrl shift PAGE_UP", "scrollUpExtendSelection",
            	"ctrl PAGE_UP", "scrollUpChangeLead",
            	"PAGE_DOWN", "scrollDown",
            	"shift PAGE_DOWN", "scrollDownExtendSelection",
            	"ctrl shift PAGE_DOWN", "scrollDownExtendSelection",
            	"ctrl PAGE_DOWN", "scrollDownChangeLead",
            	"ctrl A", "selectAll",
            	"ctrl SLASH", "selectAll",
            	"ctrl BACK_SLASH", "clearSelection",
            	"SPACE", "addToSelection",
                "ctrl SPACE", "toggleAndAnchor",
                "shift SPACE", "extendTo",
                "ctrl shift SPACE", "moveSelectionTo"}));

	    table.put("Menu.arrowIcon", menuArrowIcon);
		table.put("Menu.border", menuBorder);
		table.put("Menu.borderPainted", Boolean.TRUE);
		table.put("Menu.font", menuFontResource);
		table.put("Menu.selectionBackground", theme.getMenuSelectedBackground());
	    table.put("Menu.submenuPopupOffsetX", new Integer(-4));
	    table.put("Menu.submenuPopupOffsetY", new Integer(-3));

		table.put("MenuBar.border", menuBarBorder);
		table.put("MenuBar.background", theme.getMenuBarBackground());

        table.put("MenuItem.arrowIcon", menuItemArrowIcon);
		table.put("MenuItem.border", menuItemBorder);
		table.put("MenuItem.background", theme.getMenuBackground());
		table.put("MenuItem.selectionBackground", theme.getMenuSelectedBackground());
		table.put("MenuItem.font", menuItemFontResource);
		table.put("MenuItem.acceleratorFont", theme.getSubTextFont());
		table.put("MenuItem.acceleratorForeground", theme.getBorderNormal());
		table.put("MenuItem.disabledForeground", theme.getBorderDisabled());
		table.put("MenuItem.margin", menuItemMargin);
		table.put("MenuItem.opaque", Boolean.valueOf(barStyle == LipstikBorderFactory.MENUBAR_NONE));

		//table.put("OptionPane.background", windowBg);
		//table.put("OptionPane.border", new BorderUIResource.LineBorderUIResource(Color.BLACK, 10));

		table.put("OptionPane.buttonAreaBorder", null);
		table.put("OptionPane.warningIcon", LookAndFeel.makeIcon(getClass(), "icons/icon_warning.gif"));
		table.put("OptionPane.questionIcon", LookAndFeel.makeIcon(getClass(), "icons/icon_question.gif"));
		table.put("OptionPane.informationIcon", LookAndFeel.makeIcon(getClass(), "icons/icon_info.gif"));
		table.put("OptionPane.errorIcon", LookAndFeel.makeIcon(getClass(), "icons/icon_error.gif"));

		table.put("Panel.font", theme.getStdBoldFont());

		table.put("PasswordField.border", flatRoundBorder);
		table.put("PasswordField.font", theme.getStdFont());
		table.put("PasswordField.selectionBackground", theme.getMenuItemSelectedBackground());
		table.put("PasswordField.margin", textInsets);
		table.put("PasswordField.focusInputMap", passwordInputMap);

		table.put("PopupMenu.border", popupMenuBorder);
		table.put("PopupMenuSeparator.foreground", theme.getControlShadow());
        table.put("PopupMenuSeparator.background", theme.getMenuBackground());

		table.put("ProgressBar.background", new ColorUIResource(Color.WHITE));
		table.put("ProgressBar.foreground", theme.getProgressForeground());
		table.put("ProgressBar.border", roundBorder);
		table.put("ProgressBar.font", theme.getStdBoldFont());

		table.put("RadioButton.font", theme.getStdFont());
		table.put("RadioButton.unselectedEnabledIcon", LookAndFeel.makeIcon(getClass(), "icons/radio0.gif"));
		table.put("RadioButton.selectedEnabledIcon", LookAndFeel.makeIcon(getClass(), "icons/radio1.gif"));
		table.put("RadioButton.unselectedDisabledIcon", LookAndFeel.makeIcon(getClass(), "icons/radio0_disabled.gif"));
		table.put("RadioButton.selectedDisabledIcon", LookAndFeel.makeIcon(getClass(), "icons/radio1_disabled.gif"));
	    table.put("RadioButton.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
              "SPACE", "pressed",
              "released SPACE", "released"}));

		table.put("RadioButtonMenuItem.opaque", Boolean.FALSE);
	    table.put("RadioButtonMenuItem.background", theme.getMenuBackground());
		table.put("RadioButtonMenuItem.selectionBackground", theme.getMenuItemSelectedBackground());
		table.put("RadioButtonMenuItem.border", menuItemBorder);
		table.put("RadioButtonMenuItem.borderPainted", Boolean.FALSE);
		table.put("RadioButtonMenuItem.checkIcon", LipstikIconFactory.radioButtonMenuIcon);
		table.put("RadioButtonMenuItem.font", theme.getStdFont());
		table.put("RadioButtonMenuItem.margin", menuItemMargin);

		//    table.put("RootPane.frameBorder", internalFrameBorderResizable);
		//    table.put("RootPane.plainDialogBorder", internalFrameBorderResizable);
		//    table.put("RootPane.frameBorder", internalFrameBorderResizable);

		table.put("ScrollBar.width", new Integer(15));

		table.put("ScrollPane.viewportBorder", null);
		table.put("ScrollPane.border", scrollPaneBorder);
		
		table.put("Separator.foreground", theme.getControlShadow());
		table.put("Separator.background", theme.getControlHighlight());

	    table.put("Slider.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
		       "RIGHT", "positiveUnitIncrement",
		    "KP_RIGHT", "positiveUnitIncrement",
		        "DOWN", "negativeUnitIncrement",
		     "KP_DOWN", "negativeUnitIncrement",
		   "PAGE_DOWN", "negativeBlockIncrement",
	      "ctrl PAGE_DOWN", "negativeBlockIncrement",
		        "LEFT", "negativeUnitIncrement",
		     "KP_LEFT", "negativeUnitIncrement",
		          "UP", "positiveUnitIncrement",
		       "KP_UP", "positiveUnitIncrement",
		     "PAGE_UP", "positiveBlockIncrement",
             "ctrl PAGE_UP", "positiveBlockIncrement",
		        "HOME", "minScroll",
		         "END", "maxScroll"}));

		table.put("Spinner.border", null);
		table.put("Spinner.editorBorder", flatComboBorder);
		table.put("Spinner.background", Color.WHITE);
		table.put("Spinner.foreground", Color.BLACK);
		table.put("Spinner.font", theme.getStdBoldFont());
		table.put("Spinner.arrowButtonBorder", spinButtonBorder);
		table.put("Spinner.editorBorderPainted", Boolean.FALSE);
        table.put("Spinner.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {
			"UP", "increment",
			"KP_UP", "increment",
			"DOWN", "decrement",
			"KP_DOWN", "decrement" }));

		table.put("SplitPane.dividerSize", new Integer(8));
		table.put("SplitPane.oneTouchDividerSize", new Integer(8));
		table.put("SplitPane.border", null);
		table.put("SplitPane.highlight", theme.getControlHighlight());

		table.put("TableHeader.cellBorder", tableHeaderCellBorder);
		table.put("TableHeader.font", theme.getStdBoldFont());

		table.put("TabbedPane.font", theme.getStdBoldFont());
		table.put("TabbedPane.unselectedBackground", theme.getControl());
        table.put("TabbedPane.tabAreaInsets", tabbedPaneTabAreaInsets);
        table.put("TabbedPane.tabInsets", tabbedPaneTabInsets);
        table.put("TabbedPane.contentBorderInsets",tabbedPaneContentInsets);

        table.put("Table.cellBorder", tableHeaderCellBorder);
		table.put("Table.font", theme.getStdFont());
		table.put("Table.scrollPaneBorder", scrollPaneBorder);
		table.put("Table.gridColor", theme.getControlHighlight());
		table.put("Table.selectionBackground", theme.getMenuItemSelectedBackground());
		table.put("Table.focusCellHighlightBorder", focusBorder);
	    table.put("Table.ancestorInputMap", new UIDefaults.LazyInputMap(new Object[] {
			"ctrl C", "copy",
			"ctrl V", "paste",
			"ctrl X", "cut",
			"COPY", "copy",
			"PASTE", "paste",
			"CUT", "cut",
			"RIGHT", "selectNextColumn",
			"KP_RIGHT", "selectNextColumn",
			"shift RIGHT", "selectNextColumnExtendSelection",
			"shift KP_RIGHT", "selectNextColumnExtendSelection",
			"ctrl shift RIGHT", "selectNextColumnExtendSelection",
			"ctrl shift KP_RIGHT", "selectNextColumnExtendSelection",
			"ctrl RIGHT", "selectNextColumnChangeLead",
			"ctrl KP_RIGHT", "selectNextColumnChangeLead",
			"LEFT", "selectPreviousColumn",
			"KP_LEFT", "selectPreviousColumn",
			"shift LEFT", "selectPreviousColumnExtendSelection",
			"shift KP_LEFT", "selectPreviousColumnExtendSelection",
			"ctrl shift LEFT", "selectPreviousColumnExtendSelection",
			"ctrl shift KP_LEFT", "selectPreviousColumnExtendSelection",
			"ctrl LEFT", "selectPreviousColumnChangeLead",
			"ctrl KP_LEFT", "selectPreviousColumnChangeLead",
			"DOWN", "selectNextRow",
			"KP_DOWN", "selectNextRow",
			"shift DOWN", "selectNextRowExtendSelection",
			"shift KP_DOWN", "selectNextRowExtendSelection",
			"ctrl shift DOWN", "selectNextRowExtendSelection",
			"ctrl shift KP_DOWN", "selectNextRowExtendSelection",
			"ctrl DOWN", "selectNextRowChangeLead",
			"ctrl KP_DOWN", "selectNextRowChangeLead",
			"UP", "selectPreviousRow",
			"KP_UP", "selectPreviousRow",
			"shift UP", "selectPreviousRowExtendSelection",
			"shift KP_UP", "selectPreviousRowExtendSelection",
			"ctrl shift UP", "selectPreviousRowExtendSelection",
			"ctrl shift KP_UP", "selectPreviousRowExtendSelection",
			"ctrl UP", "selectPreviousRowChangeLead",
			"ctrl KP_UP", "selectPreviousRowChangeLead",
			"HOME", "selectFirstColumn",
			"shift HOME", "selectFirstColumnExtendSelection",
			"ctrl shift HOME", "selectFirstRowExtendSelection",
			"ctrl HOME", "selectFirstRow",
			"END", "selectLastColumn",
			"shift END", "selectLastColumnExtendSelection",
			"ctrl shift END", "selectLastRowExtendSelection",
			"ctrl END", "selectLastRow",
			"PAGE_UP", "scrollUpChangeSelection",
			"shift PAGE_UP", "scrollUpExtendSelection",
			"ctrl shift PAGE_UP", "scrollLeftExtendSelection",
			"ctrl PAGE_UP", "scrollLeftChangeSelection",
			"PAGE_DOWN", "scrollDownChangeSelection",
			"shift PAGE_DOWN", "scrollDownExtendSelection",
			"ctrl shift PAGE_DOWN", "scrollRightExtendSelection",
			"ctrl PAGE_DOWN", "scrollRightChangeSelection",
			"TAB", "selectNextColumnCell",
			"shift TAB", "selectPreviousColumnCell",
			"ENTER", "selectNextRowCell",
			"shift ENTER", "selectPreviousRowCell",
			"ctrl A", "selectAll",
			"ctrl SLASH", "selectAll",
			"ctrl BACK_SLASH", "clearSelection",
			"ESCAPE", "cancel",
			"F2", "startEditing",
			"SPACE", "addToSelection",
			"ctrl SPACE", "toggleAndAnchor",
			"shift SPACE", "extendTo",
			"ctrl shift SPACE", "moveSelectionTo" }));

		table.put("TextArea.font", theme.getStdFont());
		table.put("TextArea.margin", textInsets);
		table.put("TextArea.focusInputMap", multilineInputMap);

		table.put("TextField.border", flatRoundBorder);
		table.put("TextField.font", theme.getStdFont());
		table.put("TextField.margin", textInsets);
		table.put("TextField.focusInputMap", fieldInputMap);
		table.put("TextPane.focusInputMap", multilineInputMap);

		table.put("TitledBorder.font", theme.getStdBoldFont());
		table.put("TitledBorder.border", roundBorder);

		table.put("ToolBar.background", theme.getControl());
		table.put("ToolBar.border", toolBarBorder);
		table.put("ToolBar.rolloverBorder", toolBarButtonBorder);

		table.put("ToolTip.border", tooltipBorder);
		table.put("ToolTip.background", theme.getTooltipBackground());
		table.put("ToolTip.font", theme.getStdFont());

		table.put("ToggleButton.border", pushButtonBorder);
		table.put("ToggleButton.background", theme.getControl());
	    table.put("ToggleButton.focusInputMap",new UIDefaults.LazyInputMap(new Object[] {
	           "SPACE", "pressed",
               "released SPACE", "released"}));

		table.put("Tree.expandedIcon", LipstikIconFactory.expandedTreeIcon);
		table.put("Tree.collapsedIcon", LipstikIconFactory.collapsedTreeIcon);
		table.put("Tree.font", theme.getStdFont());
		table.put("Tree.hash", theme.getControl());
		table.put("Tree.leafIcon", LookAndFeel.makeIcon(getClass(),	"icons/leaf.gif"));
	    table.put("Tree.leftChildIndent", new Integer(7));
	    table.put("Tree.rightChildIndent", new Integer(10));
		table.put("Tree.closedIcon", LookAndFeel.makeIcon(getClass(), "icons/folder_closed.gif"));
		table.put("Tree.openIcon", LookAndFeel.makeIcon(getClass(),	"icons/folder_opened.gif"));
		table.put("Tree.expandedIcon", LookAndFeel.makeIcon(getClass(),	"icons/expanded.gif"));
		table.put("Tree.collapsedIcon", LookAndFeel.makeIcon(getClass(), "icons/collapsed.gif"));
		table.put("Tree.selectionBorderColor", theme.getControlDarkShadow());
		table.put("Tree.selectionBackground", theme.getMenuItemSelectedBackground());
	    table.put("Tree.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
			"ADD", "expand",
			"SUBTRACT", "collapse",
			"ctrl C", "copy",
			"ctrl V", "paste",
			"ctrl X", "cut",
			"COPY", "copy",
			"PASTE", "paste",
			"CUT", "cut",
			"UP", "selectPrevious",
			"KP_UP", "selectPrevious",
			"shift UP", "selectPreviousExtendSelection",
			"shift KP_UP", "selectPreviousExtendSelection",
			"ctrl shift UP", "selectPreviousExtendSelection",
			"ctrl shift KP_UP", "selectPreviousExtendSelection",
			"ctrl UP", "selectPreviousChangeLead",
			"ctrl KP_UP", "selectPreviousChangeLead",
			"DOWN", "selectNext",
			"KP_DOWN", "selectNext",
			"shift DOWN", "selectNextExtendSelection",
			"shift KP_DOWN", "selectNextExtendSelection",
			"ctrl shift DOWN", "selectNextExtendSelection",
			"ctrl shift KP_DOWN", "selectNextExtendSelection",
			"ctrl DOWN", "selectNextChangeLead",
			"ctrl KP_DOWN", "selectNextChangeLead",
			"RIGHT", "selectChild",
			"KP_RIGHT", "selectChild",
			"LEFT", "selectParent",
			"KP_LEFT", "selectParent",
			"PAGE_UP", "scrollUpChangeSelection",
			"shift PAGE_UP", "scrollUpExtendSelection",
			"ctrl shift PAGE_UP", "scrollUpExtendSelection",
			"ctrl PAGE_UP", "scrollUpChangeLead",
			"PAGE_DOWN", "scrollDownChangeSelection",
			"shift PAGE_DOWN", "scrollDownExtendSelection",
			"ctrl shift PAGE_DOWN", "scrollDownExtendSelection",
			"ctrl PAGE_DOWN", "scrollDownChangeLead",
			"HOME", "selectFirst",
			"shift HOME", "selectFirstExtendSelection",
			"ctrl shift HOME", "selectFirstExtendSelection",
			"ctrl HOME", "selectFirstChangeLead",
			"END", "selectLast",
			"shift END", "selectLastExtendSelection",
			"ctrl shift END", "selectLastExtendSelection",
			"ctrl END", "selectLastChangeLead",
			"F2", "startEditing",
			"ctrl A", "selectAll",
			"ctrl SLASH", "selectAll",
			"ctrl BACK_SLASH", "clearSelection",
			"ctrl LEFT", "scrollLeft",
			"ctrl KP_LEFT", "scrollLeft",
			"ctrl RIGHT", "scrollRight",
			"ctrl KP_RIGHT", "scrollRight",
			"SPACE", "addToSelection",
			"ctrl SPACE", "toggleAndAnchor",
			"shift SPACE", "extendTo",
			"ctrl shift SPACE", "moveSelectionTo" }));
	}

	protected static void installDefaultThemes()
	{
		installedThemes = new ArrayList();
		for (int i = themeNames.length - 1; i >= 0; i--)
			installTheme(createTheme(themeNames[i]));
	}

	public static List getInstalledThemes()
	{
		if (null == installedThemes)
			installDefaultThemes();

		return installedThemes;
	}

	public static void installTheme(LipstikColorTheme theme)
	{
		if (null == installedThemes)
			installDefaultThemes();
		installedThemes.add(theme);
	}

	protected static LipstikColorTheme createTheme(String themeName)
	{
		String className = "com.lipstikLF.theme."+themeName;
		try
		{
			Class cl = Class.forName(className);
			return (LipstikColorTheme) (cl.newInstance());
		} catch (ClassNotFoundException e)
		{
			// Ignore the exception here and log below.
		} catch (IllegalAccessException e)
		{
			// Ignore the exception here and log below.
		} catch (InstantiationException e)
		{
			// Ignore the exception here and log below.
		}
		return null;
	}

	private static String getSystemProperty(String key)
	{
		try
		{
			return System.getProperty(key);
		}
		catch (SecurityException e)
		{
			System.out.println("Can't read the System property " + key + ".");
			return null;
		}
	}

}