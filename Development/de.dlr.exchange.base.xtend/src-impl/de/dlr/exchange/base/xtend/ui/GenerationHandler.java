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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;

import de.dlr.exchange.base.xtend.CharsetProvidingEclipseResourceFileSystemAccess2;
import de.dlr.exchange.base.xtend.IGeneratorMyWithProgress;
import de.dlr.exchange.base.xtend.IGeneratorMy;
import de.dlr.exchange.base.xtend.IOptions;
import de.dlr.premise.element.AElement;
import de.dlr.premise.element.ARepository;

public class GenerationHandler extends AGenerationHandler implements IHandler {

    @Inject IGeneratorMy generator;

    @Inject IOptions genOption;

    @Inject Provider<CharsetProvidingEclipseResourceFileSystemAccess2> fileAccessProvider;
    
    /**
     * Sets the generator options on all ARepositories
     * 
     * @param editorResSet
     */
    protected void preprocessResourceSet(ResourceSet editorResSet) {
        for (Resource res : editorResSet.getResources()) {
            EObject object = res.getContents().get(0);
            if (object instanceof ARepository) {
                genOption.setOptions((ARepository) object);
            }
        }
    }
    
    @Override
    protected void generateFromAElements(IFolder srcGenFolder, ResourceSet editorResSet, List<AElement> selectedElements) {
        CharsetProvidingEclipseResourceFileSystemAccess2 fsa = setFSA(srcGenFolder);
        if (generator instanceof IGeneratorMyWithProgress) {
            IGeneratorMyWithProgress progressGenerator = (IGeneratorMyWithProgress) generator;
            runWithProgressDialog(monitor -> progressGenerator.doGenerateFromAElements(editorResSet, selectedElements, fsa, monitor));
        } else {
            generator.doGenerateFromAElements(editorResSet, selectedElements, fsa);
        }
    }
    
    @Override
    protected void generateFromResources(IFolder srcGenFolder, ResourceSet resultSet, List<Resource> selectedFiles) {
        CharsetProvidingEclipseResourceFileSystemAccess2 fsa = setFSA(srcGenFolder);
        if (generator instanceof IGeneratorMyWithProgress) {
            IGeneratorMyWithProgress progressGenerator = (IGeneratorMyWithProgress) generator;
            runWithProgressDialog(monitor -> progressGenerator.doGenerateFromResources(resultSet, selectedFiles, fsa));
        } else {
            generator.doGenerateFromResources(resultSet, selectedFiles, fsa);
        }
    }

    private CharsetProvidingEclipseResourceFileSystemAccess2 setFSA(IFolder srcGenFolder) {
        CharsetProvidingEclipseResourceFileSystemAccess2 fsa = fileAccessProvider.get();
        fsa.setProject(srcGenFolder.getProject());
        // outputPath is expected to be relative to project, so remove 1st segment (project)
        fsa.setOutputPath(srcGenFolder.getFullPath().removeFirstSegments(1).toString());
        fsa.setMonitor(new NullProgressMonitor());
        fsa.setPostProcessor(new EmptyFileCallback());
        return fsa;
    }

    private void runWithProgressDialog(IRunnableWithProgress runnable) {
        try {
            new ProgressMonitorDialog(HandlerUtil.getActiveShell(this.event)).run(true, true, runnable);
        } catch (InvocationTargetException | InterruptedException e) {
            System.err.println("Export failed!");
            e.printStackTrace();
        }
    }
}