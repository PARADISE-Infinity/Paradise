/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.calculation.transition

import de.dlr.premise.element.AGuardCondition
import de.dlr.premise.element.GuardCombination
import de.dlr.premise.element.Mode
import de.dlr.premise.element.ModeGuard
import de.dlr.premise.element.Transition
import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.registry.Junction
import de.dlr.premise.system.TransitionBalancing
import de.dlr.premise.system.TransitionParameter
import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.common.util.EList
import system.util.my.CalcHelper
import de.dlr.premise.states.StateHelper
import de.dlr.premise.util.scope.ScopedEObjectFactory

class TransitionBalancingCalculator implements ITransitionBalancingCalculator {
	
	private StochasticCalcEngine calcEngine;
    
    override boolean calculate(TransitionBalancing balancingUnscoped) {
    	val balancing = ScopedEObjectFactory.INSTANCE.createScopedEObjectForNullscope(balancingUnscoped);
    	
    	var handled = false;
        calcEngine = new StochasticCalcEngine();
        var String result = "0";
        var AParameterDef target 
        var AGuardCondition rootGuardCon 
      
      	try {
      		target = balancing.getTarget();
        	rootGuardCon = (target.eContainer as Transition).condition
      	} catch (NullPointerException npe){}
      
        
		result = handleGuardConditions(rootGuardCon, balancing)
        handled = true;
        
        if (result != null) {
        	StateHelper.setValue(target, null, result);
        }
        return handled;
    }
    
  
    private def String handleGuardConditions(AGuardCondition constr, TransitionBalancing transBal) {
		var String result = null;
		var EList<Double> dblValues = new BasicEList<Double>();

		// if condition is GuardCombination
		if (constr != null && constr instanceof GuardCombination) {

			// traverse all sub conditions
			var GuardCombination nestedConstr = constr as GuardCombination;
			for (AGuardCondition childConstr : nestedConstr.getChildren()) {
				// recursive call with childConditions
				var strVal = handleGuardConditions(childConstr, transBal);
				if (strVal != null) {
				    dblValues.add(Double.valueOf(strVal));
				} else {
                    return null;
                }
			}
			
			
			if (nestedConstr.getJunction() == Junction.OR) {
				result = calcEngine.or(dblValues).toString
			} else if (nestedConstr.getJunction() == Junction.AND) {
				result = calcEngine.and(dblValues).toString
			} else if (nestedConstr.getJunction() == Junction.XOR) {
				result = calcEngine.xor(dblValues).toString
			} else if (nestedConstr.getJunction() == Junction.VOTE) {
				result = calcEngine.vote(dblValues).toString
			} else if (nestedConstr.getJunction() == Junction.NOT) {
				result = calcEngine.not(dblValues).toString
			} else if (nestedConstr.getJunction() == Junction.PAND) {
				result = "0";
				System.err.println("PAND is not supported in Calculation")
			}

            
            
                   
		} else {
			// get parameter value related to current GuardCombination
			if (constr == null || ( constr as ModeGuard ).getMode() == null) {
				return null;
			}
            var Mode trigger = ( constr as ModeGuard ).getMode();
            for (Transition transition : CalcHelper.getTransitionsReferencingMode(trigger)) {
                var EList<TransitionParameter> tparams = new BasicEList<TransitionParameter>();
                if (transition instanceof Transition) {
                    tparams.addAll(transition.getParameters());
                }
                tparams.retainAll(transBal.getSources());
                for (TransitionParameter tparam : tparams) {
                    dblValues.add(Double.valueOf(tparam.getValue().getValue()));
                }
            }
            if (!dblValues.isEmpty()) {
                // accumulate parameters of incoming transitions like OR junction
                result = calcEngine.or(dblValues).toString
            }
		}
		return result;
	}
	
}
