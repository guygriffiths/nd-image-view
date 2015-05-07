/*******************************************************************************
 * Copyright (c) 2015 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.nd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which parses the config file and provides access to the values
 *
 * @author Guy Griffiths
 */
public class Config {
    public final static String SETTINGS = "${settings}";

    /** The path to the data */
    private String path = null;
    /** The naming format of the images to be displayed */
    private String nameFormat = null;
    /** The number of rows of images */
    private int gridRows = 0;
    /** The number of columns of images */
    private int gridCols = 0;
    /** The layout of the grid - which image goes where */
    private String[][] gridLayout = null;
    /** The selectable {@link Dimension}s which all images depend on */
    private List<Dimension> selectableDimensions = new ArrayList<>();
    /** The {@link Dimension}s which varies across the screen */
    private Dimension nonSelectableDimension = null;
    /** The relative percentages of the row heights */
    private double[] rowHeights;
    /** The relative percentages of the column widths */
    private double[] colWidths;

    /**
     * Parse the config file and initialise all of the valid variables
     * 
     * @param configFile
     *            A {@link File} object pointing to the config
     * @throws IOException
     *             If there is a problem reading the file
     * @throws ConfigException
     *             If the configuration is not valid for some reason
     */
    public Config(File configFile) throws IOException, ConfigException {
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        try {
            String plotByField = null;
            String line;

            /*
             * We want to store the lines which pertain to the grid layout
             * before parsing their contents
             */
            List<String[]> gridConfigLines = new ArrayList<>();

            /*
             * We use these so that dimensions can be defined anywhere
             */
            boolean definingDimension = false;
            Dimension currentDimension = null;

            /*
             * We start with zero-length arrays. If the row heights/column
             * widths are not defined, this means that they will all be evenly
             * distributed
             */
            String[] rowHeightsStrs = new String[0];
            String[] colWidthsStrs = new String[0];

            /*
             * Now read each line and parse
             */
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    /*
                     * Comments start with #
                     */
                    continue;
                }

                /*
                 * Check if we are about to start defining a dimension
                 */
                Pattern dimensionStart = Pattern.compile("\\[(.*)\\]");
                Matcher m = dimensionStart.matcher(line);
                if (m.find()) {
                    /*
                     * We were defining a dimension and we've just started a new
                     * one, so add the previous one to the list
                     */
                    if (definingDimension) {
                        selectableDimensions.add(currentDimension);
                    } else {
                        definingDimension = true;
                    }
                    /*
                     * For now the dimension name and title are the same, but we
                     * may want to make the titles definable (e.g.
                     * [dimname,Dimension Title])
                     * 
                     * TODO Make dimension title configurable
                     */
                    String name = m.group(1);
                    String title = m.group(1);
                    if (".".equals(name)) {
                        name = "";
                    }
                    /*
                     * Start a new dimension.
                     */
                    currentDimension = new Dimension(name, title, new ArrayList<>());
                    continue;
                }

                /*
                 * If we are defining a dimension, add this value to it
                 */
                if (definingDimension && !line.trim().isEmpty()) {
                    line = line.trim();
                    if (line.equals(".")) {
                        /*
                         * We define empty dimensions in the config with a
                         * single dot
                         */
                        currentDimension.getValues().add("");
                    } else {
                        currentDimension.getValues().add(line);
                    }
                }

                /*
                 * Define the path to the data
                 */
                if (line.startsWith("path")) {
                    if (path != null) {
                        throw new ConfigException(
                                "You must only provide a single value for path in the config");
                    }

                    path = readField(line, "path");
                    if (!path.endsWith("/")) {
                        path = path + "/";
                    }

                    definingDimension = false;
                    if (definingDimension) {
                        /*
                         * We were still defining a dimension
                         */
                        selectableDimensions.add(currentDimension);
                    }
                }

                /*
                 * Define the name format of the images
                 */
                if (line.startsWith("name_format")) {
                    if (nameFormat != null) {
                        throw new ConfigException(
                                "You must only provide a single value for name_format in the config");
                    }
                    nameFormat = readField(line, "name_format");
                    definingDimension = false;
                    if (definingDimension) {
                        /*
                         * We were still defining a dimension
                         */
                        selectableDimensions.add(currentDimension);
                    }
                }

                /*
                 * Define the dimension to plot by
                 */
                if (line.startsWith("plot_by")) {
                    if (plotByField != null) {
                        reader.close();
                        throw new ConfigException(
                                "You must only provide a single value for plot_by_field in the config");
                    }
                    plotByField = readField(line, "plot_by");
                    definingDimension = false;
                    if (definingDimension) {
                        /*
                         * We were still defining a dimension
                         */
                        selectableDimensions.add(currentDimension);
                    }
                }

                /*
                 * Define the row heights
                 */
                if (line.startsWith("row_heights")) {
                    if (rowHeightsStrs != null) {
                        throw new ConfigException(
                                "You must only provide a single value for row_heights in the config");
                    }
                    rowHeightsStrs = readField(line, "row_heights").split(",");
                    definingDimension = false;
                    if (definingDimension) {
                        /*
                         * We were still defining a dimension
                         */
                        selectableDimensions.add(currentDimension);
                    }
                }

                /*
                 * Define the column widths
                 */
                if (line.startsWith("col_widths")) {
                    if (colWidthsStrs != null) {
                        throw new ConfigException(
                                "You must only provide a single value for col_widths in the config");
                    }
                    colWidthsStrs = readField(line, "col_widths").split(",");
                    definingDimension = false;
                    if (definingDimension) {
                        /*
                         * We were still defining a dimension
                         */
                        selectableDimensions.add(currentDimension);
                    }
                }

                /*
                 * Define the grid layout
                 */
                if (line.startsWith("grid_")) {
                    String[] gridSplit = line.split("=");
                    if (gridSplit.length != 2) {
                        reader.close();
                        throw new ConfigException(
                                "Grid config options must be of the form \"grid_x_y = ...\"");
                    }
                    gridConfigLines.add(gridSplit);
                    definingDimension = false;
                    if (definingDimension) {
                        /*
                         * We were still defining a dimension
                         */
                        selectableDimensions.add(currentDimension);
                    }
                }
            }
            if (definingDimension) {
                /*
                 * We were still defining a dimension when the file ended
                 */
                selectableDimensions.add(currentDimension);
            }
            reader.close();

