/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;

import de.dlr.premise.registry.MetaData;

/**
 * Adapter to notify the GraphPopulator of model-changes.
 */
public class ModelContentAdadpter extends EContentAdapter {

    /**
     * 
     */
    public ModelContentAdadpter() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.emf.ecore.util.EContentAdapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
     */
    @Override
    public void notifyChanged(Notification notification) {
        super.notifyChanged(notification);
        if (notification.getNotifier() instanceof MetaData) {
            SelectionListener.getInstance().scheduleGraphing(300, false);
        } else {
            SelectionListener.getInstance().scheduleGraphing(300, true);
        }
    }

}
