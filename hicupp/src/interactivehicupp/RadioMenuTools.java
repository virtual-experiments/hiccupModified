package interactivehicupp;

import javax.swing.*;

public class RadioMenuTools extends JMenu {
  public interface RadioMenuEventListener {
    void itemChosen(int index);
  }

  private JCheckBoxMenuItem[] items;

  public static RadioMenuTools createRadioMenu(final String[] labels,
                                     final int initialChoice,
                                     final RadioMenuEventListener listener) {
    RadioMenuTools menu = new RadioMenuTools();
    final JCheckBoxMenuItem[] items = new JCheckBoxMenuItem[labels.length];

    for (int i = 0; i < items.length; i++) {
      final int index = i;
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(labels[i]);
      item.addActionListener(e -> {
        for (int j = 0; j < items.length; j++)
          items[j].setState(index == j);
        listener.itemChosen(index);
      });
      items[i] = item;
      menu.add(item);
    }

    items[initialChoice].setState(true);
    menu.setItems(items);

    return menu;
  }

  public void setItems(JCheckBoxMenuItem[] items) {
    this.items = items;
  }

  public void setChosenItem(int index) {
    int bound = items.length;
    for (int j = 0; j < bound; j++) {
      items[j].setState(index == j);
    }
  }
}
