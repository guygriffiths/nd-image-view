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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 * A controller which accepts actions from the {@link VariableSelector} and
 * populates the {@link ImageView}s.
 *
 * @author Guy Griffiths
 */
public class ImageController {
    /** A {@link Map} of non-selectable dimension value to {@link ImageView} */
    private Map<String, ImageView> views;
    /** The {@link Config} file defining the settings */
    private Config config;
    /** The main {@link Stage} of the application */
    private Stage mainStage;

    /**
     * @param config
     *            The {@link Config} defining the settings
     * @param primaryStage
     *            The main {@link Stage} of the application
     */
    public ImageController(Config config, Stage primaryStage) {
        views = new HashMap<>();
        this.config = config;
        this.mainStage = primaryStage;
    }

    /**
     * Registers an {@link ImageView}
     * 
     * @param view
     *            The {@link ImageView}
     * @param alias
     *            The value which refers to this view
     */
    public void addImageView(ImageView view, String alias) {
        views.put(alias, view);
    }

    /**
     * Selects a set of images
     * 
     * @param coords
     *            The values of all selectable dimensions to choose
     */
    public void selectImageSet(String... coords) {
        /*
         * Check we have the right number of arguments
         */
        List<Dimension> dimensions = config.getSelectableDimensions();
        if (coords.length != dimensions.size()) {
            throw new IllegalArgumentException(
                    "Coords of image set must be equal to total number of dimensions - 1");
        }
        /*
         * For each of the ImageViews, get the path of the resultant image and
         * set the Image
         */
        for (Entry<String, ImageView> view : views.entrySet()) {
            File path = getPath(view.getKey(), coords);
            if (path != null) {
                view.getValue().setImage(new Image("file:" + path.getAbsolutePath()));
            } else {
                view.getValue().setImage(null);
            }
        }
    }

    /**
     * Quit the application
     */
    public void quit() {
        mainStage.close();
    }

    /**
     * Toggle between fullscreen / windowed
     */
    public void toggleFullscreen() {
        mainStage.setFullScreen(!mainStage.isFullScreen());
    }

    /**
     * @return A {@link List} of {@link Dimension}s which the user can select
     *         values of
     */
    public List<Dimension> getSelectableDimensions() {
        return config.getSelectableDimensions();
    }

    /**
     * @return The {@link Dimension} which varies across the screen and hence
     *         which users cannot select
     */
    public Dimension getNonSelectableDimension() {
        return config.getNonSelectableDimension();
    }

    /**
     * Gets the {@link File} associated with the given set of co-ordinates
     * 
     * @param nonSelectableValue
     *            The value for the non-selectable {@link Dimension}
     * @param selectableValues
     *            The values for the selectable {@link Dimension}s, in the same
     *            order as the dimensions returned by
     *            {@link ImageController#getSelectableDimensions()}
     * @return A {@link File} pointing to the image, or <code>null</code> if it
     *         does not exist
     */
    public File getPath(String nonSelectableValue, String... selectableValues) {
        if (selectableValues.length != getSelectableDimensions().size()) {
            return null;
        }

        String name = config.getNameFormat();
        for (int i = 0; i < selectableValues.length; i++) {
            name = doNameReplace(name, getSelectableDimensions().get(i).getDimName(),
                    selectableValues[i]);
        }
        name = doNameReplace(name, getNonSelectableDimension().getDimName(), nonSelectableValue);

        File file = new File(config.getPath() + name);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * Convenience method for replacing tokens in the name format
     * 
     * @param nameFormat
     *            The name format
     * @param dimName
     *            The name of the dimension to replace
     * @param value
     *            The value to replace the dimName with
     * @return The name format with the substitution made
     */
    private String doNameReplace(String nameFormat, String dimName, String value) {
        if (!value.isEmpty()) {
            nameFormat = nameFormat.replaceAll("\\??\\$\\{" + dimName + "\\}", value);
        } else {
            /*
             * This deals with the case where we have an empty dimension value.
             * 
             * Name format strings can have a character which should be omitted
             * if it immediately proceeds an empty dimension, indicated by a
             * question mark
             */
            nameFormat = nameFormat.replaceAll(".\\?\\$\\{" + dimName + "\\}", value);
            nameFormat = nameFormat.replaceAll("\\$\\{" + dimName + "\\}", value);
        }
        return nameFormat;
    }
}
