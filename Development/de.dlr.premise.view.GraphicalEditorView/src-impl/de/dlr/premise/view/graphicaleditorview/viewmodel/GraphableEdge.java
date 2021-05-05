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

/**
 *
 */
public class GraphableEdge extends GraphableObject {

    private GraphableNode source = GraphableNode.NULL_NODE;

    private GraphableNode target = GraphableNode.NULL_NODE;

    private boolean bidirectional = false;

    /**
     * @param name
     */
    public GraphableEdge(String name) {
        this(name, GraphableNode.NULL_NODE, GraphableNode.NULL_NODE);
    }

    /**
     * 
     */
    public GraphableEdge(String name, GraphableNode source, GraphableNode target) {
        super(name);
        setSource(source);
        setTarget(target);
        setColor(Color.black);
        setChanged();
        notifyObservers();
    }

    /**
     * @return the source
     */
    public GraphableNode getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(GraphableNode source) {
        if (this.source != source) {
            this.source.removeEdges(this);
            this.source = source;
            source.addEdges(this);
            addObserver(source);
            setChanged();
        }
    }

    /**
     * @return the target
     */
    public GraphableNode getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(GraphableNode target) {
        if (this.target != target) {
            this.target.removeEdges(this);
            this.target = target;
            target.addEdges(this);
            setChanged();
        }
    }

    /**
     * @return if this edge is bidirectional
     */
    public boolean isBidirectional() {
        return bidirectional;
    }

    /**
     * @param bidirectional whether this edge should be bidirectional
     */
    public void setBidirectional(boolean bidirectional) {
        if (this.bidirectional != bidirectional) {
            this.bidirectional = bidirectional;
            setChanged();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dlr.premise.view.graphicaleditorview.model.GraphableObject#delete()
     */
    @Override
    public void delete() {
        source.removeEdges(this);
        target.removeEdges(this);
        setPresent(false);
        setChanged();
        notifyObservers();
    }

}
