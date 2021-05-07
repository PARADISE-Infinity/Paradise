/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.util;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.BasicEList.UnmodifiableEList;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import de.dlr.premise.element.AElement;
import de.dlr.premise.element.AGuardCondition;
import de.dlr.premise.element.AModeCombination;
import de.dlr.premise.element.ARepository;
import de.dlr.premise.element.Mode;
import de.dlr.premise.element.StateMachine;
import de.dlr.premise.element.Transition;
import de.dlr.premise.functions.ARange;
import de.dlr.premise.registry.ADataItem;
import de.dlr.premise.registry.ANameItem;
import de.dlr.premise.registry.AParameterDef;
import de.dlr.premise.registry.AValueDef;
import de.dlr.premise.registry.IPremiseObject;
import de.dlr.premise.registry.MetaData;
import de.dlr.premise.system.Balancing;
import de.dlr.premise.system.ProjectRepository;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.SystemPackage;
import de.dlr.premise.system.TransitionBalancing;
import de.dlr.premise.util.internal.PremiseHelperXtend;

public class PremiseHelper {

    protected static final String DELIM = ".";
    
	// Unique names for the meta data sections used by plug-ins for defining options
    public static final String NAME_META_SECTION_VIEW = "View";
    public static final String NAME_META_SECTION_EXPORT = "Export";

    public static final String OPT_NUMBER_PATTERN = "number representation pattern";
    public static final String OPT_NUMBER_PATTERN_SCIENTIFIC_PATTERN = "##0.0###E0";

    public static final String PLUGIN_ID = "de.dlr.premise";
    
    /**
     * Gets the root {@link Notifier} of a containment tree for the given EObject. This can be a {@link ResourceSet}, a {@link Resource}, or
     * any {@link EObject}.
     */
    public static Notifier getRootNotifier(EObject item) {
        Notifier result = null;

        if (item.eResource() != null) {
            if (item.eResource().getResourceSet() != null) {
                result = item.eResource().getResourceSet();
            } else {
                item.eResource();
            }
        } else {
            result = getRoot(item);
        }

        return result;
    }

    /**
     * Gets the root component (SystemComponent or ARepository) of composition tree starting from a given node/item
     * 
     * @see EcoreUtil.getRootContainer()
     * @param item EObject to start searching
     * @return Highest possible parent of given item (from item itself up to ARepository)
     */
    public static EObject getRoot(EObject item) {
        EObject it = item;
        if (it != null) {
        	while (it.eContainer() != null) {
        		it = it.eContainer();
        	}
        }
        return it;
    }

    /**
     * Gets the containing SystemComponent of a given item
     * 
     * @param item EObject to start searching
     * @return The 1st parent of a given item, which is a SystemComponent
     */
    public static SystemComponent getRootComponent(EObject item) {
        EObject it = item;
        while (it != null && !(it instanceof SystemComponent)) {
            it = it.eContainer();
        }
        return (SystemComponent) it;
    }
    
    /**
     * Gets the containing AElement of a given item
     * 
     * @param item EObject to start searching
     * @return The 1st parent of a given item, which is an AElement
     */
    public static AElement getRootElement(EObject item) {
        EObject it = item;
        while (it != null && !(it instanceof AElement)) {
            it = it.eContainer();
        }
        return (AElement) it;
    }

    /**
     * Try to load a given resource
     * @param filePath
     * @return
     * @throws IOException
     */
    public static EObject loadResource(String filePath) throws IOException {

        ResourceSet resourceSet = new ResourceSetImpl();
    	return loadResource(resourceSet, filePath);
    }
    
    /**
     * Try to load a given resource in a existing resource set
     * 
     * @param resoureSet
     * @param filePath
     * @return
     * @throws IOException
     */
	public static EObject loadResource(ResourceSet resourceSet, String filePath)
			throws IOException {
		// create resource
    	URI uri = URI.createFileURI(filePath);
        Resource resource = resourceSet.createResource(uri);
        if (resource == null) {
        	return null;
        }
        
        // register PREMISE package and resolve all resources
        RegistryHelper.registerPackageForResource(resource, SystemPackage.eNS_URI, SystemPackage.eINSTANCE);
        EcoreUtil.resolveAll(resourceSet);

        // resource.unload();
        resource.load(Collections.EMPTY_MAP);
        return resource.getContents().get(0);
	}
	
