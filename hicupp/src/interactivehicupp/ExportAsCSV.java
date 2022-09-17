package interactivehicupp;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

public final class ExportAsCSV {

    public static void export(Frame frame, String title, SplitView root) {
        FileDialog fileDialog = new FileDialog(frame, "Export Tree As", FileDialog.SAVE);
        fileDialog.setFile(title + ".csv");
        fileDialog.setVisible(true);

        if (fileDialog.getFile() != null) {
            String filename = fileDialog.getFile();
            if (!filename.endsWith(".csv")) filename += ".csv";

            File file = new File(fileDialog.getDirectory(), filename);
            writeFile(frame, file, root);
        }
    }

    private static void writeFile(Frame frame, File file, SplitView root) {
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.println(getTitle(root));
            printNodes(root, printWriter);
        } catch (FileNotFoundException e) {
            MessageBox.showMessage(frame, "Failed to write file: " + e.getMessage(), "Error exporting tree");
            e.printStackTrace();
        }
    }

    private static String getTitle(SplitView root) {
        List<String> parameters = new ArrayList<>();
        parameters.add("node");
        parameters.addAll(Arrays.asList(root.getParameterNames()));
        parameters.add("threshold");
        return String.join(",", parameters);
    }

    private static void printNodes(SplitView splitView, PrintWriter printWriter) {
        if (splitView != null) {
            printWriter.println(splitView.printSplitView());
            if (splitView.getLeftChild() != null)
                printNodes(splitView.getLeftChild().getChild(), printWriter);
            if (splitView.getRightChild() != null)
                printNodes(splitView.getRightChild().getChild(), printWriter);
        }
    }
}
