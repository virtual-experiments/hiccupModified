package interactivehicupp;

import hicupp.LayoutTools;
import hicupp.LoadDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class LoadCSVDialog extends LoadDialog {
    private final TextField dataFileTextField = new TextField();
    private final JCheckBox parameterFirstLineCheckBox = new JCheckBox();
    private final JList<String> columnsList = new JList<>();

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
        return (parameterFirstLineCheckBox.isSelected())? 1 : 0;
    }

    @Override
    public String printChosenColumns() {
        StringBuilder builder = new StringBuilder();

        for (int column : columnsList.getSelectedIndices()) {
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
            parameterFirstLineCheckBox.setSelected(skipFirstLine == 1);

            columnsList.removeAll();
            columnsList.setListData(reader.getParameters());
            for (int chosenColumn : chosenColumns) columnsList.setSelectedIndex(chosenColumn);
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

        JPanel mainPanel = new JPanel();
        LayoutTools.addWithMargin(this, mainPanel, 8);

        mainPanel.setLayout(new BorderLayout(6, 6));
        JPanel dataFilePanel = new JPanel();
        mainPanel.add(dataFilePanel, BorderLayout.NORTH);
        JPanel columnsPanel = new JPanel();
        mainPanel.add(columnsPanel, BorderLayout.CENTER);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                coords = null;
                setVisible(false);
            }
        });
        setBackground(SystemColor.control);

        dataFilePanel.setLayout(new BorderLayout(6, 6));
        JLabel dataFileLabel = new JLabel();
        dataFilePanel.add(dataFileLabel, BorderLayout.WEST);
        dataFilePanel.add(dataFileTextField, BorderLayout.CENTER);
        JButton browseButton = new JButton();
        dataFilePanel.add(browseButton, BorderLayout.EAST);
        dataFilePanel.add(parameterFirstLineCheckBox, BorderLayout.SOUTH);

        dataFileLabel.setText("Data file: ");
        dataFileTextField.setColumns(50);

        browseButton.setText("Browse...");
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

        parameterFirstLineCheckBox.setText("First Line is Parameter Name");
        parameterFirstLineCheckBox.setSelected(true);

        columnsPanel.setLayout(new BorderLayout(6, 6));
        JPanel columnsHeaderPanel = new JPanel();
        columnsPanel.add(columnsHeaderPanel, BorderLayout.NORTH);
        columnsPanel.add(new JScrollPane(columnsList), BorderLayout.CENTER);

        columnsHeaderPanel.setLayout(new BorderLayout(6, 6));
        JLabel columnsLabel = new JLabel();
        columnsHeaderPanel.add(columnsLabel, BorderLayout.CENTER);

        columnsLabel.setText("Columns (Ctrl/Shift to select multiple):");

        columnsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JButton loadPointsButton = new JButton("Load Points");
        loadPointsButton.addActionListener(e -> loadPoints());

        JButton cancelButton = new JButton("Cancel");
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
            boolean firstLineParameter = parameterFirstLineCheckBox.isSelected();
            reader = new CSVFileFormat(dataFileTextField.getText(), firstLineParameter);

            columnsList.removeAll();
            columnsList.setListData(reader.getParameters());
            columnsList.addSelectionInterval(0, reader.getParameters().length);

        } catch (IOException e) {
            MessageBox.showMessage(parent, "Could not read data file: " + e, getTitle());
            dataFileTextField.setText("");
            columnsList.removeAll();
            coords = null;
        }
    }

    private void loadPoints() {
        int[] columns = columnsList.getSelectedIndices();
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