package hicupp;

import java.awt.*;

public final class LayoutTools {
  /**
   * Adds component to container as the container's only child.
   */
  public static void addWithMargin(Container container, Component component, int marginSize) {
    GridBagLayout layout = new GridBagLayout();
    container.setLayout(layout);
    container.add(component);
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(marginSize, marginSize, marginSize, marginSize);
    constraints.weightx = 1;
    constraints.weighty = 1;
    layout.setConstraints(component, constraints);
  }
}
