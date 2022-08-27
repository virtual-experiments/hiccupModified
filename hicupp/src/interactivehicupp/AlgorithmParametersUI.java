package interactivehicupp;

import hicupp.FunctionMaximizer;
import hicupp.algorithms.ga.*;
import hicupp.algorithms.gd.GradientDescentParameters;
import hicupp.algorithms.sa.*;

import javax.swing.*;
import java.awt.*;

public final class AlgorithmParametersUI {

    public static void createParams(TreeDocument treeDocument) {
        switch (treeDocument.getAlgorithmIndex()) {
            case FunctionMaximizer.ANNEALING_ALGORITHM_INDEX -> AnnealingUI.annealingUI(treeDocument);
            case FunctionMaximizer.GENETIC_ALGORITHM_INDEX -> geneticUI(treeDocument);
            case FunctionMaximizer.GRADIENT_ALGORITHM_INDEX -> gradientUI(treeDocument);
            default -> treeDocument.setAlgorithmParameters(null);
        }
    }

    private static final class AnnealingUI {

        private static Dialog dialog;

        private static TextField fieldIterations;

        private static Checkbox checkboxConverge;

        private static Label labelMaxEquals;
        private static TextField fieldMaxEquals;

        private static Label labelMinEvaluations;
        private static Label labelMinTime;
        private static Label labelMaxEvaluations;
        private static Label labelMaxTime;

        private static long evaluationTime;

        private static void annealingUI(TreeDocument treeDocument) {
            Frame frame = treeDocument.getFrame();
            evaluationTime = ((AbstractNodeView) treeDocument.getPointsSourceProvider().getRoot()).getEvaluationTime();

            // initial variables
            final int initNumberOfIterations;
            final boolean initConverge;
            final int initMaxEquals;

            if (treeDocument.getAlgorithmParameters() instanceof SimulatedAnnealingParameters parameters) {
                initNumberOfIterations = parameters.numberOfIterations();
                initConverge = parameters.convergeAtMaxEquals();
                initMaxEquals = parameters.maxEquals();
            } else {
                initNumberOfIterations = 100;
                initConverge = true;
                initMaxEquals = 20;
            }

            // UI
            dialog = new Dialog(frame, "Simulated Annealing", true);
            dialog.setLayout(new SpringLayout());

            Label labelIterations = new Label("Number of iterations: ", Label.RIGHT);
            fieldIterations = new TextField(Integer.toString(initNumberOfIterations), 29);

            checkboxConverge = new Checkbox("Stop when solution does not improve", initConverge);

            labelMaxEquals = new Label("After number of iterations: ", Label.RIGHT);
            labelMaxEquals.setEnabled(initConverge);
            fieldMaxEquals = new TextField(Integer.toString(initMaxEquals), 29);
            fieldMaxEquals.setEnabled(initConverge);

            {
                String minEvaluations = (initConverge) ? Integer.toString(initMaxEquals + 1) : "N/A";
                labelMinEvaluations = new Label("Minimum number of evaluations: " + minEvaluations, Label.RIGHT);

                String minTime = (initConverge) ? Double.toString((initMaxEquals + 1) * evaluationTime / 1000d) : "N/A";
                labelMinTime = new Label("Minimum time required: " + minTime + " s", Label.LEFT);

                String maxEvaluations = Integer.toString(initNumberOfIterations + 1);
                labelMaxEvaluations = new Label("Maximum number of evaluations: " + maxEvaluations, Label.RIGHT);

                String maxTime = Double.toString((initNumberOfIterations + 1) * evaluationTime / 1000d);
                labelMaxTime = new Label("Maximum time required: " + maxTime + " s", Label.LEFT);
            }

            final Button ok = new Button("Ok");
            final Button cancel = new Button("Cancel");

            // events
            fieldIterations.addTextListener(e -> getMaximumEstimates());

            fieldMaxEquals.addTextListener(e -> getMinimumEstimates());

            cancel.addActionListener(e -> {
                dialog.dispose();

                treeDocument.setAlgorithmParameters(
                        new SimulatedAnnealingParameters(
                                initNumberOfIterations,
                                initConverge,
                                initMaxEquals
                        ));
            });

            checkboxConverge.addItemListener(e -> {
                fieldMaxEquals.setEnabled(checkboxConverge.getState());
                labelMaxEquals.setEnabled(checkboxConverge.getState());

                getMinimumEstimates();
            });

            ok.addActionListener(e -> {
                try {
                    final int numberOfIterations = Integer.parseInt(fieldIterations.getText());
                    final boolean convergeAtMaxEquals = checkboxConverge.getState();
                    final int maxEquals = Integer.parseInt(fieldMaxEquals.getText());

                    if (numberOfIterations <= 0 || maxEquals < 0) {
                        MessageBox.showMessage(frame, "Number of iterations/converges must be greater than 0.",
                                "Interactive Hicupp");
                    } else if (numberOfIterations < maxEquals && convergeAtMaxEquals) {
                        MessageBox.showMessage(frame,
                                "The number of iterations the solution stayed the same must be smaller than the " +
                                        "number of total iterations.",
                                "Interactive Hicupp");
                    } else {
                        treeDocument.setAlgorithmParameters(
                                new SimulatedAnnealingParameters(
                                        numberOfIterations,
                                        convergeAtMaxEquals,
                                        maxEquals));

                        dialog.dispose();
                    }
                } catch (NumberFormatException exception) {
                    MessageBox.showMessage(frame, "What you entered is not a full number.",
                            "Interactive Hicupp");
                }
            });

            // organisation
            dialog.add(labelIterations);
            dialog.add(fieldIterations);
            dialog.add(checkboxConverge);
            dialog.add(new Label()); // keeps checkbox to the left
            dialog.add(labelMaxEquals);
            dialog.add(fieldMaxEquals);
            dialog.add(labelMinEvaluations);
            dialog.add(labelMinTime);
            dialog.add(labelMaxEvaluations);
            dialog.add(labelMaxTime);
            dialog.add(ok);
            dialog.add(cancel);

            makeCompactGrid(dialog, 6);

            showDialog(dialog, frame);
        }

