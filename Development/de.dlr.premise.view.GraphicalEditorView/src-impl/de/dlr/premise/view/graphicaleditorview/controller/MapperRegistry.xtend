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

import de.dlr.premise.component.Satisfies
import de.dlr.premise.element.ARepository
import de.dlr.premise.element.Connection
import de.dlr.premise.element.Relation
import de.dlr.premise.element.Transition
import de.dlr.premise.registry.MetaData
import de.dlr.premise.util.PremiseHelper
import de.dlr.premise.view.graphicaleditorview.GraphicalEditorView
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.EContext
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.IAspectToModelMapper
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionHolder
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.SelectionListener
import de.dlr.premise.view.graphicaleditorview.controller.modeltoviewmodel.hierarchy.HierarchyToModelMapper
import de.dlr.premise.view.graphicaleditorview.controller.viewmodeltoview.ModelToGraphMapper
import de.dlr.premise.view.graphicaleditorview.view.GraphItemProvider
import de.dlr.premise.view.graphicaleditorview.viewmodel.GraphableModel
import java.awt.Color
import org.eclipse.emf.edit.domain.EditingDomain

import static de.dlr.premise.view.graphicaleditorview.view.GraphRegistry.getGraph

/** 
 */
class MapperRegistry {

	static GraphableModel graphableModel = new GraphableModel()
	static IAspectToModelMapper mapper = new HierarchyToModelMapper()
	static ModelToGraphMapper modelToGraphMapper = new ModelToGraphMapper(graphableModel)
	static boolean isNodeCreationAllowed = false

	static EditingDomain editingDomain

	/** 
	 * @return the mapper
	 */
	def static IAspectToModelMapper getMapper() {
		return mapper
	}

	/** 
	 * @param mapper the mapper to set
	 */
	def static void setMapper(Class<? extends IAspectToModelMapper> mapper) {
		MapperRegistry.mapper.dispose()
		MapperRegistry.mapper = mapper.newInstance
		MapperRegistry.nodeCreationAllowed = MapperRegistry.getMapper.isNodeCreationAllowed(SelectionHolder.instance.selection)
		setRelationRealizer(currentContext)
		SelectionListener.instance.scheduleGraphing(0, false)
	}

	/** 
	 * @return the currentType
	 */
	def synchronized static EContext getCurrentContext() {
		return mapper.context
	}

	/**
	 * @return the graphableModel
	 */
	def static getGraphableModel() {
		return graphableModel
	}

	/**
	 * @param graphableModel the graphableModel to set
	 */
	def static void setGraphableModel(GraphableModel graphableModel) {
		MapperRegistry.graphableModel = graphableModel
	}

	/**
	 * @return the modelToGraphMapper
	 */
	def static getModelToGraphMapper() {
		return modelToGraphMapper
	}

	/**
	 * @param modelToGraphMapper the modelToGraphMapper to set
	 */
	def static void setModelToGraphMapper(ModelToGraphMapper modelToGraphMapper) {
		MapperRegistry.modelToGraphMapper = modelToGraphMapper
	}

	/**
	 * @return the current editingDomain
	 */
	def static getEditingDomain() {
		return editingDomain
	}

	/**
	 * @param editingDomain the current editingDomain
	 */
	def static void setEditingDomain(EditingDomain editingDomain) {
		MapperRegistry.editingDomain = editingDomain
	}

	/** 
	 * Sets the default EdgeRealizer for the given type of Relations
	 */
	def private static void setRelationRealizer(EContext type) {

		switch (type) {
			case HIERARCHY: {
				getGraph()?.setDefaultEdgeRealizer(GraphItemProvider.getRelationRealizer(Color.white))
			}
			case RELATION: {
				getGraph()?.setDefaultEdgeRealizer(GraphItemProvider.getRelationRealizer(Relation))
			}
			case CONNECTION: {
				getGraph()?.setDefaultEdgeRealizer(GraphItemProvider.getRelationRealizer(Connection))
			}
			case SATISFIES: {
				getGraph()?.setDefaultEdgeRealizer(GraphItemProvider.getRelationRealizer(Satisfies))
			}
			case STATES: {
				getGraph()?.setDefaultEdgeRealizer(GraphItemProvider.getRelationRealizer(Transition))
			}
		}
	}

	/** 
	 * Checks whether the groups-option is on or off for the current context. <br>
	 * Defaults to false.
	 */
	def static boolean isGroupsEnabled(ARepository repo) {
		var String context = getOptionContext()
		if (repo !== null) {
			var MetaData metaDataContext = PremiseHelper.getMetaData(repo, context)
			var MetaData metaDataOption = PremiseHelper.getMetaData(metaDataContext,
				GraphicalEditorView.OPT_GROUP_LAYOUT)
			return PremiseHelper.isOn(metaDataOption)
		}
		return false
	}

	/** 
	 * Checks whether the edge-grouping-option is on or off for the current context. <br>
	 * Defaults to false.
	 */
	def static boolean isEdgeGroupingEnabled(ARepository repo) {
		var String context = getOptionContext()
		if (repo !== null) {
			var MetaData metaDataContext = PremiseHelper.getMetaData(repo, context)
			var MetaData metaDataOption = PremiseHelper.getMetaData(metaDataContext,
				GraphicalEditorView.OPT_GROUP_EDGES)
			return PremiseHelper.isOn(metaDataOption)
		}
		return false
	}

	def private static String getOptionContext() {
		var String context = ""

		switch (MapperRegistry.getCurrentContext()) {
			case HIERARCHY: {
				context = GraphicalEditorView.OPT_CONTEXT_HIERARCHY
			}
			case RELATION: {
				context = GraphicalEditorView.OPT_CONTEXT_RELATION
			}
			case CONNECTION: {
				context = GraphicalEditorView.OPT_CONTEXT_CONNECTION
			}
			case SATISFIES: {
				context = GraphicalEditorView.OPT_CONTEXT_SATISFIES
			}
			case STATES: {
				context = GraphicalEditorView.OPT_CONTEXT_STATES
			}
		}
		return context
	}

	def static isNodeCreationAllowed() {
		return isNodeCreationAllowed
	}

	def static isEdgeCreationAllowed() {
		return getMapper.isEdgeCreationAllowed
	}

	def static setNodeCreationAllowed(boolean newValue) {
		isNodeCreationAllowed = newValue
	}
}
