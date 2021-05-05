package de.dlr.premise.system.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.dlr.premise.component.ComponentDefinition;
import de.dlr.premise.component.ComponentFactory;
import de.dlr.premise.component.ComponentPackage;
import de.dlr.premise.component.ParameterDefinition;
import de.dlr.premise.functions.RequiredParameter;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCaseFactory;
import de.dlr.premise.functions.UseCasePackage;
import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.SystemPackage;

/*
 * This creates an empty parameter added to the selected system component
 */
public class CreateParameterHandler extends ACreateAElementHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    
		// get selection and current Object
        EObject currentObject = getCurrentObject(event);

		// check current object
		if (currentObject != null) {    
		    
			// get parent, target and the new element
            ADataItem  newElement = getNewElement(currentObject);
            EObject    parent = currentObject;
            EReference target = getTarget(currentObject);
		    
    	    if (currentObject instanceof SystemComponent) {
    	    	target = SystemPackage.Literals.SYSTEM_COMPONENT__PARAMETERS;
            }
    	    
    	    if (currentObject instanceof UseCase) {    	    	
                target = UseCasePackage.Literals.USE_CASE__REQUIRED_PARAMETERS;
    	    }
    	    
            if (currentObject instanceof ComponentDefinition) {
                target = ComponentPackage.Literals.COMPONENT_DEFINITION__PARAMETERS;
            }

            if (currentObject instanceof Parameter) {
            	parent = currentObject.eContainer();
            	target = SystemPackage.Literals.SYSTEM_COMPONENT__PARAMETERS;
            }
            
            if (currentObject instanceof RequiredParameter) {
            	parent = currentObject.eContainer();
                target = UseCasePackage.Literals.USE_CASE__REQUIRED_PARAMETERS;            	
            }

            // create and execute command
            executeCommand(event, newElement, parent, target);
            
		}
		return null;
	}

	@Override
	protected ADataItem getNewElement(final Object object) {
		
    	ADataItem  newElement = super.getNewElement(object);
    	
	    if (object instanceof SystemComponent) {
	    	newElement = createEmptyParameter();
	    }
	    
	    if (object instanceof UseCase) {
	    	newElement = createEmptyRequiredParameter(); 
	    }
	    
	    if (object instanceof ComponentDefinition) {
	    	newElement = createEmptyParameterDefintion();
	    }
	    
	    if (object instanceof Parameter) {
	    	newElement = createEmptyParameter();	    	
	    }

	    if (object instanceof RequiredParameter) {
	    	newElement = createEmptyRequiredParameter();	    	
	    }

	    
	    return newElement;
	}

	@Override
	protected EReference getTarget(final Object object) {
        EReference target = null;
        return target;
	}

	@Override
    protected void executeCommand(ExecutionEvent event, ADataItem newElement, 
			EObject parent, EReference target) {
		
		IStructuredSelection sel = getSelection(event);

		// create and execute command
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        if (activeEditor instanceof IEditingDomainProvider) {
            EditingDomain editingDomain = ((IEditingDomainProvider) activeEditor).getEditingDomain();
            AbstractCommand com = new CreateChildCommand(editingDomain, parent, target, newElement, sel.toList());
            editingDomain.getCommandStack().execute(com);
        } else {
        	if (parent instanceof SystemComponent) {
                SystemComponent comp = (SystemComponent) parent;
                comp.getParameters().add((Parameter)newElement);

        	}
        	if (parent instanceof UseCase) {
                UseCase uc = (UseCase) parent;
                uc.getRequiredParameters().add((RequiredParameter)newElement);

        	}
        	if (parent instanceof ComponentDefinition) {
                ComponentDefinition cd = (ComponentDefinition) parent;
                cd.getParameters().add((ParameterDefinition)newElement);        		
        	}
        }
	}

    protected Parameter createEmptyParameter() {
        Parameter param = SystemFactory.eINSTANCE.createParameter();
        param.setValue(RegistryFactory.eINSTANCE.createValue());
        return param;
    }
    
    protected RequiredParameter createEmptyRequiredParameter() {
    	return UseCaseFactory.eINSTANCE.createRequiredParameter();
    }

    protected ParameterDefinition createEmptyParameterDefintion() {
        return ComponentFactory.eINSTANCE.createParameterDefinition();
    }
}
