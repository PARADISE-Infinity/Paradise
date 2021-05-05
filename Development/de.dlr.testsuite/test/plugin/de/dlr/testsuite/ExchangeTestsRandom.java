/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.testsuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import de.dlr.testsuite.runner.RandomSuite;

@RunWith(RandomSuite.class)
@SuiteClasses({

    // de.dlr.exchange.base.xtend
    de.dlr.exchange.base.xtend.test.GeneratorHelperTest.class,

    // de.dlr.exchange.excel.trace.xtend
    de.dlr.exchange.excel.trace.xtend.GeneratorTest.class,

    // de.dlr.exchange.excel.tradeoff.xtend
    de.dlr.exchange.excel.tradeoff.xtend.GeneratorTest.class,

    // de.dlr.exchange.graphml.fta.xtend
    de.dlr.exchange.graphml.fta.xtend.test.GeneratorControlSysQualitativeTest.class,
    de.dlr.exchange.graphml.fta.xtend.test.GeneratorControlSysTest.class,
    de.dlr.exchange.graphml.fta.xtend.test.GeneratorPowertrainTest.class,
    de.dlr.exchange.graphml.fta.xtend.test.GeneratorVFW614Test.class,

    // de.dlr.exchange.matlab.struct.xtend
    de.dlr.exchange.matlab.struct.xtend.GeneratorTest.class,

    // de.dlr.exchange.petrinet.base.xtend

    // de.dlr.exchange.petrinet.pnml.pipe.xtend
    de.dlr.exchange.petrinet.pnml.pipe.test.GeneratorTest.class,

    // de.dlr.exchange.petrinet.snoopy.xtend
    de.dlr.exchange.petrinet.snoopy.test.GeneratorTest.class,

    // de.dlr.premise.view.graphml
    de.dlr.aspect.graphml.GraphMLToPremiseTest.class,
    de.dlr.aspect.graphml.PremiseToGraphMLTest.class,

    // de.systemsdesign.exchange.graphml.states.xtend
    de.dlr.exchange.graphml.states.xtend.test.GeneratorAircraftTest.class,
    de.dlr.exchange.graphml.states.xtend.test.GeneratorPowerTrainTest.class,
    de.dlr.exchange.graphml.states.xtend.test.GeneratorVFWTest.class,
})

public class ExchangeTestsRandom {

}
