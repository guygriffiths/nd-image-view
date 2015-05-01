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

public class ImageController {
    private Map<String, ImageView> views;
    private Config config;
    private ImagePathManager pathManager;

    public ImageController(Config config, ImagePathManager pathManager) {
        // TODO Auto-generated constructor stub
        views = new HashMap<>();
        this.config = config;
        this.pathManager = pathManager;
    }

    public void addImageView(ImageView view, String alias) {
        views.put(alias, view);
    }

    public void selectImageSet(String... coords) {
        List<Dimension> dimensions = config.getDimensions();
        if (coords.length != dimensions.size() - 1) {
            throw new IllegalArgumentException(
                    "Coords of image set must be equal to total number of dimensions - 1");
        }
        String[] pathCoords = new String[coords.length + 1];
        int offset = 0;
        for (int i = 0; i < coords.length; i++) {
            if (i == config.getPlotByFieldIndex()) {
                offset = 1;
            }
            pathCoords[i+offset] = coords[i];
        }
        for (Entry<String, ImageView> view : views.entrySet()) {
            pathCoords[config.getPlotByFieldIndex()] = view.getKey();
            File path = pathManager.getPath(pathCoords);
            if(path != null) {
                view.getValue().setImage(new Image("file:" + path.getAbsolutePath()));
            } else {
                view.getValue().setImage(null);
            }
        }
    }
}
