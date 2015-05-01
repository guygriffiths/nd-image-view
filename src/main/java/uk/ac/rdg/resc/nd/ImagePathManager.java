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
import java.util.List;

public class ImagePathManager {
    private Config config;

    public ImagePathManager(Config config) {
        this.config = config;
    }

    public List<Dimension> getVariableDimensions() {
        List<Dimension> variableDims = new ArrayList<>();
        for (Dimension d : config.getDimensions()) {
            if (!d.getDimName().equals(config.getPlotByField())) {
                variableDims.add(d);
            }
        }
        return variableDims;
    }

    public File getPath(String... coords) {
        if (coords.length != config.getDimensions().size()) {
            return null;
        }

        String name = config.getNameFormat();
        for (int i = 0; i < coords.length; i++) {
            String dimName = config.getDimensions().get(i).getDimName();
            if (!coords[i].isEmpty()) {
                name = name.replaceAll("\\??\\$\\{" + dimName + "\\}", coords[i]);
            } else {
                /*
                 * This deals with the case where we have an empty dimension
                 * value.
                 * 
                 * Name format strings can have a character which should be
                 * omitted if it immediately proceeds an empty dimension,
                 * indicated by a question mark
                 */
                name = name.replaceAll(".\\?\\$\\{" + dimName + "\\}", coords[i]);
                name = name.replaceAll("\\$\\{" + dimName + "\\}", coords[i]);
            }
        }
        File file = new File(config.getPath() + name);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }
    }
}