            /*
             * We have finished reading the config file. Now we must process
             * some of the values to store them in an accessible manner.
             * 
             * First check that all mandatory values were provided
             */

            if (path == null) {
                throw new ConfigException("You must provide a value for path in the config");
            }

            if (nameFormat == null) {
                throw new ConfigException("You must provide a value for name_format in the config");
            }

            if (plotByField == null) {
                throw new ConfigException("You must provide a value for plot_by in the config");
            }

            if (selectableDimensions.size() == 0) {
                throw new ConfigException("You must provide at least one dimension");
            }

            /*
             * Find the dimension which will vary across the screen
             */
            for (int i = 0; i < selectableDimensions.size(); i++) {
                if (selectableDimensions.get(i).getDimName().equals(plotByField)) {
                    nonSelectableDimension = selectableDimensions.remove(i);
                    break;
                }
            }
            if (nonSelectableDimension == null) {
                throw new ConfigException("You have stated the field " + plotByField
                        + " to plot by, but this is not defined as a dimension");
            }

            /*
             * We have read the config file, now process the lines defining the
             * grid layout (they all need to be read first so that we can
             * calculate the grid size)
             */
            for (String[] gridConfigLine : gridConfigLines) {
                /*
                 * gridConfigLine is already guaranteed to be of length 2.
                 */
                String[] gridIndices = gridConfigLine[0].split("_");
                if (gridIndices.length != 3) {
                    throw new ConfigException(
                            "Grid config options must be of the form \"grid_x_y = ...\"");
                }
                /*
                 * Update the number of rows
                 */
                try {
                    gridRows = Math.max(Integer.parseInt(gridIndices[1]) + 1, gridRows);
                } catch (NumberFormatException e) {
                    throw new ConfigException(
                            "Grid config options must be of the form \"grid_x_y = ...\", where x is a valid integer for the row number "
                                    + gridConfigLine[0]);
                }
                /*
                 * Update the number of columns
                 */
                try {
                    gridCols = Math.max(Integer.parseInt(gridIndices[2].trim()) + 1, gridCols);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    throw new ConfigException(
                            "Grid config options must be of the form \"grid_x_y = ...\", where y is a valid integer for the column number "
                                    + gridConfigLine[0]);
                }
            }
            if (gridRows == 0 || gridCols == 0) {
                throw new ConfigException(
                        "You must define at least one row and one column to display data in!");
            }

            /*
             * We now know how many rows / columns to define
             */
            gridLayout = new String[gridRows][gridCols];
            rowHeights = new double[gridRows];
            colWidths = new double[gridCols];

