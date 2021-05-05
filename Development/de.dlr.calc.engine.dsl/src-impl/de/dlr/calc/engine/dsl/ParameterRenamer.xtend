/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.calc.engine.dsl

import com.google.inject.Inject
import de.dlr.calc.engine.dsl.scope.IBiMapBackedBalancingScope
import de.dlr.calc.engine.dsl.xtext.calcDsl.CalcDslPackage
import de.dlr.calc.engine.dsl.xtext.calcDsl.Model
import de.dlr.calc.engine.dsl.xtext.calcDsl.ParameterLiteral
import de.dlr.calc.engine.dsl.xtext.calcDsl.SourceParameter
import java.util.Collections
import java.util.List
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator
import de.dlr.premise.system.Balancing

import static extension de.dlr.premise.util.PremiseHelper.eAllContentsIncludingRoot

class ParameterRenamer {
	BalancingScopeFactory balancingScopeMapCreator = new BalancingScopeFactory
	@Inject ResourceProviderMy resourceProviderMy
	@Inject var IResourceValidator validator

	def void doRename(Balancing balancing) {
		val usedScope = balancingScopeMapCreator.createUsedScope(balancing)
		val scope = balancingScopeMapCreator.createScope(balancing)
		val renamedFunction = doCreateRenamedFunction(balancing, usedScope, scope)
		
		val renamedScope = balancingScopeMapCreator.createRenamedScope(usedScope, scope)
		
		val changeCommand = BalancingChangeCommandCreator.createChangeCommand(balancing, renamedFunction, renamedScope)
		
		val editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(balancing)
		if (editingDomain != null) {
			editingDomain.commandStack.execute(changeCommand)
		} else {
			changeCommand.execute()
		}
	}
	
	def createRenamedFunction(Balancing balancing) {
		val scope = balancingScopeMapCreator.createScope(balancing)
		val usedScope = balancingScopeMapCreator.createUsedScope(balancing)
		doCreateRenamedFunction(balancing, usedScope, scope)
	}
	
	def private doCreateRenamedFunction(Balancing balancing, IBiMapBackedBalancingScope usedScope, IBiMapBackedBalancingScope scope) {
		val document = DocumentCreator.createDocument(usedScope, balancing)
		val resource = resourceProviderMy.createResource(document)
		EcoreUtil.resolveAll(resource)
		
		val issues = validator.validate(resource, CheckMode.ALL, null)
		if (issues.size != 0) {
			System.err.println('''Function change ommited: Function '«balancing.function»' for Balancing '«balancing.name»' is not valid!''');
			System.err.println(issues.map[severity.name + ": " + message].join("\r\n"));
			return balancing.function
		}

		val model = resource.contents.get(0) as Model
		
		
		val List<RenameData> toRename = createRenameData(model, usedScope, scope)
		
		var newText = document
		// when we change an identifiers length by renaming, all subsequent offsets will be off by that amount
		// currentAdaptedOffset tracks how much we are off, so we can apply a correction
		var currentAdaptedOffset = 0
		for (RenameData ren : toRename) {
			val position = ren.node.offset + currentAdaptedOffset
			// splice in the new name in place of the old name
			newText = newText.substring(0, position) + ren.newName + newText.substring(position + ren.oldName.length)
			
			// record change of offset as difference of name lengths
			currentAdaptedOffset += ren.newName.length - ren.oldName.length
		}
		
		val targetNode = NodeModelUtils.findNodesForFeature(model, CalcDslPackage.Literals.MODEL__TARGET).head
		
		if (targetNode == null) {
			return balancing.function ?: ""
		}
		
		val function = getNewText(newText, targetNode)
		return function.trim
	}
	
	def private createRenameData(Model model, IBiMapBackedBalancingScope usedScope, IBiMapBackedBalancingScope scope) {
		val List<RenameData> toRename = newArrayList
					
		for (s : usedScope.sources.entrySet) {
			val sourcePointer = s.value
						
			val oldName = s.key
			val newName = scope.sources.entrySet
				.findFirst[value == sourcePointer]
				?.key
				
			if (oldName != null && newName != null && oldName != newName) {
				val sourceParameter = model.definitions.filter(SourceParameter).findFirst[name == oldName]
				
				val nodes = model.body?.eAllContentsIncludingRoot
					.filter(ParameterLiteral)
					.filter[value == sourceParameter]
					.map[NodeModelUtils.findNodesForFeature(it, CalcDslPackage.Literals.PARAMETER_LITERAL__VALUE)]
					.toList
					.flatten ?: Collections.EMPTY_LIST
					
				for (node : nodes) {
					toRename.add(new RenameData(node, oldName, newName))
				}
			}
		}
		
		for (s : usedScope.targets.entrySet) {
			val targetPointer = s.value
			
			val oldName = s.key
			val newName = scope.targets.entrySet
				.findFirst[value == targetPointer]
				?.key
				
			if (oldName != null && newName != null && oldName != newName) {
				val nodes = NodeModelUtils.findNodesForFeature(model, CalcDslPackage.Literals.MODEL__TARGET)
				
				for (node : nodes) {
					toRename.add(new RenameData(node, oldName, newName))
				}
			}
		}		
		
		return toRename.sortBy[node.offset]
	}
	
	def private getNewText(String newText, INode targetNode) {
		// we use totalOffset here to we get any comments before or after the end of the function
		return newText.substring(targetNode.totalOffset)
	}
	
	@FinalFieldsConstructor private static class RenameData{
		val INode node
		val String oldName
		val String newName
	}
}