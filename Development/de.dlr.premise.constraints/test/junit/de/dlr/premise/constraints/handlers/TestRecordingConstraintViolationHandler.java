/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.constraints.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import com.google.common.collect.Streams;

import de.dlr.premise.constraints.ConstraintViolationKind;

public class TestRecordingConstraintViolationHandler implements IConstraintViolationHandler {

    private class TestCacheMap extends HashMap<EObject, List<Object>> {

        private static final long serialVersionUID = 3004015090013389497L;

        @Override
        public List<Object> get(Object k) {
            if (!containsKey(k)) {
                put((EObject) k, new ArrayList<Object>());
            }
            return super.get(k);
        }
    }

    private final Map<EObject, List<Object>> violatedCache = new TestCacheMap();
    private final Map<EObject, List<Object>> kindCache = new TestCacheMap();

    @Override
    public void removeViolations() {
        violatedCache.clear();
        kindCache.clear();
    }

    @Override
    public void removeViolations(EObject violator) {
        violatedCache.remove(violator);
        kindCache.remove(violator);
    }

    @Override
    public void addViolation(EObject violator, ConstraintViolationKind kind, Object violatedConstraint) {
        violatedCache.get(violator).add(violatedConstraint);
        kindCache.get(violator).add(kind);
    }

    public int getViolatorNumber() {
        return violatedCache.size();
    }

    public int getViolationNumber(EObject violator) {
        return violatedCache.get(violator).size();
    }

    public boolean checkInViolators(EObject... toTest) {
        return violatedCache.keySet().containsAll(Arrays.asList(toTest));
    }

    public boolean checkInViolators(Collection<? extends EObject> toTest) {
        return violatedCache.keySet().containsAll(toTest);
    }

    public boolean checkIsViolation(EObject violator, ConstraintViolationKind kind, Object violated) {
        int index = violatedCache.get(violator).indexOf(violated);
        return index != -1 && kindCache.get(violator).get(index) == kind;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (EObject violator : violatedCache.keySet()) {
            String violations = Streams.zip(violatedCache.get(violator).stream(), kindCache.get(violator).stream(), (violated, kind) -> violated + "->" + kind).collect(Collectors.joining(", "));
            sb.append(violator + ": " + violations);
            sb.append('\n');
        }
        return sb.toString();
    }
}
