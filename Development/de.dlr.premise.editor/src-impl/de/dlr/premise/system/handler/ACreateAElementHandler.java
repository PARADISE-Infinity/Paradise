package de.dlr.premise.system.handler;

import java.util.Collection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.component.ComponentDefinition;
import de.dlr.premise.component.ComponentFactory;
import de.dlr.premise.component.ComponentPackage;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCaseFactory;
import de.dlr.premise.functions.UseCasePackage;
import de.dlr.premise.functions.UseCaseRepository;

public abstract class ACreateAElementHandler implements IHandler {

	@Override
    public boolean isEnabled() {
		return true;
	}

	@Override
    public boolean isHandled() {
		return true;
	}
	
	@Override
    public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
    public void dispose() {
	}

	@Override
    public void removeHandlerListener(IHandlerListener handlerListener) {
	}
	
    protected IStructuredSelection getSelection(final ExecutionEvent event) {
    	
	    // get selection
	    IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();	    
		ISelection selection = page.getSelection();

		// get current object
		IStructuredSelection sel = null;		
		if (selection != null & selection instanceof IStructuredSelection) {
		    sel = (IStructuredSelection) selection;
		}
		
		return sel;
    }

    protected EObject getCurrentObject(ExecutionEvent event) {
        // get selection and current Object
        IStructuredSelection sel = getSelection(event);
        Object currentObject = sel.getFirstElement();

        if (currentObject instanceof DelegatingWrapperItemProvider) {
            currentObject = ((DelegatingWrapperItemProvider) currentObject).getValue();
        }

        if (currentObject instanceof EObject) {
            return (EObject) currentObject;
        }

        return null;
    }

    protected SystemComponent createEmptySystemComponent() {        
        return SystemFactory.eINSTANCE.createSystemComponent();
    }

    protected UseCase createEmptyUseCase() {
        return UseCaseFactory.eINSTANCE.createUseCase();
    }

    protected ComponentDefinition createEmptyComponentDefintion() {
        return ComponentFactory.eINSTANCE.createComponentDefinition();
    }

    /**
     * Returns the new Element from the selected object
     * @param object
     * @return
     */
    protected ADataItem getNewElement(final Object object) {
    	
    	ADataItem  newElement = null;

    	// create an empty SystemComponent
        if ((object instanceof SystemComponent) || 
            (object instanceof ProjectRepository)){             
            newElement = createEmptySystemComponent();
        } 
        
        // create an empty UseCase
        if ((object instanceof UseCase) || 
            (object instanceof UseCaseRepository)) {
            newElement = createEmptyUseCase();
        } 
        
        // create an empty ComponentDefinition
        if (object instanceof de.dlr.premise.component.ComponentRepository) {
            newElement = createEmptyComponentDefintion();
        }
        
        return newElement;
    }
    
    protected EReference getTarget(final Object object) {

        EReference target = null;

        if (object instanceof SystemComponent) {             
            target = SystemPackage.Literals.SYSTEM_COMPONENT__CHILDREN;
        }
        
        if (object instanceof ProjectRepository) {
            target = SystemPackage.Literals.PROJECT_REPOSITORY__PROJECTS;
        } 
        
        if (object instanceof UseCase) {
            target = UseCasePackage.Literals.USE_CASE__CHILDREN;
        } 	
        
        if (object instanceof UseCaseRepository) {
            target = UseCasePackage.Literals.USE_CASE_REPOSITORY__USECASES;
        }
        
        if (object instanceof de.dlr.premise.component.ComponentRepository) {
            target = ComponentPackage.Literals.COMPONENT_REPOSITORY__DEFINITIONS;
        }
        
        return target;
    }

	protected void executeCommand(ExecutionEvent event, ADataItem newElement, 
			EObject parent, EReference target) {
		
		IStructuredSelection sel = getSelection(event);

		// create and execute command
		if (target != null && newElement != null) {
		    IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		    if (activeEditor instanceof IEditingDomainProvider) {
		    	EditingDomain editingDomain = ((IEditingDomainProvider) activeEditor).getEditingDomain();
		    	AbstractCommand com = new CreateChildCommand(editingDomain, parent, target, newElement, sel.toList());
		    	editingDomain.getCommandStack().execute(com);
		    } else {
		        Collection<Object> children = (Collection<Object>) parent.eGet(target);
		        children.add(newElement);
		    }
		}
	}


}