    public static long saveResource(final String filePath, final EObject model) {
        Resource resource = new ResourceSetImpl().createResource(URI.createFileURI(filePath));
        RegistryHelper.registerPackageForResource(resource, SystemPackage.eNS_URI, SystemPackage.eINSTANCE);

        resource.getContents().add(model);
        try {
            resource.save(Collections.EMPTY_MAP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(filePath);
        return file.length();
    }

    /**
     * Gets the absolute path to the model's resource file without file name Notice, that this function can not be used in JUnit tests
     * 
     * @param model
     * @return the absolute path to the model's resource file or null
     */
    public static String getResourceAbsPath(final EObject model) {
        String absPath = null;

        Resource res = model.eResource();
        if (res != null) {
            URI uri = res.getURI();

            // resolve uri via platform
            URI resolvedUri = CommonPlugin.resolve(uri);
            if (resolvedUri.scheme() != null && resolvedUri.scheme().equals("file")) {
                // if the platform could produce a "file://" uri, use it here
                absPath = resolvedUri.trimSegments(1).toFileString();
            } else {
                // resolve manually using workspace paths
                String platformRelativePath = uri.isPlatform() ? uri.toPlatformString(true) : uri.toString();
                absPath =
                        ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(platformRelativePath)).getLocation()
                                       .removeLastSegments(1).toOSString();
            }

        }

        return absPath;
    }

    /**
     * This will get the project name, even if it is not inside the workspace but in the file system. For platform URIs such as
     * "platform:/resource/ProjectName/_GLOBAL/GlobalFuncDefs.functionpool", the project name is the 2nd segment Notice, that this function
     * can not be used in JUnit tests
     * 
     * @param model
     * @return the project name as string or empty string
     */
    public static String getProjectName(final EObject model) {
        String projectName = "";

        if (model.eResource() != null) {
            URI uri = model.eResource().getURI();
            if (uri.isPlatform()) {
                projectName = uri.segment(1);
            } else {
                projectName = uri.segment(0);
            }
        }

        return projectName;
    }

    /**
     * Checks, if the root repository of the given object has a given meta data set.
     * 
     * @param obj
     * @param metaDataName
     * @param defaultMetaDataValue is interpreted and returned as true/false if metaDataName is not found
     * @return true, if related meta data value is not off/0/false/no
     */
    public static boolean isSet(final EObject obj, final String metaDataName, final String defaultMetaDataValue) {

        boolean result = true;

        EObject root = getRoot(obj);
        if (root instanceof ARepository) {
            ARepository rep = ((ARepository) root);
            MetaData metaData = getMetaData(rep, metaDataName);
            result = isOn(metaData, defaultMetaDataValue);
        }

        return result;
    }

    /**
     * @see #isSet(EObject, String, String) Default is true
     */
    public static boolean isSet(EObject obj, String metaDataName) {
        return isSet(obj, metaDataName, "on");
    }

    /**
     * Check wether a given metaData is considered a boolean true
     * 
     * @param metaData
     * @param defaultMetaDataValue
     * @return
     */
    public static boolean isOn(final MetaData metaData, final String defaultMetaDataValue) {
        boolean result = true;

        String metaVal;
        if (metaData != null && metaData.getValue() != null) {
            metaVal = metaData.getValue().toLowerCase();
        } else {
            metaVal = defaultMetaDataValue.toLowerCase();
        }
        if (metaVal.equals("off") || metaVal.equals("0") || metaVal.equals("false") || metaVal.equals("no")) {
            result = false;
        }

        return result;
    }

    /**
     * @see #isOn(MetaData, String) Default is true
     */
    public static boolean isOn(final MetaData metaData) {
        return isOn(metaData, "on");
    }

    /**
     * Searches recursively through the meta data of given container until a given property name (case-insensitive) is found
     * 
     * @param container ARepository, SystemComponent, ACalculationEngine, MetaData, etc.
     * @param popertyName name of property to find
     * @return MetaData object with given property name
     */
    public static MetaData getMetaData(EObject container, String popertyName) {
        MetaData result = null;

        EList<MetaData> list = null;
        if (container instanceof IPremiseObject) {
            list = ((IPremiseObject) container).getMetaData();
        } else if (container instanceof MetaData) {
            // TODO md.children
            list = ((MetaData) container).getMetaData();
        }

        if (list != null) {
            for (MetaData data : list) {
                String name = data.getName();
                if (name != null && name.equalsIgnoreCase(popertyName)) {
                    result = data;
                    break;
                }
                if (!data.getMetaData().isEmpty()) {
                    result = getMetaData(data, popertyName);
                    if (result != null) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets all components/features of a given type from the given notifier
     */
    public static EList<EObject> getAll(Notifier obj, Class<? extends EObject> type) {
        EList<EObject> result;

        // dispatch to correct implementation
        if (obj instanceof ResourceSet) {
            result = getAll((ResourceSet) obj, type);
        } else if (obj instanceof Resource) {
            result = getAll((Resource) obj, type);
        } else if (obj instanceof EObject) {
            result = getAll((EObject) obj, type);
        } else {
            result = new BasicEList<EObject>();
        }

        return result;
    }

    /**
     * Gets all components/features of a given type from the given resource set
     */
    public static EList<EObject> getAll(ResourceSet resourceSet, Class<? extends EObject> type) {
        EList<EObject> result = new BasicEList<EObject>();
        for (Resource resource : resourceSet.getResources()) {
            result.addAll(getAll(resource, type));
        }
        return result;
    }

    /**
     * Gets all components/features of a given type from the given resource
     */
    public static EList<EObject> getAll(Resource resource, Class<? extends EObject> type) {
        EList<EObject> result = new BasicEList<EObject>();
        for (EObject root : resource.getContents()) {
            result.addAll(getAll(root, type));
        }
        return result;
    }

    /**
     * Gets all components/features of a given type from the given root (repository or subtree)
     * 
     * @param root of subtree or repository
     * @param type EClass retrievable by object.eClass()
     * @return list of all occurrences under given root
     */
    public static EList<EObject> getAll(EObject root, Class<? extends EObject> type) {
        EList<EObject> result = new BasicEList<EObject>();
        if (type.isInstance(root)) {
            result.add(root);
        }
        for (EObject obj : root.eContents()) {
            result.addAll(getAll(obj, type));
        }
        return result;
    }

    /**
     * @see Same as getAll(), but returned components/features have the same name
     * @param root
     * @param type
     * @param name name of components/features to be returned
     * @return list of all occurrences under given root
     */
    public static EList<ANameItem> getAll(EObject root, Class<? extends ANameItem> type, String name) {
        EList<ANameItem> result = new BasicEList<ANameItem>();
        if (type.isInstance(root) && name.equalsIgnoreCase(getMeaningfulName((ANameItem) root))) {
            result.add((ANameItem) root);
        }
        for (EObject obj : root.eContents()) {
            if (obj instanceof ANameItem) {
                result.addAll(getAll(obj, type, name));
            }
        }
        return result;
    }

    /**
     * Returns the minimal needed qualifying name of a feature like parameter or port. Useful for differentiation of features with same name
     * to enable correct selection in selection lists.
     * 
     * @param item
     * @return the qualifying part of the qualified name (with a dot included at the end of string) - can be an empty string
     */
    public static String getQualifyingNamePrefix(ANameItem item) {
        String result = "";

        String itemName = getMeaningfulName(item);
        if (item != null && itemName != null) {
            // get all items with same name
            EList<ANameItem> duplicates;
            if (item instanceof AParameterDef) {
                duplicates = getAll(PremiseHelper.getRoot(item), AParameterDef.class, itemName);
            } else {
                duplicates = getAll(PremiseHelper.getRoot(item), item.getClass(), itemName);
            }
            duplicates.remove(item);

            ANameItem parent = item;
            while (parent.eContainer() instanceof ANameItem && !duplicates.isEmpty()) {
                parent = (ANameItem) parent.eContainer();
                EList<ANameItem> parentDups = new BasicEList<ANameItem>();

                for (ANameItem dup : duplicates) {
                    if (dup.eContainer() instanceof ANameItem) {
                        ANameItem dupParent = (ANameItem) dup.eContainer();
                        // if even the names of parents are null or the same
                        if (parent.getName() == dupParent.getName()
                                || (parent.getName() != null && parent.getName().equals(dupParent.getName()))) {
                            parentDups.add(dupParent);
                        }
                    }
                }
                duplicates = parentDups;
                String parentName = getMeaningfulName(parent);
                if (parentName != null) {
                    result = parentName + '.' + result;
                } else {
                    result = '.' + result;
                }
            }
        }
        return result;
    }

    /**
     * Returns the name of the item. If the item is of a mapping type like Transition or Balancing and null or empty, the name is enhanced:
     * 
     * @param item to get a meaningful name from
     * @return enhanced meaningful name (not null)
     */
    public static String getMeaningfulName(final ANameItem item) {
        String result = "";
        if (item.getName() == null || item.getName().isEmpty()) {
            if (item instanceof Transition) {
                Transition transition = (Transition) item;
                Mode out = transition.getSource();
                Mode in = transition.getTarget();
                if (out != null) {
                    result += out.getName();
                }
                if (in != null) {
                    result += " > " + in.getName();
                }
            }
            if (item instanceof TransitionBalancing) {
                TransitionBalancing balancing = (TransitionBalancing) item;
                if (balancing.getTarget() != null) {
                    result += " > " + balancing.getTarget().getName();
                }
            }
        } else {
            result = item.getName();
        }

        if (item instanceof Balancing) {
            Balancing balancing = (Balancing) item;
            if ( (item.getName() == null || item.getName().isEmpty()) && balancing.getFunction() != null) {
                result = balancing.getFunction().trim();
            } else if (balancing.getTarget() != null) {
                result += " > " + balancing.getTarget().getName();
            }
        }

        return result;
    }
    
    /**
     * @param item comparable item
     * @param obj object compared with
     * @return
     *   true if the id's of the objects are identical otherwise return false
     */
    public static boolean sameID(final ADataItem item, final Object obj) {
        String id = item.getId();
        return obj instanceof ADataItem && id.equals(((ADataItem)obj).getId());        
    }

	/**
	 * Returns all system components after the repository root. It dosen't matter
	 *  if the EObject is the root element or any element in the system component tree. 
	 * @param obj
	 * @return
	 */
	public static List<SystemComponent> getRootComponents(EObject obj) {		
		
		List <SystemComponent> components = new ArrayList<SystemComponent>();
	
		// get the root repository
		EObject root = getRoot(obj);
		if ((root instanceof ProjectRepository) == false) {
			return components;
		}
	
		// search for the first SystemComponent
		for (EObject element : obj.eContents()) {
			if (element instanceof SystemComponent) {
				components.add((SystemComponent) element);
			}			
		}
		
		return components;
	}

	/**
	 * Register the premise factory
	 */
	public static void registerFactory() {
		// Register Premise Factory
	    Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
	    Map<String, Object> m = reg.getExtensionToFactoryMap();
	    m.put("premise", new XMIResourceFactoryImpl());
	}	

	
	
	/**
	 * Returns the parent of an ECore Object
	 * @param object ECore Object
	 * @return
	 */
	public static SystemComponent getParent(final SystemComponent component) {
		
		// check input
		if (component == null) {
			return null;
		}
		
		// get parent
		EObject parent = component.eContainer();
		if (parent instanceof SystemComponent) {
			return (SystemComponent) parent;			
		}
		
		return null;
	}
	
	public static boolean isRoot(final SystemComponent component) {
		SystemComponent parent = getParent(component);
		if (parent == null) {
			return true;
		}
		
		return false;
	}

    public static EObject getLowestCommonAncestor(EObject one, EObject two) {
        List<EObject> pathOne = new ArrayList<>();
        List<EObject> pathTwo = new ArrayList<>();

        EObject parent = one;
        do {
            pathOne.add(0, parent);
        } while (null != (parent = parent.eContainer()));
        parent = two;
        do {
            pathTwo.add(0, parent);
        } while (null != (parent = parent.eContainer()));
        for (int i = 0; i < Math.min(pathOne.size(), pathTwo.size()); i++) {
            if (pathOne.get(i) != pathTwo.get(i)) {
                return parent != null ? parent : one;
            } else {
                parent = pathOne.get(i);
            }
        }
        return parent;
    }
    

    public static <T> TreeIterator<T> getAllContents(Notifier notifier, boolean resolve) {
        if (notifier instanceof ResourceSet) {
            return EcoreUtil.getAllContents((ResourceSet) notifier, resolve);
        } else if (notifier instanceof Resource) {
            return EcoreUtil.getAllContents((Resource) notifier, resolve);
        } else if (notifier instanceof EObject) {
            return EcoreUtil.getAllContents((EObject) notifier, resolve);
        }
        throw new IllegalArgumentException();
    }
    
    /**
     * Creates an UUID
     * 
     * @return the UUID as String
     */
    public static String createId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * OCL closure operation
     */
    public static <T> Collection<T> closure(final Iterable<? extends T> base, final Function1<? super T, ? extends Iterable<? extends T>> fn) {
        return PremiseHelperXtend.closure(base, fn);
    }

    /**
     * OCL closure operation
     */
    public static <T> Collection<T> closure(T base, final Function1<? super T, ? extends Iterable<? extends T>> fn) {
        return PremiseHelperXtend.closure(base, fn);
    }
    
    /**
     * OCL closure operation
     */
    public static <T> Collection<T> closure(final Iterable<? extends T> base,
            final Function1<? super T, ? extends Iterable<? extends T>> fn, int depth) {
        return PremiseHelperXtend.closure(base, fn, depth);
    }

    /**
     * OCL closure operation
     */
    public static <T> Collection<T> closure(T base, final Function1<? super T, ? extends Iterable<? extends T>> fn, int depth) {
        return PremiseHelperXtend.closure(base, fn, depth);
    }
    
    /**
     * Create a string representation of a guard condition
     */
    public static String getGuardConditionString(AGuardCondition cond) {
        return PremiseHelperXtend.getGuardConditionString(cond, false);
    }

    /**
     * Gets all (Transition)Parameters of a given SystemComponent and its Transitions.
     * 
     * @param sc system component
     * @return List of (Transition)Parameters that are contained by the given SystemComponent
     */
    public static EList<AParameterDef> getParameters(final SystemComponent sc) {
        EList<AParameterDef> result = new BasicEList<AParameterDef>();
        result.addAll(sc.getParameters());
        
        EList<Transition> trans = getTransitions(sc);
        for (Transition transition : trans) {
            result.addAll(transition.getParameters());
        }
        return result;
    }
    
    /**
     * Gets all Transitions of a given Element and its StateMachines.
     * 
     * @param ae AElement
     * @return List of Transitions that are contained by the given Element
     */
    public static EList<Transition> getTransitions(final AElement ae) {
        EList<Transition> result = new BasicEList<Transition>();
        
        for (StateMachine sm : ae.getStatemachines()) {
            result.addAll(sm.getTransitions());
        }
        return result;
    }
    
    /**
     * Gets all Modes of a given Element and its StateMachines.
     * 
     * @param ae AElement
     * @return List of Modes that are contained by the given Element
     */
    public static EList<Mode> getModes(final AElement ae) {
        EList<Mode> result = new BasicEList<Mode>();
        
        for (StateMachine sm : ae.getStatemachines()) {
            result.addAll(sm.getModes());
        }
        return result;
    }

    /**
     * Converts an {@link Iterable} to a {@link EList} to be returned from EMF generated methods.
     * 
     * @param iterable
     * @return {@link UnmodifiableEList} containing all elements in the iterable
     */
    public static <T> EList<T> toUnmodifieableEList(Iterable<T> iterable) {
        return toUnmodifieableEList(iterable.iterator());
    }
    
    /**
     * Converts an {@link Iterator} to a {@link EList} to be returned from EMF generated methods.
     * 
     * @param iterator
     * @return {@link UnmodifiableEList} containing all elements in the iterator
     */
    public static <T> EList<T> toUnmodifieableEList(Iterator<T> iterator) {
        EList<T> eList = new BasicEList<>();
        Iterators.addAll(eList, iterator);
        return ECollections.unmodifiableEList(eList);
    }

    /**
     * Returns an iterator that iterates over an {@link EObject} itself and all the {@link #eContents direct contents} and indirect contents
     * of this object.
     */
    public static Iterator<EObject> eAllContentsIncludingRoot(EObject eObj) {
        return Iterators.concat(Iterators.singletonIterator(eObj), eObj.eAllContents());
    }

    /**
     * Flat map operation
     * 
     * @param it
     * @param transformation
     * @return
     */
    public static <T, R> Iterable<R> flatMap(final Iterable<T> it, final Function1<? super T, ? extends Iterable<R>> transformation) {
        return Iterables.concat(IterableExtensions.map(it, transformation));
    }

    /**
     * Flat map operation
     * 
     * @param it
     * @param transformation
     * @return
     */
    public static <T, R> Iterator<R> flatMap(final Iterator<T> it, final Function1<? super T, ? extends Iterator<R>> transformation) {
        return Iterators.concat(IteratorExtensions.map(it, transformation));
    }

    public static String getStrValue(AValueDef valueDef) {
        EObject root = EcoreUtil.getRootContainer(valueDef);
        ARepository repo = null;
        if (root instanceof ARepository) {
            repo = (ARepository) root;
        }
        
        String value = valueDef.getValue();
    
        DecimalFormat formatter = new DecimalFormat(OPT_NUMBER_PATTERN_SCIENTIFIC_PATTERN);
        DecimalFormatSymbols symbol = DecimalFormatSymbols.getInstance(Locale.US);
        formatter.setDecimalFormatSymbols(symbol);
        
        try {
            formatter.applyPattern(getMetaData(repo, OPT_NUMBER_PATTERN).getValue());
        } catch (Exception e) {
            // TODO: handle exception
        }
    
        String label = value;
        if (label == null) {
            label = "";
        } else {
            try {
                float temp = Float.parseFloat(label);
                if (Float.isNaN(temp)) {
                    label = "NaN";
                } else if ((Math.abs(temp) > 100000 || Math.abs(temp) < 0.001) && temp != 0) {
                    label = formatter.format(temp);
                } else if (temp % 1 != 0) {
                    label = Math.round(temp * 1000.0) / 1000.0 + "";
                } else {
                    label = "" + (int) temp;
                }
            } catch (NumberFormatException e) {
            }
        }
    
        return label;
    }
    
    /** Gets the absolute File path of a Resource*/
    public static String getAbsoluteFilePathFrom(Resource resource){
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        
        String relFilePath = resource.getURI().path().replaceFirst("/resource","");
        
        String projectRootPath = root.getLocation().toString();
        String absFilePath = projectRootPath + relFilePath;
        
        return absFilePath;
    }

    /**
     * Creates a string representing a range
     */
    public static String getRangeString(ARange range) {
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
        df.applyPattern("#.##");
    
        String lower, upper;
        try {
            lower = df.format(Double.parseDouble(range.getLower()));
        } catch (NumberFormatException e) {
            lower = "-Inf";
        } catch (NullPointerException e) {
            lower = "-Inf";
        }
        try {
            upper = df.format(Double.parseDouble(range.getUpper()));
        } catch (NumberFormatException e) {
            upper = "Inf";
        } catch (NullPointerException e) {
            upper = "Inf";
        }
    
        String label = "[" + lower + ", " + upper + "]";
        return label;
    }

    public static String getModeCombinationString(AModeCombination modeCombi) {
        String label = "";
        EList<Mode> targetCombi = modeCombi.getModes();
        if (!targetCombi.isEmpty()) {
            label += " : ";
            int i;
            for (i = 0; i < targetCombi.size() - 1; i++) {
                label += targetCombi.get(i).getName() + " & ";
            }
            label += targetCombi.get(i).getName();
        }
        return label;
    }
}
