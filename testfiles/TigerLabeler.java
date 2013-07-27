/*
Copyright (c) 2005, Dale Anson
All rights reserved.
Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 * Neither the name of the <ORGANIZATION> nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package sidekick.java.node;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import eclipseicons.EclipseIconsPlugin;

import sidekick.java.options.*;

/**
 * Most of the display settings (how to show) are handled here.
 *
 * @author    Dale Anson
 * @version   $Revision: 18719 $
 */
public class TigerLabeler {

    // current option settings
    private static OptionValues options = null;

    // various icons for display
    protected static ImageIcon CU_ICON = null;
    protected static ImageIcon IMPORT_ICON = null;
    protected static ImageIcon ERROR_ICON = null;
    protected static ImageIcon CLASS_ICON = null;
    protected static ImageIcon INNER_CLASS_ICON = null;
    protected static ImageIcon EXTENDS_ICON = null;
    protected static ImageIcon IMPLEMENTS_ICON = null;
    protected static ImageIcon INTERFACE_ICON = null;
    protected static ImageIcon CONSTRUCTOR_ICON = null;
    protected static ImageIcon METHOD_ICON = null;
    protected static ImageIcon THROWS_ICON = null;
    protected static ImageIcon FIELD_ICON = null;
    protected static ImageIcon ENUM_ICON = null;
    protected static ImageIcon REGEX_PRODUCTION_ICON = null;
    protected static ImageIcon TOKEN_MGR_PRODUCTION_ICON = null;
    protected static ImageIcon JAVA_PRODUCTION_ICON = null;
    protected static ImageIcon BNF_PRODUCTION_ICON = null;
    protected static ImageIcon DEFAULT_ICON = null;

    // eclipse-style icons
    protected static ImageIcon E_CU_ICON = null;
    protected static ImageIcon E_IMPORT_ICON = null;
    protected static ImageIcon E_ERROR_ICON = null;
    protected static ImageIcon E_CLASS_ICON = null;
    protected static ImageIcon E_INNER_CLASS_DEFAULT_ICON = null;
    protected static ImageIcon E_INNER_CLASS_PUBLIC_ICON = null;
    protected static ImageIcon E_INNER_CLASS_PROTECTED_ICON = null;
    protected static ImageIcon E_INNER_CLASS_PRIVATE_ICON = null;
    protected static ImageIcon E_INTERFACE_ICON = null;
    protected static ImageIcon E_CONSTRUCTOR_ICON = null;
    protected static ImageIcon E_METHOD_DEFAULT_ICON = null;
    protected static ImageIcon E_METHOD_PUBLIC_ICON = null;
    protected static ImageIcon E_METHOD_PROTECTED_ICON = null;
    protected static ImageIcon E_METHOD_PRIVATE_ICON = null;
    protected static ImageIcon E_FIELD_DEFAULT_ICON = null;
    protected static ImageIcon E_FIELD_PUBLIC_ICON = null;
    protected static ImageIcon E_FIELD_PROTECTED_ICON = null;
    protected static ImageIcon E_FIELD_PRIVATE_ICON = null;
    protected static ImageIcon E_ENUM_DEFAULT_ICON = null;
    protected static ImageIcon E_ENUM_PUBLIC_ICON = null;
    protected static ImageIcon E_ENUM_PROTECTED_ICON = null;
    protected static ImageIcon E_ENUM_PRIVATE_ICON = null;
    protected static ImageIcon E_CONSTRUCTOR_DEC_ICON = null;
    protected static ImageIcon E_ABSTRACT_DEC_ICON = null;
    protected static ImageIcon E_STATIC_DEC_ICON = null;
    protected static ImageIcon E_FINAL_DEC_ICON = null;

