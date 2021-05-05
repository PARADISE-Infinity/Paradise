/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.safetyview.helper;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EContentAdapter;

import de.dlr.premise.view.safetyview.SafetyViewPage;

public class SafetyChangedContentAdapter extends EContentAdapter {
    
    
    private SafetyViewPage safetyView;
    private boolean isDisposing = false;

    public SafetyChangedContentAdapter(SafetyViewPage safetyView){
        super();
        this.safetyView = safetyView;
    }
    
    /**
     * This method is to be called when all instances of this adapter are to be deleted,
     * so that no more notifications are sent.
     * 
     * There is no need to revert this, nor does one need to check the state.
     */
    public void setDisposing(){
        isDisposing = true;
    }
    
    @Override
    public void notifyChanged(Notification notification){
        // only notify if a Safety Editor is open
        if (!isDisposing) {
            super.notifyChanged(notification);
            safetyView.refreshObservedList();
        }
            
    }
    
}
