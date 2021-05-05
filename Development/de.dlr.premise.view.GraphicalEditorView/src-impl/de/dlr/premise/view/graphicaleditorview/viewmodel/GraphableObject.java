/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.viewmodel;

import java.awt.Color;
import java.util.Observable;

/**
 *
 */
public abstract class GraphableObject extends Observable {

    private String name = "";

    private Color color = Color.black;

    private boolean present = true;

    public GraphableObject(String name) {
        setName(name);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
        setChanged();
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
        setChanged();
    }

    /**
     * Remove this GraphableObject and any dependent ones from the model.
     */
    public abstract void delete();

    /**
     * @return Whether this GraphableObject is still present.
     */
    public boolean isPresent() {
        return present;
    }

    /**
     * @param present the value returned by {@link #isPresent()}
     */
    protected void setPresent(boolean present) {
        this.present = present;
    }
}
