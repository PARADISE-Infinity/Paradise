/**
 * Copyright (c)
 * DLR Institute of Flight Systems, Braunschweig, Germany
 * All rights reserved
 * 
 * @version SVN: $Id$
 */
package de.dlr.premise.system.util;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;


public interface FunctionalSelectionListener extends SelectionListener {

    @Override
    default void widgetDefaultSelected(SelectionEvent e) {
        // unused
    }

}