        private static void getMinimumEstimates() {
            String minEvaluations = "N/A";
            String minTime = "N/A";
            try {
                if (checkboxConverge.getState()) {
                    int maxEquals = Integer.parseInt(fieldMaxEquals.getText()) + 1;
                    double time = maxEquals * evaluationTime / 1000d;

                    minEvaluations = Integer.toString(maxEquals);
                    minTime = Double.toString(time);
                }
            } catch (NumberFormatException ignore) { }
            finally {
                labelMinEvaluations.setText("Minimum number of evaluations: " + minEvaluations);
                labelMinTime.setText("Minimum time required: " + minTime + " s");
            }
        }

        private static void getMaximumEstimates() {
            String maxEvaluations = "N/A";
            String maxTime = "N/A";
            try {
                int iterations = Integer.parseInt(fieldIterations.getText()) + 1;
                double time = iterations * evaluationTime / 1000d;

                maxEvaluations = Integer.toString(iterations);
                maxTime = Double.toString(time);
            } catch (NumberFormatException ignore) {
            } finally {
                labelMaxEvaluations.setText("Maximum number of evaluations: " + maxEvaluations);
                labelMaxTime.setText("Maximum time required: " + maxTime + " s");
            }
        }
    }

    private static void geneticUI(TreeDocument treeDocument) {
        Frame frame = treeDocument.getFrame();

        // initial variables
        final int initPop;
        final int initGens;
        final int initMutations;
        final int initSpawns;
        final boolean initConverge;
        final int initMaxEquals;

        if (treeDocument.getAlgorithmParameters() instanceof GeneticAlgorithmParameters parameters) {
            initPop = parameters.populationSize();
            initGens = parameters.maxGenerations();
            initMutations = parameters.mutationsPerGen();
            initSpawns = parameters.spawnsPerGen();
            initConverge = parameters.convergeAtMaxEquals();
            initMaxEquals = parameters.maxEquals();
        } else {
            initPop = 20;
            initGens = 30;
            initMutations = 5;
            initSpawns = 10;
            initConverge = false;
            initMaxEquals = 5;
        }

        // UI
        final Dialog dialog = new Dialog(frame, "Genetic Algorithm", true);
        dialog.setLayout(new SpringLayout());

        final Label labelPopulation = new Label("Population size: ", Label.RIGHT);
        final TextField fieldPopulation = new TextField(Integer.toString(initPop), 29);

        final Label labelGens = new Label("Number of generations: ", Label.RIGHT);
        final TextField fieldGens = new TextField(Integer.toString(initGens), 29);

        final Label labelMutations = new Label("Mutations per generation: ", Label.RIGHT);
        final TextField fieldMutations = new TextField(Integer.toString(initMutations), 29);

        final Label labelSpawns = new Label("Spawns per generation: ", Label.RIGHT);
        final TextField fieldSpawns = new TextField(Integer.toString(initSpawns), 29);

        final Checkbox checkboxConverge = new Checkbox("Stop when solution does not improve", initConverge);

        final Label labelMaxEquals = new Label("After number of iterations: ", Label.RIGHT);
        labelMaxEquals.setEnabled(initConverge);
        final TextField fieldMaxEquals = new TextField(Integer.toString(initMaxEquals), 29);
        fieldMaxEquals.setEnabled(initConverge);

        final Button ok = new Button("Ok");
        final Button cancel = new Button("Cancel");

        // events
        cancel.addActionListener(e -> {
            dialog.dispose();

            treeDocument.setAlgorithmParameters(
                    new GeneticAlgorithmParameters(
                            initPop,
                            initGens,
                            initMutations,
                            initSpawns,
                            initConverge,
                            initMaxEquals));
        });

        checkboxConverge.addItemListener(e -> {
                fieldMaxEquals.setEnabled(checkboxConverge.getState());
                labelMaxEquals.setEnabled(checkboxConverge.getState());
        });

        ok.addActionListener(e -> {
            try {
                final int popSize = Integer.parseInt(fieldPopulation.getText());
                final int gens = Integer.parseInt(fieldGens.getText());
                final int mutations = Integer.parseInt(fieldMutations.getText());
                final int spawns = Integer.parseInt(fieldSpawns.getText());
                final boolean converge = checkboxConverge.getState();
                final int maxEquals = Integer.parseInt(fieldMaxEquals.getText());

                if (popSize <= 0 || gens <= 0)
                    MessageBox.showMessage(frame,
                            "Number of generations / population size must be greater than 0.",
                            "Interactive Hicupp");
                else if (mutations < 0 || spawns < 0 || maxEquals < 0)
                    MessageBox.showMessage(frame,
                            "All parameters need to be positive.",
                            "Interactive Hicupp");
                else if (converge && popSize < maxEquals)
                    MessageBox.showMessage(frame,
                            "The number of generations the fittest stayed the same must be smaller than the " +
                                    "number of total generations.",
                            "Interactive Hicupp");
                else {
                    treeDocument.setAlgorithmParameters(
                            new GeneticAlgorithmParameters(
                                    popSize,
                                    gens,
                                    mutations,
                                    spawns,
                                    converge,
                                    maxEquals));

                    dialog.dispose();
                }
            } catch (NumberFormatException exception) {
                MessageBox.showMessage(frame, "What you entered is not a full number.",
                        "Interactive Hicupp");
            }
        });

        // organisation
        dialog.add(labelPopulation);
        dialog.add(fieldPopulation);
        dialog.add(labelGens);
        dialog.add(fieldGens);
        dialog.add(labelMutations);
        dialog.add(fieldMutations);
        dialog.add(labelSpawns);
        dialog.add(fieldSpawns);
        dialog.add(checkboxConverge);
        dialog.add(new Label()); // keeps checkbox on the left
        dialog.add(labelMaxEquals);
        dialog.add(fieldMaxEquals);
        dialog.add(ok);
        dialog.add(cancel);

        makeCompactGrid(dialog, 7);

        showDialog(dialog, frame);
    }

