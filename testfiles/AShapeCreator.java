package org.columba.calendar.ui.calendar;

import com.miginfocom.ashape.AShapeUtil;
import com.miginfocom.ashape.PolygonShape;
import com.miginfocom.ashape.SizeRangeVisibility;
import com.miginfocom.ashape.animation.OverrideAnimator;
import com.miginfocom.ashape.animation.TimeLine;
import com.miginfocom.ashape.animation.animations.ColorAnimation;
import com.miginfocom.ashape.interaction.AnimatorCommand;
import com.miginfocom.ashape.interaction.MouseKeyInteractor;
import com.miginfocom.ashape.interaction.TimerInteractor;
import com.miginfocom.ashape.layout.CutEdgeAShapeLayout;
import com.miginfocom.ashape.layout.RowAShapeLayout;
import com.miginfocom.ashape.shapes.*;
import com.miginfocom.calendar.datearea.DefaultDateArea;
import com.miginfocom.util.FirstOrLastComparator;
import com.miginfocom.util.PropertyKey;
import com.miginfocom.util.gfx.GfxUtil;
import com.miginfocom.util.gfx.RoundRectangle;
import com.miginfocom.util.gfx.ShapeGradientPaint;
import com.miginfocom.util.gfx.SliceSpec;
import com.miginfocom.util.gfx.geometry.AbsRect;
import com.miginfocom.util.gfx.geometry.AtPoint;
import com.miginfocom.util.gfx.geometry.PlaceRect;
import com.miginfocom.util.gfx.geometry.filters.OperFilter;
import com.miginfocom.util.gfx.geometry.links.AShapeLinkNumber;
import com.miginfocom.util.gfx.geometry.links.PolyLinkNumber;
import com.miginfocom.util.gfx.geometry.numbers.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Class that has some creator methods for AShapes.
 * @author unknown
 * 
 */
public class AShapeCreator {
    /*
	// ****************************************
	// * Static example shape creation
	// ****************************************

	/**
	 * createClipTestShape method
	 * Creates the default shape.
	 * @return root
	 */
	public static RootAShape createClipTestShape() {

		String text = "The lazy dog jumed over the aircraft carrier. asdfasdfasd fa sdfasdf asd fas dfa sdf";

		RowAShapeLayout vRowLayout = new RowAShapeLayout(
				SwingConstants.VERTICAL, null);

		ContainerAShape vRowContainer = new ContainerAShape("vBox",
				AbsRect.FILL, vRowLayout);

		ContainerAShape textContainer = new ContainerAShape("cs", AbsRect.FILL);

		TextAShape textShape = new TextAShape("t1", text, AbsRect.FILL,
				TextAShape.TYPE_WRAP_TEXT, new Font("tahoma", Font.PLAIN, 11),
				Color.GRAY, new AtFraction(0.5f), new AtFraction(0.5f),
				GfxUtil.AA_HINT_OFF);

		textShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		textContainer.addSubShape(textShape);

		vRowContainer.addSubShape(textContainer);

		RootAShape root = new RootAShape(vRowContainer);

		AShapeUtil.addEnterExitOverride(textShape, textShape, AShape.A_PAINT,
				Color.WHITE, true, true);

		return root;
	}

