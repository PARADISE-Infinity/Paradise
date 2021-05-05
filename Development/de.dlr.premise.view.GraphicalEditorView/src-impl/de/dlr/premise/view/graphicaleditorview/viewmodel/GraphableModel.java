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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 *
 */
public class GraphableModel extends Observable implements IGraphableModel, Observer, Iterable<GraphableObject> {

    private Set<GraphableNode> rootNodes = new HashSet<>();
    private Icon defaultNodeIcon = Icon.ROUND_RECT;

    /*
     * (non-Javadoc)
     * 
     * @see de.dlr.premise.view.graphicaleditorview.model.IGraphableModel#getRootNodes()
     */
    @Override
    public Set<GraphableNode> getRootNodes() {
        return rootNodes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dlr.premise.view.graphicaleditorview.model.IGraphableModel#setRootNodes(java.util.List)
     */
    @Override
    public void setRootNodes(Collection<GraphableNode> nodes) {
        rootNodes = new HashSet<>();
        for (GraphableNode node : nodes) {
            if (!this.contains(node)) {
                rootNodes.add(node);
                node.addObserver(this);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dlr.premise.view.graphicaleditorview.model.IGraphableModel#addRootNodes(de.dlr.premise.view.graphicaleditorview.model.
     * GraphableNode[])
     */
    @Override
    public void addRootNodes(GraphableNode... graphableNodes) {
        for (GraphableNode gNode : graphableNodes) {
            rootNodes.add(gNode);
            gNode.addObserver(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.dlr.premise.view.graphicaleditorview.model.IGraphableModel#removeRootNodes(de.dlr.premise.view.graphicaleditorview.model.
     * GraphableNode[])
     */
    @Override
    public void removeRootNodes(GraphableNode... graphableNodes) {
        for (GraphableNode gNode : graphableNodes) {
            setChanged();
            notifyObservers(gNode);
            if (rootNodes.remove(gNode)) {
                addRootNodes(gNode.getChildren().toArray(new GraphableNode[gNode.getChildren().size()]));
                gNode.deleteObserver(this);
            }
        }
    }

    public boolean contains(GraphableObject object) {
        if (rootNodes.contains(object)) {
            return true;
        }
        for (GraphableNode root : rootNodes) {
            if (root.contains(object)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        // TODO check if notifying is really necessary all the time
        setChanged();
        if (arg == null) {
            notifyObservers(o);
        } else {
            notifyObservers(arg);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<GraphableObject> iterator() {
        return new Iterator<GraphableObject>() {

            List<GraphableObject> list = new LinkedList<>();
            int index = 0;
            {
                addToList(rootNodes);
            }

            private void addToList(Collection<GraphableNode> toAdd) {
                for (GraphableNode node : toAdd) {
                    list.add(node);
                    list.addAll(node.getEdges());
                    addToList(node.getChildren());
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public GraphableObject next() {
                return list.get(index++);
            }

            @Override
            public boolean hasNext() {
                return list.size() > index;
            }
        };
    }

    /**
     * @return the defaultNodeIcon
     */
    public Icon getDefaultNodeIcon() {
        return defaultNodeIcon;
    }

    /**
     * @param defaultNodeIcon the defaultNodeIcon to set
     */
    public void setDefaultNodeIcon(Icon defaultNodeIcon) {
        this.defaultNodeIcon = defaultNodeIcon;
    }

}
