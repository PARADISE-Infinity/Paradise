/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.excel.tradeoff.xtend;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

public class RelativeExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

    @Override
    protected Bundle getBundle() {
        return Activator.getDefault().getBundle();
    }

    @Override
    protected Injector getInjector() {
        return Activator.getDefault().getInjector(Activator.COMMAND.RELATIVE);
    }

}
