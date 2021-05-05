/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl.xtext.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.editor.contentassist.FQNPrefixMatcher.LastSegmentFinder;

import de.dlr.calc.engine.dsl.xtext.ui.contentassist.CalcLastSegmentFinder;

/**
 * Use this class to register components to be used within the IDE.
 */
public class CalcDslUiModule extends de.dlr.calc.engine.dsl.xtext.ui.AbstractCalcDslUiModule {
	public CalcDslUiModule(AbstractUIPlugin plugin) {
		super(plugin);
	}
	
    public Class<? extends LastSegmentFinder> bindLastSegmentFinder() {
        return CalcLastSegmentFinder.class;
    }
}
