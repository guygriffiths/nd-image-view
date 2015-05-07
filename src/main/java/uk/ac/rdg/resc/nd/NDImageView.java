/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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
import java.io.FileNotFoundException;
import java.io.IOException;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import uk.ac.rdg.resc.nd.Config.ConfigException;

/**
 * Main class for the NDImageView application
 *
 * @author Guy Griffiths
 */
public class NDImageView extends Application {
    private static final int GAP = 8;
    private static final int BORDER = 4;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        /*
         * Load the config file or output a useful error message and quit
         */
        Config conf;
        try {
            conf = new Config(new File("settings.cfg"));
        } catch (ConfigException e) {
            System.out.println("Problem with settings.cfg: ");
            System.out.println(e.getMessage());
            primaryStage.close();
            return;
        } catch (FileNotFoundException e) {
            System.out
                    .println("No settings.cfg present.  A file named \"settings.cfg\" should be present in the same directory as this program");
            primaryStage.close();
            return;
        } catch (IOException e) {
            System.out.println("Problem reading settings.cfg present.  Stack trace follows:");
            e.printStackTrace();
            primaryStage.close();
            return;
        }

        /*
         * Create the controller and selector view
         */
        ImageController controller = new ImageController(conf, primaryStage);
        VariableSelector selector = new VariableSelector(controller);

        /*
         * General application settings
         */
        primaryStage.setTitle("N-dimensional Image View");
        primaryStage.setFullScreen(true);

        /*
         * The grid which will hold the images and settings
         */
        GridPane grid = new GridPane();
        grid.getStyleClass().add("nd-grid-main");
        grid.setHgap(GAP);
        grid.setVgap(GAP);
        grid.setPadding(new Insets(BORDER));

        String[][] gridLayout = conf.getGridLayout();

        double[] cWidths = conf.getColWidths();
        double[] rHeights = conf.getRowHeights();

        /*
         * Set the row / column sizes
         */
        for (int i = 0; i < conf.getNRows(); i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(rHeights[i] * 100);
            grid.getRowConstraints().add(rc);
        }
        for (int j = 0; j < conf.getNCols(); j++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(cWidths[j] * 100);
            grid.getColumnConstraints().add(cc);
        }

        boolean settingsAdded = false;
        for (int i = 0; i < conf.getNRows(); i++) {
            /* Rows */
            for (int j = 0; j < conf.getNCols(); j++) {
                /* Columns */
                String var = gridLayout[i][j];
                if (var != null) {
                    if (Config.SETTINGS.equals(var)) {
                        /*
                         * We want the settings dialogue here
                         */
                        if (settingsAdded) {
                            System.out
                                    .println("You may only define one location for the variable selector in settings.cfg");
                            primaryStage.close();
                            return;
                        }
                        grid.add(selector, j, i);
                        GridPane.setHgrow(selector, Priority.ALWAYS);
                        GridPane.setVgrow(selector, Priority.ALWAYS);
                        settingsAdded = true;
                    } else {
                        /*
                         * Add an ImageView to hold the image
                         */
                        ImageView view = new ImageView();
                        view.setCache(true);
                        grid.add(view, j, i);
                        GridPane.setHalignment(view, HPos.CENTER);
                        GridPane.setValignment(view, VPos.CENTER);
                        /*
                         * Make sure it resizes properly with the window 
                         */
                        view.fitWidthProperty().bind(
                                grid.widthProperty()
                                        .subtract(BORDER * 2 + GAP * (conf.getNCols() - 1))
                                        .multiply(cWidths[j]));
                        view.fitHeightProperty().bind(
                                grid.heightProperty()
                                        .subtract(BORDER * 2 + GAP * (conf.getNRows() - 1))
                                        .multiply(rHeights[i]));
                        view.setPreserveRatio(true);
                        /*
                         * Register it with the controller
                         */
                        controller.addImageView(view, var);
                    }
                } else {
                    /*
                     * No image has been defined for this grid space.
                     */
                    Label l = new Label("No image");
                    grid.add(l, j, i);
                    GridPane.setHgrow(l, Priority.ALWAYS);
                    GridPane.setVgrow(l, Priority.ALWAYS);
                }
            }
        }

        if (!settingsAdded) {
            System.out
                    .println("No selector positioned - you must position the variable selector by defining \"grid_i_j = ${settings}\" in settings.cfg");
            primaryStage.close();
            return;
        }

        /*
         * Trigger the images to be drawn
         */
        selector.selectImageSet();

        int WINDOW_WIDTH = 500;
        int WINDOW_HEIGHT = 500;

        grid.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        Scene scene = new Scene(grid, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/nd-image-view.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
