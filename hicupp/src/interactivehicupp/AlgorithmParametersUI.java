package interactivehicupp;

import hicupp.Function;
import hicupp.FunctionMaximizer;
import hicupp.ProjectionIndexFunction;
import hicupp.algorithms.AlgorithmParameters;
import hicupp.algorithms.AlgorithmUtilities;
import hicupp.algorithms.ga.*;
import hicupp.algorithms.gd.GradientDescentParameters;
import hicupp.algorithms.sa.*;

import javax.swing.*;
import java.awt.*;

public final class AlgorithmParametersUI {

    public static void createParams(TreeDocument treeDocument) {
        switch (treeDocument.getAlgorithmIndex()) {
            case FunctionMaximizer.ANNEALING_ALGORITHM_INDEX -> AnnealingUI.show(treeDocument);
            case FunctionMaximizer.GENETIC_ALGORITHM_INDEX -> GeneticUI.show(treeDocument);
            case FunctionMaximizer.GRADIENT_ALGORITHM_INDEX -> GradientUI.show(treeDocument);
            default -> treeDocument.setAlgorithmParameters(null);
        }
    }

    public static void logParameters(TreeDocument treeDocument) {
        TextArea log = treeDocument.getLogTextArea();
        AlgorithmParameters parameters = treeDocument.getAlgorithmParameters();

        if (log != null) {
            log.append("Parameters: ");

            switch (treeDocument.getAlgorithmIndex()) {
                case FunctionMaximizer.ANNEALING_ALGORITHM_INDEX -> AnnealingUI.log(log, parameters);
                case FunctionMaximizer.GENETIC_ALGORITHM_INDEX -> GeneticUI.log(log, parameters);
                case FunctionMaximizer.GRADIENT_ALGORITHM_INDEX -> GradientUI.log(log, parameters);
                default -> log.append("Not applicable.\n\n");
            }
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

        public static void show(TreeDocument treeDocument) {
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

            String minEvaluations = (initConverge) ? Integer.toString(initMaxEquals + 1) : "N/A";
            labelMinEvaluations = new Label("Minimum number of evaluations: " + minEvaluations, Label.RIGHT);

            String minTime = (initConverge) ? Double.toString((initMaxEquals + 1) * evaluationTime / 1000d) : "N/A";
            labelMinTime = new Label("Minimum time required: " + minTime + " s", Label.LEFT);

            String maxEvaluations = Integer.toString(initNumberOfIterations + 1);
            labelMaxEvaluations = new Label("Maximum number of evaluations: " + maxEvaluations, Label.RIGHT);

            String maxTime = Double.toString((initNumberOfIterations + 1) * evaluationTime / 1000d);
            labelMaxTime = new Label("Maximum time required: " + maxTime + " s", Label.LEFT);

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

        public static void log(TextArea log, AlgorithmParameters parameters) {
            if (parameters instanceof SimulatedAnnealingParameters params) {
                log.append("Iterations - " + params.numberOfIterations() +
                           ((params.convergeAtMaxEquals())? (", Stop when solution does not improve after number of " +
                                   "iterations - " + params.maxEquals()) : "") +
                           "\n\n"
                );
            } else throw new RuntimeException("Wrong parameters type.");
        }
    }

    private static final class GeneticUI {

        private static Dialog dialog;

        private static TextField fieldPopulation;
        private static TextField fieldGens;
        private static TextField fieldMutations;
        private static TextField fieldSpawns;

        private static Checkbox checkboxConverge;

        private static Label labelMaxEquals;
        private static TextField fieldMaxEquals;

        private static Label labelMinEvaluations;
        private static Label labelMinTime;
        private static Label labelMaxEvaluations;
        private static Label labelMaxTime;

        private static int evaluationsPerIteration = -1;
        private static double evaluationTime = -1;

        static void show(TreeDocument treeDocument) {
            Frame frame = treeDocument.getFrame();
            evaluationTime = ((AbstractNodeView) treeDocument.getPointsSourceProvider().getRoot()).getEvaluationTime();

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
            dialog = new Dialog(frame, "Genetic Algorithm", true);
            dialog.setLayout(new SpringLayout());

            Label labelPopulation = new Label("Population size: ", Label.RIGHT);
            fieldPopulation = new TextField(Integer.toString(initPop), 29);

            Label labelGens = new Label("Number of generations: ", Label.RIGHT);
            fieldGens = new TextField(Integer.toString(initGens), 29);

            Label labelMutations = new Label("Mutations per generation: ", Label.RIGHT);
            fieldMutations = new TextField(Integer.toString(initMutations), 29);

            Label labelSpawns = new Label("Spawns per generation: ", Label.RIGHT);
            fieldSpawns = new TextField(Integer.toString(initSpawns), 29);

            checkboxConverge = new Checkbox("Stop when solution does not improve", initConverge);

            labelMaxEquals = new Label("After number of iterations: ", Label.RIGHT);
            labelMaxEquals.setEnabled(initConverge);
            fieldMaxEquals = new TextField(Integer.toString(initMaxEquals), 29);
            fieldMaxEquals.setEnabled(initConverge);

            labelMinEvaluations = new Label("Minimum number of evaluations: ", Label.RIGHT);
            labelMinTime = new Label("Minimum time required: s", Label.LEFT);

            labelMaxEvaluations = new Label("Maximum number of evaluations: ", Label.RIGHT);
            labelMaxTime = new Label("Maximum time required: s", Label.LEFT);

            getEstimates();

            final Button ok = new Button("Ok");
            final Button cancel = new Button("Cancel");

            // events
            fieldPopulation.addTextListener(e -> getEstimates());
            fieldGens.addTextListener(e -> getEstimates());
            fieldMutations.addTextListener(e -> getEstimates());
            fieldSpawns.addTextListener(e -> getEstimates());
            fieldMaxEquals.addTextListener(e -> getEstimates());

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
                getEstimates();
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
            dialog.add(labelMinEvaluations);
            dialog.add(labelMinTime);
            dialog.add(labelMaxEvaluations);
            dialog.add(labelMaxTime);
            dialog.add(ok);
            dialog.add(cancel);

            makeCompactGrid(dialog, 9);

            showDialog(dialog, frame);
        }

        private static void calculateEvaluationsPerIteration() {
            try {
                int populationSize = Integer.parseInt(fieldPopulation.getText());
                int mutations = Integer.parseInt(fieldMutations.getText());
                int spawns = Integer.parseInt(fieldSpawns.getText());

                evaluationsPerIteration = populationSize +  // crossover
                        mutations +       // 1 mutation costs 1 evaluation
                        spawns;           // 1 spawn costs 1 evaluation
            } catch (NumberFormatException e) {
                evaluationsPerIteration = -1;
            }
        }

        private static void getEstimates() {
            String minEvaluations = "N/A";
            String minTime = "N/A";
            String maxEvaluations = "N/A";
            String maxTime = "N/A";

            try {
                calculateEvaluationsPerIteration();
                if (evaluationsPerIteration == -1) throw new NumberFormatException();

                if (checkboxConverge.getState()) {
                    int min =
                            Integer.parseInt(fieldPopulation.getText())  +  // initial random population
                            evaluationsPerIteration * Integer.parseInt(fieldMaxEquals.getText()); // total evaluations

                    minEvaluations = Integer.toString(min);
                    minTime = Double.toString(min * evaluationTime / 1000d);
                }

                int max =
                        Integer.parseInt(fieldPopulation.getText())  +  // initial random population
                        evaluationsPerIteration * Integer.parseInt(fieldGens.getText());    // total evaluations

                maxEvaluations = Integer.toString(max);
                maxTime = Double.toString(max * evaluationTime / 1000d);
            } catch (NumberFormatException ignore) { }
            finally {
                labelMinEvaluations.setText("Minimum number of evaluations: " + minEvaluations);
                labelMinTime.setText("Minimum time: " + minTime + " s");
                labelMaxEvaluations.setText("Maximum number of evaluations: " + maxEvaluations);
                labelMaxTime.setText("Maximum time: " + maxTime + " s");
            }
        }

        public static void log(TextArea log, AlgorithmParameters parameters) {
            if (parameters instanceof GeneticAlgorithmParameters params) {
                log.append("Population size - " + params.populationSize() + ", " +
                        "Number of generations - " + params.maxGenerations() + ", " +
                        "Mutations per generation - " + params.mutationsPerGen() + ", " +
                        "Spawns per generation - " + params.spawnsPerGen() +
                        ((params.convergeAtMaxEquals())? (", Stop when solution does not improve after number of " +
                                "iterations - " + params.maxEquals()) : "") +
                        "\n\n"
                );
            } else throw new RuntimeException("Wrong parameters type.");
        }
    }

    private static final class GradientUI {

        private static Dialog dialog;

        private static TextField fieldIterations;
        private static TextField fieldSolutions;
        private static Checkbox checkboxConverge;
        private static Label labelMaxEquals;
        private static TextField fieldMaxEquals;

        private static Label labelMinEvaluations;
        private static Label labelMinTime;
        private static Label labelMaxEvaluations;
        private static Label labelMaxTime;

        private static long evaluationTime;
        private static int argumentCount;

        static void show(TreeDocument treeDocument) {
            Frame frame = treeDocument.getFrame();

            AbstractNodeView nodeView = (AbstractNodeView) treeDocument.getPointsSourceProvider().getRoot();
            evaluationTime = nodeView.getEvaluationTime();
            argumentCount = nodeView.getClassNode().getDimensionCount() - 1;

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
                initSolutions = 5;
                initConverge = true;
                initMaxEquals = 20;
            }

            // UI
            dialog = new Dialog(frame, "Gradient Ascent", true);
            dialog.setLayout(new SpringLayout());

            final Label labelIterations = new Label("Number of iterations: ", Label.RIGHT);
            fieldIterations = new TextField(Integer.toString(initIterations), 29);

            final Label labelSolutions = new Label("Number of initial random solutions: ", Label.RIGHT);
            fieldSolutions = new TextField(Integer.toString(initSolutions), 29);

            checkboxConverge =
                    new Checkbox("Stop when the solution does not improve", initConverge);

            labelMaxEquals = new Label("After number of iterations: ", Label.RIGHT);
            labelMaxEquals.setEnabled(initConverge);
            fieldMaxEquals = new TextField(Integer.toString(initMaxEquals), 29);
            fieldMaxEquals.setEnabled(initConverge);

            labelMinEvaluations = new Label("Minimum number of evaluations: ", Label.RIGHT);
            labelMinTime = new Label("Minimum time required: s", Label.LEFT);

            labelMaxEvaluations = new Label("Maximum number of evaluations: ", Label.RIGHT);
            labelMaxTime = new Label("Maximum time required: s", Label.LEFT);

            getMinimumEstimates();
            getMaximumEstimates();

            final Button ok = new Button("Ok");
            final Button cancel = new Button("Cancel");

            // events
            fieldIterations.addTextListener(e -> getMaximumEstimates());
            fieldSolutions.addTextListener(e -> {
                getMinimumEstimates();
                getMaximumEstimates();
            });
            fieldMaxEquals.addTextListener(e -> getMinimumEstimates());

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

                getMinimumEstimates();
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
            dialog.add(labelMinEvaluations);
            dialog.add(labelMinTime);
            dialog.add(labelMaxEvaluations);
            dialog.add(labelMaxTime);
            dialog.add(ok);
            dialog.add(cancel);

            makeCompactGrid(dialog, 7);

            showDialog(dialog, frame);
        }

        private static int evaluationsPerIteration(int iterations, int solutions) {
            int evaluations = solutions * 2 * argumentCount +   // finding gradient, worst case scenario
                              solutions;                        // calculate new position

            return evaluations * iterations;
        }

        private static void getMinimumEstimates() {
            String minEvaluations = "N/A";
            String minTime = "N/A";

            if (checkboxConverge.getState()) {
                try {
                    int maxEquals = Integer.parseInt(fieldMaxEquals.getText());
                    int solutions = Integer.parseInt(fieldSolutions.getText());
                    int evaluations = solutions + evaluationsPerIteration(maxEquals, solutions);

                    minEvaluations = Integer.toString(evaluations);
                    minTime = Double.toString(evaluations * evaluationTime / 1000d);
                } catch (NumberFormatException ignore) { }
            }

            labelMinEvaluations.setText("Minimum number of evaluations: " + minEvaluations);
            labelMinTime.setText("Minimum time required: " + minTime + " s");
        }

        private static void getMaximumEstimates() {
            String maxEvaluations = "N/A";
            String maxTime = "N/A";

            try {
                int iterations = Integer.parseInt(fieldIterations.getText());
                int solutions = Integer.parseInt(fieldSolutions.getText());
                int evaluations = solutions + evaluationsPerIteration(iterations, solutions);

                maxEvaluations = Integer.toString(evaluations);
                maxTime = Double.toString(evaluations * evaluationTime / 1000d);
            } catch (NumberFormatException ignore) { }
            finally {
                labelMaxEvaluations.setText("Maximum number of evaluations: " + maxEvaluations);
                labelMaxTime.setText("Maximum time required: " + maxTime + " s");
            }
        }

        public static void log(TextArea log, AlgorithmParameters parameters) {
            if (parameters instanceof GradientDescentParameters params) {
                log.append("Iterations - " + params.maxIterations() + ", " +
                        "Number of initial random solutions - " + params.numberOfSolutions() +
                        ((params.convergeAtMaxEquals())? (", Stop when solution does not improve after number of " +
                                "iterations - " + params.maxEquals()) : "") +
                        "\n\n"
                );
            } else throw new RuntimeException("Wrong parameters type.");
        }
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

    /**
     * Find the evaluation time of given set of points with chosen projection index
     * @param projectionIndex chosen projection index
     * @param nodeView node to be evaluated
     */
    public static void evaluationTime(int projectionIndex, AbstractNodeView nodeView) {
        Function projectionIndexFunction = new ProjectionIndexFunction(projectionIndex, nodeView.getClassNode());

        Thread thread = new Thread(() -> {
            long total = 0;
            int counter = 0;

            while (counter < 10 && total < 10000) {
                double[] x = AlgorithmUtilities
                        .generateRandomArguments(projectionIndexFunction.getArgumentCount(), 1);

                long start = System.currentTimeMillis();
                projectionIndexFunction.evaluate(x);
                long end = System.currentTimeMillis();

                long duration = end - start;

                if (duration != 0) {
                    nodeView.setEvaluationTime(duration);
                    counter++;
                    total += duration;
                    System.out.print(duration + " ");
                }
            }

            long average = Math.round((double) total / (double) counter);
            System.out.println("\nCount: " + counter + " Average (ms): " + average);
            nodeView.setEvaluationTime(average);
        });

        thread.start();
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
