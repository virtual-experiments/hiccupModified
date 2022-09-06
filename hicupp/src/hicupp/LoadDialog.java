package hicupp;

import java.awt.*;

public abstract class LoadDialog extends Dialog {
    public LoadDialog(Frame parent, String title, boolean b) {
        super(parent, title, b);
    }

    public abstract int getColumnsCount();
    public abstract double[] getCoords();
    public abstract String getFilename();
    public abstract String[] getParameterNames();
    public abstract int skipFirstLine();
    public abstract String printChosenColumns();
    public abstract void load(String filename, int skipFirstLine, int[] chosenColumns);
}