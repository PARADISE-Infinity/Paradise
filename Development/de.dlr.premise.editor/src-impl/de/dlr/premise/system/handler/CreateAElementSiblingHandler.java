package de.dlr.premise.system.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCasePackage;
import de.dlr.premise.functions.UseCaseRepository;
import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;

/*
 * This creates an instance of AElement
 */
public class CreateAElementSiblingHandler extends ACreateAElementHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    
        EObject currentObject = getCurrentObject(event);

		// check current object
		if (currentObject != null) {

			// get parent, target and the new element
            ADataItem  newElement = getNewElement(currentObject);
            EObject    parent = currentObject;
            EReference target = getTarget(currentObject);

            // reset parent and target for sibling handling
            if (currentObject instanceof SystemComponent) {
                target = SystemPackage.Literals.SYSTEM_COMPONENT__CHILDREN;
                parent = currentObject.eContainer();
                if (parent instanceof ProjectRepository) {
                    target = SystemPackage.Literals.PROJECT_REPOSITORY__PROJECTS;                	
                }
            } 
            
            if (currentObject instanceof UseCase) {
                target = UseCasePackage.Literals.USE_CASE__CHILDREN;
                parent = currentObject.eContainer();
                if (parent instanceof UseCaseRepository) {
                    target = UseCasePackage.Literals.USE_CASE_REPOSITORY__USECASES;                	
                }
            }

            // create and execute command
            executeCommand(event, newElement, parent, target);
		}
		return null;
	}
}
