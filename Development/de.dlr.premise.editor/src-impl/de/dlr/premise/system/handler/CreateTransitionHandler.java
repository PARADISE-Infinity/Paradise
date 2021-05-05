package de.dlr.premise.system.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.DelegatingWrapperItemProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.ElementPackage;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.Transition;


/*
 * This creates a PortmMapping for a selected Input/Output
 */
public class CreateTransitionHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    return doExecute(event, false);
	}

    protected Object doExecute(ExecutionEvent event, boolean swapSourceAndTarget) {
        // get selection
	    IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();	    
		ISelection selection = page.getSelection();

		if (selection != null & selection instanceof IStructuredSelection) {
			IStructuredSelection structSelection = (IStructuredSelection) selection;
			
			// get an check if 2 Objects are selected
			Object[] arrSelection = structSelection.toArray();
			if (arrSelection.length != 2) {
				return null;
			}
			
            Object sourceObject, targetObject;
            if (!swapSourceAndTarget) {
                sourceObject = arrSelection[0];
                targetObject = arrSelection[1];
            } else {
                sourceObject = arrSelection[1];
                targetObject = arrSelection[0];
            }

            Object firstSelected = unwrap(sourceObject);
            Object secondSelected = unwrap(targetObject);

			// handle selected system components
            if ((firstSelected instanceof Mode || secondSelected instanceof Mode) && (firstSelected != secondSelected)) {

				// get modes and create a Transition
				Mode source = (Mode) firstSelected;
                Mode target = (Mode) secondSelected;

                
                // add to the correct StateMachine				
				if ((source.eContainer() instanceof StateMachine) && 
					(target.eContainer() instanceof StateMachine)) {

				    if (source.eContainer() != target.eContainer()){
				        // abort if modes are not contained in the same statemachine
				        return null;
				    }
				    
					Transition pm  = ElementFactory.eINSTANCE.createTransition();
	                pm.setSource(source);
	                pm.setTarget(target);				

					StateMachine root = (StateMachine)source.eContainer();

	                IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
	                if (activeEditor instanceof IEditingDomainProvider) {
	                    EditingDomain editingDomain = ((IEditingDomainProvider) activeEditor).getEditingDomain();
	                    editingDomain.getCommandStack().execute(new CreateChildCommand(editingDomain, root,
	                                                                                   ElementPackage.Literals.STATE_MACHINE__TRANSITIONS,
	                                                                                   pm, structSelection.toList()));
	                } else {
	                    root.getTransitions().add(pm);
	                }
				}
			}
		}
		return null;
    }

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

    private Object unwrap(Object selected) {
        if (selected instanceof DelegatingWrapperItemProvider) {
            return ((DelegatingWrapperItemProvider) selected).getValue();
        }

        return selected;
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
    
    public static class Reversed extends CreateTransitionHandler implements IHandler {
        @Override
        public Object execute(ExecutionEvent event) throws ExecutionException {
            return doExecute(event, true);
        }
    }
}
