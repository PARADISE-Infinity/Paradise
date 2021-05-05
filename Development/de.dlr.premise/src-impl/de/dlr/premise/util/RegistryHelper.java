/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.util;

import java.lang.reflect.Field;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;


/**
 * @author hschum
 *
 */
public class RegistryHelper {

    /**
     * Registers a package and an overridden factory, if not already done. Should be called if plug-in is not executed (e.g. for JUnit
     * tests).
     *
     * @param packageNsUri Namespace URI of package (String)
     * @param factoryImpl Object of overridden factory (EFactoryImpl)
     */
    public static void registerFactory(final String packageNsUri, final EFactory factoryImpl) {
        EFactory currentFactory = getFactoryInstance(packageNsUri);

        if (currentFactory != null && !currentFactory.getClass().equals(factoryImpl.getClass())) {
            throw new RuntimeException(
                    "Tried to register a different type of factory for a package where a factory was already registered! eINSTANCE static field will NOT be up to date!");
        }

        final EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(packageNsUri);

        EPackage.Descriptor descriptor;
        descriptor = new EPackage.Descriptor() {

            @Override
            public EPackage getEPackage() {
                return ePackage;
            }

            @Override
            public EFactory getEFactory() {
                return factoryImpl;
            }
        };
        // register factory
        EPackage.Registry.INSTANCE.put(packageNsUri, descriptor);
    }

    /**
     * Gets the shared instance (static eINSTANCE field on the factory interface) of the factory for a given package.
     *
     * It first gets an instance of the type registered as factory for the given packageNsUri. Note that the registry will most likely
     * return a new instance of the registered package, if it was registered through the "factory_override" extension point. Therefore it
     * cannot be used directly.
     *
     * The returned factory will however most likely implement the factory interface for the package. Therefore, we can access its eINSTANCE
     * field reflectively to get the shared instance.
     *
     * @param packageNsUri Namespace URI of package (String)
     * @return The shared instance of the package's factory or null, if none can be found.
     */
    public static EFactory getFactoryInstance(final String packageNsUri) {
        EFactory currentFactory = EPackage.Registry.INSTANCE.getEFactory(packageNsUri);

        if (currentFactory != null) {
            // Try to access the static field eINSTANCE of the retrieved class and use is in the original classes stead.
            Class<?> clazz = currentFactory.getClass();
            try {
                Field staticInstanceField = clazz.getField("eINSTANCE");
                Object staticInstance = staticInstanceField.get(currentFactory);
                if (staticInstance instanceof EFactory) {
                    currentFactory = (EFactory) staticInstance;
                }
            } catch (SecurityException e) {
                currentFactory = null;
            } catch (NoSuchFieldException e) {
                currentFactory = null;
            } catch (IllegalArgumentException e) {
                currentFactory = null;
            } catch (IllegalAccessException e) {
                currentFactory = null;
            }
        }
        return currentFactory;
    }

    /**
     * Registers a file extension with a corresponding factory
     *
     * @param extension File extension to be registered with resource factory (String)
     * @param factoryImpl Object of resource factory (ReourceFactoryImpl)
     */
    static public void registerExtensionFactory(final String extension, final ResourceFactoryImpl factoryImpl) {
        // if factory is not registered, register it
        if (Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().get(extension) != factoryImpl) {
            Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(extension, factoryImpl);
        }
    }

    /**
     * Registers a package in the resource registry of a given resource
     *
     * @param resource Resource (e.g. .xmi file) for the package to register
     * @param nsURI Namespace URI for the package to register (EPackage.eNS_URI)
     * @param packageInstance Instance of package (EPackage.eINSTANCE())
     */
    static public void registerPackageForResource(final Resource resource, final String nsURI, final EPackage packageInstance) {
        EPackage.Registry registry = resource.getResourceSet().getPackageRegistry();
        if (registry.get(nsURI) != packageInstance) {
            registry.put(nsURI, packageInstance);
        }
    }
}
