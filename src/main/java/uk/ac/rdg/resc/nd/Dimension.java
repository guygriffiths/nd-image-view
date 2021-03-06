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

import java.util.List;

/**
 * A class defining a dimension which images in the viewer can vary along.
 *
 * @author Guy Griffiths
 */
public class Dimension {
    /** The ID of the dimension */
    private final String dimName;
    /** The title of the dimension */
    private final String dimTitle;
    /** The valid values of the dimension */
    private final List<String> values;

    /**
     * Create a new dimension
     * 
     * @param dimName
     *            The id of the dimension (i.e. what it is referred to in the
     *            name format)
     * @param dimTitle
     *            The title of the dimension to be displayed in the coordinate
     *            selector
     * @param values
     *            A {@link List} of values which this dimension can take
     */
    public Dimension(String dimName, String dimTitle, List<String> values) {
        this.dimName = dimName;
        this.dimTitle = dimTitle;
        this.values = values;
    }

    /**
     * @return The name of the dimension
     */
    public String getDimName() {
        return dimName;
    }

    /**
     * @return The title of the dimension
     */
    public String getDimTitle() {
        return dimTitle;
    }

    /**
     * @return The valid values of the dimension
     */
    public List<String> getValues() {
        return values;
    }
}
