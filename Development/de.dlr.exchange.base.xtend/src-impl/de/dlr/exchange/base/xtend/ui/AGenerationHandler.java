/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.exchange.base.xtend.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.EcoreUtil2;

import de.dlr.premise.element.AElement;
import de.dlr.premise.system.presentation.my.EditorHelper;
import de.dlr.premise.util.urihandlers.URIHandlerHelper;

public abstract class AGenerationHandler extends AbstractHandler {

    private static final String NAME_EXPORT_DIR = "export";
    
    protected ExecutionEvent event;

    public AGenerationHandler() {
        super();
    }
    
    protected abstract void generateFromAElements(IFolder srcGenFolder, ResourceSet editorResSet, List<AElement> selectedElements);

    protected abstract void generateFromResources(IFolder srcGenFolder, ResourceSet resultSet, List<Resource> selectedFiles);
    
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException { 
        this.event = event;

        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
    
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object firstElement = structuredSelection.getFirstElement();
    
            if (firstElement instanceof IFile) { // Files have been selected in the navigator
    
                // 
                // Create a new resource set into which we load the selected files
                //
                ResourceSet resultSet = new ResourceSetImpl();
                
                IFile file = (IFile) firstElement;
                List<Resource> selectedFiles = new ArrayList<>();
    
                IFolder srcGenFolder = createSrcGenFolder(file);
    
                //
                // Disable Notify temporarily. Otherwise the export would take ages.
                //
                resultSet.getLoadOptions().put(XMIResource.OPTION_DISABLE_NOTIFY, Boolean.TRUE);
    
                //
                // register URI handlers
                //
                URIHandlerHelper.registerInto(resultSet.getURIConverter());
    
                //
                // Get the Resources of the selected files
                //
                for (Object element : structuredSelection.toList()) {
                    if (element instanceof IFile) {
                        file = (IFile) element;
                        URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), false);
                        resultSet.getResource(uri, true);
    
                        // add the selected Resource to the selectedFiles list
                        for (Resource resource : resultSet.getResources()) {
                            if (resource.getURI().equals(uri)) {
                                selectedFiles.add(resource);
                            }
                        }
                    }
                }
    
                //
                // Resolve all external resources for the premise files
                //
                resolveAllResources(resultSet);
                
                
                //
                // Track modifications in res set (for later save
                //
                for (Resource res : resultSet.getResources()) {
                    res.setTrackingModification(true);
                }
                
                //
                // Set Options
                //
                preprocessResourceSet(resultSet);
                
                //
                // Start the generator
                //
                generateFromResources(srcGenFolder, resultSet, selectedFiles);
                
                //
                // Save the resources to persist newly set options
                //
                for (Resource res : resultSet.getResources()) {
                    try {
                        if (res.isModified()) {
                            res.save(null);
                        }
                    } catch (IOException e) {
                        System.err.println("Couldn't save file");
                        e.printStackTrace();
                    }
                }
    
            } else { // AElements have been selected in the editor
    
                List<AElement> selectedElements = new ArrayList<AElement>();
    
                IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
                IEditorPart editor = page.getActiveEditor();
                final ResourceSet editorResSet = EditorHelper.getResourceSet(editor);
    
                if (editorResSet != null) {
    
                    //
                    // Add every EObject that we have selected to our selectedElements Array
                    //
                    for (Object o : structuredSelection.toList()) {
                        if (o instanceof AElement) {
                            selectedElements.add((AElement) o);
                        }
                    }
    
                    //
                    // Set Options
                    //
                    EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(editorResSet);
                    if (editingDomain != null) {
                        editingDomain.getCommandStack().execute(new ChangeCommand(editorResSet) {
                            
                            @Override
                            protected void doExecute() {
                                preprocessResourceSet(editorResSet);
                            }
                        });
                    } else {
                        preprocessResourceSet(editorResSet);
                    }
    
                    //
                    // Create the output folder
                    //
                    EObject firstEElement = (EObject) structuredSelection.getFirstElement();
                    IFile file = getFile(firstEElement.eResource());
                    IFolder srcGenFolder = createSrcGenFolder(file);
    
                    //
                    // Resolve all external resources for the premise files
                    //
                    resolveAllResources(editorResSet);
    
                    //
                    // Start the generator
                    //
                    generateFromAElements(srcGenFolder, editorResSet, selectedElements);
    
                }
            }
    
        }
        return null;
    }
    
    /**
     * Allow clients to preprocess a resource set before generator is run.
     * @param editorResSet
     */
    protected void preprocessResourceSet(ResourceSet editorResSet) {
        // Do nothing by default
    }

    /**
     * Resolve all external resources for the premise files
     * 
     * @param resultSet
     */
    private void resolveAllResources(ResourceSet resultSet) {
        try {
            for (Resource resource : (Resource[]) resultSet.getResources().toArray()) {
                resource.load(null);
                EcoreUtil2.resolveAll(resultSet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IFolder createSrcGenFolder(IFile file) {
        IFolder srcGenFolder = null;
        if (file.getParent() instanceof IFolder) {
            srcGenFolder = ((IFolder) file.getParent()).getFolder(NAME_EXPORT_DIR);
        } else if (file.getParent() instanceof IProject) {
            srcGenFolder = ((IProject) file.getParent()).getFolder(NAME_EXPORT_DIR);
        }
    
        // if premise file is not already in export folder (e.g. after import)
        if (!srcGenFolder.getParent().getName().equalsIgnoreCase(NAME_EXPORT_DIR)) {
            if (!srcGenFolder.exists()) {
                try {
                    srcGenFolder.create(true, true, new NullProgressMonitor());
                } catch (CoreException e) {
                    System.err.println("Error while exporting: Could not create export folder '" + srcGenFolder.getLocationURI() + "'");
                    System.err.println(e.getMessage());
                }
            }
        } else {
            srcGenFolder = (IFolder) srcGenFolder.getParent();
        }
        return srcGenFolder;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    protected IFile getFile(Resource res) {
        Resource resource = res;
        if (resource != null) {
            URI uri = resource.getURI();
            uri = resource.getResourceSet().getURIConverter().normalize(uri);
    
            // decode the URI in case there are encoded characters being known to cause problems when creating the srcGenFolder
            uri = URI.createURI(URI.decode(uri.toString()));
    
            String scheme = uri.scheme();
            if ("platform".equals(scheme) && uri.segmentCount() > 1 && "resource".equals(uri.segment(0))) {
                StringBuffer platformResourcePath = new StringBuffer();
                for (int j = 1, size = uri.segmentCount(); j < size; ++j) {
                    platformResourcePath.append('/');
                    platformResourcePath.append(uri.segment(j));
                }
                return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(platformResourcePath.toString()));
            }
        }
        return null;
    }

}