    // load the icons
    static {
        // JBrowse icons
        try {
            CU_ICON = (ImageIcon) GUIUtilities.loadIcon("OpenFile.png");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            IMPORT_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Import.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            ERROR_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Error.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            CLASS_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Class.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            INNER_CLASS_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/InnerClass.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            EXTENDS_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Extends.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            IMPLEMENTS_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Implements.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            INTERFACE_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Interface.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            CONSTRUCTOR_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Constructor.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            METHOD_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Method.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            THROWS_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Throws.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            FIELD_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Field.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            ENUM_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Constructor.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            DEFAULT_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/Operation.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            REGEX_PRODUCTION_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/RegexProduction.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            BNF_PRODUCTION_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/BNFProduction.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            JAVA_PRODUCTION_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/JavaProduction.gif"));
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            TOKEN_MGR_PRODUCTION_ICON = new ImageIcon(TigerLabeler.class.getClassLoader().getResource("sidekick/java/icons/TokenMgrProduction.gif"));
        }
        catch (Exception e) {     // NOPMD
        }

        // Eclipse icons
        try {
            E_CU_ICON = EclipseIconsPlugin.getIcon("jcu_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_IMPORT_ICON = EclipseIconsPlugin.getIcon("imp_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_INNER_CLASS_PUBLIC_ICON = EclipseIconsPlugin.getIcon("innerclass_public_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_INNER_CLASS_PROTECTED_ICON = EclipseIconsPlugin.getIcon("innerclass_protected_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_INNER_CLASS_PRIVATE_ICON = EclipseIconsPlugin.getIcon("innerclass_private_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_INNER_CLASS_DEFAULT_ICON = EclipseIconsPlugin.getIcon("innerclass_default_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_CLASS_ICON = EclipseIconsPlugin.getIcon("class_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_INTERFACE_ICON = EclipseIconsPlugin.getIcon("int_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_METHOD_PUBLIC_ICON = EclipseIconsPlugin.getIcon("methpub_obj.gif");
        }
        catch (Exception e) {     // NOPMD
            e.printStackTrace();
        }
        try {
            E_METHOD_PROTECTED_ICON = EclipseIconsPlugin.getIcon("methpro_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_METHOD_PRIVATE_ICON = EclipseIconsPlugin.getIcon("methpri_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_METHOD_DEFAULT_ICON = EclipseIconsPlugin.getIcon("methdef_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_CONSTRUCTOR_DEC_ICON = EclipseIconsPlugin.getIcon("constr_ovr.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_FIELD_PUBLIC_ICON = EclipseIconsPlugin.getIcon("field_public_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_FIELD_PROTECTED_ICON = EclipseIconsPlugin.getIcon("field_protected_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_FIELD_PRIVATE_ICON = EclipseIconsPlugin.getIcon("field_private_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_FIELD_DEFAULT_ICON = EclipseIconsPlugin.getIcon("field_default_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_ENUM_PUBLIC_ICON = EclipseIconsPlugin.getIcon("enum_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_ENUM_PROTECTED_ICON = EclipseIconsPlugin.getIcon("enum_protected_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_ENUM_PRIVATE_ICON = EclipseIconsPlugin.getIcon("enum_private_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_ENUM_DEFAULT_ICON = EclipseIconsPlugin.getIcon("enum_default_obj.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_ABSTRACT_DEC_ICON = EclipseIconsPlugin.getIcon("abstract_co.gif");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_STATIC_DEC_ICON = EclipseIconsPlugin.getIcon("static_co.png");
        }
        catch (Exception e) {     // NOPMD
        }
        try {
            E_FINAL_DEC_ICON = EclipseIconsPlugin.getIcon("final_co.gif");
        }
        catch (Exception e) {     // NOPMD
        }

    }

    /**
     * Sets the displayOptions attribute of the TigerLabeler class
     *
     * @param opts  The new displayOptions value
     */
    public static void setOptionValues(OptionValues opts) {
        options = opts;
    }

    /**
     * Gets the icon attribute of the TigerLabeler class
     *
     * @param tn
     * @return    The icon value
     */
    public static Icon getIcon(TigerNode tn) {
        if (options == null) {
            return null;
        }

        ImageIcon icon = null;

        if (options.getShowIcons()) {
            if (options.getShowIconsLikeEclipse()) {
                int modifiers = tn.getModifiers();
                switch (tn.getOrdinal()) {
                    case TigerNode.ERROR:
                        icon = ERROR_ICON;
                        break;
                    case TigerNode.COMPILATION_UNIT:
                        icon = E_CU_ICON;
                        break;
                    case TigerNode.IMPORT:
                        icon = E_IMPORT_ICON;
                        break;
                    case TigerNode.CLASS:
                        if (((ClassNode) tn).isInnerClass()) {
                            if (ModifierSet.isPublic(modifiers))
                                icon = E_INNER_CLASS_PUBLIC_ICON;
                            else if (ModifierSet.isProtected(modifiers))
                                icon = E_INNER_CLASS_PROTECTED_ICON;
                            else if (ModifierSet.isPrivate(modifiers))
                                icon = E_INNER_CLASS_PRIVATE_ICON;
                            else
                                icon = E_INNER_CLASS_DEFAULT_ICON;
                        }
                        else {
                            icon = E_CLASS_ICON;
                        }
                        icon = addModifiers(modifiers, icon);
                        break;
                    case TigerNode.EXTENDS:
                        icon = EXTENDS_ICON;
                        break;
                    case TigerNode.IMPLEMENTS:
                        icon = IMPLEMENTS_ICON;
                        break;
                    case TigerNode.INTERFACE:
                        icon = E_INTERFACE_ICON;
                        break;
                    case TigerNode.CONSTRUCTOR:
                        ImageIcon bottom;
                        if (ModifierSet.isPublic(modifiers))
                            bottom = E_METHOD_PUBLIC_ICON;
                        else if (ModifierSet.isProtected(modifiers))
                            bottom = E_METHOD_PROTECTED_ICON;
                        else if (ModifierSet.isPrivate(modifiers))
                            bottom = E_METHOD_PRIVATE_ICON;
                        else
                            bottom = E_METHOD_DEFAULT_ICON;
                        ImageIcon top = E_CONSTRUCTOR_DEC_ICON;
                        icon = blend(bottom, SwingUtilities.SOUTH_WEST, top, SwingUtilities.NORTH_EAST, new Dimension(18, 16));
                        icon = addModifiers(modifiers, icon);
                        break;
                    case TigerNode.METHOD:
                        if (ModifierSet.isPublic(modifiers))
                            icon = E_METHOD_PUBLIC_ICON;
                        else if (ModifierSet.isProtected(modifiers))
                            icon = E_METHOD_PROTECTED_ICON;
                        else if (ModifierSet.isPrivate(modifiers))
                            icon = E_METHOD_PRIVATE_ICON;
                        else
                            icon = E_METHOD_DEFAULT_ICON;
                        icon = addModifiers(modifiers, icon);
                        break;
                    case TigerNode.THROWS:
                        icon = THROWS_ICON;
                        break;
                    case TigerNode.FIELD:
                        if (ModifierSet.isPublic(modifiers))
                            icon = E_FIELD_PUBLIC_ICON;
                        else if (ModifierSet.isProtected(modifiers))
                            icon = E_FIELD_PROTECTED_ICON;
                        else if (ModifierSet.isPrivate(modifiers))
                            icon = E_FIELD_PRIVATE_ICON;
                        else
                            icon = E_FIELD_DEFAULT_ICON;
                        icon = addModifiers(modifiers, icon);
                        break;
                    case TigerNode.ENUM:
                        if (ModifierSet.isPublic(modifiers))
                            icon = E_ENUM_PUBLIC_ICON;
                        else if (ModifierSet.isProtected(modifiers))
                            icon = E_ENUM_PROTECTED_ICON;
                        else if (ModifierSet.isPrivate(modifiers))
                            icon = E_ENUM_PRIVATE_ICON;
                        else
                            icon = E_ENUM_DEFAULT_ICON;
                        icon = addModifiers(modifiers, icon);
                        break;
                    case TigerNode.BNF_PRODUCTION:
                        icon = BNF_PRODUCTION_ICON;
                        break;
                    case TigerNode.REGEX_PRODUCTION:
                        icon = REGEX_PRODUCTION_ICON;
                        break;
                    case TigerNode.JAVA_PRODUCTION:
                        icon = JAVA_PRODUCTION_ICON;
                        break;
                    case TigerNode.TOKEN_MGR_PRODUCTION:
                        icon = TOKEN_MGR_PRODUCTION_ICON;
                        break;
                    default:
                        icon = DEFAULT_ICON;
                        break;
                }
            }
            else {
                switch (tn.getOrdinal()) {
                    case TigerNode.ERROR:
                        icon = ERROR_ICON;
                        break;
                    case TigerNode.COMPILATION_UNIT:
                        icon = CU_ICON;
                        break;
                    case TigerNode.IMPORT:
                        icon = IMPORT_ICON;
                        break;
                    case TigerNode.CLASS:
                        icon = ((ClassNode) tn).isInnerClass() ? INNER_CLASS_ICON : CLASS_ICON;
                        break;
                    case TigerNode.EXTENDS:
                        icon = EXTENDS_ICON;
                        break;
                    case TigerNode.IMPLEMENTS:
                        icon = IMPLEMENTS_ICON;
                        break;
                    case TigerNode.INTERFACE:
                        icon = INTERFACE_ICON;
                        break;
                    case TigerNode.CONSTRUCTOR:
                        icon = CONSTRUCTOR_ICON;
                        break;
                    case TigerNode.METHOD:
                        icon = METHOD_ICON;
                        break;
                    case TigerNode.THROWS:
                        icon = THROWS_ICON;
                        break;
                    case TigerNode.FIELD:
                        icon = FIELD_ICON;
                        break;
                    case TigerNode.ENUM:
                        icon = ENUM_ICON;
                        break;
                    case TigerNode.BNF_PRODUCTION:
                        icon = BNF_PRODUCTION_ICON;
                        break;
                    case TigerNode.REGEX_PRODUCTION:
                        icon = REGEX_PRODUCTION_ICON;
                        break;
                    case TigerNode.JAVA_PRODUCTION:
                        icon = JAVA_PRODUCTION_ICON;
                        break;
                    case TigerNode.TOKEN_MGR_PRODUCTION:
                        icon = TOKEN_MGR_PRODUCTION_ICON;
                        break;
                    default:
                        icon = DEFAULT_ICON;
                        break;
                }
            }
        }
        // may be null to not show an icon
        return icon;
    }

    /**
     * Adds a feature to the Modifiers attribute of the TigerLabeler class
     *
     * @param modifiers  The feature to be added to the Modifiers attribute
     * @param bottom     The feature to be added to the Modifiers attribute
     * @return           Description of the Returned Value
     */
    private static ImageIcon addModifiers(int modifiers, ImageIcon bottom) {
        ImageIcon icon = bottom;
        int width = 18;
        if (ModifierSet.isAbstract(modifiers)) {
            icon = blend(bottom, SwingUtilities.SOUTH_WEST, E_ABSTRACT_DEC_ICON, SwingUtilities.NORTH_EAST, new Dimension(width, 16));
            width += 6;
        }
        if (ModifierSet.isStatic(modifiers)) {
            icon = blend(bottom, SwingUtilities.SOUTH_WEST, E_STATIC_DEC_ICON, SwingUtilities.NORTH_EAST, new Dimension(width, 16));
            width += 6;
        }
        if (ModifierSet.isFinal(modifiers)) {
            icon = blend(bottom, SwingUtilities.SOUTH_WEST, E_FINAL_DEC_ICON, SwingUtilities.NORTH_EAST, new Dimension(width, 16));
        }
        return icon;
    }

    /**
     * Description of the Method
     *
     * @param bottom
     * @param bottomLocation
     * @param top
     * @param topLocation
     * @param minimumSize
     * @return                Description of the Returned Value
     */
    private static ImageIcon blend(ImageIcon bottom, int bottomLocation, ImageIcon top, int topLocation, Dimension minimumSize) {
        return IconBlender.blend(jEdit.getActiveView(), bottom, bottomLocation, top, topLocation, minimumSize);
    }

    /**
     * Gets the text attribute of the TigerLabeler class
     *
     * @param tn
     * @return    The text value
     */
    public static String getText(TigerNode tn) {
        if (tn != null) {
            if (options == null) {
                return tn.toString();
            }

            // build the string for the label
            StringBuffer sb = new StringBuffer();

            // maybe add the line number
            if (options.getShowLineNum()) {
                sb.append(tn.getStartLocation().line).append(": ");   //.append(tn.getStartLocation().column).append("::").append( tn.getEndLocation().line ).append( ": " ).append(tn.getEndLocation().column);
            }

            // add visibility modifiers, use either +, #, -, or public,
            // protected, private
            int modifiers = tn.getModifiers();
            if (options.getVisSymbols()) {
                if (ModifierSet.isPublic(modifiers))
                    sb.append('+');
                else if (ModifierSet.isProtected(modifiers))
                    sb.append('#');
                else if (ModifierSet.isPrivate(modifiers))
                    sb.append('-');
                else
                    sb.append(' ');
            }
            else if (options.getVisWords()) {
                if (ModifierSet.isPublic(modifiers))
                    sb.append("public ");
                else if (ModifierSet.isProtected(modifiers))
                    sb.append("protected ");
                else if (ModifierSet.isPrivate(modifiers))
                    sb.append("private ");
                else
                    sb.append(' ');
            }

            // maybe show keywords, this is the "Keywords specified by icons" setting,
            // which seems like an odd choice of words to me.  I was expecting icons,
            // but I think it means more like "show keywords beside icons"
            if (options.getShowIconKeywords()) {
                switch (tn.getOrdinal()) {
                    case TigerNode.CLASS:
                        sb.append("class ");
                        break;
                    case TigerNode.EXTENDS:
                        sb.append("extends ");
                        break;
                    case TigerNode.IMPLEMENTS:
                        sb.append("implements ");
                        break;
                }
            }

            // maybe add misc. modifiers, e.g. synchronized, native, transient, etc
            if (options.getShowMiscMod()) {
                String mods = ModifierSet.modifiersAsString(modifiers);
                if (mods != null && mods.length() > 0)
                    sb.append(mods).append(' ');
            }

            // for methods and fields, maybe add return type before node name
            if (!options.getTypeIsSuffixed()) {
                switch (tn.getOrdinal()) {
                    case TigerNode.CONSTRUCTOR:
                        sb.append("/*constructor*/");
                        break;
                    case TigerNode.METHOD:
                    case TigerNode.BNF_PRODUCTION:
                        sb.append(((MethodNode) tn).getReturnType()).append(' ');
                        break;
                    case TigerNode.FIELD:
                        sb.append(((FieldNode) tn).getType()).append(' ');
                        break;
                    case TigerNode.ENUM:
                        sb.append("enum ");
                        break;
                }
            }

            // maybe qualify inner class/interface name
            if (options.getShowNestedName() && tn.getParent() != null &&
                    (tn.getOrdinal() == TigerNode.CLASS || tn.getOrdinal() == TigerNode.INTERFACE) &&
                    (tn.getParent().getOrdinal() == TigerNode.CLASS || tn.getParent().getOrdinal() == TigerNode.INTERFACE)) {
                sb.append(tn.getParent().getName()).append('.');
            }

            // add the node name
            switch (tn.getOrdinal()) {
                case TigerNode.EXTENDS:
                    sb.append("class ");
                    break;
                case TigerNode.IMPLEMENTS:
                    sb.append("interface ");
                    break;
            }
            sb.append(tn.getName());

            // maybe show generics type arguments
            if (options.getShowTypeArgs()) {
                String typeParams = null;
                if (tn.getOrdinal() == TigerNode.CLASS) {
                    typeParams = ((ClassNode) tn).getTypeParams();
                }
                else if (tn.getOrdinal() == TigerNode.EXTENDS) {
                    typeParams = ((ExtendsNode) tn).getTypeParams();
                }
                else if (tn.getOrdinal() == TigerNode.IMPLEMENTS) {
                    typeParams = ((ImplementsNode) tn).getTypeParams();
                }
                else if (tn.getOrdinal() == TigerNode.FIELD) {
                    typeParams = ((FieldNode) tn).getTypeParams();
                }
                if (typeParams != null)
                    sb.append(typeParams);
            }

            // for constructors and methods, maybe add the arguments
            if (options.getShowArguments()) {
                if (tn.getOrdinal() == TigerNode.CONSTRUCTOR) {
                    sb.append('(').append(((ConstructorNode) tn).getFormalParams(options.getShowArgumentNames(), options.getTypeIsSuffixed(), options.getShowMiscMod(), options.getShowTypeArgs())).append(')');
                }
                else if (tn.getOrdinal() == TigerNode.METHOD || tn.getOrdinal() == TigerNode.BNF_PRODUCTION) {
                    sb.append('(').append(((MethodNode) tn).getFormalParams(options.getShowArgumentNames(), options.getTypeIsSuffixed(), options.getShowMiscMod(), options.getShowTypeArgs())).append(')');
                }
            }

            // for methods and fields, maybe add return type after node name
            if (options.getTypeIsSuffixed()) {
                switch (tn.getOrdinal()) {
                    case TigerNode.CONSTRUCTOR:
                        sb.append(": &lt;init&gt;");
                        break;
                    case TigerNode.METHOD:
                    case TigerNode.BNF_PRODUCTION:
                        sb.append(" : ").append(((MethodNode) tn).getReturnType());
                        break;
                    case TigerNode.FIELD:
                        sb.append(" : ").append(((FieldNode) tn).getType());
                        break;
                    case TigerNode.ENUM:
                        sb.append(" : enum");
                        break;
                }
            }

            String labelText = toHtml(sb.toString());
            sb = new StringBuffer();
            sb.append("<html>");

            // maybe underline static items
            if (options.getStaticUlined() && ModifierSet.isStatic(tn.getModifiers())) {
                sb.append("<u>");
            }

            // maybe set abstract items in italics
            if (options.getAbstractItalic() && ModifierSet.isAbstract(tn.getModifiers())) {
                sb.append("<i>");
            }
            sb.append(labelText);

            return sb.toString();
        }
        else
            return tn.toString();
    }

    /**
     * Gets the toolTipText attribute of the TigerLabeler class
     *
     * @param tn
     * @return    The toolTipText value
     */
    public static String getToolTipText(TigerNode tn) {
        return tn.toString();
    }

    /**
     * Description of the Method
     *
     * @param s
     * @return   Description of the Returned Value
     */
    private static String toHtml(String s) {
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        return s;
    }

	public static ImageIcon getMethodIcon() {
		if (!options.getShowIcons()) return null; 
		if (options.getShowIconsLikeEclipse()) {
			return E_METHOD_DEFAULT_ICON;
		}
		return METHOD_ICON;
	}

	public static ImageIcon getFieldIcon() {
		if (!options.getShowIcons()) return null; 
		if (options.getShowIconsLikeEclipse()) {
			return E_FIELD_DEFAULT_ICON;
		}
		return FIELD_ICON;
	}

	public static ImageIcon getConstructorIcon() {
		if (!options.getShowIcons()) return null; 
		if (options.getShowIconsLikeEclipse()) {
			return E_CONSTRUCTOR_ICON;
		}
		return CONSTRUCTOR_ICON;
	}

	public static ImageIcon getClassIcon() {
		if (!options.getShowIcons()) return null; 
		if (options.getShowIconsLikeEclipse()) {
			return E_CLASS_ICON;
		}
		return CLASS_ICON;
	}

	public static ImageIcon getInnerClassIcon(boolean isInterface, boolean isEnum) {
		if (!options.getShowIcons()) return null; 
		if (options.getShowIconsLikeEclipse()) {
			if (isInterface)
				return E_INTERFACE_ICON;
			else if (isEnum)
				return E_ENUM_DEFAULT_ICON;
			return E_INNER_CLASS_DEFAULT_ICON;
		}
		if (isInterface)
			return INTERFACE_ICON;
		else if (isEnum)
			return ENUM_ICON;
		return INNER_CLASS_ICON;
	}

}

