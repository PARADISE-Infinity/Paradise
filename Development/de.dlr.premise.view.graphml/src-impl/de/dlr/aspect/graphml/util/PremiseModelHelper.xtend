/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.aspect.graphml.util

import de.dlr.premise.component.ComponentRepository
import de.dlr.premise.element.AElement
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.functions.UseCase
import de.dlr.premise.functions.UseCaseRepository
import java.util.List

class PremiseModelHelper {
	def static dispatch List<? extends AElement> getContained(ProjectRepository prRep) {
		return prRep.projects
	}

	def static dispatch List<? extends AElement>  getContained(UseCaseRepository ucRep) {
		return ucRep.usecases
	}
	
	def static dispatch List<? extends AElement> getContained(ComponentRepository compRep) {
		return compRep.components
	}
	
	def static dispatch List<? extends AElement>  getContained(UseCase uc) {
		return uc.children
	}

	def static dispatch List<? extends AElement>  getContained(SystemComponent sc) {
		return sc.children.filter(SystemComponent).toList
	}
	
	def static dispatch getRelations(UseCase uc) {
		return uc.relations
	}
	
	def static dispatch getRelations(SystemComponent sc) {
		return sc.relations
	}
}