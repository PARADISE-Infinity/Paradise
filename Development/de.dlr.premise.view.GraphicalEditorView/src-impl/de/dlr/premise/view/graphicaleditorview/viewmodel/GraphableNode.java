/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.viewmodel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry;

/**
 *
 */
public class GraphableNode extends GraphableObject implements Observer {

    public static final GraphableNode NULL_NODE = new GraphableNode("") {

        @Override
        public void setName(String name) {
        }

        @Override
        public void setColor(Color color) {
        }

        @Override
        public GraphableNode addChildren(GraphableNode... graphableNodes) {
            return this;
        }

        @Override
        public GraphableNode addEdges(GraphableEdge... graphableEdges) {
            return this;
        }

        @Override
        public void setParent(GraphableNode parent) {
        }

        @Override
        public void delete() {
        }
    };

    private GraphableNode parent = NULL_NODE;

    private List<GraphableNode> children = new ArrayList<>();

    private List<GraphableEdge> edges = new ArrayList<>();

    private Icon icon = Icon.ROUND_RECT;

    private boolean secondary = false;

    /**
     * @param name
     */
    public GraphableNode(String name) {
        super(name);
        setColor(Color.decode("#D4D4D4"));
        setChanged();
        notifyObservers();
    }

    /**
     * 
     */
    public GraphableNode(String name, GraphableNode parent) {
        this(name);
        setParent(parent);
    }

    /**
     * @return the children
     */
    public List<GraphableNode> getChildren() {
        return children;
    }

    /**
     * Convenience method for adding children.
     */
    public GraphableNode addChildren(GraphableNode... graphableNodes) {
        children.addAll(Arrays.asList(graphableNodes));
        for (GraphableNode node : graphableNodes) {
            node.addObserver(this);
        }
        setChanged();
        notifyObservers();
        return this;
    }

    public void removeChildren(GraphableNode... graphableNodes) {
        children.removeAll(Arrays.asList(graphableNodes));
        setChanged();
        notifyObservers();
    }

    /**
     * @return the edges
     */
    public List<GraphableEdge> getEdges() {
        return edges;
    }

    /**
     * Convenience method for adding edges.
     */
    public GraphableNode addEdges(GraphableEdge... graphableEdges) {
        edges.addAll(Arrays.asList(graphableEdges));
        setChanged();
        notifyObservers();
        return this;
    }

    public void removeEdges(GraphableEdge... graphableEdges) {
        edges.removeAll(Arrays.asList(graphableEdges));
        setChanged();
        notifyObservers();
    }

    /**
     * @return the parent
     */
    public GraphableNode getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(GraphableNode parent) {
        if (parent == NULL_NODE) {
            MapperRegistry.getGraphableModel().addRootNodes(this);
            setChanged();
        }
        if (this.parent != parent) {
            this.parent.removeChildren(this);
            this.parent = parent;
            parent.addChildren(this);
            setChanged();
        }
    }

    @Override
    public void delete() {
        for (GraphableEdge edge : new ArrayList<>(edges)) {
            edge.delete();
        }
        parent.removeChildren(this);
        setPresent(false);
        setChanged();
        notifyObservers();
        MapperRegistry.getGraphableModel().removeRootNodes(this);
    }

    /**
     * @param object
     * @return
     */
    public boolean contains(GraphableObject object) {
        if (children.contains(object) || edges.contains(object))
            return true;

        for (GraphableNode child : children) {
            if (child.contains(object))
                return true;
        }

        for (GraphableEdge edge : edges) {
            if (edge.equals(object))
                return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object obj) {
        setChanged();
        if (obj == null) {
            notifyObservers(o);
        } else {
            notifyObservers(obj);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "GraphableNode [name=" + getName() + "]";
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
        setChanged();
    }

    /**
     * @return if this node is part of the secondary tree (i.e. function-tree)
     */
    public boolean isSecondary() {
        return secondary;
    }

    /**
     * @param secondary whether this node should be part of the secondary tree (i.e. function-tree)
     */
    public void setSecondary(boolean secondary) {
        this.secondary = secondary;
    }
    
    /**
     * Reset the color to the default
     */
    public void resetColor() {
        setColor(Color.decode("#D4D4D4"));
    }

}
