package de.dlr.premise.system.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.CreateChildCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.dlr.premise.component.ComponentReferencePointer;
import de.dlr.premise.element.AElement;
import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.Relation;
import de.dlr.premise.functions.UseCase;
import de.dlr.premise.functions.UseCasePackage;
import de.dlr.premise.graph.APointer;
import de.dlr.premise.graph.INode;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.SystemPackage;

/*
 * This creates a Relation for a selected Input/Output
 */
public class CreateRelationHandler implements IHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        return doExecute(event, false);
    }

    protected Object doExecute(ExecutionEvent event, boolean swapSourceAndTarget) throws ExecutionException {
        // get selection
        ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);

        if (selection != null & selection instanceof IStructuredSelection) {
            IStructuredSelection structSelection = (IStructuredSelection) selection;

            // check if 2 AElements
            Object[] arrSelection = structSelection.toArray();
            if (arrSelection.length != 2 || arrSelection[0] == arrSelection[1]) {
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
            
            APointer<? extends EObject> sourcePointerCandidate = CreateEdgeHelper.convertSelectedToPointer(sourceObject);
            APointer<? extends EObject> targetPointerCandidate = CreateEdgeHelper.convertSelectedToPointer(targetObject);

            // Elements must be different
            if (sourcePointerCandidate != null && sourcePointerCandidate.equals(targetPointerCandidate)) {
                return null;
            }

            EObject sourceCandidate = sourcePointerCandidate == null ? null : sourcePointerCandidate.getTarget();
            EObject targetCandidate = targetPointerCandidate == null ? null : targetPointerCandidate.getTarget();

            if (sourceCandidate instanceof INode && targetCandidate instanceof INode) {
                AElement parent = null;
                if (sourcePointerCandidate instanceof ComponentReferencePointer) {
                    parent = (AElement) ((ComponentReferencePointer<?>) sourcePointerCandidate).getComponentReference().eContainer();
                } else {
                    EObject parentCandidate = sourceCandidate;
                    while (!(parentCandidate instanceof AElement)) {
                        parentCandidate = parentCandidate.eContainer();
                    }
                    parent = (AElement) parentCandidate;
                }

                APointer<INode> sourcePointer = (APointer<INode>) sourcePointerCandidate;
                APointer<INode> targetPointer = (APointer<INode>) targetPointerCandidate;

                Relation rel = ElementFactory.eINSTANCE.createRelation();
                rel.setSourcePointer(sourcePointer);
                rel.setTargetPointer(targetPointer);

                IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
                if (activeEditor instanceof IEditingDomainProvider) {
                    EditingDomain editingDomain = ((IEditingDomainProvider) activeEditor).getEditingDomain();
                    EReference feature = null;
                    if (parent instanceof UseCase) {
                        feature = UseCasePackage.Literals.USE_CASE__RELATIONS;
                    } else if (parent instanceof SystemComponent) {
                        feature = SystemPackage.Literals.SYSTEM_COMPONENT__RELATIONS;
                    }
                    CreateChildCommand command = new CreateChildCommand(editingDomain, parent, feature, rel, structSelection.toList());
                    editingDomain.getCommandStack().execute(command);
                } else if (parent instanceof SystemComponent) {
                    ((SystemComponent) parent).getRelations().add(rel);
                } else if (parent instanceof UseCase) {
                    Relation relationUseCase = (Relation) (Relation) rel;
                    ((UseCase) parent).getRelations().add(relationUseCase);
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

    @Override
    public void addHandlerListener(IHandlerListener handlerListener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void removeHandlerListener(IHandlerListener handlerListener) {
    }
    
    public static class Reversed extends CreateRelationHandler implements IHandler {
        @Override
        public Object execute(ExecutionEvent event) throws ExecutionException {
            return doExecute(event, true);
        }
    }
}
