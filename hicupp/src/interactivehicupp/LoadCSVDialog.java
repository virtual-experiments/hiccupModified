package interactivehicupp;

import hicupp.LayoutTools;
import hicupp.LoadDialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class LoadCSVDialog extends LoadDialog {
    private final TextField dataFileTextField = new TextField();
    private final Checkbox parameterFirstLineCheckBox = new Checkbox();
    private final List columnsList = new List(10);

    private int columnsCount;
    private double[] coords;
    private final Frame parent;
    private CSVFileFormat reader;
    private String[] parameterNames;

    @Override
    public double[] getCoords() {
        return coords;
    }

    @Override
    public int getColumnsCount() {
        return columnsCount;
    }

    @Override
    public String getFilename() {
        return dataFileTextField.getText();
    }

    @Override
    public String[] getParameterNames() {
        return parameterNames;
    }

    @Override
    public int skipFirstLine() {
        return (parameterFirstLineCheckBox.getState())? 1 : 0;
    }

    @Override
    public String printChosenColumns() {
        StringBuilder builder = new StringBuilder();

        for (int column : columnsList.getSelectedIndexes()) {
            builder.append(column);
            builder.append(" ");
        }

        return builder.toString();
    }

    @Override
    public void load(String filename, int skipFirstLine, int[] chosenColumns) {
        try {
            reader = new CSVFileFormat(filename, skipFirstLine == 1);
            coords = reader.getCoordinatesFromChosenColumns(chosenColumns);
            columnsCount = chosenColumns.length;
            parameterNames = reader.getChosenParameters(chosenColumns);

            dataFileTextField.setText(filename);
            parameterFirstLineCheckBox.setState(skipFirstLine == 1);

            columnsList.removeAll();
            for (String param : reader.getParameters()) columnsList.add(param);
            for (int chosenColumn : chosenColumns) columnsList.select(chosenColumn);
        } catch (IOException e) {
            MessageBox.showMessage(parent, "Could not read data file: " + e, getTitle());
        }
    }

    @Override
    public void disableColumnsSelection() {
        columnsList.setEnabled(false);
    }

    public LoadCSVDialog(Frame parent, String title) {
        super(parent, title, true);

        this.parent = parent;

        Panel mainPanel = new Panel();
        LayoutTools.addWithMargin(this, mainPanel, 8);

        mainPanel.setLayout(new BorderLayout(6, 6));
        Panel dataFilePanel = new Panel();
        mainPanel.add(dataFilePanel, BorderLayout.NORTH);
        Panel columnsPanel = new Panel();
        mainPanel.add(columnsPanel, BorderLayout.CENTER);
        Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                coords = null;
                setVisible(false);
            }
        });
        setBackground(SystemColor.control);

        dataFilePanel.setLayout(new BorderLayout(6, 6));
        Label dataFileLabel = new Label();
        dataFilePanel.add(dataFileLabel, BorderLayout.WEST);
        dataFilePanel.add(dataFileTextField, BorderLayout.CENTER);
        Button browseButton = new Button();
        dataFilePanel.add(browseButton, BorderLayout.EAST);
        dataFilePanel.add(parameterFirstLineCheckBox, BorderLayout.SOUTH);

        dataFileLabel.setText("Data file: ");
        dataFileTextField.setColumns(50);

        browseButton.setLabel("Browse...");
        browseButton.addActionListener(e -> {
            FileDialog fileDialog = new FileDialog(LoadCSVDialog.this.parent,
                    "Choose a Data File", FileDialog.LOAD);
            String filename = dataFileTextField.getText();
            if (!filename.equals("")) {
                columnsList.setEnabled(true);
                File file = new File(filename);
                if (file.getParent() != null)
                    fileDialog.setDirectory(file.getParent());
                fileDialog.setFile(file.getName());
            }
            fileDialog.setVisible(true);
            if (fileDialog.getFile() != null) {
                dataFileTextField.setText(new File(fileDialog.getDirectory(), fileDialog.getFile()).toString());
                readFile();
            }
        });

        parameterFirstLineCheckBox.setLabel("First Line is Parameter Name");

        columnsPanel.setLayout(new BorderLayout(6, 6));
        Panel columnsHeaderPanel = new Panel();
        columnsPanel.add(columnsHeaderPanel, BorderLayout.NORTH);
        columnsPanel.add(columnsList, BorderLayout.CENTER);

        columnsHeaderPanel.setLayout(new BorderLayout(6, 6));
        Label columnsLabel = new Label();
        columnsHeaderPanel.add(columnsLabel, BorderLayout.CENTER);

        columnsLabel.setText("Columns:");

        columnsList.setMultipleMode(true);

        Button loadPointsButton = new Button("Load Points");
        loadPointsButton.addActionListener(e -> loadPoints());

        Button cancelButton = new Button("Cancel");
        cancelButton.addActionListener(e -> {
            coords = null;
            setVisible(false);
        });

        buttonsPanel.add(loadPointsButton);
        buttonsPanel.add(cancelButton);

        pack();
        Dimension screenSize = getToolkit().getScreenSize();
        Dimension size = getSize();
        setLocation((screenSize.width - size.width) / 2,
                (screenSize.height - size.height) / 2);
    }

    private void readFile() {
        try {
            boolean firstLineParameter = parameterFirstLineCheckBox.getState();
            reader = new CSVFileFormat(dataFileTextField.getText(), firstLineParameter);

            columnsList.removeAll();
            for (String param : reader.getParameters())
                columnsList.add(param);

        } catch (IOException e) {
            MessageBox.showMessage(parent, "Could not read data file: " + e, getTitle());
            dataFileTextField.setText("");
            columnsList.removeAll();
            coords = null;
        }
    }

    private void loadPoints() {
        int[] columns = columnsList.getSelectedIndexes();
        if (columns.length < 2) {
            MessageBox.showMessage(parent, "Select at least two columns!", getTitle());
            return;
        }
        columnsCount = columns.length;
        coords = reader.getCoordinatesFromChosenColumns(columns);
        parameterNames = reader.getChosenParameters(columns);
        setVisible(false);
    }
}