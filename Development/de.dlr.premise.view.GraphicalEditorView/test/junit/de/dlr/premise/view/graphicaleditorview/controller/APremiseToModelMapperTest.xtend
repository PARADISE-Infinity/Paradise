/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.view.graphicaleditorview.controller

import de.dlr.premise.element.Connection
import de.dlr.premise.element.ElementFactory
import de.dlr.premise.element.Relation
import de.dlr.premise.query.QueryMode
import de.dlr.premise.query.persistent.PersistableQuery
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import de.dlr.premise.system.SystemFactory
import de.dlr.premise.system.presentation.my.query.CollectionBasedPredicate
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.EContext
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionHolder
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionListener
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.connection.ConnectionToModelMapper
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.hierarchy.HierarchyToModelMapper
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.relation.RelationToModelMapper
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.satisfies.SatisfiesToModelMapper
import de.dlr.premise.view.graphicaleditorview.view.GraphRegistry
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableEdge
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableNode
import org.eclipse.emf.common.command.BasicCommandStack
import org.eclipse.emf.common.command.CommandStack
import org.eclipse.emf.common.notify.AdapterFactory
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.impl.ResourceImpl
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain
import org.eclipse.emf.edit.provider.ComposedAdapterFactory
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory
import org.eclipse.emf.edit.tree.provider.TreeItemProviderAdapterFactory
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import static de.dlr.premise.view.graphicaleditorview.controller.MapperRegistry.*
import static de.dlr.premise.view.graphicaleditorview.view.GraphRegistry.*
import static org.junit.Assert.*

/** 
 */
class APremiseToModelMapperTest {

	var ProjectRepository repo
	var SystemComponent root
	var SystemComponent child1
	var SystemComponent child2
	var SystemComponent subchild1
	var SystemComponent subchild2
	var Parameter parameter1
	var Relation relation1
	var Connection<SystemComponent> connection1

	static var SelectionListener selectionListener

	@BeforeClass
	static def void setupBeforeClass() {
		selectionListener = SelectionListener.instance
		selectionListener.enableSchedule = false
		mapper = RelationToModelMapper
		graphableModel.deleteObserver(modelToGraphMapper)
		GraphRegistry.depth = 0
	}

