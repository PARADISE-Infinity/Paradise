/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl.xtext.ui.labeling

import com.google.inject.Inject
import de.dlr.calc.engine.dsl.xtext.calcDsl.SourceParameter
import de.dlr.calc.engine.dsl.xtext.calcDsl.TargetParameter
import java.net.URL
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider
import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider
import de.dlr.calc.engine.dsl.xtext.calcDsl.Function

/**
 * Provides labels for a EObjects.
 * 
 * see http://www.eclipse.org/Xtext/documentation.html#labelProvider
 */
class CalcDslLabelProvider extends DefaultEObjectLabelProvider {
	
	val static PREFIX = "platform:/plugin/de.dlr.premise.edit/icons/full/obj16/"

	@Inject
	new(AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	// Labels and icons can be computed like this:
	
//	def text(Greeting ele) {
//		'A greeting to ' + ele.name
//	}
//

	def String text(Function it) '''
		«name»(«parameters.join(", ")»«IF lastParameterMultiple && parameters.size > 0»...«ENDIF»)
	'''
	
	def image(SourceParameter ele) {
		ImageDescriptor.createFromURL(new URL(PREFIX + "Parameter.gif"))
	}	
	
	def image(TargetParameter ele) {
		ImageDescriptor.createFromURL(new URL(PREFIX + "Parameter.gif"))
	}
	
	def image(Function ele) {
		ImageDescriptor.createFromURL(new URL(PREFIX + "Function.gif"))
	}
}
