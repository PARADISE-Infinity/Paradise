/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.application.cli;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import de.dlr.application.WindowTitle;
import de.dlr.application.cli.action.Actions;
import de.dlr.premise.common.command.AssociatedCommandsCommandStack;
import de.dlr.premise.registry.Value;
import de.dlr.premise.system.Parameter;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.util.BaseRegistryHelper;
import de.dlr.premise.util.PremiseHelper;
import de.dlr.premise.util.urihandlers.PremiseLibraryURIHandler;
import de.dlr.premise.util.urihandlers.URIHandlerHelper;


public class Application implements IApplication {
        
    private static Logger logger = Logger.getRootLogger();
    
    private ProjectRepository repository = null;
    private ArgumentsHandler arguments = new ArgumentsHandler();
    private IWorkspaceRoot workSpace;

    @Override
    public Object start(final IApplicationContext context) throws Exception {

        setLogging();        
        logger.info("Paradise Standalone " + WindowTitle.VERSION);
        
        // try to open the workspace
        workSpace = getWorkspace().getRoot();

        // convert input arguments
        @SuppressWarnings("rawtypes")
        Map argsMap = context.getArguments();
        String[] args = (String[]) argsMap.get("application.args");
        
        if (args.length < 1) {
            arguments.printHelp();
            stop();
        }

        try {
            arguments.parseArguments(args);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            arguments.printHelp();
            stop();
        }
        
        if (arguments.isHelp()) {
            arguments.printHelp();
            stop();
        }
        
        if (arguments.isVerbose()) {
            logger.setLevel(Level.DEBUG);
        }

        // do all the actions
        ResourceSet resourceSet = null;
        Actions actions = null;
        switch (arguments.getAction()) {
            case LIST_MODELS:
                logger.debug("- list workspace models -----------------");
                printModels();
                break;
            case LIST_PROJECTS:
                logger.debug("- list workspace projects ---------------");
                printProjects();
                break;
            case CALCULATION:
                logger.debug("- calculate -----------------------------");
                resourceSet = loadFile();
                printData("> Input");
                actions = new Actions(resourceSet);
                actions.calculate();
                printData("> Output");
                save(resourceSet);
                break;
            case VALIDATION: 
                logger.debug("- validate ------------------------------");
                resourceSet = loadFile();
                actions = new Actions(resourceSet);
                List<String> errorMsgs = actions.validate();
                System.out.println(String.join(System.lineSeparator(), errorMsgs));
                break;
            case CONSTRAINT_CHECK: 
                logger.debug("- check constraints ---------------------");
                resourceSet = loadFile();
                actions = new Actions(loadFile());
                List<String> violationMsgs = actions.checkConstraints();
                System.out.println(String.join(System.lineSeparator(), violationMsgs));
                break;
            default:
                break;
        }            

        stop();
        return IApplication.EXIT_OK;

    }

    @Override
    public void stop() {
        logger.info("-----------------------------------------");
        logger.info("PARADISE has done the work");
    }
    
    // get the resource set
    private ResourceSet loadFile() throws Exception {
        
        ResourceSet resourceSet = null;
        
        // get file name and load the whole resource set if necessary
        String fileName = arguments.getFileName();  
        if (fileName != null) {
            resourceSet = getResourceSet(fileName);
            if (resourceSet == null) {
                logger.error("unable to load the complete model " + fileName);
                stop();
            }
        }
        return resourceSet;
    }

    /**
     * @param fileName
     * Load model with all resources
     */
    private ResourceSet getResourceSet(final String fileName) throws Exception {

        String premiseFile = null;
        ResourceSet resourceSet = null;
        URI uri = URI.createFileURI(fileName);
                
        if(uri.isPlatform()) {
            String project = getProject(uri);

            IProject wksProject = workSpace.getProject(project);
            if (wksProject == null) {
                throw new IOException("Parent project for " + fileName + " is missing");
            }
            if(wksProject.exists() && wksProject.isOpen()) {
                wksProject.open(null);
            }
            
            IFile file = null;
            String path = getPath(uri);
            if (path != null) {
                IFolder wksPath = wksProject.getFolder(path);
                file = wksPath.getFile(uri.lastSegment().toString());
            } else {
                file = wksProject.getFile(uri.lastSegment().toString());
            }
            
            if (file == null || !file.exists()) {
                throw new IOException(fileName + " file not found");
            }

            premiseFile = file.getRawLocation().toOSString();

        } else {
            
            File file = new File(fileName);
            if (file == null || !file.exists()) {
                throw new IOException(fileName + " file not found");                
            }
            
            premiseFile = fileName;
        }

        try {
            resourceSet = initializeEditingDomain().getResourceSet();
            repository = (ProjectRepository) PremiseHelper.loadResource(resourceSet, premiseFile);
            
            // manually add base.registry
            URIHandlerHelper.registerInto(resourceSet.getURIConverter());
            resourceSet.getResource(PremiseLibraryURIHandler.PREMISE_GLOBAL_REGISTRY_URI, true);
            
            EcoreUtil.resolveAll(resourceSet);
        } catch (Exception e) {
            logger.error("Unable to load model: " + premiseFile);
        }
    
        if (repository != null) {        
            SystemComponent premiseRoot = repository.getProjects().get(0);
            logger.debug("Model   : " + premiseFile + " loaded");
            logger.debug("Version : " + repository.getMetaModel());
            logger.debug("Root    : " + premiseRoot.getName());
        }
        
        return resourceSet;
    }