	@Before
	def void setupBefore() {
		// create a simple premise-model
		parameter1 = SystemFactory.eINSTANCE.createParameter
		parameter1.name = "parameter1"

		subchild1 = SystemFactory.eINSTANCE.createSystemComponent
		subchild1.name = "subchild1"
		subchild1.parameters.add(parameter1)

		subchild2 = SystemFactory.eINSTANCE.createSystemComponent
		subchild2.name = "subchild2"

		child1 = SystemFactory.eINSTANCE.createSystemComponent
		child1.name = "child1"
		child1.children.add(subchild1)
		child1.children.add(subchild2)

		child2 = SystemFactory.eINSTANCE.createSystemComponent
		child2.name = "child2"

		relation1 = ElementFactory.eINSTANCE.createRelation
		relation1.name = "relation1"
		relation1.source = child2
		relation1.target = subchild2

		connection1 = ElementFactory.eINSTANCE.createConnection
		connection1.name = "connection1"
		connection1.source = child1
		connection1.target = child2

		root = SystemFactory.eINSTANCE.createSystemComponent
		root.name = "root"
		root.children.add(child1)
		root.children.add(child2)
		root.relations.add(relation1)
		root.connections.add(connection1)

		repo = SystemFactory.eINSTANCE.createProjectRepository
		repo.projects.add(root)

		SelectionHolder.instance.query = new PersistableQuery("empty", "", QueryMode.FILTER);
		SelectionHolder.instance.predicate = new CollectionBasedPredicate[]
		SelectionHolder.instance.predicate.setCollections(
			#[root, child1, child2, subchild1, subchild2, parameter1, relation1, connection1], #[])

		// setup eclipse command stuff
		var AdapterFactory adapterFactory = new ComposedAdapterFactory(
			#[new ReflectiveItemProviderAdapterFactory, new TreeItemProviderAdapterFactory])
		var CommandStack commandStack = new BasicCommandStack()
		var editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack)
		var resourceSet = editingDomain.resourceSet
		var resource = new ResourceImpl
		resource.contents.add(repo)
		resource.basicSetResourceSet(resourceSet, null)
	}

	@Test
	def mapRootToModel() {
		mapper = RelationToModelMapper
		mapper.selectionChanged(createSelection(root))

		val flatGraphableModel = graphableModel.map[name].toList

		assertTrue(flatGraphableModel.contains("root"))
		assertTrue(flatGraphableModel.contains("subchild2"))
		assertTrue(flatGraphableModel.contains("parameter1"))
		assertTrue(flatGraphableModel.contains("relation1"))
		assertTrue(flatGraphableModel.contains("connection1"))
	}

	@Test
	def selectionChanged() {
		mapper = RelationToModelMapper
		mapper.selectionChanged(createSelection(root))
		mapper.selectionChanged(createSelection(parameter1, child2))
		val model = graphableModel.map[name].toList

		assertFalse(model.contains("root"))
		assertFalse(model.contains("child1"))
		assertTrue(model.contains("parameter1"))
		assertTrue(model.contains("child2"))
	}

	@Test
	def contentChanged() {
		mapper = ConnectionToModelMapper
		repo.eAdapters.clear
		SelectionHolder.instance.selection = createSelection(root)
		mapper.selectionChanged(SelectionHolder.instance.selection)

		child2.name = "childish"
		mapper.selectionChanged(SelectionHolder.instance.selection)

		var model = graphableModel.map[name].toList
		assertTrue(model.contains("childish"))
	}

	@Test
	def contextChanged() {
		mapper = SatisfiesToModelMapper
		mapper.selectionChanged(createSelection(root))

		var flatGraphableModel = graphableModel.map[name].toList

		assertTrue(flatGraphableModel.contains("root"))
		assertTrue(flatGraphableModel.contains("subchild2"))
		assertTrue(flatGraphableModel.contains("parameter1"))

		mapper = ConnectionToModelMapper
		mapper.selectionChanged(createSelection(root))
		flatGraphableModel = graphableModel.map[name].toList

		assertTrue(flatGraphableModel.contains("root"))
		assertTrue(flatGraphableModel.contains("subchild2"))
		assertTrue(flatGraphableModel.contains("parameter1"))
	}

	@Test
	def getContext() {
		mapper = RelationToModelMapper
		assertEquals(mapper.context, EContext.RELATION)

		mapper = ConnectionToModelMapper
		assertEquals(mapper.context, EContext.CONNECTION)

		mapper = SatisfiesToModelMapper
		assertEquals(mapper.context, EContext.SATISFIES)
	}

	@Test
	def filterIsActive() {
		mapper = HierarchyToModelMapper
		mapper.selectionChanged(createSelection(root))

		var flatGraphableModel = graphableModel.map[name].toList

		assertTrue(flatGraphableModel.contains("root"))
		assertTrue(flatGraphableModel.contains("subchild2"))
		assertTrue(flatGraphableModel.contains("parameter1"))
		assertTrue(flatGraphableModel.contains("relation1"))
		assertTrue(flatGraphableModel.contains("connection1"))

		// filter parameters and relations
		SelectionHolder.instance.predicate.setCollections(#[root, child1, child2, subchild1, subchild2, connection1],
			#[parameter1, relation1])

		mapper.selectionChanged(createSelection(root))

		flatGraphableModel = graphableModel.map[name].toList

		assertTrue(flatGraphableModel.contains("root"))
		assertTrue(flatGraphableModel.contains("subchild2"))
		assertFalse(flatGraphableModel.contains("parameter1"))
		assertFalse(flatGraphableModel.contains("relation1"))
		assertTrue(flatGraphableModel.contains("connection1"))

		// filter connections
		SelectionHolder.instance.predicate.setCollections(#[root, child1, child2, subchild1, subchild2, parameter1,
			relation1], #[connection1])

		mapper.selectionChanged(createSelection(root))

		flatGraphableModel = graphableModel.map[name].toList

		assertTrue(flatGraphableModel.contains("root"))
		assertTrue(flatGraphableModel.contains("subchild2"))
		assertTrue(flatGraphableModel.contains("parameter1"))
		assertTrue(flatGraphableModel.contains("relation1"))
		assertFalse(flatGraphableModel.contains("connection1"))
	}

	@Test
	def dispose() {
		mapper = RelationToModelMapper
		mapper.selectionChanged(createSelection(root))
		mapper.dispose()
		assertTrue(graphableModel.isEmpty)
	}

	@Test
	def isSelectionEmptyForMapper() {
		val s = newArrayList()
		s.add(ElementFactory.eINSTANCE.createConnection)
		s.add(ElementFactory.eINSTANCE.createMode)
		s.add(ElementFactory.eINSTANCE.createGuardCombination)

		val selection = createSelection(s)
		assertTrue(mapper.isSelectionEmptyForMapper(selection))

		selection.add(SystemFactory.eINSTANCE.createSystemComponent)
		assertFalse(mapper.isSelectionEmptyForMapper(selection))
	}

	@Test
	def graphableModelChanged() {
		mapper = ConnectionToModelMapper
		mapper.selectionChanged(createSelection(root))

		var relation = graphableModel.findFirst[name == "relation1"]
		var editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(root)
		println(editingDomain)
		relation.delete

		var gNodes = graphableModel.filter(GraphableNode)
		new GraphableEdge("new_connection", gNodes.findFirst[name == "child1"], gNodes.findFirst[name == "child2"])

		assertFalse(root.relations.contains(relation1))
		assertTrue(root.connections.exists[name == "new_connection"])
	}

	private def createSelection(EObject... elements) {
		var selection = newArrayList(elements)
		if (!mapper.isSelectionEmptyForMapper(selection)) {
			selectionListener.prepare(selection)
		}
		return selection
	}
}