    private static void gradientUI(TreeDocument treeDocument) {
        Frame frame = treeDocument.getFrame();

        // initial variables
        final int initIterations;
        final int initSolutions;
        final boolean initConverge;
        final int initMaxEquals;

        if (treeDocument.getAlgorithmParameters() instanceof GradientDescentParameters parameters) {
            initIterations = parameters.maxIterations();
            initSolutions = parameters.numberOfSolutions();
            initConverge = parameters.convergeAtMaxEquals();
            initMaxEquals = parameters.maxEquals();
        } else {
            initIterations = 100;
            initSolutions = 15;
            initConverge = true;
            initMaxEquals = 10;
        }

        // UI
        final Dialog dialog = new Dialog(frame, "Gradient Descent", true);
        dialog.setLayout(new SpringLayout());

        final Label labelIterations = new Label("Number of iterations: ", Label.RIGHT);
        final TextField fieldIterations = new TextField(Integer.toString(initIterations), 29);

        final Label labelSolutions = new Label("Number of solutions: ", Label.RIGHT);
        final TextField fieldSolutions = new TextField(Integer.toString(initSolutions), 29);

        final Checkbox checkboxConverge =
                new Checkbox("Stop when the solution does not improve", initConverge);

        final Label labelMaxEquals = new Label("After number of iterations: ", Label.RIGHT);
        labelMaxEquals.setEnabled(initConverge);
        final TextField fieldMaxEquals = new TextField(Integer.toString(initMaxEquals), 29);
        fieldMaxEquals.setEnabled(initConverge);

        final Button ok = new Button("Ok");
        final Button cancel = new Button("Cancel");

        // events
        cancel.addActionListener(e -> {
            dialog.dispose();

            treeDocument.setAlgorithmParameters(
                    new GradientDescentParameters(
                            initIterations,
                            initSolutions,
                            initConverge,
                            initMaxEquals
            ));
        });

        checkboxConverge.addItemListener(e -> {
            labelMaxEquals.setEnabled(checkboxConverge.getState());
            fieldMaxEquals.setEnabled(checkboxConverge.getState());
        });

        ok.addActionListener(e -> {
            try {
                final int iterations = Integer.parseInt(fieldIterations.getText());
                final int numberOfSolutions = Integer.parseInt(fieldSolutions.getText());
                final boolean converge = checkboxConverge.getState();
                final int maxEquals = Integer.parseInt(fieldMaxEquals.getText());

                if (iterations <= 0 || numberOfSolutions <= 0)
                    MessageBox.showMessage(frame,
                            "Number of iterations / solutions must be greater than 0.",
                            "Interactive Hicupp");
                else if (converge && maxEquals <= 0)
                    MessageBox.showMessage(frame,
                            "All parameters need to be positive.",
                            "Interactive Hicupp");
                else if (converge && iterations <= maxEquals)
                    MessageBox.showMessage(frame,
                            "The number of iterations the solution stayed the same must be smaller than the " +
                                    "number of total iterations.",
                            "Interactive Hicupp");
                else {
                    treeDocument.setAlgorithmParameters(
                            new GradientDescentParameters(
                                    iterations,
                                    numberOfSolutions,
                                    converge,
                                    maxEquals
                            )
                    );

                    dialog.dispose();
                }
            } catch (NumberFormatException exception) {
                MessageBox.showMessage(frame, "What you entered is not a full number.",
                        "Interactive Hicupp");
            }
        });

        // organisation
        dialog.add(labelIterations);
        dialog.add(fieldIterations);
        dialog.add(labelSolutions);
        dialog.add(fieldSolutions);
        dialog.add(checkboxConverge);
        dialog.add(new Label());    // keeps checkbox on the left
        dialog.add(labelMaxEquals);
        dialog.add(fieldMaxEquals);
        dialog.add(ok);
        dialog.add(cancel);

        makeCompactGrid(dialog, 5);

        showDialog(dialog, frame);
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
    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * 2 + col);
        return layout.getConstraints(c);
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum
     * preferred width of the components in that column;
     * height is similarly determined for each row.
     * The parent is made just big enough to fit them all.
     * @param rows number of rows
     *
     */
    private static void makeCompactGrid(Container parent, int rows) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        //Align all cells in each column and make them the same width.
        Spring x = Spring.constant(6);
        for (int c = 0; c < 2; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width,
                        getConstraintsForCell(r, c, parent).
                                getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(6)));
        }

        //Align all cells in each row and make them the same height.
        Spring y = Spring.constant(6);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < 2; c++) {
                height = Spring.max(height,
                        getConstraintsForCell(r, c, parent).
                                getHeight());
            }
            for (int c = 0; c < 2; c++) {
                SpringLayout.Constraints constraints =
                        getConstraintsForCell(r, c, parent);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(6)));
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}
