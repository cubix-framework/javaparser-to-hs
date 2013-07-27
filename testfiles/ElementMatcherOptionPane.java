package elementmatcher;

import ise.java.awt.LambdaLayout;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.gui.ColorWellButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class ElementMatcherOptionPane extends AbstractOptionPane {

    private List<ProviderPanel> panels = new ArrayList<ProviderPanel>();

    public ElementMatcherOptionPane() {
        super("elementmatcher");
    }

    @Override
    protected void _init() {
        for (Iterator<ElementProvider<?>> it=ElementMatcherPlugin.getInstance().getProviderManager().getProviders(); it.hasNext(); ) {
            final ElementProvider<?> provider = it.next();
            final ProviderPanel providerPanel = new ProviderPanel(provider);
            panels.add(providerPanel);
            providerPanel.updateState();
            addComponent(providerPanel, GridBagConstraints.HORIZONTAL);
        }
    }

    @Override
    protected void _save() {
        for (ProviderPanel panel: panels) {
            panel.save();
        }
    }

    private class ProviderPanel extends JPanel {

        private final ElementProvider<?> provider;
        private final JCheckBox enabled;
        private final ColorWellButton color;
        private final JCheckBox underline;

        private ProviderPanel(ElementProvider<?> provider) {
            super(new LambdaLayout());
            this.provider = provider;
            setBorder(new TitledBorder(provider.getName()));
            // create components
            enabled = new JCheckBox("enabled", provider.isEnabled());
            color = new ColorWellButton(provider.getColor());
            underline = new JCheckBox("underline", provider.isUnderline());
            enabled.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    updateState();
                }
            });
            // lay out
            add(enabled, "0,0,2,,W");
            add(new JLabel("color:"), "0,1,,,W");
            add(color, "1,1,,,W");
            add(underline, "0,2,2,,W");
        }

        private void updateState() {
            color.setEnabled(enabled.isSelected());
            underline.setEnabled(enabled.isSelected());
        }

        private void save() {
            provider.setEnabled(enabled.isSelected());
            provider.setColor(color.getSelectedColor());
            provider.setUnderline(underline.isSelected());
        }

    }

}