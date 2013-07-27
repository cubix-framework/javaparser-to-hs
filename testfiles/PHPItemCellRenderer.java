package gatchan.phpparser.project.itemfinder;

import javax.swing.*;
import java.awt.*;

/** @author Matthieu Casanova */
public final class PHPItemCellRenderer implements ListCellRenderer {
  private final PHPItemCellPanel comp = new PHPItemCellPanel();

  private String searchString;

  public Component getListCellRendererComponent(JList list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus) {
    if (value instanceof PHPItem) {
      comp.setItem((PHPItem) value, searchString);
    } else if (value instanceof String) {
      comp.setString((String) value,searchString);
    } else {
      comp.setString(value.toString(), searchString);
    }
    if (isSelected) {
      comp.setBackground(list.getSelectionBackground());
    } else {
      comp.setBackground(list.getBackground());
    }
    return comp;
  }

  public void setSearchString(String searchString) {
    this.searchString = searchString;
  }

  private static final class PHPItemCellPanel extends JPanel {
    private final JLabel label1 = new JLabel();
    private final JLabel label2 = new JLabel();

    private PHPItemCellPanel() {
      super(new FlowLayout(FlowLayout.LEFT));

      label2.setForeground(Color.gray);
      add(label1);
      add(label2);
    }

    private void setItem(PHPItem phpItem, String searchString) {
      String text;
      String name = phpItem.getName();
      text = getHtmlText(searchString, name);
      label1.setIcon(phpItem.getIcon());
      label1.setText(text);
      label2.setText(phpItem.getPath());
    }

    private static String getHtmlText(String searchString, String name) {
      String text;
      if (searchString == null) {
        text = name;
      } else {
        int i = name.toLowerCase().indexOf(searchString);
        int searchStringLength = searchString.length();
        if (i == 0) {
          text = "<html><font color='blue'><b>" + name.substring(0,
                                                                 searchStringLength) + "</b></font>" + name.substring(searchStringLength) + "</html>";
        } else if (i == -1) {
          text = name;
        } else {
          String s = name.substring(0, i);
          String s2 = name.substring(i, i + searchStringLength);
          String s3 = name.substring(i + searchStringLength);
          text = "<html>" + s + "<font color='blue'><b>" + s2 + "</b></font>" + s3 + "</html>";
        }
      }
      return text;
    }

    private void setString(String name, String searchString) {
      String text;
      text = getHtmlText(searchString, name);

      label1.setIcon(null);
      label1.setText(text);
      label2.setText(null);
    }
  }
}
