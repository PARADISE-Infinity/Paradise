/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.element.provider.my;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import de.dlr.premise.element.ElementFactory;
import de.dlr.premise.element.Relation;
import de.dlr.premise.provider.util.PremiseAdapterFactoryHelper;
import de.dlr.premise.system.SystemComponent;
import de.dlr.premise.system.SystemFactory;

public class RelationItemProviderMyTest {

    private static AdapterFactory adapterFactory;
    private static RelationItemProviderMy itemAdapter;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // initialize adapter factory
        adapterFactory = PremiseAdapterFactoryHelper.createComposedAdapterFactory();    
    }

	@Test
	public void testRelationItemProviderMy() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new RelationItemProviderMy(adapterFactory);
        assertNotNull(itemAdapter);
	}

	@Test
	public void testGetText() throws Exception {
        // initialize item adapter by using registry factory (to allow calling getText())
        itemAdapter = new RelationItemProviderMy(adapterFactory);

        Relation sysRel = null;
        assertNull(itemAdapter.getText(sysRel));
                
        sysRel = ElementFactory.eINSTANCE.createRelation();
        SystemComponent src = SystemFactory.eINSTANCE.createSystemComponent();
        src.setName("1");
        sysRel.setSource(src);

        SystemComponent tar = SystemFactory.eINSTANCE.createSystemComponent();
        tar.setName("1");
        sysRel.setTarget(tar);
        
        assertEquals("Relation SystemComponent 1 > SystemComponent 1", itemAdapter.getText(sysRel));
	}
}
