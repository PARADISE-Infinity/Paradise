/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.base.xtend;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import de.dlr.premise.element.AElement;

public interface IGeneratorMyWithProgress extends IGeneratorMy {
    default public void doGenerateFromAElements(ResourceSet input, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {
        doGenerateFromAElements(input, selectedElements, fsa, new NullProgressMonitor());
    }
    
    default public void doGenerateFromResources(ResourceSet input, List<Resource> selectedFiles, ICharsetProvidingFileSystemAccess fsa) {
        doGenerateFromResources(input, selectedFiles, fsa, new NullProgressMonitor());
    }
    
    public void doGenerateFromAElements(ResourceSet input, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor);

    public void doGenerateFromResources(ResourceSet input, List<Resource> selectedFiles, ICharsetProvidingFileSystemAccess fsa, IProgressMonitor monitor);
}
