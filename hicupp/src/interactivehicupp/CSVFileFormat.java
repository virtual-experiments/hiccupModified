package interactivehicupp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CSVFileFormat {
    private String[] parameters;
    private ArrayList<ArrayList<Double>> data;

    public CSVFileFormat(String file, boolean firstLineParameter) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        int lineNumber = 0;
        int numberOfDimensions = 0;

        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("\"", "");
            String[] values = line.split(",");

            if (lineNumber == 0) {   // first line
                numberOfDimensions = values.length;
                data = new ArrayList<>(numberOfDimensions);

                if (firstLineParameter) {
                    parameters = values;

                    for (int i = 0; i < numberOfDimensions; i++)
                        data.add(new ArrayList<>());
                }
                else {
                    parameters = new String[numberOfDimensions];

                    for (int i = 0; i < numberOfDimensions; i++) {
                        parameters[i] = "p" + i;        // default parameter name

                        ArrayList<Double> newDimension = new ArrayList<>();
                        try {
                            newDimension.add(Double.parseDouble(values[i]));    // add first value
                        } catch (NumberFormatException | NullPointerException e) {
                            throw new IOException("Invalid number format in line " + lineNumber + 1 +
                                    ", column " + i + 1 + ".");
                        }
                        data.add(newDimension);
                    }
                }
            } else {    // other lines
                if (numberOfDimensions != values.length)
                    throw new IOException("Different number of dimensions in line " + lineNumber + 1);

                for (int i = 0; i < numberOfDimensions; i++) {
                    try {
                        ArrayList<Double> currentDimension = data.get(i);
                        currentDimension.add(Double.parseDouble(values[i]));
                    } catch (NumberFormatException | NullPointerException e) {
                        throw new IOException("Invalid number format in line " + lineNumber + 1 +
                                ", column " + i + 1 + ".");
                    }
                }
            }

            lineNumber++;
        }
    }

    public int getNumberOfPoints() {
        return data.get(0).size();
    }

    public String[] getParameters() {
        return parameters;
    }

    public double[] getCoordinatesFromChosenColumns(int[] chosenColumns) {
        ArrayList<Double> coordinates = new ArrayList<>(getNumberOfPoints() * chosenColumns.length);

        for (int chosenColumn : chosenColumns) {
            coordinates.addAll(data.get(chosenColumn));
        }

        return coordinates.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
