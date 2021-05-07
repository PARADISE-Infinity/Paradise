/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.validation;

import org.eclipse.core.resources.IFile;

import de.dlr.premise.system.extensionpoints.IBeforeModelLoad;
import de.dlr.premise.validation.OCLRegistration;
import de.dlr.premise.validation.ocl.ResourceSetAwareOCLDelegateDomainFactory;

/**
 * Runs OCLRegistration, which registers the {@link ResourceSetAwarePivotModelManager} for use with the pivot OCL environment. See
 * {@link ResourceSetAwareOCLDelegateDomainFactory} for details.
 * 
 * Note: In earlier versions, this was only done in the {@link Startup}. But when the workbench is opened with an editor being restored, the
 * code initializing the editor will run before {@link Startup} is actually executed in this project. In this case, the wrong DelegateDomain
 * is used to create the InvocationDelegates. InvocationDelegates are stored in static final fields in the model XYZImpl classes, so it's
 * impossible to change them later on.
 * 
 * By running this BeforeModelLoad, we make sure that the InvocationDelegate fields have not yet been accessed.
 * 
 * @version SVN: $Id$
 */
public class RegisterOCLBeforeModelLoad implements IBeforeModelLoad {
    @Override
    public void execute(IFile file) {
        OCLRegistration.register();
    }
}
