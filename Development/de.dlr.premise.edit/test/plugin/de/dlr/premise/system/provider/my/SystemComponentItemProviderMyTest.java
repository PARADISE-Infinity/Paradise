/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.swt.graphics.Image;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.system.provider.my.SystemComponentItemProviderMy;
import de.dlr.premise.system.SystemFactory;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.registry.MetaTypeDef;
import de.dlr.premise.registry.RegistryFactory;
import de.dlr.premise.registry.provider.my.RegistryItemProviderAdapterFactoryMy;

public class SystemComponentItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static SystemComponentItemProviderMy itemAdapter;
    private static SystemComponent systemComponent;
    private static String defaultImageUrl = "/icons/full/obj16/SystemComponent.gif";
    private static String icon64 =
            "R0lGODlhEAAQAOeEAJJHFIVJKppGDo1JJH5NNI5LJpxJEo5LMZ5KEo5NKYpOL5hMIqxJDKhKEJpNHZpPHalME4dZAIlbAItcAIxdALdPEY1eALhQ"
          + "EpVXOr5QDJtXNpdkAJlmAJ5mEalhMK9jL6RqEq1kM6BtIKdvALJnNqptFa1sI6xzALtrN79rNLB2ALR4AKl6HMlvNLtzQ8pwNbh7AKt2XqV5Uql9JstyNsxyNcJ0QLt9AKt+J"
          + "rp/Db9/ANJ1OK+CKNN3OsKCALOFK8WEANl5OqWDc8iFALmCT8qHALeJLcyIALCLY7yNMMCQMr2RNcWUNcaRW8+dPM2bYs6cY9GhQs2lUtegasumcdmoSN+tc924aeq/X+nAYO"
          + "bDhuTFje3JbunKf+zLgevNgu7OhO3Qh/HPi+/Qh+/Rh/PRjfDTivTTgPDUi/XUkPLWjvLYkPTakvTciPTblPnbl/3bl/bel/7cmPbfmPfhm/jilfnjnf3jnPnlnvvmof/movv"
          + "oo+7m4/zppP/qpvzrpv3uqf3uqv7vq//xrP//////////////////////////////////////////////////////////////////////////////////////////////////"
          + "/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////"
          + "/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////"
          + "///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////yH"
          + "5BAEKAP8ALAAAAAAQABAAAAjYAP8JPEKwoEGBCIu0yYLlzB09EPX4KYLw35BBgupwqSJHjhUUU4ZUBAKoT59AUeDAcdHDBpCKPv7kyfPHyRMoTXYQMVFRxx47dvYwCVKjBYkX"
          + "NCrewDNnDh4lKTyE0KAlRkUYdNy4oZPkwwMAB97wyYFwRZw1a+IYEWAAgQMMC0ogVMEGDRo2PyDIQBKAQQMQCE+oCRNGDY8LVMQQyFChA8IRZr58MYNjgJAtCgokEIGQA5kuX"
          + "cbMKEO6TBoOFS0sueIFzAQKsGFbqChwAwspEmjrrhhh97+AADs=";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        adapterFactory = new RegistryItemProviderAdapterFactoryMy();
        itemAdapter = new SystemComponentItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
    }

    @Test
    public void testGetText() throws Exception {
        // initialize SystemComponent
        systemComponent = SystemFactory.eINSTANCE.createSystemComponent();

        assertEquals("SystemComponent ", itemAdapter.getText(systemComponent));

        systemComponent.setName("Component");
        assertEquals("SystemComponent Component", itemAdapter.getText(systemComponent));
    }

    @Test
    public void testGetImage() throws Exception {
        // initialize SystemComponent
        systemComponent = SystemFactory.eINSTANCE.createSystemComponent();

        assertEquals(URL.class, itemAdapter.getImage(systemComponent).getClass());
        assertEquals(defaultImageUrl, ((URL) itemAdapter.getImage(systemComponent)).getPath());

        // add 2 MetaTypeDefs
        MetaTypeDef metaType1 = RegistryFactory.eINSTANCE.createMetaTypeDef();
        metaType1.setName("Type1");
        MetaTypeDef metaType2 = RegistryFactory.eINSTANCE.createMetaTypeDef();
        metaType2.setName("Type2");
        systemComponent.getMetaTypes().add(metaType1);
        systemComponent.getMetaTypes().add(metaType2);
        assertEquals(defaultImageUrl, ((URL) itemAdapter.getImage(systemComponent)).getPath());

        // change icon of 2nd metaType, nothing happens
        metaType2.setIconBase64(icon64);
        assertEquals(defaultImageUrl, ((URL) itemAdapter.getImage(systemComponent)).getPath());

        // change icon of 1st metaType, icon changed
        metaType1.setIconBase64(icon64);
        assertEquals(Image.class, itemAdapter.getImage(systemComponent).getClass());

        // call of getImage() sets additional adapters?
        itemAdapter.getImage(systemComponent);
    }
}
