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

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class NDImageView extends Application {

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Config conf = new Config(new File("settings.cfg"));
//        System.out.println(conf.getNameFormat());
//        System.out.println(conf.getPlotByField());
//        for(Dimension d : conf.getDimensions()) {
//            System.out.println(d.getDimName()+": ");
//            for(String dimVal : d.getValues()) {
//                System.out.println("\t"+dimVal);
//            }
//        }

        ImagePathManager im = new ImagePathManager(conf);
//        System.out.println(im.getPath("AATSR","argo","skin","raw","spatial"));
//        System.out.println(im.getPath("AATSR","argo","skin","raw",""));
//        System.out.println(im.getPath("AATSR","","skin","raw","asdasd"));
//        System.out.println(im.getPath("AATSR","argo","skin","raw",""));

        ImageController controller = new ImageController(conf, im);

        VariableSelector selector = new VariableSelector(controller, im);

        primaryStage.setTitle("N-dimensional Image View");
        primaryStage.setFullScreen(true);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(4));

        String[][] gridLayout = conf.getGridLayout();
        for (int i = 0; i < gridLayout.length; i++) {
            for (int j = 0; j < gridLayout[i].length; j++) {
                String var = gridLayout[i][j];
                if (var != null) {
                    if (Config.SETTINGS.equals(var)) {
                        /*
                         * We want the settings dialogue here
                         */
                        grid.add(selector, j, i);
                        GridPane.setHgrow(selector, Priority.ALWAYS);
                        GridPane.setVgrow(selector, Priority.ALWAYS);
                    } else {
                        ImageView view = new ImageView();
                        grid.add(view, j, i);
                        GridPane.setHgrow(view, Priority.ALWAYS);
                        GridPane.setVgrow(view, Priority.ALWAYS);
                        controller.addImageView(view, var);
                        view.fitWidthProperty().bind(
                                grid.widthProperty().divide(gridLayout[i].length));
                        view.setPreserveRatio(true);
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

//        grid.add(view, col, row);
//        
//        SettingsPane settings = controller.getSettingsPane();
//        grid.add(settings, col, 0);

        controller.selectImageSet("AATSR", "argo", "skin", "raw");

        int WINDOW_WIDTH = 500;
        int WINDOW_HEIGHT = 500;

        grid.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        grid.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        Scene scene = new Scene(grid, WINDOW_WIDTH, WINDOW_HEIGHT);
//        scene.getStylesheets().add(getClass().getResource("/cloudmask.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
