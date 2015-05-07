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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * The settings window and main area for selecting which values of the dimension
 * show in the main window
 *
 * @author Guy Griffiths
 */
public class VariableSelector extends VBox {
    /** The {@link ImageController} which will receive events */
    private ImageController controller;
    /** The {@link ChoiceBox}s which control the dimensions */
    private List<ChoiceBox<String>> dimChoices;

    /** The last {@link ChoiceBox} whose value was changed */
    private ChoiceBox<String> lastChanged = null;
    /** The last choice in the {@link ChoiceBox} whose value was changed */
    private String lastChoice = null;
    /**
     * Flag to disable image selection for when we want to repopulate the
     * choices
     */
    private boolean disableImageSelection = true;

    /**
     * Create a new {@link VariableSelector}
     * 
     * @param controller
     *            The {@link ImageController} which will receive events and
     *            provide information
     */
    public VariableSelector(ImageController controller) {
        super(20);
        getStyleClass().add("nd-settings");

        /*
         * Add a small documentation panel
         */
        TitledPane title = new TitledPane();
        title.setText("ND Image Viewer");
        title.setCollapsible(false);
        Label docs = new Label(
                "Change the individual dimensions which make up the images below.\n\n"
                        + "Clicking the 'Switch to last' button allows you to switch back and forth between views to easily see the effects of changing a single dimension\n\n"
                        + "To configure the images, modify the settings.cfg file.  For more information see README.md");
        docs.setWrapText(true);
        title.setContent(docs);
        getChildren().add(title);

        /*
         * Toggle between current and previous view. Create this now, because
         * the action handler for the dimension chooser needs it
         */
        Button toggle = new Button("Switch to last");
        toggle.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (lastChanged != null && lastChoice != null) {
                    lastChanged.setValue(lastChoice);
                }
            }
        });
        toggle.setDisable(true);

        dimChoices = new ArrayList<>();

        /*
         * Grid pane to keep the labels and buttons
         */
        GridPane variablesGrid = new GridPane();
        variablesGrid.getStyleClass().add("nd-grid-variables");
        variablesGrid.setHgap(10);
        variablesGrid.setVgap(10);
        variablesGrid.setPadding(new Insets(20));
        variablesGrid.setMinWidth(0);

        /*
         * Add a box for each of the selectable dimensions
         */
        for (int i = 0; i < controller.getSelectableDimensions().size(); i++) {
            /*
             * Get the dimension which can be changed
             */
            Dimension dimension = controller.getSelectableDimensions().get(i);
            /*
             * Add a label for it
             */
            variablesGrid.add(new Label(dimension.getDimTitle()), 0, i);

            /*
             * Create a choice box with the dimension values
             */
            ChoiceBox<String> dimChoice = new ChoiceBox<>(
                    FXCollections.observableArrayList(dimension.getValues()));

            /*
             * If the value changes, pick a new set of images
             */
            dimChoice.valueProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue,
                        String newValue) {
                    if (!disableImageSelection) {
                        lastChanged = dimChoice;
                        lastChoice = oldValue;
                        if (lastChanged != null && lastChoice != null) {
                            toggle.setDisable(false);
                        }
                        selectImageSet();
                    }
                }
            });

            /*
             * Add it to the list of choices and to the grid
             */
            dimChoices.add(dimChoice);
            variablesGrid.add(dimChoice, 1, i);
        }

        /*
         * By default the first value from each dimension is chosen, but this
         * may not be valid.
         * 
         * We generate all possible valid combinations, and then check until we
         * have one which is valid. When this is found, set the coordinate value
         * to the valid combination.
         */
        List<String[]> allCombinations = allCoordCombinations(controller.getSelectableDimensions());
        boolean valid = false;
        for (String[] c : allCombinations) {
            for (String nonSelectableValue : controller.getNonSelectableDimension().getValues()) {
                File path = controller.getPath(nonSelectableValue, c);
                if (path != null) {
                    valid = true;
                    break;
                }
            }
            if (valid) {
                for (int i = 0; i < c.length; i++) {
                    dimChoices.get(i).setValue(c[i]);
                }
                break;
            }
        }

        getChildren().add(variablesGrid);
        getChildren().add(toggle);

        /*
         * The fullscreen button
         */
        Button toggleFullscreenButton = new Button("Toggle fullscreen");
        toggleFullscreenButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.toggleFullscreen();
            }
        });
        getChildren().add(toggleFullscreenButton);

        /*
         * The quit button
         */
        Button quit = new Button("Quit");
        quit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.quit();
            }
        });
        getChildren().add(quit);

        this.controller = controller;

        /*
         * Setup done, enable image selection
         */
        disableImageSelection = false;
    }

    /**
     * Generates all possible combinations of the available dimensions
     * 
     * @param dimensions
     *            A {@link List} of the {@link Dimension}s to generate
     *            combinations for
     * @return A {@link List} of arrays of {@link String}s which represent all
     *         possible combinations of values
     */
    private static List<String[]> allCoordCombinations(List<Dimension> dimensions) {
        /*
         * The method is destructive so we take a copy
         */
        dimensions = new ArrayList<>(dimensions);
        if (dimensions.size() == 0) {
            return new ArrayList<String[]>();
        }
        Dimension firstDimension = dimensions.remove(0);
        List<String[]> coordCombination = new ArrayList<String[]>();
        for (String coord : firstDimension.getValues()) {
            String[] coords = new String[] { coord };
            coordCombination.add(coords);
        }

        return coordCombinationsHelper(dimensions, coordCombination);
    }

    /**
     * Helper method
     * 
     * @param remainingDimensions
     *            The remaining dimensions which have not been combined yet
     * @param coordCombinations
     *            Already processed combinations
     * @return
     */
    private static List<String[]> coordCombinationsHelper(List<Dimension> remainingDimensions,
            List<String[]> coordCombinations) {
        if (remainingDimensions.size() == 0) {
            return coordCombinations;
        }
        Dimension myFirstList = remainingDimensions.remove(0);
        List<String[]> newCombinations = new ArrayList<String[]>();

        for (String[] s : coordCombinations) {
            for (String s2 : myFirstList.getValues()) {
                String[] values = Arrays.copyOf(s, s.length + 1);
                values[s.length] = s2;
                newCombinations.add(values);
            }
        }

        return coordCombinationsHelper(remainingDimensions, newCombinations);
    }

    /**
     * @return The values currently selected, as an array of {@link String}s
     */
    private String[] getSelectedValues() {
        String[] selected = new String[dimChoices.size()];
        for (int i = 0; i < selected.length; i++) {
            selected[i] = dimChoices.get(i).getValue();
        }
        return selected;
    }

    /**
     * Sets the currently selected values as the images
     */
    void selectImageSet() {
        controller.selectImageSet(getSelectedValues());
        repopulateChoices();
    }

    /**
     * Given the currently selected dimensions, repopulates the choice boxes so
     * that no set can be selected which would be entirely empty
     */
    private void repopulateChoices() {
        /*
         * Add a box for each of the selectable dimensions
         */
        for (int i = 0; i < controller.getSelectableDimensions().size(); i++) {
            /*
             * Get the dimension which can be changed
             */
            Dimension dimension = controller.getSelectableDimensions().get(i);

            ChoiceBox<String> dimChoice = dimChoices.get(i);
            String currentValue = dimChoice.getSelectionModel().getSelectedItem();

            ObservableList<String> newValues = FXCollections.observableArrayList();
            Dimension nonSelectableDimension = controller.getNonSelectableDimension();
            for (String value : dimension.getValues()) {
                String[] selectedValues = getSelectedValues();
                selectedValues[i] = value;
                /*
                 * Check the non-selectable image paths until we find one which
                 * exists (File object returned is non-null). If all images are
                 * unavailable, this value is not added
                 */
                for (String nonSelectableValue : nonSelectableDimension.getValues()) {
                    File path = controller.getPath(nonSelectableValue, selectedValues);
                    if (path != null) {
                        newValues.add(value);
                        break;
                    }
                }
            }

            /*
             * We don't want to retrigger selection and repopulation, so we
             * disable image selection before programmatically setting the value
             */
            disableImageSelection = true;
            dimChoice.setItems(newValues);
            dimChoice.setValue(currentValue);
            disableImageSelection = false;
        }
    }
}
