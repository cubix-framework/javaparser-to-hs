/*
This file, unless otherwise noted, is wholly the work of Dale Anson,
danson@grafidog.com. The complete contents of this file is hereby 
released into the public domain, with no rights reserved. For questions, 
concerns, or comments, please email the author.
*/

package ise.calculator;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import static ise.calculator.Base.*;

/**
 * An RPN calculator. I googled all over, but didn't find any open source rpn
 * calculators, so wrote this one. It does all the math functions in
 * java.lang.Math, plus the standard add, subtract, multiply, divide, and mod
 * operations. It uses the standard 4 register stack rpn found in HP
 * calculators, and supports double (float), integer, hexidecimal, octal, and
 * binary numbers. <p>
 *
 * Some of this is overkill, I borrowed the math, num, and op classes from the
 * Math task that I wrote for Ant, which goes to a lot of trouble to handle
 * complex nested formulas, probably it is much more than is needed for this
 * calculator.
 *
 * @author    Dale Anson, July 2003
 * @version   $Revision: 19813 $
 */
public class CalculatorPanel extends JPanel implements WindowConstants {

    // use strict math?
    private boolean _strict = false;

    // current base used for calculations
    private int current_base = BASE_10;

    // current mode used track big decimal, float, big int, or int modes
    private int current_mode = FLOAT;

    // the register stack
    private JTextField x_register = new JTextField(60);
    private JTextField y_register = new JTextField(60);
    private JTextField z_register = new JTextField(60);
    private JTextField t_register = new JTextField(60);

    // all the buttons
    private RectangleButton plus;
    private RectangleButton minus;
    private RectangleButton modulus;
    private RectangleButton multiply;
    private RectangleButton divide;    // NOPMD
    private RectangleButton enter;
    private RectangleButton and;
    private RectangleButton or;
    private RectangleButton not;
    private RectangleButton xor;
    private RectangleButton x_label;
    private RectangleButton y_label;
    private RectangleButton z_label;
    private RectangleButton t_label;
    private RectangleToggleButton bigdecimal_mode;    // NOPMD
    private RectangleToggleButton bigint_mode;    // NOPMD
    private RectangleToggleButton float_mode;
    private RectangleToggleButton integer_mode;    // NOPMD
    private RectangleToggleButton base_16_btn;
    private RectangleToggleButton base_10_btn;
    private RectangleToggleButton base_8_btn;
    private RectangleToggleButton base_2_btn;
    private RectangleButton store;    // NOPMD
    private RectangleButton recall;    // NOPMD
    private RectangleButton clear;    // NOPMD
    private RectangleButton all_clear;    // NOPMD
    private RectangleButton roll_up;    // NOPMD
    private RectangleButton roll_down;    // NOPMD
    private RectangleButton xy;    // NOPMD
    private RectangleButton last_x;    // NOPMD
    private JButton euler;    // NOPMD
    private JButton pi;    // NOPMD
    private RectangleToggleButton strict;    // NOPMD
    private RectangleButton chs;    // NOPMD  +/-, change sign


    // the number panel is a separate class
    private NumberPanel number_panel = null;

    // storage for the last x value
    private String last_x_value = "";

    // storage for the store value
    private String store_value = "";

    // the close operation, added this for jEdit plugin so closing the calculator
    // doesn't close jEdit.
    private int close_operation = EXIT_ON_CLOSE;

    // action command to button map
    private HashMap<String, AbstractButton> buttons = new HashMap<String, AbstractButton>();

    // variables for macro recording --
    // is recording happening?
    private boolean recording = false;

    // storage of the recorded steps
    private ArrayList<String> macro = null;

