/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.calculation.valuechangedcontentadapter;

import de.dlr.calc.engine.dsl.ParameterRenamer;
import de.dlr.premise.calculation.IBalancingCalculator;
import de.dlr.premise.calculation.transition.ITransitionBalancingCalculator;
import de.dlr.premise.system.ABalancing;
import de.dlr.premise.util.cyclecheck.IBalancingCycleChecker;
import system.util.my.ValueChangedContentAdapter;

public class SpiedValueChangedContentAdapter extends ValueChangedContentAdapter {

    private BalancingCycleCheckerMock cycleChecker;
    private ParameterRenamerMock parameterRenamer;
    private BalancingCalculatorMock balancingCalculator;
   
    public BalancingCalculatorMock getBalancingCalculator() {
        initBalancingCalculator();
        return balancingCalculator;
    }
    
    public BalancingCycleCheckerMock getCycleCheckAdapter() {
        initCycleChecker();
        return cycleChecker;
    }
    
    public ParameterRenamerMock getParameterRenamer() {
        initParameterRenamer();
        return parameterRenamer;
    }
    
    public void reset() {
        cycleChecker.reset();
        parameterRenamer.reset();
        balancingCalculator.reset();
    }
    
    @Override
    protected IBalancingCalculator createBalancingCalculator() {
        initBalancingCalculator();
        return balancingCalculator;
    }

    @Override
    protected ITransitionBalancingCalculator createTransitionBalancingCalculator() {
        initBalancingCalculator();
        return balancingCalculator;
    }

    @Override
    protected IBalancingCycleChecker createCycleChecker(ABalancing<?> bal) {
        initCycleChecker();
        return cycleChecker;
    }
    
    @Override
    protected ParameterRenamer createParameterRenamer() {
        initParameterRenamer();
        return parameterRenamer;
    }

    private void initBalancingCalculator() {
        if (balancingCalculator == null)  {
            balancingCalculator = new BalancingCalculatorMock();
        }
    }

    private void initCycleChecker() {
        if (cycleChecker == null) {
            cycleChecker = new BalancingCycleCheckerMock();
        }
    }

    private void initParameterRenamer() {
        if (parameterRenamer == null) {
            parameterRenamer = new ParameterRenamerMock();
        }
    }
}
