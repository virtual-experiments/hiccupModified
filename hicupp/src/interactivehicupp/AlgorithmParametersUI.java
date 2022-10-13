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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class AlgorithmParametersUI {

    public interface Response {
        void confirm();
        void cancel();
    }

    public static void createParams(TreeDocument treeDocument, int index, Response response) {
        switch (index) {
            case FunctionMaximizer.ANNEALING_ALGORITHM_INDEX -> AnnealingUI.create(treeDocument, response);
            case FunctionMaximizer.GENETIC_ALGORITHM_INDEX -> GeneticUI.create(treeDocument, response);
            case FunctionMaximizer.GRADIENT_ALGORITHM_INDEX -> GradientUI.create(treeDocument, response);
            default -> {
                response.confirm();
                treeDocument.setAlgorithmParameters(null);
            }
        }
    }

    public static void logParameters(TreeDocument treeDocument) {
        JTextArea log = treeDocument.getLogTextArea();
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

    private static class AnnealingUI {

        private final JDialog dialog;

        private final JTextField fieldIterations;

        private final JCheckBox checkboxConverge;

        private final JLabel labelMaxEquals;
        private final JTextField fieldMaxEquals;

        private final JLabel labelMinEvaluations;
        private final JLabel labelMinTime;
        private final JLabel labelMaxEvaluations;
        private final JLabel labelMaxTime;

        private final long evaluationTime;

        public AnnealingUI(TreeDocument treeDocument, Response response) {
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
            dialog = new JDialog(frame, "Simulated Annealing", true);
            dialog.setLayout(new SpringLayout());

            JLabel labelIterations = new JLabel("Number of iterations: ", JLabel.RIGHT);
            fieldIterations = new JTextField(Integer.toString(initNumberOfIterations));

            checkboxConverge = new JCheckBox("Stop when solution does not improve", initConverge);

            labelMaxEquals = new JLabel("After number of iterations: ", JLabel.RIGHT);
            labelMaxEquals.setEnabled(initConverge);
            fieldMaxEquals = new JTextField(Integer.toString(initMaxEquals));
            fieldMaxEquals.setEnabled(initConverge);

            String minEvaluations = (initConverge) ? Integer.toString(initMaxEquals + 1) : "N/A";
            labelMinEvaluations = new JLabel("Minimum number of evaluations: " + minEvaluations, JLabel.RIGHT);

            String minTime = (initConverge) ? Double.toString((initMaxEquals + 1) * evaluationTime / 1000d) : "N/A";
            labelMinTime = new JLabel("Estimate minimum time: " + minTime + " s", JLabel.LEFT);

            String maxEvaluations = Integer.toString(initNumberOfIterations + 1);
            labelMaxEvaluations = new JLabel("Maximum number of evaluations: " + maxEvaluations, JLabel.RIGHT);

            String maxTime = Double.toString((initNumberOfIterations + 1) * evaluationTime / 1000d);
            labelMaxTime = new JLabel("Estimate maximum time: " + maxTime + " s", JLabel.LEFT);

            final JButton ok = new JButton("Ok");
            final JButton cancel = new JButton("Cancel");

            // events
            fieldIterations.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getMaximumEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getMaximumEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getMaximumEstimates();
                }
            });

            fieldMaxEquals.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getMinimumEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getMinimumEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getMinimumEstimates();
                }
            });

            cancel.addActionListener(e -> {
                dialog.dispose();
                response.cancel();
            });

            checkboxConverge.addItemListener(e -> {
                fieldMaxEquals.setEnabled(checkboxConverge.isSelected());
                labelMaxEquals.setEnabled(checkboxConverge.isSelected());

                getMinimumEstimates();
            });

            ok.addActionListener(e -> {
                try {
                    final int numberOfIterations = Integer.parseInt(fieldIterations.getText());
                    final boolean convergeAtMaxEquals = checkboxConverge.isSelected();
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
                        response.confirm();
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
            dialog.add(new JLabel()); // keeps checkbox to the left
            dialog.add(labelMaxEquals);
            dialog.add(fieldMaxEquals);
            dialog.add(labelMinEvaluations);
            dialog.add(labelMinTime);
            dialog.add(labelMaxEvaluations);
            dialog.add(labelMaxTime);
            dialog.add(ok);
            dialog.add(cancel);

            dialog.setLayout(new GridLayout(6, 2, 8, 8));

            showDialog(dialog, frame, response);
        }

        private void getMinimumEstimates() {
            String minEvaluations = "N/A";
            String minTime = "N/A";
            try {
                if (checkboxConverge.isSelected()) {
                    int maxEquals = Integer.parseInt(fieldMaxEquals.getText()) + 1;
                    double time = maxEquals * evaluationTime / 1000d;

                    minEvaluations = Integer.toString(maxEquals);
                    minTime = Double.toString(time);
                }
            } catch (NumberFormatException ignore) { }
            finally {
                labelMinEvaluations.setText("Minimum number of evaluations: " + minEvaluations);
                labelMinTime.setText("Estimate minimum time: " + minTime + " s");
            }
        }

        private void getMaximumEstimates() {
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
                labelMaxTime.setText("Estimate maximum time: " + maxTime + " s");
            }
        }

        public static void log(JTextArea log, AlgorithmParameters parameters) {
            if (parameters instanceof SimulatedAnnealingParameters params) {
                log.append("Iterations - " + params.numberOfIterations() +
                           ((params.convergeAtMaxEquals())? (", Stop when solution does not improve after number of " +
                                   "iterations - " + params.maxEquals()) : "") +
                           "\n\n"
                );
            } else throw new RuntimeException("Wrong parameters type.");
        }

        public static void create(TreeDocument treeDocument, Response response) {
            new AnnealingUI(treeDocument, response);
        }
    }

    private static class GeneticUI {

        private final JDialog dialog;

        private final JTextField fieldPopulation;
        private final JTextField fieldGens;
        private final JTextField fieldMutations;
        private final JTextField fieldSpawns;

        private final JCheckBox checkboxConverge;

        private final JLabel labelMaxEquals;
        private final JTextField fieldMaxEquals;

        private final JLabel labelMinEvaluations;
        private final JLabel labelMinTime;
        private final JLabel labelMaxEvaluations;
        private final JLabel labelMaxTime;

        private static int evaluationsPerIteration = -1;
        private static double evaluationTime = -1;

        public GeneticUI(TreeDocument treeDocument, Response response) {
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
            dialog = new JDialog(frame, "Genetic Algorithm", true);
            dialog.setLayout(new SpringLayout());

            JLabel labelPopulation = new JLabel("Population size: ", JLabel.RIGHT);
            fieldPopulation = new JTextField(Integer.toString(initPop));

            JLabel labelGens = new JLabel("Number of generations: ", JLabel.RIGHT);
            fieldGens = new JTextField(Integer.toString(initGens));

            JLabel labelMutations = new JLabel("Mutations per generation: ", JLabel.RIGHT);
            fieldMutations = new JTextField(Integer.toString(initMutations));

            JLabel labelSpawns = new JLabel("Spawns per generation: ", JLabel.RIGHT);
            fieldSpawns = new JTextField(Integer.toString(initSpawns));

            checkboxConverge = new JCheckBox("Stop when solution does not improve", initConverge);

            labelMaxEquals = new JLabel("After number of iterations: ", JLabel.RIGHT);
            labelMaxEquals.setEnabled(initConverge);
            fieldMaxEquals = new JTextField(Integer.toString(initMaxEquals));
            fieldMaxEquals.setEnabled(initConverge);

            labelMinEvaluations = new JLabel("Minimum number of evaluations: ", JLabel.RIGHT);
            labelMinTime = new JLabel("Estimate minimum time: s", JLabel.LEFT);

            labelMaxEvaluations = new JLabel("Maximum number of evaluations: ", JLabel.RIGHT);
            labelMaxTime = new JLabel("Estimate maximum time: s", JLabel.LEFT);

            getEstimates();

            final JButton ok = new JButton("Ok");
            final JButton cancel = new JButton("Cancel");

            // events
            fieldPopulation.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getEstimates();
                }
            });
            fieldGens.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getEstimates();
                }
            });
            fieldMutations.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getEstimates();
                }
            });
            fieldSpawns.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getEstimates();
                }
            });
            fieldMaxEquals.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getEstimates();
                }
            });

            cancel.addActionListener(e -> {
                dialog.dispose();
                response.cancel();
            });

            checkboxConverge.addItemListener(e -> {
                fieldMaxEquals.setEnabled(checkboxConverge.isSelected());
                labelMaxEquals.setEnabled(checkboxConverge.isSelected());
                getEstimates();
            });

            ok.addActionListener(e -> {
                try {
                    final int popSize = Integer.parseInt(fieldPopulation.getText());
                    final int gens = Integer.parseInt(fieldGens.getText());
                    final int mutations = Integer.parseInt(fieldMutations.getText());
                    final int spawns = Integer.parseInt(fieldSpawns.getText());
                    final boolean converge = checkboxConverge.isSelected();
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
                        response.confirm();
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
            dialog.add(new JLabel()); // keeps checkbox on the left
            dialog.add(labelMaxEquals);
            dialog.add(fieldMaxEquals);
            dialog.add(labelMinEvaluations);
            dialog.add(labelMinTime);
            dialog.add(labelMaxEvaluations);
            dialog.add(labelMaxTime);
            dialog.add(ok);
            dialog.add(cancel);

            dialog.setLayout(new GridLayout(9, 2, 8, 8));

            showDialog(dialog, frame, response);
        }

        private void calculateEvaluationsPerIteration() {
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

        private void getEstimates() {
            String minEvaluations = "N/A";
            String minTime = "N/A";
            String maxEvaluations = "N/A";
            String maxTime = "N/A";

            try {
                calculateEvaluationsPerIteration();
                if (evaluationsPerIteration == -1) throw new NumberFormatException();

                if (checkboxConverge.isSelected()) {
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
                labelMinTime.setText("Estimate minimum time: " + minTime + " s");
                labelMaxEvaluations.setText("Maximum number of evaluations: " + maxEvaluations);
                labelMaxTime.setText("Estimate maximum time: " + maxTime + " s");
            }
        }

        public static void log(JTextArea log, AlgorithmParameters parameters) {
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

        public static void create(TreeDocument treeDocument, Response response) {
            new GeneticUI(treeDocument, response);
        }
    }

    private static class GradientUI {

        private final JDialog dialog;

        private final JTextField fieldIterations;
        private final JTextField fieldSolutions;
        private final JCheckBox checkboxConverge;
        private final JLabel labelMaxEquals;
        private final JTextField fieldMaxEquals;

        private final JLabel labelMinEvaluations;
        private final JLabel labelMinTime;
        private final JLabel labelMaxEvaluations;
        private final JLabel labelMaxTime;

        private final long evaluationTime;
        private final int argumentCount;

        public GradientUI(TreeDocument treeDocument, Response response) {
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
            dialog = new JDialog(frame, "Gradient Ascent", true);
            dialog.setLayout(new SpringLayout());

            final JLabel labelIterations = new JLabel("Number of iterations: ", JLabel.RIGHT);
            fieldIterations = new JTextField(Integer.toString(initIterations));

            final JLabel labelSolutions = new JLabel("Number of initial random solutions: ", JLabel.RIGHT);
            fieldSolutions = new JTextField(Integer.toString(initSolutions));

            checkboxConverge =
                    new JCheckBox("Stop when the solution does not improve", initConverge);

            labelMaxEquals = new JLabel("After number of iterations: ", JLabel.RIGHT);
            labelMaxEquals.setEnabled(initConverge);
            fieldMaxEquals = new JTextField(Integer.toString(initMaxEquals));
            fieldMaxEquals.setEnabled(initConverge);

            labelMinEvaluations = new JLabel("Minimum number of evaluations: ", JLabel.RIGHT);
            labelMinTime = new JLabel("Estimate minimum time: s", JLabel.LEFT);

            labelMaxEvaluations = new JLabel("Maximum number of evaluations: ", JLabel.RIGHT);
            labelMaxTime = new JLabel("Estimate maximum time: s", JLabel.LEFT);

            getMinimumEstimates();
            getMaximumEstimates();

            final JButton ok = new JButton("Ok");
            final JButton cancel = new JButton("Cancel");

            // events
            fieldIterations.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getMaximumEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getMaximumEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getMaximumEstimates();
                }
            });
            fieldSolutions.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getMinimumEstimates();
                    getMaximumEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getMinimumEstimates();
                    getMaximumEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getMinimumEstimates();
                    getMaximumEstimates();
                }
            });
            fieldMaxEquals.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    getMaximumEstimates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    getMaximumEstimates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    getMaximumEstimates();
                }
            });

            cancel.addActionListener(e -> {
                dialog.dispose();
                response.cancel();
            });

            checkboxConverge.addItemListener(e -> {
                labelMaxEquals.setEnabled(checkboxConverge.isSelected());
                fieldMaxEquals.setEnabled(checkboxConverge.isSelected());

                getMinimumEstimates();
            });

            ok.addActionListener(e -> {
                try {
                    final int iterations = Integer.parseInt(fieldIterations.getText());
                    final int numberOfSolutions = Integer.parseInt(fieldSolutions.getText());
                    final boolean converge = checkboxConverge.isSelected();
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
                        response.confirm();
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
            dialog.add(new JLabel());    // keeps checkbox on the left
            dialog.add(labelMaxEquals);
            dialog.add(fieldMaxEquals);
            dialog.add(labelMinEvaluations);
            dialog.add(labelMinTime);
            dialog.add(labelMaxEvaluations);
            dialog.add(labelMaxTime);
            dialog.add(ok);
            dialog.add(cancel);

            dialog.setLayout(new GridLayout(7, 2, 8, 8));

            showDialog(dialog, frame, response);
        }

        private int evaluationsPerIteration(int iterations, int solutions) {
            int evaluations = solutions * 2 * argumentCount +   // finding gradient, worst case scenario
                              solutions;                        // calculate new position

            return evaluations * iterations;
        }

        private void getMinimumEstimates() {
            String minEvaluations = "N/A";
            String minTime = "N/A";

            if (checkboxConverge.isSelected()) {
                try {
                    int maxEquals = Integer.parseInt(fieldMaxEquals.getText());
                    int solutions = Integer.parseInt(fieldSolutions.getText());
                    int evaluations = solutions + evaluationsPerIteration(maxEquals, solutions);

                    minEvaluations = Integer.toString(evaluations);
                    minTime = Double.toString(evaluations * evaluationTime / 1000d);
                } catch (NumberFormatException ignore) { }
            }

            labelMinEvaluations.setText("Minimum number of evaluations: " + minEvaluations);
            labelMinTime.setText("Estimate minimum time: " + minTime + " s");
        }

        private void getMaximumEstimates() {
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
                labelMaxTime.setText("Estimate maximum time: " + maxTime + " s");
            }
        }

        public static void log(JTextArea log, AlgorithmParameters parameters) {
            if (parameters instanceof GradientDescentParameters params) {
                log.append("Iterations - " + params.maxIterations() + ", " +
                        "Number of initial random solutions - " + params.numberOfSolutions() +
                        ((params.convergeAtMaxEquals())? (", Stop when solution does not improve after number of " +
                                "iterations - " + params.maxEquals()) : "") +
                        "\n\n"
                );
            } else throw new RuntimeException("Wrong parameters type.");
        }

        public static void create(TreeDocument treeDocument, Response response) {
            new GradientUI(treeDocument, response);
        }
    }

    private static void showDialog(Dialog dialog, Frame frame, Response response) {
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                response.cancel();
            }
        });

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
}
