/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.unity.parameter.xtend

import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.exchange.base.xtend.IGeneratorMy
import de.dlr.premise.element.AElement
import de.dlr.premise.util.PremiseHelper
import java.util.HashMap
import java.util.List
import java.util.Map
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import de.dlr.premise.system.ComponentReference
import de.dlr.premise.system.Parameter
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent

class UnityParameterGenerator implements IGeneratorMy {

	public static String OPT_TECH = "UnityParameter"
	
	private static val VEHICLE_TOP_NAME = "Spitze_SLOT"
	private static val VEHICLE_BODY_NAME = "Rumpf_SLOT"
	private static val VEHICLE_ENGINE_NAME = "Triebwerk_SLOT"
	private static val SAGITTA_KEY_NAME = "Sagitta"
	private static var heightT = 0.0
	private static var heightR = 0.0
	private static var CwGes = 0.0
	private static Map<String, String> partNameToID = new HashMap()
	
	
	
	enum VehiclePart {
		Spitze, Rumpf, Triebwerk;
	}
	
	def fillPartNameMapWithIDs() {
		partNameToID.put("Sagitta", "0")
		partNameToID.put("Spitze_Rund", "1")
		partNameToID.put("Spitze_Spitz", "2")
		partNameToID.put("Spitze_Ueberstehend", "3")
		partNameToID.put("Rumpf_Ohne", "4")
		partNameToID.put("Rumpf_Drohne", "5")
		partNameToID.put("Rumpf_Delta", "6")
		partNameToID.put("Triebwerk_Jet", "7")
		partNameToID.put("Triebwerk_Einfach", "8")
		partNameToID.put("Triebwerk_Dreifach", "9")
	}
	
	override doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {
		(selectedElements as List<?> as List<SystemComponent>).findFirstVehicle.doGenerate(fsa)
	}
	
