/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.application.cli.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.xbase.lib.Pair;

import de.dlr.premise.constraints.ConstraintChecker;
import de.dlr.premise.constraints.ConstraintViolationKind;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.validation.OCLRegistration;
import system.util.my.ValueChangedContentAdapter;

public class Actions {

    private ResourceSet resourceSet;
    
    public Actions(ResourceSet resourceSet ) {
        this.resourceSet = resourceSet;
    }

    /**
     * Do the calculation of a premise model.
     */
    public void calculate() {

        ValueChangedContentAdapter adapter = new ValueChangedContentAdapter();        
        adapter.recalculate(resourceSet, new NullProgressMonitor());
    }

    /**
     * check all constraints of a premise model. 
     */
    public List<String> checkConstraints() {    

        CLConstraintViolationHandler handler = new CLConstraintViolationHandler();
        ConstraintChecker constrChecker = new ConstraintChecker(handler);

        resourceSet.eAdapters().add(constrChecker);
        constrChecker.recheck(resourceSet);

        ArrayList<String> violations = new ArrayList<String>(); 
        for(Map.Entry<EObject, Pair<ConstraintViolationKind, Object>> entry : handler.getViolatorCache().entries()) {              
            EObject violator = entry.getKey();
            ConstraintViolationKind kind = entry.getValue().getKey();
            // Object violated = entry.getValue().getValue();
            if (violator instanceof AValueDef) {
                violator = violator.eContainer();
            }
            violations.add(getName(violator) + " : " + kind);
        }
        
        return violations;
    }

    /**
     * Validate a premise model.
     */
    public List<String> validate() {
        OCLRegistration.register();
        
        Diagnostician diagnostician = new Diagnostician();
        BasicDiagnostic diagnostic = new BasicDiagnostic();

        for (Resource res : resourceSet.getResources()) {
            if (res.getContents().size() > 0 && !"ecore".equals(res.getURI().fileExtension())) {
                try {
                    diagnostician.validate(res.getContents().get(0), diagnostic);
                } catch (IndexOutOfBoundsException e) {
                    // ignore EMF bug happening when deleting elements
                }
            }
        }

        List<String> result = new ArrayList<>();
        for (Diagnostic diagnose: diagnostic.getChildren()) {
            String source = getName(diagnose.getData().get(0));
            String severity = getSeverity(diagnose.getSeverity());
            
            result.add(source + " : " + diagnose.getMessage() + " [" + severity + "]");
        }

        return result;      
    }

    protected String getName(Object obj) {
        String source = null;
        if (obj instanceof ANameItem) {
            source = PremiseHelper.getMeaningfulName((ANameItem) obj);
        }
        if (source == null || "".equals(source)) {
            source = EcoreUtil.getURI((EObject) obj).fragment();
        }
        return source;
    }     

    /**
     * @param severity
     * @return
     *  Returns severity string for print outs
     */
    private String getSeverity(int severity) {

        String name = "cancel";

        switch (severity) {
        case Diagnostic.OK:
            name  = "ok";
            break;

        case Diagnostic.WARNING:                
            name  = "warning";
            break;
        case Diagnostic.ERROR:                
            name  = "error";
            break;
        default:
            break;
        }
        return name;
    }    
}
