/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.application.rcp;

@SuppressWarnings("restriction")
public class ShowViewContributionItem extends org.eclipse.ui.internal.ShowViewMenu {
    public ShowViewContributionItem() {
        this("de.dlr.application.rcp.ShowView");
    }
    public ShowViewContributionItem(String id) {
        super(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow(), id);
    }
}