	override doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles, ICharsetProvidingFileSystemAccess fsa) {
		val root = selectedFiles.head.contents.head as ProjectRepository
		val rootElements = root.projects
		rootElements.findFirstVehicle.doGenerate(fsa)
	}
	
	def findFirstVehicle(List<SystemComponent> selectedElements) {
		selectedElements.findFirst[vehicle]
	}
	
	def isVehicle(SystemComponent selectedElement){
		selectedElement.containsSystemComponentWithName(VEHICLE_TOP_NAME) 
			&& selectedElement.containsSystemComponentWithName(VEHICLE_BODY_NAME) 
			&& selectedElement.containsSystemComponentWithName(VEHICLE_ENGINE_NAME)
	}
	
	def containsSystemComponentWithName(SystemComponent element, String name){
		if(element.getReferencedChildren.findFirst[it.name.equals(name)] != null ){
			return true
		}
	}
	
	def doGenerate(AElement selectedElement, ICharsetProvidingFileSystemAccess fsa) {
		if(selectedElement == null){
			println("Export abgebrochen!")
			println("Folgende SystemComponents müssen vorhanden sein:")
			println("---- <beliebiger Name>")
			println("   |-"+VEHICLE_TOP_NAME)
			println("   |-"+VEHICLE_BODY_NAME)
			println("   |-"+VEHICLE_ENGINE_NAME)
			return null
		}
		fillPartNameMapWithIDs()
		val fileName = GeneratorHelper::encodeFileName(selectedElement.name)
		fsa.generateFile(fileName + ".xml" ,
			'''
			<?xml version="1.0" encoding="«fsa.getFileCharset(fileName)»"?>
			«IF(fileName == SAGITTA_KEY_NAME)»
			<ItemDatabase xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
			  <RocketParts>
			    «generateItemEntry(selectedElement as SystemComponent)»
			  </RocketParts>
			</ItemDatabase>
			«ELSE»
			«{	// Save the Cw value as global parameter to calculate later local Cw values for the parts
				CwGes = Double.parseDouble((selectedElement as SystemComponent).getReferencedChildren.findFirst[it.name.equals(VEHICLE_TOP_NAME)]
				.getParametersFromItemEntry.getValueFromParameter("Luftwiederstand"));""}»
			<ItemDatabase xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
			  <RocketParts>
			    «generateItemEntry(selectedElement as SystemComponent, VEHICLE_ENGINE_NAME)»
			    «generateItemEntry(selectedElement as SystemComponent, VEHICLE_BODY_NAME)»
			    «generateItemEntry(selectedElement as SystemComponent, VEHICLE_TOP_NAME)»
			  </RocketParts>
			</ItemDatabase>
			«ENDIF»
			«{println("Datei wurde exportiert!");""}»
			'''
		)
	}
		
	def generateItemEntry(SystemComponent root, String partName) {
		var partComponent = root.getReferencedChildren.findFirst[it.name.equals(partName)]
		var params = partComponent.getParametersFromItemEntry
		
		var VehiclePart vehiclePart;
		switch partName{
			case VEHICLE_TOP_NAME: vehiclePart = VehiclePart.Spitze
			case VEHICLE_BODY_NAME: vehiclePart = VehiclePart.Rumpf
			case VEHICLE_ENGINE_NAME: vehiclePart = VehiclePart.Triebwerk
		}
		
		return 
		'''
		<ItemEntry>
		  <name>«vehiclePart»</name>
		  <prefabID>«getPartID(partComponent)»</prefabID>
		  <mass>«params.getValueFromParameter("Gewicht")»</mass>
		  <drag>«CwGes.calculateLocalDrag(vehiclePart)»</drag>
		  <thrust>«params.getValueFromParameter("Schub")»</thrust>
		  <tankVol>«params.getValueFromParameter("Tankinhalt")»</tankVol>
		  <fuelConsumption>«params.getValueFromParameter("Verbrauch")»</fuelConsumption>
		  <lift>«params.getValueFromParameter("Auftrieb")»</lift>
		  <v3pos>
		    <x>0</x>
		    <y>«vehiclePart.calculatePositioningHeight(params)»</y>
		    <z>0</z>
		  </v3pos>
		  <v3rot>
		    <x>0</x>
		    <y>0</y>
		    <z>0</z>
		  </v3rot>
		  <v3sca>
		    <x>«root.parametersFromItemEntry.getValueFromParameter("Durchmesser")»</x>
		    <y>«root.parametersFromItemEntry.getValueFromParameter("Durchmesser")»</y>
		    <z>«IF(vehiclePart == VehiclePart.Spitze)»«params.getValueFromParameter("Hoehe")»«
		    	ELSEIF(vehiclePart == VehiclePart.Rumpf)»«params.getValueFromParameter("Laenge")»«
		    	ELSEIF(vehiclePart == VehiclePart.Triebwerk)»«params.getValueFromParameter("Durchmesser")»«
		    	ENDIF»</z>
		  </v3sca>
		</ItemEntry>
		'''
	}
	
	def generateItemEntry(SystemComponent root) {									// Only for Sagitta case
		var params = root.getParametersFromItemEntry
						
		return 
		'''
		<ItemEntry>
		  <name>«SAGITTA_KEY_NAME»</name>
		  <prefabID>0</prefabID>
		  <mass>«params.getValueFromParameter("Gewicht ges")»</mass>
		  <drag>«params.getValueFromParameter("Luftwiederstand")»</drag>
		  <thrust>«params.getValueFromParameter("Schub")»</thrust>
		  <tankVol>«params.getValueFromParameter("Tankinhalt")»</tankVol>
		  <fuelConsumption>«params.getValueFromParameter("Verbrauch")»</fuelConsumption>
		  <lift>«params.getValueFromParameter("Auftrieb")»</lift>
		  <v3pos>
		    <x>0</x>
		    <y>0</y>
		    <z>0</z>
		  </v3pos>
		  <v3rot>
		    <x>0</x>
		    <y>0</y>
		    <z>0</z>
		  </v3rot>
		  <v3sca>
		    <x>«root.parametersFromItemEntry.getValueFromParameter("Durchmesser")»</x>
		    <y>«root.parametersFromItemEntry.getValueFromParameter("Durchmesser")»</y>
		    <z>«root.parametersFromItemEntry.getValueFromParameter("Durchmesser")»</z>
		  </v3sca>
		</ItemEntry>
		'''
	}
		
	def getPartID(SystemComponent root) {
		var compRefs = PremiseHelper::getAll(root, ComponentReference)
		var name = (compRefs.get(0) as ComponentReference).activeImplementation.name
		return partNameToID.get(name)
	}
	
	def getParametersFromItemEntry(SystemComponent root){
		var compRefs = PremiseHelper::getAll(root, ComponentReference)
		var actParams = getActiveParameterFromComponentReference(compRefs.get(0) as ComponentReference)
		var allParams = (PremiseHelper::getAll(root, Parameter) as List<?> as List<Parameter>) + actParams
		return allParams
	}
	
	def getActiveParameterFromComponentReference(ComponentReference compRef){
		return PremiseHelper::getAll(compRef.activeImplementation, Parameter) as List<?> as List<Parameter>
	}
	
	def getValueFromParameter(Iterable<Parameter> params, String name){
		if(!params.nullOrEmpty){
			var parameter = params.findFirst[it.name.equals(name)]
			if(parameter != null){
				var value = parameter.value.value
				if(value.nullOrEmpty){
					value = "0"
				}
				return value
			}
		}
		return "0"
	}
		
	def calculateLocalDrag(double cwges, VehiclePart vehiclePart){
		var localCw = 0.0
		if(vehiclePart == VehiclePart.Spitze){
			localCw = cwges * 0.8 * (1.0/3.0)
		} else{
			localCw = cwges * 0.8 * (1.0/3.0) + cwges * 0.1
		}
		return localCw
	}
	
	def calculatePositioningHeight(VehiclePart vehiclePart, Iterable<Parameter> params) {
		var height = "0"
		if(vehiclePart == VehiclePart.Spitze){
			height = ""+(heightT + heightR + (Double.parseDouble(params.getValueFromParameter("Hoehe")) / 2))
		}else if(vehiclePart == VehiclePart.Rumpf){
			heightR = Double.parseDouble(params.getValueFromParameter("Laenge"))
			height = ""+(heightT + heightR/2)
		}else if(vehiclePart == VehiclePart.Triebwerk){
			heightT = Double.parseDouble(params.getValueFromParameter("Durchmesser"))
			height = ""+(heightT / 2)
		}
		return height
	}
}
