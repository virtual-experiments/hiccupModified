package interactivehicupp;

import java.awt.*;
import java.util.stream.IntStream;

public class RadioMenuTools extends Menu {
  public interface RadioMenuEventListener {
    void itemChosen(int index);
  }

  private CheckboxMenuItem[] items;

  public static RadioMenuTools createRadioMenu(final String[] labels,
                                     final int initialChoice,
                                     final RadioMenuEventListener listener) {
    RadioMenuTools menu = new RadioMenuTools();
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
    menu.setItems(items);

    return menu;
  }

  public void setItems(CheckboxMenuItem[] items) {
    this.items = items;
  }

  public void setChosenItem(int index) {
    int bound = items.length;
    for (int j = 0; j < bound; j++) {
      items[j].setState(index == j);
    }
  }
}
