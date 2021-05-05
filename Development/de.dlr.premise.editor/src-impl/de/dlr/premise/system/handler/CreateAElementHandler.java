package de.dlr.premise.system.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import de.dlr.premise.registry.ADataItem;

/*
 * This creates an instance of AElement
 */
public class CreateAElementHandler extends ACreateAElementHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    
        EObject currentObject = getCurrentObject(event);

		// check current object
		if (currentObject != null) {
		    
			// get parent, target and the new element
			ADataItem  newElement = getNewElement(currentObject);
            EObject    parent     = currentObject;
            EReference target     = getTarget(currentObject);

            // create and execute command
            executeCommand(event, newElement, parent, target);
		}
		return null;
	}
}
