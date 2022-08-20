package interactivehicupp;

import hicupp.FunctionMaximizer;
import hicupp.algorithms.ga.*;
import hicupp.algorithms.sa.*;

import javax.swing.*;
import java.awt.*;

public final class AlgorithmParametersUI {

    public static void createParams(TreeDocument treeDocument) {
        switch (treeDocument.getAlgorithmIndex()) {
            case FunctionMaximizer.ANNEALING_ALGORITHM_INDEX -> annealingUI(treeDocument);
            case FunctionMaximizer.GENETIC_ALGORITHM_INDEX -> geneticUI(treeDocument);
            case FunctionMaximizer.GRADIENT_ALGORITHM_INDEX -> gradientUI(treeDocument);
            default -> treeDocument.setAlgorithmParameters(null);
        }
    }

    private static void annealingUI(TreeDocument treeDocument) {
        Frame frame = treeDocument.getFrame();

        // initial variables
        int initNumberOfIterations = 20;
        boolean initConverge = true;
        int initMaxEquals = 5;

        if (treeDocument.getAlgorithmParameters() instanceof SimulatedAnnealingParameters parameters) {
            initNumberOfIterations = parameters.numberOfIterations();
            initConverge = parameters.convergeAtMaxEquals();
            initMaxEquals = parameters.maxEquals();
        }

        // UI
        final Dialog dialog = new Dialog(frame, "Simulated Annealing", true);
        dialog.setLayout(new SpringLayout());

        final Label labelIterations = new Label("Number of iterations: ", Label.RIGHT);
        final TextField fieldIterations = new TextField(Integer.toString(initNumberOfIterations));

        final Checkbox checkboxConverge = new Checkbox("Stop when solution does not improve", true);
        checkboxConverge.setState(initConverge);

        final Label labelMaxEquals = new Label("After number of iterations: ", Label.RIGHT);
        final TextField fieldMaxEquals = new TextField(Integer.toString(initMaxEquals));
        fieldMaxEquals.setEnabled(initConverge);

        final Button ok = new Button("Ok");
        final Button cancel = new Button("Cancel");

        // events
        cancel.addActionListener(e -> dialog.dispose());

        checkboxConverge.addItemListener(e ->
                fieldMaxEquals.setEnabled(checkboxConverge.getState()));

        ok.addActionListener(e -> {
            try {
                final int numberOfIterations = Integer.parseInt(fieldIterations.getText());
                final boolean convergeAtMaxEquals = checkboxConverge.getState();
                final int maxEquals = Integer.parseInt(fieldMaxEquals.getText());

                if (numberOfIterations <= 0 || maxEquals <= 0) {
                    MessageBox.showMessage(frame, "Number of iterations/converges must be greater than 0.", "Interactive Hicupp");
                } else if (numberOfIterations < maxEquals && convergeAtMaxEquals) {
                    MessageBox.showMessage(frame, "The number of converges must be smaller than the number of iterations.", "Interactive Hicupp");
                } else {
                    treeDocument.setAlgorithmParameters(
                            new SimulatedAnnealingParameters(
                                    numberOfIterations,
                                    convergeAtMaxEquals,
                                    maxEquals));

                    dialog.dispose();
                }
            } catch (NumberFormatException exception) {
                MessageBox.showMessage(frame, "What you entered is not a full number.", "Interactive Hicupp");
            }
        });

        // organisation
        dialog.add(labelIterations);
        dialog.add(fieldIterations);
        dialog.add(new Label()); // keeps checkbox to the right
        dialog.add(checkboxConverge);
        dialog.add(labelMaxEquals);
        dialog.add(fieldMaxEquals);
        dialog.add(cancel);
        dialog.add(ok);

        makeCompactGrid(dialog,
                4, 2,
                6, 6,
                6, 6);

        showDialog(dialog, frame);
    }

    private static void geneticUI(TreeDocument treeDocument) {
        final int populationSize = 10;
        final int maxGenerations = 200;
        final int mutationsPerGen = 2;
        final int spawnsPerGen = 10;
        final boolean convergeAtMaxEquals = true;
        final int maxEquals = 3;

        treeDocument.setAlgorithmParameters(
                new GeneticAlgorithmParameters(
                        populationSize,
                        maxGenerations,
                        mutationsPerGen,
                        spawnsPerGen,
                        convergeAtMaxEquals,
                        maxEquals));
    }

    private static void gradientUI(TreeDocument treeDocument) {

    }

    private static void showDialog(Dialog dialog, Frame frame) {
        dialog.pack();
        dialog.setResizable(false);
        Dimension dialogSize = dialog.getSize();
        Dimension screenSize = frame.getToolkit().getScreenSize();
        dialog.setLocation((screenSize.width - dialogSize.width) / 2,
                (screenSize.height - dialogSize.height) / 2);
        dialog.setVisible(true);
    }

    /*
     * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions
     * are met:
     *
     *   - Redistributions of source code must retain the above copyright
     *     notice, this list of conditions and the following disclaimer.
     *
     *   - Redistributions in binary form must reproduce the above copyright
     *     notice, this list of conditions and the following disclaimer in the
     *     documentation and/or other materials provided with the distribution.
     *
     *   - Neither the name of Oracle or the names of its
     *     contributors may be used to endorse or promote products derived
     *     from this software without specific prior written permission.
     *
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
     * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
     * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
     * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
     * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
     * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
     * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
     * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
     * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
     * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
     * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     */

    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(
            int row, int col,
            Container parent,
            int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    private static void makeCompactGrid(Container parent,
                                       int rows, int cols,
                                       int initialX, int initialY,
                                       int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent, cols).
                                getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height,
                        getConstraintsForCell(r, c, parent, cols).
                                getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}