	/**
	 * createLightShape method
	 * @return root
	 */
	public static RootAShape createLightShape() {

		Color bgPaint = new Color(255, 200, 200);

		// Color bgPaint = new Color(255, 150, 150);

		// Color titleBgPaint = null;//new Color(200, 50, 50, 255);

		// Color selectedTitlePaint = new Color(200, 100, 100);

		// Color bulletPaint = new Color(100, 100, 100);

		Color outlinePaint = new Color(128, 0, 0);

		Color moOutlinePaint = new Color(0, 0, 0);

		Color textPaint = new Color(50, 50, 50);

		// Color textPaint = new Color(0, 0, 0, 50);

		Font titleFont = new Font("SansSerif", Font.BOLD, 11);

		RootAShape root = new RootAShape();

		FillAShape bgAShape = new FillAShape("bg", new Rectangle(0, 0, 1, 1),
				AbsRect.FILL_INSIDE, bgPaint, GfxUtil.AA_HINT_OFF);

		PlaceRect contentAbsRect = new AbsRect(new AtStart(12), new AtStart(0),
				new AtEnd(-1), new AtEnd(-1));

		ContainerAShape content = new ContainerAShape("dock", contentAbsRect,
				new CutEdgeAShapeLayout());

		PlaceRect titleTextAbsRect = new AbsRect(new AtStart(0),
				new AtStart(0), new AtEnd(0), new AtStart(12), null, null, null);

		TextAShape timeTitleText = new TextAShape("titleText", "",
				titleTextAbsRect, TextAShape.TYPE_SINGE_LINE, titleFont,
				textPaint, new AtStart(0), new AtStart(-3), GfxUtil.AA_HINT_OFF);

		DrawAShape outlineAShape = new DrawAShape("outline", new Rectangle(0,
				0, 1, 1), AbsRect.FILL, outlinePaint, new BasicStroke(1f),
				GfxUtil.AA_HINT_OFF);

		outlineAShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		outlineAShape.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR));

		content.addSubShape(timeTitleText);

		bgAShape.addSubShape(content);

		root.addSubShape(bgAShape);

		root.addSubShape(outlineAShape);

		AShapeUtil.setResizeBoxes(root, SwingConstants.HORIZONTAL, 4);

		AShapeUtil.addEnterExitOverride(outlineAShape, outlineAShape,
				AShape.A_PAINT, moOutlinePaint, false, true);

		// Drag, resize interactions

		PropertyKey trigger = MouseKeyInteractor.MOUSE_PRESS;

		Integer button = new Integer(MouseEvent.BUTTON1);

		AShapeUtil.addMouseFireEvent(outlineAShape, trigger,
				DefaultDateArea.AE_SELECTED_PRESSED, false, false, button);

		// AShapeUtil.addMouseFireEvent(outlineAShape, trigger,
		// DefaultDateArea.AE_CATEGORY_DRAG_PRESSED, false, false, button);

		AShapeUtil.addMouseFireEvent(outlineAShape, trigger,
				DefaultDateArea.AE_DRAG_PRESSED, false, true, button);

		return root;
	}

	/**
	 * createRelValueShape method
	 * @return root
	 */
	public static RootAShape createRelValueShape() {

		RootAShape root = new RootAShape();

		PlaceRect pr = new AbsRect(new AtStart(0), new AtStart(0),
				new AtEnd(0), new AtEnd(0));

		AShape fillShape = new FillAShape("fill", new Rectangle(0, 0, 1, 1),
				pr, Color.BLACK, GfxUtil.AA_HINT_OFF);

		root.addSubShape(fillShape);

		return root;
	}

	/**
	 * createTabbedShape method
	 * Creates a tabbed shape
	 * 
	 * @return root
	 */
    /*	public static RootAShape createTabbedShape() {

		RootAShape root = new RootAShape();

		root.setRepaintPadding(new Insets(2, 2, 2, 2));

		root.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		root.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR));

		int tabHeight = 15;

		int tabSlope = 15;

		int tabGapWidth = 30; // even

		// Edges of the poly shape

		AtNumber leftEdgeX = new AShapeLinkNumber(root, AtStart.START0,
				PolyLinkNumber.X, true);

		AtNumber rightEdgeX = new AShapeLinkNumber(root, AtEnd.END1,
				PolyLinkNumber.X, true); // END1 because the draw should be
											// correct

		AtNumber topEdgeY = new AShapeLinkNumber(root, AtStart.START0,
				PolyLinkNumber.Y, true);

		AtNumber bottomEdgeY = new AShapeLinkNumber(root, AtEnd.END1,
				PolyLinkNumber.Y, true); // END1 because the draw should be
											// correct

		AtNumber topMiddletX = new AShapeLinkNumber(root, new AtFraction(0.5f),
				PolyLinkNumber.X, true);

		AtNumber tabDiveStartX = new OperFilter(topMiddletX, OperFilter.SUB,
				new AtFixed(tabGapWidth >> 1));

		AtNumber tabDiveEndX = new OperFilter(topMiddletX, OperFilter.ADD,
				new AtFixed(tabGapWidth >> 1));

		AtNumber tabLowerY = new OperFilter(topEdgeY, OperFilter.ADD,
				new AtFixed(tabHeight));

		// First (left) tab

		PolygonShape leftTabShape = new PolygonShape();

		leftTabShape.addPoint(new AtPoint(leftEdgeX, topEdgeY));

		leftTabShape.addPoint(new AtPoint(tabDiveStartX, topEdgeY, 0, 0,
				tabSlope, 0));

		leftTabShape.addPoint(new AtPoint(tabDiveEndX, tabLowerY, -0, 0, 0, 0));

		leftTabShape.addPoint(new AtPoint(rightEdgeX, tabLowerY));

		leftTabShape.addPoint(new AtPoint(rightEdgeX, bottomEdgeY));

		leftTabShape.addPoint(new AtPoint(leftEdgeX, bottomEdgeY));

		Color outlineColor = new Color(100, 100, 100);

		Color tabBg = new Color(245, 245, 245);

		Font tabTextFont = new Font("SansSerif", Font.BOLD, 12);

		Font textFont = new Font("SansSerif", Font.PLAIN, 11);

		AbsRect leftTabTextRect = new AbsRect(new AtStart(4), AtStart.START0,
				new AtFraction(.4f), new AtStart(15));

		AbsRect textRect = new AbsRect(new AtStart(4), new AtStart(20),
				new AtEnd(-5), AtEnd.END1);

		SizeRangeVisibility hideIfSmallVis = new SizeRangeVisibility(true);

		hideIfSmallVis.addHorizontalVisibilityRange(-1000, 35);

		hideIfSmallVis.addVerticalVisibilityRange(-1000, 35);

		AbsRect rightTabTextRect = new AbsRect(new AtFraction(.6f),
				AtStart.START0, AtEnd.END1, new AtStart(15));

		AShape leftTab = new FillAShape("leftTab", leftTabShape, AbsRect.FILL,
				tabBg, GfxUtil.AA_HINT_ON);

		leftTab.setAttribute(AShape.A_VISIBILITY, hideIfSmallVis);

		leftTab.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		AShape leftTabOutline = new DrawAShape("leftTabOutline", leftTabShape,
				AbsRect.FILL, outlineColor, new BasicStroke(1f),
				GfxUtil.AA_HINT_ON);

		AShape leftTabText = new TextAShape("leftTabText", "Info",
				leftTabTextRect, TextAShape.TYPE_SINGE_LINE, tabTextFont,
				Color.DARK_GRAY, AtStart.START1, AtStart.START0,
				GfxUtil.AA_HINT_OFF);

		AShape leftText = new TextAShape("leftText", "$summary$", textRect,
				TextAShape.TYPE_WRAP_TEXT, textFont, Color.DARK_GRAY,
				AtStart.START0, AtStart.START0, GfxUtil.AA_HINT_OFF);

		leftTabText.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		leftTabText.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Second (right) tab

		PolygonShape rightTabShape = new PolygonShape();

		rightTabShape.addPoint(new AtPoint(leftEdgeX, tabLowerY));

		rightTabShape
				.addPoint(new AtPoint(tabDiveStartX, tabLowerY, 0, 0, 0, 0));

		rightTabShape.addPoint(new AtPoint(tabDiveEndX, topEdgeY, -tabSlope, 0,
				0, 0));

		rightTabShape.addPoint(new AtPoint(rightEdgeX, topEdgeY));

		rightTabShape.addPoint(new AtPoint(rightEdgeX, bottomEdgeY));

		rightTabShape.addPoint(new AtPoint(leftEdgeX, bottomEdgeY));

		AShape rightTab = new FillAShape("rightTab", rightTabShape,
				AbsRect.FILL, tabBg, GfxUtil.AA_HINT_ON);

		rightTab.setAttribute(AShape.A_VISIBILITY, hideIfSmallVis);

		rightTab.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		AShape rightTabOutline = new DrawAShape("rightTabOutline",
				rightTabShape, AbsRect.FILL, outlineColor, new BasicStroke(1f),
				GfxUtil.AA_HINT_ON);

		AShape rightTabText = new TextAShape("rightTabText", "Times",
				rightTabTextRect, TextAShape.TYPE_SINGE_LINE, tabTextFont,
				Color.DARK_GRAY, AtEnd.END1, AtStart.START0,
				GfxUtil.AA_HINT_OFF);

		AShape rightText = new TextAShape("rightText",
				"Start:\n  $startTime$\nEnd:\n  $endTimeExcl$", textRect,
				TextAShape.TYPE_WRAP_TEXT, textFont, Color.DARK_GRAY,
				AtStart.START0, AtStart.START0, GfxUtil.AA_HINT_OFF);

		rightTabText.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		rightTabText.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Fallback shape for small sizes

		AShape smallFill = new FillAShape("smallFill",
				new Rectangle(0, 0, 1, 1), AbsRect.FILL, tabBg,
				GfxUtil.AA_HINT_OFF);

		smallFill.setAttribute(AShape.A_VISIBILITY, hideIfSmallVis
				.getInverted());

		smallFill.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		AShape smallOutline = new DrawAShape("smallOutline", new Rectangle(0,
				0, 1, 1), AbsRect.FILL, outlineColor, new BasicStroke(1f),
				GfxUtil.AA_HINT_ON);

		AShape smallText = new TextAShape("smallText", "$startTime$ $summary$",
				AbsRect.FILL_INSIDE, TextAShape.TYPE_SINGE_LINE, textFont,
				Color.DARK_GRAY, AtStart.START1, AtStart.START0,
				GfxUtil.AA_HINT_OFF);

		ContainerAShape tabsContainer = new ContainerAShape("tabsContainer",
				AbsRect.FILL);

		rightTab.addSubShape(rightText);

		rightTab.addSubShape(rightTabText);

		rightTab.addSubShape(rightTabOutline);

		leftTab.addSubShape(leftText);

		leftTab.addSubShape(leftTabText);

		leftTab.addSubShape(leftTabOutline);

		tabsContainer.addSubShape(rightTab);

		tabsContainer.addSubShape(leftTab);

		smallFill.addSubShape(smallOutline);

		smallFill.addSubShape(smallText);

		root.addSubShape(tabsContainer);

		root.addSubShape(smallFill);

		AShapeUtil.setResizeBoxes(root, SwingConstants.VERTICAL, 4);

		AShapeUtil.enableMouseOverCursor(root);

		AShapeUtil.enableMouseOverState(root);

		PropertyKey mpTrigger = MouseKeyInteractor.MOUSE_PRESS;

		AShapeUtil.addEnterExitOverride(leftTab, leftTabOutline,
				AShape.A_PAINT, Color.BLACK, true, true);

		AShapeUtil.addEnterExitOverride(rightTab, rightTabOutline,
				AShape.A_PAINT, Color.BLACK, true, true);

		AShapeUtil.addEnterExitOverride(smallFill, smallOutline,
				AShape.A_PAINT, Color.BLACK, true, true);

		AShapeUtil.addEnterExitOverride(leftTabText, leftTabText,
				AShape.A_UNDERLINE_HEIGHT, new Integer(1), true, true);

		AShapeUtil.addEnterExitOverride(rightTabText, rightTabText,
				AShape.A_UNDERLINE_HEIGHT, new Integer(1), true, true);

		// Resize events

		Integer button = new Integer(MouseEvent.BUTTON1);

		AShapeUtil.addMouseFireEvent(root, mpTrigger,
				DefaultDateArea.AE_SELECTED_PRESSED, false, false, button);

		AShapeUtil.addMouseRemoveOverride(leftTabText, tabsContainer,
				mpTrigger, true, AShape.A_SUB_SHAPE_SORTER, null, true, false);

		AShapeUtil.addMouseOverride(rightTabText, tabsContainer, mpTrigger,
				AShape.A_SUB_SHAPE_SORTER, new FirstOrLastComparator(rightTab,
						false), true, false, false);

		AShapeUtil.addMouseFireEvent(root, mpTrigger,
				DefaultDateArea.AE_DRAG_PRESSED, false, true, button);

		AShapeUtil.addMouseEventBlock(root, false, new Integer(
				MouseEvent.MOUSE_MOVED));

		// States

		return root;
	}

	/**
	 * createGanttFolderShape method
	 * @param bgPaint
	 * @param font
	 * @param fgColor
	 * @param shadowInsets
	 * @return root
	 */
	public static RootAShape createGanttFolderShape(Paint bgPaint, Font font,
			Color fgColor, Insets shadowInsets) {

		RootAShape root = new RootAShape();

		root.setRepaintPadding(new Insets(2, 2, 4, 4));

		GeneralPath startPoly = new GeneralPath();

		startPoly.moveTo(0, 0);

		startPoly.lineTo(10, 0);

		startPoly.lineTo(0, 10);

		startPoly.closePath();

		AbsRect startRect = new AbsRect(AtStart.START0, AtStart.START0,
				new AtStart(10), new AtStart(10));

		FillAShape startShape = new FillAShape("start", startPoly, startRect,
				Color.BLUE, GfxUtil.AA_HINT_ON);

		// Shadow

		// PlaceRect bgAbsRect = new AbsRect(new AtStart(0), new AtStart(0), new
		// AtEnd(0), new AtEnd(0), null, null, shadowInsets);

		// FillAShape filledShadow = new FillAShape("shadowShape", new
		// RoundRectangle(0, 0, 1, 1, 5, 5), bgAbsRect, new Color(0, 0, 0, 100),
		// GfxUtil.AA_HINT_ON);

		// SliceSpec shwSI = new SliceSpec(new Insets(10, 10, 10, 10),
		// SliceSpec.TYPE_TILE_CUT, SliceSpec.OPT_BORDER);

		// FeatherAShape shadow = new FeatherAShape("shadow", filledShadow, new
		// Color(255, 255, 255, 0), 5, shwSI);

		FillAShape bgShape = new FillAShape("bgFill",
				new Rectangle(0, 0, 1, 1), AbsRect.FILL, bgPaint,
				GfxUtil.AA_HINT_OFF);

		// AbsRect textRect = new AbsRect(new AtStart(4), AtStart.START0, new
		// AtEnd(-4), AtEnd.END0);

		// AShape textShape = new TextAShape("text", "$" + GridRow.NAME + "$",
		// textRect, TextAShape.TYPE_SINGE_LINE, font, fgColor,
		// AtFraction.CENTER, AtStart.START0, Boolean.FALSE);

		bgShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		bgShape.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR));

		// root.addSubShape(shadow);

		root.addSubShape(bgShape);

		// root.addSubShape(textShape);

		root.addSubShape(startShape);

		// root.addSubShape(textShape);

		return root;
	}

	/**
	 * createGanttRect method
	 * @param bgPaint
	 * @param font
	 * @param fgColor
	 * @param shadowInsets
	 * @return root
	 */
	public static RootAShape createGanttRect(Paint bgPaint, Font font,
			Color fgColor, Insets shadowInsets)	{

		RootAShape root = new RootAShape();

		root.setRepaintPadding(new Insets(2, 2, 4, 4));

		// Shadow

		// PlaceRect bgAbsRect = new AbsRect(new AtStart(0), new AtStart(0), new
		// AtEnd(0), new AtEnd(0), null, null, shadowInsets);

		// FillAShape filledShadow = new FillAShape("shadowShape", new
		// RoundRectangle(0, 0, 1, 1, 5, 5), bgAbsRect, new Color(0, 0, 0, 100),
		// Boolean.TRUE);

		// SliceSpec shwSI = new SliceSpec(new Insets(10, 10, 10, 10),
		// SliceSpec.TYPE_TILE_CUT, SliceSpec.OPT_BORDER);

		// FeatherAShape shadow = new FeatherAShape("shadow", filledShadow, new
		// Color(255, 255, 255, 0), 5, shwSI);

		FillAShape bgShape = new FillAShape("bgFill",
				new Rectangle(0, 0, 1, 1), AbsRect.FILL, bgPaint,
				GfxUtil.AA_HINT_OFF);

		// DrawAShape outlineShape = new DrawAShape("bgFill", new Rectangle(0,
		// 0, 1, 1), AbsRect.FILL, outlinePaint, new BasicStroke(1f),
		// Boolean.FALSE);

		AbsRect textRect = new AbsRect(new AtStart(3), AtStart.START0,
				new AtEnd(-3), AtEnd.END0);

		AShape textShape = new TextAShape("text", "$duration$", textRect,
				TextAShape.TYPE_SINGE_LINE, font, fgColor, AtFraction.CENTER,
				AtStart.START0, GfxUtil.AA_HINT_OFF);

		bgShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		bgShape.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR));

		// root.addSubShape(shadow);

		root.addSubShape(bgShape);

		// root.addSubShape(outlineShape);

		root.addSubShape(textShape);

		AShapeUtil.enableMouseOverCursor(root);

		AShapeUtil.setResizeBoxes(bgShape, SwingConstants.HORIZONTAL, 4);

		AShapeUtil.addMouseEventBlock(bgShape, false, new Integer(
				MouseEvent.MOUSE_MOVED));

		return root;
	}

	/**
	 * createDefaultPicShape method
	 * Creates the default shape.
	 * 
	 * @return root
	 */
	public static RootAShape createDefaultPicShape() {
		
		RootAShape root = new RootAShape();

		BufferedImage image = GfxUtil.getImageFromString("C:/testShape.png",
				null, null);

		SliceSpec sliceSpec = new SliceSpec(new Insets(15, 15, 15, 15),
				SliceSpec.TYPE_TILE_CUT, SliceSpec.OPT_BORDER);

		SlicedImageAShape slicedImageShape = new SlicedImageAShape("slice",
				image, sliceSpec, AbsRect.FILL);

		PlaceRect middleAbsRect = new AbsRect(new AtStart(5), new AtStart(5),
				new AtEnd(-5), new AtEnd(-5));

		ShapeGradientPaint middBG = new ShapeGradientPaint(new Color(250, 250,
				250), new Color(230, 230, 230), 270, 1f, 0.5f, false);

		FillAShape middleBG = new FillAShape("middleBG", new Rectangle(0, 0, 1,
				1), middleAbsRect, middBG, GfxUtil.AA_HINT_OFF);

		slicedImageShape.addSubShape(middleBG);

		// root.setAttribute(AShape.A_REPORT_HIT_SHAPE, Boolean.TRUE);

		AShapeUtil.setResizeBoxes(root, SwingConstants.VERTICAL, 4);

		root.addSubShape(slicedImageShape);

		return root;
	}

	/**
	 * createDockLayoutShape method
	 * @return root
	 */
	public static RootAShape createDockLayoutShape() {

		RootAShape root = new RootAShape();

		ContainerAShape dockAShape = new ContainerAShape("dock", AbsRect.FILL,
				new CutEdgeAShapeLayout());

		// SizeConstraint sConstr1 = new SizeConstraint(new AtFixed(10), new
		// AtFraction(1), 1.0f);

		// SizeConstraint sConstr2 = new SizeConstraint(new AtFixed(0), new
		// AtFraction(0.5f), 1.0f);

		// SizeConstraint sConstr3 = new SizeConstraint(new AtFixed(0), new
		// AtFraction(1), 0.0f);

		// SizeConstraint sConstr4 = new SizeConstraint(new AtFixed(0), new
		// AtFraction(1), 0.0f);

		FillAShape fillShape1A = new FillAShape("dockFill1", new Rectangle(0,
				0, 1, 1), new AbsRect(SwingConstants.BOTTOM, new Integer(20),
				null), Color.BLUE, GfxUtil.AA_HINT_OFF);

		FillAShape fillShape2A = new FillAShape("dockFill2", new Rectangle(0,
				0, 1, 1), new AbsRect(SwingConstants.RIGHT, new Integer(20),
				null), Color.GREEN, GfxUtil.AA_HINT_OFF);

		FillAShape fillShape3A = new FillAShape("dockFill3", new Rectangle(0,
				0, 1, 1),
				new AbsRect(SwingConstants.TOP, new Integer(20), null),
				Color.YELLOW, GfxUtil.AA_HINT_OFF);

		FillAShape fillShape4A = new FillAShape("dockFill4", new Rectangle(0,
				0, 1, 1),
				new AbsRect(SwingConstants.LEFT, new Float(1f), null),
				Color.GRAY, GfxUtil.AA_HINT_OFF);

		dockAShape.addSubShape(fillShape1A);

		dockAShape.addSubShape(new DividerAShape(new AbsRect(
				SwingConstants.BOTTOM, new Integer(1)), Color.RED));

		dockAShape.addSubShape(fillShape2A);

		dockAShape.addSubShape(new DividerAShape(new AbsRect(
				SwingConstants.RIGHT, new Integer(1)), Color.RED));

		dockAShape.addSubShape(fillShape3A);

		dockAShape.addSubShape(new DividerAShape(new AbsRect(
				SwingConstants.TOP, new Integer(1)), Color.RED));

		dockAShape.addSubShape(fillShape4A);

		root.addSubShape(dockAShape);

		root.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		root.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR));

		AShapeUtil.setResizeBoxes(root, SwingConstants.VERTICAL, 4);

		return root;
	}

	/**
	 * createBoxLayoutShape method
	 * Creates the default shape.
	 * 
	 * @param parent
	 * @return root
	 */
	public static RootAShape createBoxLayoutShape(Container parent) {

		DrawAShape borderAShape = new DrawAShape("border", new Rectangle(0, 0,
				1, 1), AbsRect.FILL, new Color(150, 150, 225), new BasicStroke(
				1f), GfxUtil.AA_HINT_OFF);

		AtRefNumber[] hSizes = new AtRefNumber[] { new AtFixed(12),
				new AtFraction(1), new AtFixed(12) };

		RowAShapeLayout hRowLayout = new RowAShapeLayout(
				SwingConstants.HORIZONTAL, hSizes);

		ContainerAShape hBoxAShape = new ContainerAShape("hBox",
				AbsRect.FILL_INSIDE, hRowLayout);

		AtRefNumber[] vSizes = null;

		ShapeGradientPaint vGapPaint = new ShapeGradientPaint(new Color(150,
				150, 225), new Color(200, 200, 225), 0, 1f, 0.5f, false);

		RowAShapeLayout vRowLayout = new RowAShapeLayout(
				SwingConstants.VERTICAL, vSizes);

		ContainerAShape vBoxAShape = new ContainerAShape("vBox", AbsRect.FILL,
				vRowLayout);

		vBoxAShape.addSubShape(createHorTextShape("t1",
				"A demo text that is a little tad bit longer", new AtStart(2),
				new AtStart(1)));

		vBoxAShape.addSubShape(new DividerAShape(SwingConstants.HORIZONTAL,
				new Integer(1), vGapPaint));

		vBoxAShape.addSubShape(createHorTextShape("t2", "Short demo text",
				new AtEnd(0), new AtFraction(0.5f)));

		vBoxAShape.addSubShape(new DividerAShape(SwingConstants.HORIZONTAL,
				new Integer(1), vGapPaint));

		vBoxAShape.addSubShape(createHorTextShape("t3", "Shorter", new AtStart(
				2), new AtFraction(0.5f)));

		vBoxAShape.addSubShape(new DividerAShape(SwingConstants.HORIZONTAL,
				new Integer(1), vGapPaint));

		vBoxAShape
				.addSubShape(createHorTextShape(
						"t4",
						"This text just goes on and on and on.\n\nIt simply doesn't stop",
						new AtEnd(-1), new AtEnd(-1)));

		if (parent != null) {

			vBoxAShape.addSubShape(createJComponentShape("Button 1", parent));

			vBoxAShape.addSubShape(createJComponentShape("Button 2", parent));

			vBoxAShape.addSubShape(createJComponentShape("Button 3", parent));

			vBoxAShape.addSubShape(createJComponentShape("Button 4", parent));

		}

		hBoxAShape.addSubShape(createVerTextShape("v1", "MiG InfoCom AB",
				TextAShape.TYPE_SINGE_LINE_ROT_CCW));

		hBoxAShape.addSubShape(vBoxAShape);

		hBoxAShape.addSubShape(createVerTextShape("v2", "MiG InfoCom AB",
				TextAShape.TYPE_SINGE_LINE_ROT_CW));

		borderAShape.addSubShape(hBoxAShape);

		borderAShape.setAttributeDeep(AShape.A_CLIP_TYPE,
				AShape.CLIP_PARENT_BOUNDS, 8);

		RootAShape root = new RootAShape(borderAShape);

		borderAShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		borderAShape.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR));

		AShapeUtil.enableMouseOverCursor(root);

		AShapeUtil.enableMouseOverState(borderAShape);

		AShapeUtil.setResizeBoxes(root, SwingConstants.VERTICAL, 4);

		PropertyKey trigger = MouseKeyInteractor.MOUSE_PRESS;

		Integer button = new Integer(MouseEvent.BUTTON1);

		AShapeUtil.addMouseFireEvent(borderAShape, trigger,
				DefaultDateArea.AE_DRAG_PRESSED, false, true, button);

		ColorAnimation colAnim = new ColorAnimation(Color.GRAY, Color.BLACK);

		// Animations for text

		for (int i = 1; i < 5; i++) {

			String shapeName = "t" + i;

			OverrideAnimator animator = new OverrideAnimator(colAnim,
					new TimeLine(500, 10), AShape.A_PAINT, shapeName);

			AnimatorCommand forwardCmd = new AnimatorCommand(animator,
					OverrideAnimator.CMD_FORWARD);

			AnimatorCommand backwardCmd = new AnimatorCommand(animator,
					OverrideAnimator.CMD_BACKWARD);

			AShapeUtil.addEnterExitCommands(root.getSubShapeDeep(shapeName),
					forwardCmd, backwardCmd, true);

		}

		AShapeUtil.addMouseEventBlock(borderAShape, false, new Integer(
				MouseEvent.MOUSE_MOVED));

		return root;
	}

	/**
	 * createHorTextShape method
	 * @param name
	 * @param text
	 * @param xAlign
	 * @param yAlign
	 * @return container
	 */
	public static AShape createHorTextShape(String name, String text,
			AtRefRangeNumber xAlign, AtRefRangeNumber yAlign) {

		ContainerAShape container = new ContainerAShape();

		ShapeGradientPaint bgPaint = new ShapeGradientPaint(new Color(200, 200,
				255), new Color(230, 230, 255), 90, 1f, 0.5f, false);

		FillAShape mainAShape = new FillAShape("box" + name, new Rectangle(0,
				0, 1, 1), AbsRect.FILL, bgPaint, GfxUtil.AA_HINT_OFF);

		Font font = new Font("tahoma", Font.PLAIN, 11);

		TextAShape textAShape = new TextAShape(name, text, AbsRect.FILL,
				TextAShape.TYPE_WRAP_TEXT, font, Color.GRAY, xAlign, yAlign,
				GfxUtil.AA_HINT_OFF);

		textAShape.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.HAND_CURSOR));

		mainAShape.addSubShape(textAShape);

		container.addSubShape(mainAShape);

		textAShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		return container;
	}

	/**
	 * createVerTextShgape method
	 * @param name
	 * @param text
	 * @param rot
	 * @return
	 */
	public static AShape createVerTextShape(String name, String text, int rot) {

		ContainerAShape container = new ContainerAShape();

		ShapeGradientPaint bgPaint = new ShapeGradientPaint(new Color(180, 180,
				180), new Color(230, 230, 230), 90, 1f, 0.5f, false);

		FillAShape mainAShape = new FillAShape("box" + name, new Rectangle(0,
				0, 1, 1), AbsRect.FILL, bgPaint, GfxUtil.AA_HINT_OFF);

		Font font = new Font("sansserif", Font.PLAIN, 11);

		TextAShape textAShape = new TextAShape(name, text, AbsRect.FILL, rot,
				font, new Color(255, 255, 255), new AtFraction(0f),
				new AtFraction(0.5f), GfxUtil.AA_HINT_ON);

		mainAShape.addSubShape(textAShape);

		container.addSubShape(mainAShape);

		// textAShape.setAttribute(AShape.A_REPORT_HIT_SHAPE, Boolean.TRUE);

		return container;
	}

	/**
	 * createJComponentShape method
	 * @param name
	 * @param parent
	 * @return container
	 */
	public static AShape createJComponentShape(String name, Container parent)

	{

		ContainerAShape container = new ContainerAShape();

		JButton butt = new JButton(name);

		parent.add(butt);

		JComponentAShape componentAShape = new JComponentAShape(name, butt,
				AbsRect.FILL);

		componentAShape.setAttribute(AShape.A_PREFERRED_HEIGHT, new Integer(
				butt.getPreferredSize().height));

		container.addSubShape(componentAShape);

		container.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		return container;
	}

	/**
	 * createDarkGreyShape method
	 * Creates the default shape.
	 * 
	 * @return root
	 */
	public static RootAShape createDarkGrayShape() 
	{
		// ShapeGradientPaint bgPaint = new ShapeGradientPaint(new Color(235,
		// 235, 235), new Color(255, 255, 255), 180, 1f, 0.5f, false);

		Paint bgPaint = new Color(245, 245, 245);

		// ShapeGradientPaint titlePaint = new ShapeGradientPaint(new Color(255,
		// 150, 150), new Color(255, 200, 200), 0, 0.7f, 0.7f, false);

		Paint titlePaint = new Color(120, 120, 120);

		ContainerAShape containerAShape = new ContainerAShape("dcontainer",
				AbsRect.FILL);

		containerAShape.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR));

		containerAShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		ContainerAShape content = new ContainerAShape("ddock", AbsRect.FILL,
				new CutEdgeAShapeLayout());

		// Shadow

		PlaceRect bgAbsRect = new AbsRect(new AtStart(0), new AtStart(0),
				new AtEnd(0), new AtEnd(0), null, null, new Insets(0, 0, 2, 2));

		FillAShape filledShadow = new FillAShape("dshadowShape",
				new RoundRectangle(0, 0, 1, 1, 5, 5), bgAbsRect, new Color(0,
						0, 0, 70), GfxUtil.AA_HINT_ON);

		SliceSpec shwSI = new SliceSpec(new Insets(10, 10, 10, 10),
				SliceSpec.TYPE_TILE_CUT, SliceSpec.OPT_BOTTOM_RIGHT);

		FeatherAShape shadow = new FeatherAShape("dshadow", filledShadow,
				new Color(255, 255, 255, 0), 5, shwSI);

		// Title

		PlaceRect titleDockRect = new AbsRect(SwingConstants.TOP, new Integer(
				11));

		FillAShape northTitle = new FillAShape("dtitle", new Rectangle(0, 0, 1,
				1), titleDockRect, titlePaint, GfxUtil.AA_HINT_OFF);

		PlaceRect nttAbsRect = new AbsRect(new AtStart(3), new AtStart(0),
				new AtEnd(-2), new AtEnd(0), null, null, null);

		TextAShape timeTitleText = new TextAShape("dtitleText",
				"$startTime$ - $endTimeExcl$", nttAbsRect,
				TextAShape.TYPE_SINGE_LINE, new Font("SansSerif", Font.PLAIN,
						11), Color.WHITE, new AtStart(0), new AtStart(-3),
				GfxUtil.AA_HINT_OFF, new Point(1, 1), Color.DARK_GRAY);

		timeTitleText.setAttribute(AShape.A_CLIP_TYPE,
				AShape.CLIP_PARENT_BOUNDS);

		// Content

		FillAShape textBackground = new FillAShape("dbackground",
				new Rectangle(0, 0, 1, 1), new AbsRect(SwingConstants.BOTTOM,
						new Float(1f)), bgPaint, GfxUtil.AA_HINT_OFF);

		textBackground.setAttribute(AShape.A_CROP_TO_VISIBILITY_BOUNDS,
				Boolean.TRUE);

		ContainerAShape textDockContainerA = new ContainerAShape("dtextDock",
				AbsRect.FILL_INSIDE, new CutEdgeAShapeLayout());

		PlaceRect durDockRect = new AbsRect(SwingConstants.BOTTOM, new Integer(
				11));

		TextAShape durText = new TextAShape("ddurText", "$duration$",
				durDockRect, TextAShape.TYPE_SINGE_LINE, new Font("sansserif",
						Font.PLAIN, 9), Color.GRAY, new AtEnd(0),
				new AtFraction(0.5f), GfxUtil.AA_HINT_OFF);

		durText.setAttribute(AShape.A_CLIP_TYPE, AShape.CLIP_PARENT_BOUNDS);

		PlaceRect textDockRect = new AbsRect(SwingConstants.TOP, new Float(1f));

		TextAShape mainText = new TextAShape("dtext", "$summary$",
				textDockRect, TextAShape.TYPE_WRAP_TEXT, new Font("sansserif",
						Font.PLAIN, 11), Color.DARK_GRAY, new AtStart(0),
				new AtStart(0), GfxUtil.AA_HINT_OFF);

		mainText.setAttribute(AShape.A_CLIP_TYPE, AShape.CLIP_PARENT_BOUNDS);

		// Outline

		DrawAShape outline = new DrawAShape("doutline", new Rectangle(0, 0, 1,
				1), AbsRect.FILL, new Color(180, 180, 200),
				new BasicStroke(1f), GfxUtil.AA_HINT_OFF);

		// Build tree

		containerAShape.addSubShape(shadow);

		containerAShape.addSubShape(content);

		content.addSubShape(northTitle);

		northTitle.addSubShape(timeTitleText);

		content.addSubShape(textBackground);

		textBackground.addSubShape(textDockContainerA);

		textDockContainerA.addSubShape(durText);

		textDockContainerA.addSubShape(new DividerAShape(new AbsRect(
				SwingConstants.BOTTOM, new Integer(1)), Color.LIGHT_GRAY));

		textDockContainerA.addSubShape(mainText);

		containerAShape.addSubShape(outline);

		RootAShape root = new RootAShape(containerAShape);

		root.setRepaintPadding(new Insets(2, 2, 4, 4));

		HashMap timerMap = new HashMap();

		timerMap.put(TimerInteractor.PROP_INITIAL_DELAY, new Integer(0));

		timerMap.put(TimerInteractor.PROP_REPEAT_MILLIS, new Integer(500));

		timerMap.put(TimerInteractor.PROP_REPEAT_COUNT, new Integer(11));

		AShapeUtil.enableMouseOverCursor(root);

		AShapeUtil.enableMouseOverState(containerAShape);

		AShapeUtil.setResizeBoxes(containerAShape, SwingConstants.VERTICAL, 14);

		// Drag, resize interactions

		PropertyKey trigger = MouseKeyInteractor.MOUSE_PRESS;

		Integer button = new Integer(MouseEvent.BUTTON1);

		AShapeUtil.addMouseFireEvent(containerAShape, trigger,
				DefaultDateArea.AE_SELECTED_PRESSED, true, false, button);

		AShapeUtil.addMouseFireEvent(containerAShape, trigger,
				DefaultDateArea.AE_DRAG_PRESSED, true, true, button);

		AShapeUtil.addMouseEventBlock(containerAShape, false, new Integer(
				MouseEvent.MOUSE_MOVED));

		return root;
	}

	/**
	 * RootAShape method
	 * Creates the default shape.
	 */
	public static RootAShape createTraslucentShapeHorizontal() {
		Color bgPaint = new Color(255, 200, 200);

		Color bulletPaint = null;

		Color outlinePaint = new Color(128, 0, 0);

		// Color moOutlinePaint = new Color(0, 0, 0);

		Color textPaint = new Color(50, 50, 50);

		Font titleFont = new Font("SansSerif", Font.BOLD, 10);

		RootAShape root = new RootAShape();

		FillAShape bgAShape = new FillAShape("bg", new RoundRectangle(0, 0, 1,
				1, 8, 8), AbsRect.FILL_INSIDE, bgPaint, GfxUtil.AA_HINT_ON);

		PlaceRect bulletRect = new AbsRect(new AtStart(2), new AtStart(2));

		FillAShape bulletAShape = new FillAShape("bulletBackground",
				new Ellipse2D.Float(0, 0, 8, 8), bulletRect, bulletPaint,
				GfxUtil.AA_HINT_ON);

		PlaceRect contentAbsRect = new AbsRect(new AtStart(3), new AtStart(0),
				new AtEnd(-1), new AtEnd(-1));

		ContainerAShape content = new ContainerAShape("dock", contentAbsRect,
				new CutEdgeAShapeLayout());

		PlaceRect titleTextAbsRect = new AbsRect(new AtStart(0),
				new AtStart(0), new AtEnd(0), new AtEnd(-1), null, null, null);

		TextAShape timeTitleText = new TextAShape("titleText",
				"$startTime$-$endTimeExcl$ $summary$", titleTextAbsRect,
				TextAShape.TYPE_WRAP_TEXT, titleFont, textPaint,
				new AtStart(0), new AtStart(-2), GfxUtil.AA_HINT_OFF);

		DrawAShape outlineAShape = new DrawAShape("outline",
				new RoundRectangle(0, 0, 1, 1, 12, 12), AbsRect.FILL,
				outlinePaint, new BasicStroke(1.2f), GfxUtil.AA_HINT_ON);

		outlineAShape.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR));

		outlineAShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		bgAShape.addSubShape(bulletAShape);

		content.addSubShape(timeTitleText);
		bgAShape.addSubShape(content);
		root.addSubShape(bgAShape);
		root.addSubShape(outlineAShape);
		AShapeUtil.enableMouseOverCursor(root);
		AShapeUtil.enableMouseOverState(outlineAShape);
		AShapeUtil.setResizeBoxes(root, SwingConstants.HORIZONTAL, 4);

		// addEnterExitOverride(outlineAShape, outlineAShape, AShape.A_PAINT,
		// moOutlinePaint, false, true);

		// Drag, resize interactions

		PropertyKey trigger = MouseKeyInteractor.MOUSE_PRESS;
		Integer button = new Integer(MouseEvent.BUTTON1);
		AShapeUtil.addMouseFireEvent(outlineAShape, trigger,
				DefaultDateArea.AE_SELECTED_PRESSED, true, false, button);
		AShapeUtil.addMouseFireEvent(outlineAShape, trigger,
				DefaultDateArea.AE_DRAG_PRESSED, true, true, button);
		AShapeUtil.addMouseEventBlock(outlineAShape, false, new Integer(
				MouseEvent.MOUSE_MOVED));

		return root;
	}
}
