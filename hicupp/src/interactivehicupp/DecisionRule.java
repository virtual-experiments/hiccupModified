package interactivehicupp;

import hicupp.trees.Split;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class DecisionRule extends Frame {

    private final TextArea textArea;
    private final int nodeNumber;

    public DecisionRule(int nodeNumber) throws HeadlessException {
        super("Decision Rule Node " + nodeNumber + " - Interactive Hicupp");
        this.nodeNumber = nodeNumber;

        textArea = new TextArea();
        textArea.setEditable(false);
        add(textArea, BorderLayout.CENTER);

        MenuBar menuBar = new MenuBar();
        setMenuBar(menuBar);

        Menu fileMenu = new Menu("File");
        fileMenu.setFont(new Font("MenuFont", Font.PLAIN, 14));
        menuBar.add(fileMenu);

        MenuItem save = new MenuItem("Save");
        save.addActionListener(e -> onSaveClick());
        fileMenu.add(save);
    }

    public void setTextArea(String[] parameterNames, Split split) {
        StringBuilder builder = new StringBuilder();
        double[] axis = split.getAxis();
        int maxParameterLength = Integer.MIN_VALUE;

        for (int i = 0; i < axis.length; i++) {
            if (i != 0) builder.append("\t+\n");
            String parameterName = parameterNames[i];

            builder.append(parameterName);
            builder.append("\t*\t");
            builder.append(axis[i]);

            if (parameterName.length() > maxParameterLength) maxParameterLength = parameterName.length();
        }

        System.out.println(maxParameterLength);
        builder.append("\n");
        builder.append("\u200E".repeat(maxParameterLength));
        builder.append("\t<\t");
        builder.append(split.getThreshold());

        textArea.setText(builder.toString());
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);

        if (b) {
            pack();
            Dimension frameSize = getSize();
            Dimension screenSize = getToolkit().getScreenSize();

            setLocation((screenSize.width - frameSize.width) / 2,
                    (screenSize.height - frameSize.height) / 2);
        }
    }

    private void onSaveClick() {
        FileDialog fileDialog = new FileDialog(this, "Save Decision Rule As", FileDialog.SAVE);
        fileDialog.setFile("Node " + nodeNumber + " Decision Rule.txt");
        fileDialog.setVisible(true);

        if (fileDialog.getFile() != null) {
            try {
                String filename = fileDialog.getFile();
                if (!filename.endsWith(".txt")) filename += ".txt";

                Writer writer = new FileWriter(new File(fileDialog.getDirectory(), filename));
                writer.write(textArea.getText());
                writer.close();
            } catch (IOException exception) {
                MessageBox.showMessage(this, "Could not save the decision rule: " + exception,
                        "Interactive Hicupp");
            }
        }
    }
}
