/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.ocl.hacks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ocl.pivot.Type;
import org.eclipse.ocl.pivot.internal.evaluation.PivotModelManager;
import org.eclipse.ocl.pivot.internal.library.executor.LazyModelManager;
import org.eclipse.ocl.pivot.internal.utilities.EnvironmentFactoryInternal;

/**
 * A ModelManager is the backing implementation of the Type.allInstances() operation in OCL. For a given {@link EObject} provided as context
 * it returns all instances of a provided type in a freely chosen scope. By default, the implementation provided by {@link LazyModelManager}
 * is used, which uses the context object's containing resource as scope.
 * 
 * This implementation uses the resource set if possible and falls back to the resource if none can be found. By that, it allows for
 * elements of the registry to be referred from the premise file. It is largely taken from {@link LazyModelManager} and modified to fit our
 * needs.
 * 
 * @author steh_ti
 */
public class ResourceSetAwarePivotModelManager extends PivotModelManager {

	private final Map<Type, Set<EObject>> modelManager = new HashMap<Type, Set<EObject>>();
    private Collection<EObject> roots;

    /**
     * Taken from {@link LazyModelManager} and modified to use a resource's resource set if possible
     * 
     * Initializes me with the context element of an OCL expression evaluation. I discover the scope of the model from this element.
     * 
     * @param context my context element
     */
    public ResourceSetAwarePivotModelManager(EnvironmentFactoryInternal environmentFactory, EObject context) {
		super(environmentFactory, context);

        context = EcoreUtil.getRootContainer(context);
        if (context.eResource() != null) {
            if (context.eResource().getResourceSet() != null) {
                roots = new ArrayList<EObject>();
                // the extent is the resource set
                for (Resource res : context.eResource().getResourceSet().getResources()) {
                    roots.addAll(res.getContents());
                }
            } else {
                roots = context.eResource().getContents(); // the extent is the resource
            }
        } else {
            roots = Collections.singleton(context); // can only search this object tree
        }
    }

    /**
     * Taken from {@link LazyModelManager#get(DomainType)} unmodified. It is only present here, since we need it to use our modified roots
     * field instead of the original, which we cannot change since it is private to the {@link LazyModelManager}.
     * 
     * Lazily computes the extent of the specified class <code>key</code>.
     * 
     * @param key a class in the model
     */
    @Override
	public Set<EObject> get(org.eclipse.ocl.pivot.Class type) {
		// TODO: Optimize by parsing ahead of time to find all EClasses that we will query
		Set<EObject> result = modelManager.get(type);		
		if (result == null) {
			synchronized (modelManager) {
				result = modelManager.get(type);		
				if (result == null) {
					result = new HashSet<EObject>();
					modelManager.put(type, result);			
					for (Iterator<EObject> iter = EcoreUtil.getAllContents(roots); iter.hasNext();) {
						EObject next = iter.next();				
						if ((next != null) && isInstance(type, next)) {
							result.add(next);
						}
					}
				}		
			}
		}		
		// FIXME subclasses
		return result;
	}
}