    /** 
     * save the model stored in the repository
     * @param fileName
     */
    private void save(final ResourceSet resSet) {
        
        for (Resource res : resSet.getResources()) {
            try {
                res.save(Collections.emptyMap());
                logger.debug("Model " + res.getURI() + " saved");
            } catch (IOException e) {
                logger.error("Model " + res.getURI() + " couldn't be saved");
            }
        }
    }    
    
    /**
     * @return
     *  Initialize and return the required editing domain. 
     */
    private EditingDomain initializeEditingDomain() {
        AdapterFactory adapterFactory = new AdapterFactoryImpl();
        BasicCommandStack commandStack = new AssociatedCommandsCommandStack();        
        return new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
    }

    private IWorkspace getWorkspace() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return workspace;
    }
    
    // the first URI segment should the project name
    private String getProject(URI uri) {
        
        // ensure a project name is given, at least the URI has two segments 
        if (uri.segmentCount() > 1) {
            return uri.segment(0).toString();
        }
        
        return null;
    }
    
    // the segments between 1 and last of the URI should be the path
    private String getPath(URI uri) {

        if (uri.segmentCount() < 3) {
            return null;
        }

        URI path = URI.createURI(uri.segment(1));
        for (int i = 2; i < uri.segmentCount() - 1; i++) {
            path.appendSegment(uri.segment(i));
        }
                
        return path.toString();
    }

    /**
     * Prints the list of open projects in the current workspace
     */
    private void printProjects() {
        for(IProject project : workSpace.getProjects()) {
            if (project.isOpen()) {
                logger.info(project.getFullPath().toOSString());
            }
        }
    }    
    
    /**
     * Prints the list of open projects in the current workspace
     */
    private void printModels() {

        for(IProject project : workSpace.getProjects()) {        
            if (project.isOpen()) {
                try {                
                    for (IResource resource : project.members()) {
                        // show only PREMISE model files
                        if (resource instanceof IFile) {
                            IFile file = (IFile) resource;
                            String ext = file.getFileExtension();
                            if (file.exists() && (ext.equals("premise") || 
                                                  ext.equals("system"))) {
                                logger.info(file.getFullPath().toOSString());
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }                
            }
        }
    }
    
    private void printData(final String msg) {
        
        System.out.println(msg);
        
        List<EObject> inputs = BaseRegistryHelper.getElementsByMetaTypes(BaseRegistryHelper.METATYPE_CAL_INPUT_ID, repository);
        for(EObject input : inputs) {
            Parameter iPara = null;
            if (input instanceof Parameter) {
                iPara = (Parameter) input;
                String value = getValue(iPara);
                System.out.println("Input[" + iPara.getName() + "]:" + value);                
            }            
        }
        
        List<EObject> outputs = BaseRegistryHelper.getElementsByMetaTypes(BaseRegistryHelper.METATYPE_CAL_OUTPUT_ID, repository);
        for(EObject output : outputs) {
            Parameter oPara = null;
            if (output instanceof Parameter) {
                oPara = (Parameter) output;
                String value = getValue(oPara);
                
                System.out.println("Output[" + oPara.getName() + "]:" + value);
            }
        }
    }

    /** 
     * return value string
     * @param para
     * @return
     */
    private String getValue(Parameter para) {

        Value val = para.getValue();
        if (val == null) {
            return "NaN";
        }

        return val.getValue();
    }
    
    private void setLogging() {

        // set basic configuration
//        BasicConfigurator.configure();
//        String log4jConfPathBase = Platform
//                .getInstallLocation()
//                .getURL()
//                .getPath()
//                .substring(
//                        1,
//                        Platform.getInstallLocation().getURL().getPath()
//                                .length() - 1);
//
//        String log4jConfPath = log4jConfPathBase + "/log4j.properties";
//        PropertyConfigurator.configure(log4jConfPath);
        
        SimpleLayout layout = new SimpleLayout();
        ConsoleAppender consoleAppender = new ConsoleAppender( layout );
        logger.addAppender( consoleAppender );
        logger.setLevel(Level.INFO);
        logger.info("logging on");        
    }
}
