/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.petrinet.pnml.pipe.xtend;

import de.dlr.exchange.base.xtend.AGeneratorModule;
import de.dlr.exchange.base.xtend.IGeneratorMy;
import de.dlr.exchange.base.xtend.IOptions;
import de.dlr.exchange.petrinet.base.xtend.Options;
import de.dlr.exchange.petrinet.pnml.pipe.xtend.Generator;

public class GeneratorModule extends AGeneratorModule {

	public Class<? extends IGeneratorMy> bindIGenerator() {
		return Generator.class;
	}

    public Class<? extends IOptions> bindIOptions() {
        return Options.class;
    }
}