    /** Constructor for CalculatorPanel  */
    public CalculatorPanel() {
        setName("CalculatorPanel");
        setLayout(new BorderLayout());

        loadFunctions();

        // register panel
        JPanel register_panel = createRegisterPanel();

        // control panel
        JPanel control_panel = createControlPanel();

        // function panel
        JPanel function_panel = createFunctionPanel();

        // operation panel
        JPanel operation_panel = createOperationPanel();

        // number panel
        number_panel = new NumberPanel(x_register);

        // add action listeners to buttons
        addActionListeners();

        // menus
        createMenus();

        // create the display
        JPanel main = new JPanel(new LambdaLayout());

        // top panel
        main.add(register_panel, "0, 0, 1, 1, 0, w");
        main.add(KappaLayout.createVerticalStrut(11), "0, 1");

        // center panel is control panel
        main.add(control_panel, "0, 2, 1, 1, 0, w");
        main.add(KappaLayout.createVerticalStrut(11), "0, 3");

        // bottom panel
        JPanel bottom_panel = new JPanel(new LambdaLayout());
        bottom_panel.add(function_panel, "0, 0, 1, 1, N,, ");
        bottom_panel.add(KappaLayout.createHorizontalStrut(11), "1, 0");
        bottom_panel.add(operation_panel, "2, 0, 1, 1, N,, ");
        bottom_panel.add(KappaLayout.createHorizontalStrut(11), "3, 0");
        bottom_panel.add(number_panel, "4, 0, 1, 1, N,, ");
        main.add(bottom_panel, "0, 4, 1, 1, 0, w");

        add(main, BorderLayout.CENTER);

        float_mode.doClick();
        x_register.requestFocus();
        x_register.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent fe) {
                if (fe.getOppositeComponent() == null) {
                    return;
                }
                Container c = SwingUtilities.getAncestorNamed("CalculatorPanel", fe.getOppositeComponent());
                if (c != null) {
                    x_register.requestFocus();
                }
            }
        } );
    }

    /**
     * @param btns A comma separated list of button action command names
     * and display text, e.g. "cmd, display, cmd, display,..."
     */
    private void makeZeroParamButtons(String btns) {
        StringTokenizer st = new StringTokenizer(btns, ",");
        while (st.hasMoreTokens()) {
            String cmd = st.nextToken();
            String text = st.nextToken();
            JButton btn = new RectangleButton("<html>" + text);
            btn.setActionCommand(cmd);
            btn.addActionListener(zero_op_listener);
            buttons.put(cmd, btn);
        }
    }

    /**
     * @param btns A comma separated list of button action command names
     * and display text, e.g. "cmd, display, cmd, display,..."
     */
    private void makeOneParamButtons(String btns) {
        StringTokenizer st = new StringTokenizer(btns, ",");
        while (st.hasMoreTokens()) {
            String cmd = st.nextToken();
            String text = st.nextToken();
            JButton btn = new RectangleButton("<html>" + text);
            btn.setActionCommand(cmd);
            btn.addActionListener(unary_op_listener);
            buttons.put(cmd, btn);
        }
    }

    /**
     * @param btns A comma separated list of button action command names
     * and display text, e.g. "cmd, display, cmd, display,..."
     */
    private void makeTwoParamButtons(String btns) {
        StringTokenizer st = new StringTokenizer(btns, ",");
        while (st.hasMoreTokens()) {
            String cmd = st.nextToken();
            String text = st.nextToken();
            JButton btn = new RectangleButton("<html>" + text);
            btn.setActionCommand(cmd);
            btn.addActionListener(binary_op_listener);
            buttons.put(cmd, btn);
        }
    }

    /**
     * Converts the given value from the old base to the new base.
     *
     * @param value     the value to convert
     * @param old_base  the base of value prior to conversion
     * @param new_base  the base of value after conversion
     * @return          value represented in new_base
     */
    public String convertToBase(String value, int old_base, int new_base) {
        if (value == null || value.length() == 0) {
            return "";
        }

        // depends on the mode...
        switch (current_mode) {

            case BIGDECIMAL:
                if (new_base != BASE_10) {
                    throw new IllegalArgumentException("illegal base, must be 10, was " + new_base);
                }
                if (value.indexOf('.') >= 0) {
                    return new BigDecimal(value).toString();
                } else if (value.equals("Infinity") || value.equals("NaN")) {
                    return "Error: cannot convert to BigDecimal";
                } else {
                    value = new BigInteger(value, old_base).toString(new_base);
                }
                return new BigDecimal(value).toString();
            case FLOAT:
                if (new_base != BASE_10) {
                    throw new IllegalArgumentException("illegal base, must be 10, was " + new_base);
                }
                if (old_base == new_base) {
                    return String.valueOf(new BigDecimal(value).doubleValue());
                } else {
                    value = new BigInteger(value, old_base).toString(new_base);
                }
                return String.valueOf(new BigDecimal(value).doubleValue());
            case BIGINT:
                if (value.indexOf('.') >= 0) {
                    value = new BigDecimal(value).toBigInteger().toString();
                } else {
                    value = new BigInteger(value, old_base).toString(new_base);
                }
                return value;
            case INT:
            default:
                int n;
                if (old_base == BASE_10) {
                    n = new Double(Double.parseDouble(value)).intValue();
                } else {
                    n = Integer.parseInt(value, old_base);
                }
                return Integer.toString(n, new_base);
        }
    }

    /**
     * Set the calculation mode, must be one of INT, BIGINT, FLOAT, or
     * BIGDECIMAL.
     *
     * @param mode  The new mode value
     */
    public void setMode(int mode) {
        switch (mode) {
            case INT:
            case BIGINT:
            case FLOAT:
            case BIGDECIMAL:
                current_mode = mode;
                break;
            default:
                throw new IllegalArgumentException("invalid mode: " + mode);
        }
        number_panel.setBase(current_base, current_mode);
    }

    /**
     * Gets the mode attribute of the CalculatorPanel object
     *
     * @return   The mode value, one of INT, BIGINT, FLOAT, or
     * BIGDECIMAL.
     */
    public int getMode() {
        return current_mode;
    }

    /**
     * Sets the closeOperation attribute of the CalculatorPanel object
     *
     * @param operation  The new closeOperation value, one of DISPOSE_ON_CLOSE,
     * DO_NOTHING_ON_CLOSE, EXIT_ON_CLOSE, or HIDE_ON_CLOSE.
     */
    public void setCloseOperation(int operation) {
        switch (operation) {
            case DISPOSE_ON_CLOSE:
            case DO_NOTHING_ON_CLOSE:
            case EXIT_ON_CLOSE:
            case HIDE_ON_CLOSE:
                break;
            default:
                throw new IllegalArgumentException("Invalid close operation, see javax.swing.WindowConstants.");
        }
        close_operation = operation;
    }

    /**
     * Gets the closeOperation attribute of the CalculatorPanel object
     *
     * @return   The closeOperation value, one of DISPOSE_ON_CLOSE,
     * DO_NOTHING_ON_CLOSE, EXIT_ON_CLOSE, or HIDE_ON_CLOSE.
     */
    public int getCloseOperation() {
        return close_operation;
    }

    /** Set focus to the X register. */
    public void addNotify() {
        super.addNotify();
        x_register.requestFocusInWindow();
    }

    // check if the 'built in' functions and constants need to be unpacked.
    // Assume that if calc_dir exists, then all is well, otherwise, unpack
    // all.txt into it.
    private void loadFunctions() {
        File calc_dir = new File(System.getProperty("calc.home"), ".calc");
        if (!calc_dir.exists()) {
            System.out.print("Unpacking... ");
            calc_dir.mkdirs();
            InputStream is = getClass().getClassLoader().getResourceAsStream("all.txt");
            FunctionPackager fp = new FunctionPackager();
            fp.unpack(is);
            try {
                Calculator.PREFS.node("constants_menu").clear();
                Calculator.PREFS.node("function_menu").clear();
            } catch (Exception e) { }            // NOPMD
            // System.out.println( "Done." );
        }
    }

    private void createMenus() {
        JMenuBar menubar = new JMenuBar();

        final JMenu function_menu = new JMenu("Function");
        menubar.add(function_menu);
        JMenuItem show_functions_mi = new JMenuItem("Functions...");
        JMenuItem record_mi = new JMenuItem("Record");
        JMenuItem stop_mi = new JMenuItem("Stop Recording");
        function_menu.add(show_functions_mi);
        function_menu.add(record_mi);
        function_menu.add(stop_mi);
        function_menu.add(new JSeparator());
        loadMenu(function_menu, "function_menu");
        function_menu.add(new JSeparator());
        JMenuItem exit_mi = new JMenuItem("Close");
        function_menu.add(exit_mi);

        final JMenu constants_menu = new JMenu("Constants");
        menubar.add(constants_menu);

        JMenuItem show_constants_mi = new JMenuItem("Constants...");

        constants_menu.add(show_constants_mi);
        constants_menu.add(new JSeparator());
        loadMenu(constants_menu, "constants_menu");
        JMenu help_menu = new JMenu("Help");
        menubar.add(help_menu);
        JMenuItem help_mi = new JMenuItem("Help");
        help_menu.add(help_mi);
        JMenuItem about_mi = new JMenuItem("About");
        help_menu.add(about_mi);

        add(menubar, BorderLayout.NORTH);

        // function menu actions
        show_functions_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                FunctionChooser.showChooser(CalculatorPanel.this, function_menu, functionPlayer);
            }
        }
       );

        record_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                x_label.setEnabled(true);
                y_label.setEnabled(true);
                z_label.setEnabled(true);
                t_label.setEnabled(true);
                recording = true;
                macro = new ArrayList<String>();
                number_panel.setRecording(recording, macro);
            }
        }
       );
        stop_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // save the macro to a file
                x_label.setEnabled(false);
                y_label.setEnabled(false);
                z_label.setEnabled(false);
                t_label.setEnabled(false);
                recording = false;

                String name = JOptionPane.showInputDialog(CalculatorPanel.this, "Enter a name for this function:", "Enter Name", JOptionPane.QUESTION_MESSAGE);
                if (name == null) {
                    return;
                }
                String desc = JOptionPane.showInputDialog(CalculatorPanel.this, "Enter a description for this function:", "Enter Description", JOptionPane.QUESTION_MESSAGE);
                try {
                    File calc_dir = new File(System.getProperty("calc.home"), ".calc");
                    calc_dir.mkdirs();
                    File f = File.createTempFile("calc", ".calc", new File(System.getProperty("calc.home"), ".calc"));
                    StringBuffer macro_steps = new StringBuffer();
                    for (String step : macro) {
                        macro_steps.append(step).append('\n');
                    }
                    FunctionWriter fw = new FunctionWriter(f);
                    fw.write(name, desc, macro_steps.toString());

                    // insert the new function into the function menu -- the
                    // new function is initially inserted just above the last
                    // separator, but will be sorted on next start
                    int offset = function_menu.getItemCount() - 2;
                    JMenuItem mi = new JMenuItem(name);
                    mi.setActionCommand(f.getName().substring(0, f.getName().lastIndexOf(".")));
                    mi.setToolTipText(desc);
                    mi.addActionListener(functionPlayer);
                    function_menu.insert(mi, offset);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CalculatorPanel.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    number_panel.setRecording(false, null);
                }

            }
        }
       );

        exit_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                switch (close_operation) {
                    case DISPOSE_ON_CLOSE:
                        Frame f = GUIUtils.getRootFrame(CalculatorPanel.this);
                        f.dispose();
                        break;
                    case EXIT_ON_CLOSE:
                        System.exit(0);
                        break;
                    case HIDE_ON_CLOSE:
                        setVisible(false);
                        break;
                    default:
                        break;
                }
            }
        }
       );

        // constants menu actions
        show_constants_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                ConstantsChooser.showChooser(CalculatorPanel.this, constants_menu, functionPlayer);
            }
        }
       );

        // help menu actions
        help_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    java.net.URL help_url = getClass().getClassLoader().getResource("index.html");
                    System.out.println(help_url);
                    new AboutDialog(GUIUtils.getRootJFrame(CalculatorPanel.this), "Help", help_url, true).setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
       );

        about_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(CalculatorPanel.this, "<html><b>RPN Calculator</b><p>by Dale Anson<br>Version 1.2.0", "About Calculator", JOptionPane.INFORMATION_MESSAGE);
            }
        }
       );
    }

    /**
     * @param type either "function_menu" or "constants_menu"
     */
    private void loadMenu(JMenu menu, String type) {
        try {
            File calc_dir = new File(System.getProperty("calc.home"), ".calc");
            if (!calc_dir.exists()) {
                return;
            }
            Preferences prefs = Calculator.PREFS.node(type);
            String[] constants = prefs.keys();
            Arrays.sort(constants);
            for (int i = 0; i < constants.length; i++) {
                try {
                    String constant = constants[i];
                    FunctionReader fr = new FunctionReader(new File(calc_dir, constant));
                    String name = fr.getName();
                    String desc = fr.getDescription();
                    String cmd = fr.getCommand();
                    JMenuItem mi = new JMenuItem(name);
                    mi.setToolTipText(desc);
                    mi.setActionCommand(cmd);
                    mi.addActionListener(functionPlayer);
                    buttons.put(cmd, mi);
                    menu.add(mi);
                } catch (Exception e) {         // NOPMD
                    //e.printStackTrace();      
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The operation panel contains the buttons for the math operations,
     * e.g. plus, minus, divide, etc.
     */
    private JPanel createOperationPanel() {
        JPanel operation_panel = new JPanel(new LambdaLayout());

        plus = new RectangleButton("<html><b>+");
        plus.setActionCommand("+");
        buttons.put("+", plus);
        plus.setToolTipText("Add X and Y");
        minus = new RectangleButton("<html><b>-");
        minus.setActionCommand("-");
        buttons.put("-", minus);
        minus.setToolTipText("Subtract X from Y");
        modulus = new RectangleButton("<html><b>\\");
        modulus.setActionCommand("\\");
        buttons.put("\\", modulus);
        modulus.setToolTipText("X mod Y");
        multiply = new RectangleButton("<html><b>x");
        multiply.setActionCommand("*");
        buttons.put("*", multiply);
        multiply.setToolTipText("Multiply X times Y");
        divide = new RectangleButton("<html><b>&#247;");
        divide.setActionCommand("/");
        buttons.put("/", divide);
        divide.setToolTipText("Divide Y by X");
        enter = new RectangleButton("<html><b>Enter &#8593;</html>");
        enter.setActionCommand("enter");
        buttons.put("enter", enter);
        enter.setToolTipText("Enter");

        and = new RectangleButton("&");
        and.setActionCommand("and");
        buttons.put("and", and);
        and.setToolTipText("X and Y");
        and.setEnabled(false);
        or = new RectangleButton("|");
        or.setActionCommand("or");
        buttons.put("or", or);
        or.setToolTipText("X or Y");
        or.setEnabled(false);
        not = new RectangleButton("~");
        not.setActionCommand("not");
        buttons.put("not", not);
        not.setToolTipText("not X");
        not.setEnabled(false);
        xor = new RectangleButton("^");
        xor.setActionCommand("xor");
        buttons.put("xor", xor);
        xor.setToolTipText("X xor Y");
        xor.setEnabled(false);

        chs = new RectangleButton("<html>&#177;");        // +/-, plus minus
        chs.setActionCommand("chs");
        buttons.put("chs", chs);
        chs.setToolTipText("Change sign");

        plus.addActionListener(binary_op_listener);
        minus.addActionListener(binary_op_listener);
        modulus.addActionListener(binary_op_listener);
        multiply.addActionListener(binary_op_listener);
        divide.addActionListener(binary_op_listener);
        and.addActionListener(binary_op_listener);
        or.addActionListener(binary_op_listener);
        not.addActionListener(unary_op_listener);
        xor.addActionListener(binary_op_listener);
        chs.addActionListener(chs_listener);

        operation_panel.add(enter, "0, 0, 2, 1, 0, wh, 2");

        operation_panel.add(and, "0, 2, 1, 1, 0, wh, 2");
        operation_panel.add(or, "0, 3, 1, 1, 0, wh, 2");
        operation_panel.add(not, "0, 4, 1, 1, 0, wh, 2");
        operation_panel.add(xor, "0, 5, 1, 1, 0, wh, 2");
        operation_panel.add(modulus, "0, 6, 1, 1, 0, wh, 2");

        operation_panel.add(minus, "1, 2, 1, 1, 0, wh, 2");
        operation_panel.add(plus, "1, 3, 1, 1, 0, wh, 2");
        operation_panel.add(multiply, "1, 4, 1, 1, 0, wh, 2");
        operation_panel.add(divide, "1, 5, 1, 1, 0, wh, 2");
        operation_panel.add(chs, "1, 6, 1, 1, 0, wh, 2");

        return operation_panel;
    }

    /**
     * The register panel contains the four text fields for numeric output.
     */
    private JPanel createRegisterPanel() {
        x_register.setDocument(new RegisterDocument(current_base, current_mode));
        y_register.setDocument(new RegisterDocument(current_base, current_mode));
        z_register.setDocument(new RegisterDocument(current_base, current_mode));
        t_register.setDocument(new RegisterDocument(current_base, current_mode));

        Font old_font = x_register.getFont();
        Font register_font = new Font("Monospaced", old_font.getStyle(), old_font.getSize());
        x_register.setFont(register_font);
        y_register.setFont(register_font);
        z_register.setFont(register_font);
        t_register.setFont(register_font);

        x_register.setHorizontalAlignment(JTextField.RIGHT);
        y_register.setHorizontalAlignment(JTextField.RIGHT);
        z_register.setHorizontalAlignment(JTextField.RIGHT);
        t_register.setHorizontalAlignment(JTextField.RIGHT);

        x_label = new RectangleButton("X");
        x_label.setEnabled(false);
        x_label.setActionCommand("x");
        x_label.addActionListener(label_listener);
        buttons.put("x", x_label);
        y_label = new RectangleButton("Y");
        y_label.setEnabled(false);
        y_label.setActionCommand("y");
        y_label.addActionListener(label_listener);
        buttons.put("y", y_label);
        z_label = new RectangleButton("Z");
        z_label.setEnabled(false);
        z_label.setActionCommand("z");
        z_label.addActionListener(label_listener);
        buttons.put("z", x_label);
        t_label = new RectangleButton("T");
        t_label.setEnabled(false);
        t_label.setActionCommand("t");
        t_label.addActionListener(label_listener);
        buttons.put("t", t_label);

        JPanel register_panel = new JPanel(new LambdaLayout());
        register_panel.add(t_label, "0, 0, 1, 1, W, 0, 2");
        register_panel.add(z_label, "0, 1, 1, 1, W, 0, 2");
        register_panel.add(y_label, "0, 2, 1, 1, W, 0, 2");
        register_panel.add(x_label, "0, 3, 1, 1, W, 0, 2");
        register_panel.add(t_register, "1, 0, R, 1, 0, wh, 2");
        register_panel.add(z_register, "1, 1, R, 1, 0, wh, 2");
        register_panel.add(y_register, "1, 2, R, 1, 0, wh, 2");
        register_panel.add(x_register, "1, 3, R, 1, 0, wh, 2");

        return register_panel;
    }

    /**
     * The control panel contains the buttons to set the base, numeric type
     * (BigDecimal, float, etc) and the register controls (clear, roll up/down,
     * etc.)
     */
    private JPanel createControlPanel() {
        LambdaLayout control_layout = new LambdaLayout();
        JPanel control_panel = new JPanel(control_layout);

        // left side panel, holds the mode and base buttons
        JPanel left_panel = new JPanel(new LambdaLayout());

        bigdecimal_mode = new RectangleToggleButton("BD");
        bigdecimal_mode.setActionCommand(String.valueOf(BIGDECIMAL));
        bigdecimal_mode.setToolTipText("BigDecimal mode");

        bigint_mode = new RectangleToggleButton("BI");
        bigint_mode.setActionCommand(String.valueOf(BIGINT));
        bigint_mode.setToolTipText("BigInteger mode");

        float_mode = new RectangleToggleButton("F");
        float_mode.setActionCommand(String.valueOf(FLOAT));
        float_mode.setSelected(true);
        float_mode.setToolTipText("Floating point mode");

        integer_mode = new RectangleToggleButton("I");
        integer_mode.setActionCommand(String.valueOf(INT));
        integer_mode.setToolTipText("Integer mode");

        base_16_btn = new RectangleToggleButton("16");
        base_16_btn.setActionCommand(String.valueOf(BASE_16));
        base_16_btn.setToolTipText("Base 16 hexadecimal mode");

        base_10_btn = new RectangleToggleButton("10");
        base_10_btn.setActionCommand(String.valueOf(BASE_10));
        base_10_btn.setToolTipText("Base 10 decimal mode");

        base_8_btn = new RectangleToggleButton("8");
        base_8_btn.setActionCommand(String.valueOf(BASE_8));
        base_8_btn.setToolTipText("Base 8 octal mode");

        base_2_btn = new RectangleToggleButton("2");
        base_2_btn.setActionCommand(String.valueOf(BASE_2));
        base_2_btn.setToolTipText("Base 2 binary mode");

        ButtonGroup mode_group = new ButtonGroup();
        mode_group.add(bigdecimal_mode);
        mode_group.add(bigint_mode);
        mode_group.add(float_mode);
        mode_group.add(integer_mode);

        ButtonGroup base_group = new ButtonGroup();
        base_group.add(base_16_btn);
        base_group.add(base_10_btn);
        base_group.add(base_8_btn);
        base_group.add(base_2_btn);

        ActionListener mode_listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String ac = ae.getActionCommand();
                int cmd = Integer.parseInt(ac);
                setMode(cmd);
                switch (cmd) {
                    case BIGDECIMAL:
                    case FLOAT:
                        base_16_btn.setEnabled(false);
                        base_8_btn.setEnabled(false);
                        base_2_btn.setEnabled(false);
                        base_10_btn.setEnabled(true);
                        base_10_btn.doClick();
                        and.setEnabled(false);
                        or.setEnabled(false);
                        not.setEnabled(false);
                        xor.setEnabled(false);
                        modulus.setEnabled(false);
                        break;
                    case BIGINT:
                    case INT:
                        base_16_btn.setEnabled(true);
                        base_8_btn.setEnabled(true);
                        base_2_btn.setEnabled(true);
                        and.setEnabled(true);
                        or.setEnabled(true);
                        not.setEnabled(true);
                        xor.setEnabled(true);
                        modulus.setEnabled(true);
                        AbstractButton base_btn = null;
                        if (base_16_btn.isSelected()) {
                            base_btn = base_16_btn;
                        } else if (base_8_btn.isSelected()) {
                            base_btn = base_8_btn;
                        } else if (base_2_btn.isSelected()) {
                            base_btn = base_2_btn;
                        } else {
                            base_btn = base_10_btn;
                        }
                        base_btn.setSelected(false);
                        base_btn.doClick();
                }
            }

        };

        ActionListener base_listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                RectangleToggleButton source = (RectangleToggleButton) ae.getSource();
                int base = Integer.parseInt(source.getActionCommand());

                String x_value = x_register.getText();
                String y_value = y_register.getText();
                String z_value = z_register.getText();
                String t_value = t_register.getText();

                x_register.setDocument(new RegisterDocument(base, current_mode));
                y_register.setDocument(new RegisterDocument(base, current_mode));
                z_register.setDocument(new RegisterDocument(base, current_mode));
                t_register.setDocument(new RegisterDocument(base, current_mode));

                x_register.setText(convertToBase(x_value, current_base, base));
                y_register.setText(convertToBase(y_value, current_base, base));
                z_register.setText(convertToBase(z_value, current_base, base));
                t_register.setText(convertToBase(t_value, current_base, base));

                store_value = convertToBase(store_value, current_base, base);

                current_base = base;
                number_panel.setBase(current_base, current_mode);
            }
        };

        bigdecimal_mode.addActionListener(mode_listener);
        bigint_mode.addActionListener(mode_listener);
        float_mode.addActionListener(mode_listener);
        integer_mode.addActionListener(mode_listener);
        base_16_btn.addActionListener(base_listener);
        base_10_btn.addActionListener(base_listener);
        base_8_btn.addActionListener(base_listener);
        base_2_btn.addActionListener(base_listener);

        // right side panel, holds the register manipulation buttons
        JPanel right_panel = new JPanel(new LambdaLayout());

        store = new RectangleButton("STO");
        store.setActionCommand("sto");
        store.setToolTipText("Store current X value");
        buttons.put("sto", store);
        recall = new RectangleButton("RCL");
        recall.setActionCommand("rcl");
        recall.setToolTipText("Recall previously stored value to X register");
        buttons.put("rcl", recall);
        clear = new RectangleButton("CLR");
        clear.setActionCommand("clr");
        buttons.put("clr", clear);
        clear.setToolTipText("Clear X");
        all_clear = new RectangleButton("AC");
        all_clear.setToolTipText("Clear All");
        all_clear.setActionCommand("ac");
        buttons.put("ac", all_clear);
        roll_up = new RectangleButton("<html>R&#8593;");
        roll_up.setToolTipText("Roll Up");
        roll_up.setActionCommand("ru");
        buttons.put("ru", roll_up);
        roll_down = new RectangleButton("<html>R&#8595;");
        roll_down.setToolTipText("Roll Down");
        roll_down.setActionCommand("rd");
        buttons.put("rd", roll_down);
        xy = new RectangleButton("<html>X&#8596;Y");
        xy.setToolTipText("Swap X and Y");
        xy.setActionCommand("xy");
        buttons.put("xy", xy);
        last_x = new RectangleButton("LstX");
        last_x.setToolTipText("Put the previous X in the X register");
        last_x.setActionCommand("lstx");
        buttons.put("lstx", last_x);

        // layout the panels
        left_panel.add(bigdecimal_mode, "0, 0, 1, 1, 0,, 2");
        left_panel.add(float_mode, "1, 0, 1, 1, 0,, 2");
        left_panel.add(bigint_mode, "2, 0, 1, 1, 0,, 2");
        left_panel.add(integer_mode, "3, 0, 1, 1, 0,, 2");

        left_panel.add(base_16_btn, "0, 1, 1, 1, 0,, 2");
        left_panel.add(base_10_btn, "1, 1, 1, 1, 0,, 2");
        left_panel.add(base_8_btn, "2, 1, 1, 1, 0,, 2");
        left_panel.add(base_2_btn, "3, 1, 1, 1, 0,, 2");

        right_panel.add(store, "0, 0, 1, 1, 0,, 2");
        right_panel.add(clear, "1, 0, 1, 1, 0,, 2");
        right_panel.add(all_clear, "2, 0, 1, 1, 0,, 2");
        right_panel.add(last_x, "3, 0, 1, 1, 0,, 2");

        right_panel.add(recall, "0, 1, 1, 1, 0,, 2");
        right_panel.add(roll_up, "1, 1, 1, 1, 0,, 2");
        right_panel.add(roll_down, "2, 1, 1, 1, 0,, 2");
        right_panel.add(xy, "3, 1, 1, 1, 0,, 2");

        control_panel.add(left_panel, "0, 0, 1, 1, W");
        control_panel.add(right_panel, "1, 0, 1, 1, E");

        store.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                store_value = x_register.getText();
            }
        }
       );

        recall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                x_register.setText(store_value);
            }
        }
       );

        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                x_register.setText("");
            }
        }
       );

        all_clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                x_register.setText("");
                y_register.setText("");
                z_register.setText("");
                t_register.setText("");
            }
        }
       );

        roll_up.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String temp = t_register.getText();
                t_register.setText(z_register.getText());
                z_register.setText(y_register.getText());
                y_register.setText(x_register.getText());
                x_register.setText(temp);
            }
        }
       );

        roll_down.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String temp = x_register.getText();
                x_register.setText(y_register.getText());
                y_register.setText(z_register.getText());
                z_register.setText(t_register.getText());
                t_register.setText(temp);
            }
        }
       );

        xy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String temp = x_register.getText();
                x_register.setText(y_register.getText());
                y_register.setText(temp);
            }
        }
       );

        last_x.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                t_register.setText(z_register.getText());
                z_register.setText(y_register.getText());
                y_register.setText(x_register.getText());
                x_register.setText(last_x_value);
                x_register.selectAll();
                x_register.requestFocus();
            }
        }
       );

        return control_panel;
    }

    /**
     * The function panel contains the buttons on the left side of the
     * calculator for the built-in functions, e.g. sin, cos, etc.
     */
    private JPanel createFunctionPanel() {
        JPanel function_panel = new JPanel(new LambdaLayout());
        KappaLayout.Constraints cons = KappaLayout.createConstraint();
        cons.p = 2;

        // these are in pairs, the first is the function name, these correspond with
        // method names in java.lang.Math or ise.calculator.Math. The second is some
        // html markup for displaying the function name
        String one_param = "exp,e<sup>x</sup>,acos,acos,asin,asin,atan,atan,log,ln,cos,cos,sin,sin,tan,tan,ceil,ceil,floor,floor,rint,rint,round,round,sqrt,&#8730;,toDegrees,deg,toRadians,rad,factorial,x!";
        makeOneParamButtons(one_param);
        String[] fparams = one_param.split(",");
        for (int i = 0; i < fparams.length; i = i + 2) {
            function_panel.add(buttons.get(fparams[i]), cons);
            ++cons.x;
            if (cons.x == 4) {
                cons.x %= 4;
                ++cons.y;
            }
        }

        String two_param = "atan2,atan2,max,max,min,min,pow,y<sup>x</sup>";
        makeTwoParamButtons(two_param);
        fparams = two_param.split(",");
        for (int i = 0; i < fparams.length; i = i + 2) {
            function_panel.add(buttons.get(fparams[i]), cons);
            ++cons.x;
            if (cons.x == 4) {
                cons.x %= 4;
                ++cons.y;
            }
        }

        String zero_param = "random,rndm";
        makeZeroParamButtons(zero_param);
        cons.x = 0;
        ++cons.y;
        function_panel.add(buttons.get("random"), cons);

        euler = new RectangleButton("<html><i>e");
        euler.setToolTipText("Euler's e");
        euler.setActionCommand("e");
        buttons.put("euler", euler);
        euler.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                x_register.setText(Double.toString(java.lang.Math.E));
            }
        }
       );
        euler.addActionListener(label_listener);

        pi = new RectangleButton("<html>&#960;");
        pi.setToolTipText("Pi");
        pi.setActionCommand("pi");
        buttons.put("pi", pi);
        pi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                x_register.setText(Double.toString(java.lang.Math.PI));
            }
        }
       );
        pi.addActionListener(label_listener);

        strict = new RectangleToggleButton("<html>strict");
        strict.setToolTipText("Use strict math mode");
        strict.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                RectangleToggleButton btn = (RectangleToggleButton) ae.getSource();
                _strict = btn.isSelected();
            }
        }
       );
        ++cons.x;
        function_panel.add(euler, cons);
        ++cons.x;
        function_panel.add(pi, cons);
        ++cons.x;
        function_panel.add(strict, cons);

        return function_panel;
    }

    private void addActionListeners() {
        // add action listeners for operation panel
        enter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                t_register.setText(z_register.getText());
                z_register.setText(y_register.getText());
                y_register.setText(x_register.getText());
                x_register.selectAll();
                x_register.requestFocus();
            }
        }
       );
        enter.addActionListener(label_listener);

        number_panel.addActionListener(label_listener);

        // x-register actions
        InputMap inputMap = x_register.getInputMap();
        ActionMap actionMap = x_register.getActionMap();
        setInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, inputMap);
        setActionMap(actionMap);

        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "enter");
        actionMap.put("enter", new AbstractAction("enter") {
            public void actionPerformed(ActionEvent ae) {
                enter.doClick();
            }
        } );

        inputMap.put(KeyStroke.getKeyStroke('+'), "plus");
        actionMap.put("plus", new AbstractAction("plus") {
            public void actionPerformed(ActionEvent ae) {
                plus.doClick();
            }
        } );

        inputMap.put(KeyStroke.getKeyStroke('-'), "minus");
        actionMap.put("minus", new AbstractAction("minus") {
            public void actionPerformed(ActionEvent ae) {
                minus.doClick();
            }
        } );

        inputMap.put(KeyStroke.getKeyStroke('*'), "multiply");
        actionMap.put("multiply", new AbstractAction("multiply") {
            public void actionPerformed(ActionEvent ae) {
                multiply.doClick();
            }
        } );

        inputMap.put(KeyStroke.getKeyStroke('/'), "divide");
        actionMap.put("divide", new AbstractAction("divide") {
            public void actionPerformed(ActionEvent ae) {
                divide.doClick();
            }
        } );

        inputMap.put(KeyStroke.getKeyStroke('\\'), "modulus");
        actionMap.put("modulus", new AbstractAction("modulus") {
            public void actionPerformed(ActionEvent ae) {
                modulus.doClick();
            }
        } );

        inputMap.put(KeyStroke.getKeyStroke('|'), "or");
        actionMap.put("or", new AbstractAction("or") {
            public void actionPerformed(ActionEvent ae) {
                or.doClick();
            }
        } );

        inputMap.put(KeyStroke.getKeyStroke('&'), "and");
        actionMap.put("and", new AbstractAction("and") {
            public void actionPerformed(ActionEvent ae) {
                and.doClick();
            }
        } );

        inputMap.put(KeyStroke.getKeyStroke('^'), "xor");
        actionMap.put("xor", new AbstractAction("xor") {
            public void actionPerformed(ActionEvent ae) {
                xor.doClick();
            }
        } );

        inputMap.put(KeyStroke.getKeyStroke('~'), "not");
        actionMap.put("not", new AbstractAction("not") {
            public void actionPerformed(ActionEvent ae) {
                not.doClick();
            }
        } );

    }

    // action listener for those functions that take no parameters --
    // this might eventually need some work, right now, only one function
    // (random) uses no parameters, although I should be able to generate large
    // random numbers with big ints.
    private ActionListener zero_op_listener = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            String operation = ae.getActionCommand();
            if (recording) {
                macro.add(operation);
            }
            String type = "double";
            final Op op = new Op(operation, type);
            op.setStrict(_strict);

            CalculatorPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Number result = null;
            String answer = "";
            try {
                Num num = op.calculate();
                result = num.getValue();
            } catch (ArithmeticException e) {
                answer = "Error: " + e.getMessage();
            }

            if (!answer.startsWith("Error")) {
                switch (current_mode) {
                    case FLOAT:
                        double d = result.doubleValue();
                        answer = Double.toString(d);
                        break;
                    default:
                        int a = result.intValue();
                        answer = convertToBase(Integer.toString(a), BASE_10, current_base);
                }
            }
            try {
                last_x_value = x_register.getText();
                x_register.setText(answer);
                x_register.requestFocus();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CalculatorPanel.this.setCursor(Cursor.getDefaultCursor());
            }
        }
    };

    // action listener for those function that take one parameter
    private ActionListener unary_op_listener = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            String operation = ae.getActionCommand();
            if (recording) {
                macro.add(operation);
            }
            String xs = x_register.getText();
            if (xs == null || xs.length() == 0) {
                return;
            }
            String type = "int";
            switch (current_mode) {
                case FLOAT:
                    type = "double";
                    break;
                case BIGINT:
                    type = "bigint";
                    break;
                case BIGDECIMAL:
                    type = "bigdecimal";
                    break;
                case INT:
                default:
                    type = "int";
                    break;
            }

            // convert to base 10 to do the math
            xs = convertToBase(xs, current_base, BASE_10);
            final Op op = new Op(operation, type);
            op.setStrict(_strict);
            op.addNum(new Num(xs));

            CalculatorPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Number result = null;
            String answer = "";
            try {
                Num num = op.calculate();
                result = num.getValue();
            } catch (Exception e) {
                answer = "Error: " + e.getMessage();
            }

            if (!answer.startsWith("Error")) {
                switch (current_mode) {
                    case BIGDECIMAL:
                        answer = result.toString();
                        break;
                    case FLOAT:
                        double d = result.doubleValue();
                        answer = Double.toString(d);
                        break;
                    case BIGINT:
                        if (result instanceof BigInteger) {
                            answer = ((BigInteger) result).toString(current_base);
                        } else {
                            answer = "Error: operation not allowed in BI mode";
                        }
                        break;
                    case INT:
                    default:
                        int a = result.intValue();
                        answer = Integer.toString(a);
                }

                // convert the answer back from base 10 to the current base
                answer = convertToBase(answer, BASE_10, current_base);
            }
            try {
                last_x_value = x_register.getText();
                x_register.setText(answer);
                x_register.requestFocus();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CalculatorPanel.this.setCursor(Cursor.getDefaultCursor());
            }
        }
    };

    // action listener for those functions that take two parameters
    private ActionListener binary_op_listener = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            String operation = ae.getActionCommand();
            if (recording) {
                macro.add(operation);
            }
            String xs = x_register.getText();
            String ys = y_register.getText();
            if (xs == null || xs.length() == 0) {
                return;
            }
            if (ys == null || ys.length() == 0) {
                return;
            }
            String type = "int";
            switch (current_mode) {
                case FLOAT:
                    type = "double";
                    break;
                case BIGINT:
                    type = "bigint";
                    break;
                case BIGDECIMAL:
                    type = "bigdecimal";
                    break;
                case INT:
                default:
                    type = "int";
                    break;
            }

            // convert to base 10 to do the math
            xs = convertToBase(xs, current_base, BASE_10);
            ys = convertToBase(ys, current_base, BASE_10);

            final Op op = new Op(operation, type);
            op.setStrict(_strict);
            op.addNum(new Num(ys));
            op.addNum(new Num(xs));

            CalculatorPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Number result = null;
            String answer = "";
            try {
                Num num = op.calculate();
                result = num.getValue();
            } catch (ArithmeticException e) {
                answer = "Error: " + e.getMessage();
                e.printStackTrace();
            }

            if (!answer.startsWith("Error")) {
                switch (current_mode) {
                    case BIGDECIMAL:
                        answer = result.toString();
                        break;
                    case BIGINT:
                        answer = result.toString();
                        if (answer.indexOf('.') > 0) {
                            answer = answer.substring(0, answer.indexOf('.'));
                        }
                        break;
                    case FLOAT:
                        double d = result.doubleValue();
                        answer = Double.toString(d);
                        break;
                    default:
                        int a = result.intValue();
                        answer = Integer.toString(a);
                }

                // convert the answer from base 10 back to the current base
                answer = convertToBase(answer, BASE_10, current_base);
            }
            try {
                if (answer.startsWith("Error")) {
                    return;
                }
                RegisterDocument rd = new RegisterDocument(current_base, current_mode);
                rd.insertString(0, answer, null);
                last_x_value = x_register.getText();
                x_register.setText(answer);
                y_register.setText(z_register.getText());
                z_register.setText(t_register.getText());
                t_register.setText("");
                x_register.requestFocus();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                CalculatorPanel.this.setCursor(Cursor.getDefaultCursor());
            }
        }
    };

    private ActionListener label_listener = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            if (recording) {
                String cmd = ae.getActionCommand();
                if (cmd.equals("x") || cmd.equals("y") || cmd.equals("z") || cmd.equals("t")) {
                    while (true) {
                        if (macro.size() == 0) {
                            break;
                        }
                        String c = (String) macro.get(macro.size() - 1);
                        if (Character.isDigit(c.charAt(0))) {
                            macro.remove(macro.size() - 1);
                        } else {
                            break;
                        }
                    }
                    macro.add(cmd);
                } else {
                    macro.add(ae.getActionCommand());
                }
            }
        }
    };

    /**
     * Executes stored functions, constants, and conversions.
     */
    private ActionListener functionPlayer = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            try {
                String old_last_x = last_x_value;
                if (old_last_x == null || old_last_x.length() == 0) {
                    old_last_x = x_register.getText();
                }
                String cmd = ae.getActionCommand();
                String function = cmd + ".calc";
                File calc_dir = new File(System.getProperty("calc.home"), ".calc");
                File f = new File(calc_dir, function);
                FunctionReader fr = new FunctionReader(f);
                String func = fr.getFunction();
                boolean is_constant = fr.isConstant();
                BufferedReader br = new BufferedReader(new StringReader(func));
                String line = br.readLine();
                while (line != null) {
                    // constants are special functions that have only one line
                    // that contains a number
                    if (is_constant) {
                        line = line.trim();
                        for (int i = 0; i < line.length(); i++) {
                            number_panel.doClick(line.substring(i, i + 1));
                        }
                        // x_register.setText( line );
                        break;
                    }
                    line = line.trim().toLowerCase();
                    AbstractButton btn = (AbstractButton) buttons.get(line);
                    if (btn != null) {
                        btn.doClick();
                    } else {
                        number_panel.doClick(line);
                    }
                    line = br.readLine();
                }
                br.close();
                last_x_value = old_last_x;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    ActionListener chs_listener = new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            // can change sign for the whole number in X or for just the
            // exponent, attempt to insert the string at the current caret
            // position and let the document model sort it out.
            String previous = x_register.getText();
            try {
                JButton b = (JButton) ae.getSource();
                Document doc = x_register.getDocument();
                doc.insertString(x_register.getCaret().getDot(), b.getActionCommand(), null);
            } catch (Exception e) {
                x_register.setText(previous);
            }
            x_register.requestFocus();
        }
    };
}