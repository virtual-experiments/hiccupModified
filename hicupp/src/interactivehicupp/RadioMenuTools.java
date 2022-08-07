package interactivehicupp;

import java.awt.*;

public class RadioMenuTools {
  public interface RadioMenuEventListener {
    void itemChosen(int index);
  }
  
  public static Menu createRadioMenu(final String[] labels,
                                     final int initialChoice,
                                     final RadioMenuEventListener listener) {
    Menu menu = new Menu();
    final CheckboxMenuItem[] items = new CheckboxMenuItem[labels.length];
    for (int i = 0; i < items.length; i++) {
      final int index = i;
      CheckboxMenuItem item = new CheckboxMenuItem(labels[i]);
      item.addItemListener(e -> {
        for (int j = 0; j < items.length; j++)
          items[j].setState(index == j);
        listener.itemChosen(index);
      });
      items[i] = item;
      menu.add(item);
    }
    items[initialChoice].setState(true);
    return menu;
  }
}
