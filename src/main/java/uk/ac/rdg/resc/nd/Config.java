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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {
    public final static String SETTINGS = "${settings}";

    private String path = null;
    private String nameFormat = null;
    private String plotByField = null;
    private int plotByFieldIndex;
    private int gridRows = 0;
    private int gridCols = 0;
    private String[][] gridLayout = null;
    private List<Dimension> dimensions = new ArrayList<>();

    public Config(File configFile) throws IOException, ConfigException {
        BufferedReader reader = new BufferedReader(new FileReader(configFile));
        String line;

        List<String[]> gridConfigLines = new ArrayList<>();
        boolean definingDimension = false;
        Dimension currentDimension = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#") || line.trim().isEmpty()) {
                /*
                 * Comments start with #
                 */
                continue;
            }

            Pattern dimensionStart = Pattern.compile("\\[(.*)\\]");
            Matcher m = dimensionStart.matcher(line);
            if (m.find()) {
                if (definingDimension) {
                    dimensions.add(currentDimension);
                } else {
                    definingDimension = true;
                }
                String name = m.group(1);
                String title = m.group(1);
                if(".".equals(name)) {
                    name = "";
                }
                currentDimension = new Dimension(name, title, new ArrayList<>());
                continue;
            }

            if (definingDimension && !line.trim().isEmpty()) {
                line = line.trim();
                if (line.equals(".")) {
                    /*
                     * We define empty dimensions in the config with a single
                     * dot
                     */
                    currentDimension.getValues().add("");
                } else {
                    currentDimension.getValues().add(line);
                }
            }
            
            if (line.startsWith("path")) {
                String[] pathSplit = line.split("=");
                if (pathSplit.length != 2) {
                    reader.close();
                    throw new ConfigException(
                            "Path config option must be of the form \"path = ...\"");
                }
                path = pathSplit[1].trim();
                if(!path.endsWith("/")) {
                    path = path + "/";
                }
                definingDimension = false;
                if(definingDimension) {
                    /*
                     * We were still defining a dimension
                     */
                    dimensions.add(currentDimension);
                }
            }

            if (line.startsWith("name_format")) {
                String[] nfSplit = line.split("=");
                if (nfSplit.length != 2) {
                    reader.close();
                    throw new ConfigException(
                            "Name format config option must be of the form \"name_format = ...\"");
                }
                nameFormat = nfSplit[1].trim();
                definingDimension = false;
                if(definingDimension) {
                    /*
                     * We were still defining a dimension
                     */
                    dimensions.add(currentDimension);
                }
            }

            if (line.startsWith("plot_by")) {
                String[] pbSplit = line.split("=");
                if (pbSplit.length != 2) {
                    reader.close();
                    throw new ConfigException(
                            "Plot by field config option must be of the form \"plot_by = ...\"");
                }
                plotByField = pbSplit[1].trim();
                definingDimension = false;
                if(definingDimension) {
                    /*
                     * We were still defining a dimension
                     */
                    dimensions.add(currentDimension);
                }
            }

            if (line.startsWith("grid_")) {
                String[] gridSplit = line.split("=");
                if (gridSplit.length != 2) {
                    reader.close();
                    throw new ConfigException(
                            "Grid config options must be of the form \"grid_x_y = ...\"");
                }
                gridConfigLines.add(gridSplit);
                definingDimension = false;
                if(definingDimension) {
                    /*
                     * We were still defining a dimension
                     */
                    dimensions.add(currentDimension);
                }
            }
        }
        if(definingDimension) {
            /*
             * We were still defining a dimension
             */
            dimensions.add(currentDimension);
        }
        reader.close();
        
        if (path == null) {
            throw new ConfigException("You must provide a value for path in the config");
        }

        if (nameFormat == null) {
            throw new ConfigException("You must provide a value for name_format in the config");
        }

        if (plotByField == null) {
            throw new ConfigException("You must provide a value for plot_by in the config");
        }

        if (dimensions.size() == 0) {
            throw new ConfigException("You must provide at least one dimension");
        }

        boolean plotDimFound = false;
        for (int i=0;i<dimensions.size();i++) {
            if (dimensions.get(i).getDimName().equals(plotByField)) {
                plotDimFound = true;
                plotByFieldIndex = i;
                break;
            }
        }
        if (!plotDimFound) {
            throw new ConfigException("You have stated the field " + plotByField
                    + " to plot by, but this is not defined as a dimension");
        }

        /*
         * We have read the config file, now process the lines defining the grid
         * layout (they all need to be read first so that we can calculate the
         * grid size)
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
            try {
                gridRows = Math.max(Integer.parseInt(gridIndices[1]) + 1, gridRows);
            } catch (NumberFormatException e) {
                throw new ConfigException(
                        "Grid config options must be of the form \"grid_x_y = ...\", where x is a valid integer for the row number "
                                + gridConfigLine[0]);
            }
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
        gridLayout = new String[gridRows][gridCols];
        for (String[] gridConfigLine : gridConfigLines) {
            /*
             * gridConfigLine is already guaranteed to be of length 2.
             */
            String[] gridIndices = gridConfigLine[0].split("_");
            /*
             * This looks fraught with possible exceptions, but it isn't, since
             * they will have been thrown in the previous section
             */
            String gridContents = gridConfigLine[1].trim();
            if(".".equals(gridContents)) {
                gridContents = "";
            }
            gridLayout[Integer.parseInt(gridIndices[1])][Integer.parseInt(gridIndices[2].trim())] = gridContents;
        }
    }
    
    public String getPath() {
        return path;
    }

    public String getNameFormat() {
        return nameFormat;
    }

    public String getPlotByField() {
        return plotByField;
    }
    
    public int getPlotByFieldIndex() {
        return plotByFieldIndex;
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public String[][] getGridLayout() {
        return gridLayout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dimensions == null) ? 0 : dimensions.hashCode());
        result = prime * result + Arrays.hashCode(gridLayout);
        result = prime * result + ((nameFormat == null) ? 0 : nameFormat.hashCode());
        result = prime * result + ((plotByField == null) ? 0 : plotByField.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Config other = (Config) obj;
        if (dimensions == null) {
            if (other.dimensions != null)
                return false;
        } else if (!dimensions.equals(other.dimensions))
            return false;
        if (!Arrays.deepEquals(gridLayout, other.gridLayout))
            return false;
        if (nameFormat == null) {
            if (other.nameFormat != null)
                return false;
        } else if (!nameFormat.equals(other.nameFormat))
            return false;
        if (plotByField == null) {
            if (other.plotByField != null)
                return false;
        } else if (!plotByField.equals(other.plotByField))
            return false;
        return true;
    }

    public class ConfigException extends Exception {
        private static final long serialVersionUID = 1L;

        public ConfigException(String message) {
            super(message);
        }
    }
}
