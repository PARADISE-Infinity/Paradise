/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.query.persistent

import de.dlr.premise.element.ARepository
import de.dlr.premise.query.QueryMode
import de.dlr.premise.registry.MetaData
import de.dlr.premise.registry.RegistryFactory
import de.dlr.premise.registry.RegistryPackage
import de.dlr.premise.util.PremiseHelper
import java.util.Set
import org.eclipse.emf.common.command.CommandStack
import org.eclipse.emf.common.command.CompoundCommand
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.edit.command.AddCommand
import org.eclipse.emf.edit.command.RemoveCommand
import org.eclipse.emf.edit.command.SetCommand
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import org.eclipse.emf.edit.domain.EditingDomain

/**
 * @author enge_do
 */
package class MetadataQueryAccess implements PersistentQueryAccess {
	private static val viewMetaName = "View"
	private static val rootMetaName = "Persisted Queries"
	private static val modeMetaName = "Mode"
	private static val metaDataChildrenFeature = RegistryPackage.Literals.IPREMISE_OBJECT__META_DATA
	private static val metaDataValueFeature = RegistryPackage.Literals.META_DATA__VALUE
	private static val metaDataFeature = RegistryPackage.Literals.IPREMISE_OBJECT__META_DATA

	private val ResourceSet resourceSet

	private var EObject metaDataRoot

	new(ResourceSet resourceSet) {
		this.resourceSet = resourceSet
	}

	override Set<PersistableQuery> readQueries() {
		metaDataRoots.map[metaData].flatten.map[fromMetaData].toSet
	}

	private def PersistableQuery fromMetaData(MetaData metaData) {
		val modeString = metaData.metaData.findFirst[name == modeMetaName]?.value?.toUpperCase ?: QueryMode.FILTER.name
		new PersistableQuery(metaData.name, metaData.value, QueryMode.valueOf(modeString))
	}

	override void addQuery(PersistableQuery query) {
		val metaData = createMetaData(query.name, query.query)
		val modeMetaData = createMetaData(modeMetaName, query.mode.name)
		addUsingCommand(metaData, modeMetaData)
	}

	private def void addUsingCommand(MetaData query, MetaData mode) {
		val command = new CompoundCommand()

		findOrCreateMetaDataRoot(command)
		command.appendAndExecute(new AddCommand(editingDomain, metaDataRoot, metaDataChildrenFeature, query))
		command.appendAndExecute(new AddCommand(editingDomain, query, metaDataChildrenFeature, mode))

		if (command.canExecute) {
			commandStack.execute(command)
		}
	}

	private def void findOrCreateMetaDataRoot(CompoundCommand command) {
		metaDataRoot = metaDataRoots.head
		if (metaDataRoot === null) {
			metaDataRoot = createMetaData(rootMetaName, null)
			val repository = resourceSet.resources.head.contents.head as ARepository
			var metaDataView = PremiseHelper.getMetaData(repository, viewMetaName)
			if (metaDataView === null) {
				metaDataView = createMetaData(viewMetaName, null)
				command.appendAndExecute(new AddCommand(getEditingDomain(repository), repository, metaDataFeature, metaDataView))
			}
			command.appendAndExecute(
				new AddCommand(getEditingDomain(repository), metaDataView, metaDataChildrenFeature, metaDataRoot))
		}
	}

	override void updateQuery(PersistableQuery query) {
		val command = new CompoundCommand()
		findOrCreateMetaDataRoot(command)

		val metaData = metaDataRoots.map[PremiseHelper.getMetaData(it, query.name)].filterNull.head
		val modeMetaData = findOrCreateModeMetaData(command, metaData)

		command.append(new SetCommand(editingDomain, metaData, metaDataValueFeature, query.query))
		command.append(new SetCommand(editingDomain, modeMetaData, metaDataValueFeature, query.mode.name))

		if (command.canExecute) {
			commandStack.execute(command)
		}
	}

	private def MetaData findOrCreateModeMetaData(CompoundCommand command, MetaData query) {
		var mode = PremiseHelper.getMetaData(query, modeMetaName)
		if (mode === null) {
			mode = createMetaData(modeMetaName, null)
			command.append(new AddCommand(editingDomain, query, metaDataChildrenFeature, mode))
		}
		return mode
	}

	override void removeQuery(PersistableQuery query) {
		val metaData = metaDataRoots.map[PremiseHelper.getMetaData(it, query.name)].filterNull.head
		metaDataRoot = metaData.eContainer
		val command = new RemoveCommand(editingDomain, metaData.eContainer, metaDataChildrenFeature, metaData)
		if (command.canExecute) {
			commandStack.execute(command)
		}
	}

	private def getMetaDataRoots() {
		resourceSet.resources.map[contents].flatten.map[PremiseHelper.getMetaData(it, rootMetaName)].filterNull
	}

	private def CommandStack getCommandStack() {
		editingDomain.commandStack
	}

	private def EditingDomain getEditingDomain() {
		getEditingDomain(metaDataRoot)
	}

	private def EditingDomain getEditingDomain(EObject root) {
		AdapterFactoryEditingDomain.getEditingDomainFor(root)
	}

	private def createMetaData(String name, String value) {
		val metaData = RegistryFactory.eINSTANCE.createMetaData()
		metaData.name = name
		metaData.value = value
		return metaData
	}
}
