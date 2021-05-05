/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.parameterviewer;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.dlr.premise.element.AModeCombination;
import de.dlr.premise.element.ARepository;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.registry.Registry;
import de.dlr.premise.system.ABalancing;
import de.dlr.premise.system.ComponentReference;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;

public class ParameterContentProvider implements ITreeContentProvider {

    private Boolean isSourceTree;

    public ParameterContentProvider(Boolean isSourceTree) {
        super();
        this.isSourceTree = isSourceTree;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInputObj, Object newInputObj) {

    }

    @Override
    public Object[] getElements(Object obj) {
        return (Object[]) obj;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return getChildren((EObject) parentElement).toArray();
    }

    private List<EObject> getChildren(EObject element) {
        return element.eContents().stream().map(ParameterContentProvider::getElementOrActiveImplementation)
                      .filter(ParameterContentProvider::instanceofRecognizedElement).collect(Collectors.toList());
    }

    private static EObject getElementOrActiveImplementation(EObject element) {
        if (element instanceof ComponentReference) {
            return ((ComponentReference) element).getActiveImplementation();
        }
        return element;
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
        return !getChildren(element).isEmpty();
    }

    /*
     * Check if a Object is an instanceof an element that should be displayed in the Parameter Viewer
     * */
    public static Boolean instanceofRecognizedElement(Object element) {
        if (element instanceof SystemComponent) {
            EObject sysCom = (EObject) element;

            return !sysCom.eContents().isEmpty();
        }

        return (element instanceof AParameterDef || element instanceof SystemComponent || element instanceof AValueDef
                || element instanceof AModeCombination || element instanceof ABalancing ||element instanceof ProjectRepository || element instanceof Registry);
    }
}
