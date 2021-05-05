/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.registry.impl.my;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.dlr.premise.registry.Note;
import de.dlr.premise.registry.RegistryFactory;

public class RegistryFactoryImplMyTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testCreateNote() throws Exception {

		// get expected default data
		String author = System.getProperty("user.name");

		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
		String time = df.format(dt);
		
		// test creation
		Note note = RegistryFactory.eINSTANCE.createNote();
		assertNotNull(note);

		assertEquals(author, note.getAuthor());
		assertEquals(time, note.getDate());
		assertNull(note.getText());
	}
}