            /*
             * Process all available row heights, dividing the left over space
             * equally
             */
            double totalRowFrac = 0;
            for (int i = 0; i < gridRows; i++) {
                double rowHeight;
                if (i >= rowHeightsStrs.length) {
                    rowHeight = (1.0 - totalRowFrac) / (gridRows - rowHeightsStrs.length);
                } else {
                    rowHeight = Double.parseDouble(rowHeightsStrs[i]) / 100.0;
                    totalRowFrac += rowHeight;
                }
                rowHeights[i] = rowHeight;
            }

            /*
             * Process all available column widths, dividing the left over space
             * equally
             */
            double totalColFrac = 0;
            for (int i = 0; i < gridCols; i++) {
                double colWidth;
                if (i >= colWidthsStrs.length) {
                    colWidth = (1.0 - totalColFrac) / (gridCols - colWidthsStrs.length);
                } else {
                    colWidth = Double.parseDouble(colWidthsStrs[i]) / 100.0;
                    totalColFrac += colWidth;
                }
                colWidths[i] = colWidth;
            }

            /*
             * Now store the values to be displayed in each grid cell
             */
            for (String[] gridConfigLine : gridConfigLines) {
                /*
                 * gridConfigLine is already guaranteed to be of length 2.
                 */
                String[] gridIndices = gridConfigLine[0].split("_");
                /*
                 * This looks fraught with possible exceptions, but it isn't,
                 * since they will have been thrown in the previous section
                 */
                String gridContents = gridConfigLine[1].trim();
                if (".".equals(gridContents)) {
                    gridContents = "";
                }
                if (!nonSelectableDimension.getValues().contains(gridContents)
                        && !SETTINGS.equals(gridContents)) {
                    throw new ConfigException("You have chosen to plot by \"" + plotByField
                            + "\", but the plot at co-ords (" + gridIndices[1] + ","
                            + gridIndices[2] + ") is set to \"" + gridContents
                            + "\", which is not one of the valid values for the " + plotByField
                            + " dimension.");
                }
                gridLayout[Integer.parseInt(gridIndices[1])][Integer
                        .parseInt(gridIndices[2].trim())] = gridContents;
            }
        } catch (ConfigException e) {
            /*
             * If we caught a ConfigException, re-throw it here.
             * 
             * This is just so we can use the finally block to close the
             * reader...
             */
            throw e;
        } finally {
            reader.close();
        }
    }

    /**
     * Read a field of the form "key = value" and split it
     * 
     * @param line
     *            The line to be read
     * @param fieldName
     *            The field name to display in the exception if the line is
     *            formatted badly
     * @return The trimmed value
     * @throws ConfigException
     *             If the line is not of the form "key = value"
     */
    private String readField(String line, String fieldName) throws ConfigException {
        String[] fieldSplit = line.split("=");
        if (fieldSplit.length != 2) {
            throw new ConfigException("Path config option must be of the form \"" + fieldName
                    + " = ...\"");
        }
        return fieldSplit[1].trim();
    }

    /**
     * @return The path to the data
     */
    public String getPath() {
        return path;
    }

    /**
     * @return The name format of the images
     */
    public String getNameFormat() {
        return nameFormat;
    }

    /**
     * @return The {@link Dimension} which will vary across the screen
     */
    public Dimension getNonSelectableDimension() {
        return nonSelectableDimension;
    }

    /**
     * @return A {@link List} of {@link Dimension}s which will be selectable
     */
    public List<Dimension> getSelectableDimensions() {
        return selectableDimensions;
    }

    /**
     * @return A 2D array containing the values of the non-selectable dimension
     *         and where on the screen they should be plotted
     */
    public String[][] getGridLayout() {
        return gridLayout;
    }

    /**
     * @return The number of rows in the layout
     */
    public int getNRows() {
        return gridRows;
    }

    /**
     * @return The number of columns in the layout
     */
    public int getNCols() {
        return gridCols;
    }

    /**
     * @return An array of size ({@link Config#getNRows()}) containing the
     *         percentage heights of the rows in the layout
     */
    public double[] getRowHeights() {
        return rowHeights;
    }

    /**
     * @return An array of size ({@link Config#getNCols()}) containing the
     *         percentage widths of the columns in the layout
     */
    public double[] getColWidths() {
        return colWidths;
    }

    /**
     * A class used to indicate a problem or inconsistency in the config file
     *
     * @author Guy Griffiths
     */
    public class ConfigException extends Exception {
        private static final long serialVersionUID = 1L;

        public ConfigException(String message) {
            super(message);
        }
    }
}
