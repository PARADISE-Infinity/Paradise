/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.safetyview;

import java.util.LinkedList;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.dlr.premise.element.AElement;
import de.dlr.premise.element.ARepository;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.registry.Registry;
import de.dlr.premise.safety.impl.my.FailureHelper;
import de.dlr.premise.system.Parameter;

public class SafetyContentProvider implements ITreeContentProvider {

    private Boolean isSourceTree;

    public SafetyContentProvider(Boolean isSourceTree) {
        super();
        this.isSourceTree = isSourceTree;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInputObj, Object newInputObj) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getElements(Object obj) {
        Object[] retList = ((LinkedList<EObject>) obj).toArray();
        return retList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getChildren(Object parentElement) {
        LinkedList<EObject> retList = new LinkedList<EObject>();
        if (parentElement instanceof LinkedList<?>) {
            for (EObject eObj : ((LinkedList<EObject>) parentElement)) {
                for (EObject obj : ((EObject) eObj).eContents()) {
                    if (instanceofRecognizedElement(obj)) {
                        retList.add(obj);
                    }
                }
            }
        } else {
            for (EObject obj : ((EObject) parentElement).eContents()) {
                if (instanceofRecognizedElement(obj)) {
                    retList.add(obj);
                }
            }
        }
        return retList.toArray();
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof ARepository)
            return null;
        else
            return ((EObject) element).eContainer();
    }

    @Override
    public boolean hasChildren(Object obj) {
        EObject element = (EObject) obj;
        if(isSourceTree && obj instanceof Parameter){
            return false;
        }
        return !element.eContents().isEmpty();
    }

    /*
     * Check if a Object is an instanceof an element that should be displayed in the Parameter Viewer
     * Supported Elements: AElement, StateMachine
     * */
    public static Boolean instanceofRecognizedElement(Object element) {

        boolean isElement = false;

        isElement = (element instanceof StateMachine ||
                     element instanceof AElement     ||
                     element instanceof ARepository  ||
                     element instanceof Registry );

        // in case we have a state machine only failure state machines should be recognized
        if (element instanceof StateMachine) {
            isElement = FailureHelper.validFailureStateMachine((StateMachine) element);
        }

        return isElement;
    }
}
