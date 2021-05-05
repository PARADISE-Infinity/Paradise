/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.exchange.matlab.struct.xtend

import de.dlr.exchange.base.xtend.GeneratorHelper
import de.dlr.exchange.base.xtend.ICharsetProvidingFileSystemAccess
import de.dlr.exchange.base.xtend.IGeneratorMy
import de.dlr.premise.element.AElement
import de.dlr.premise.element.ARepository
import de.dlr.premise.system.ProjectRepository
import de.dlr.premise.system.SystemComponent
import java.util.ArrayList
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.IFileSystemAccess

import static extension de.dlr.premise.util.PremiseHelper.*

class Generator implements IGeneratorMy {

	public static String OPT_TECH = "Matlab"
	
	override doGenerateFromAElements(ResourceSet resSet, List<AElement> selectedElements, ICharsetProvidingFileSystemAccess fsa) {
		if(!resSet.resources.nullOrEmpty && resSet.resources.get(0).contents.get(0) instanceof ARepository){
			if(!selectedElements.nullOrEmpty){
				selectedElements.compile(fsa)
			} else {
				println("No elements were found to export.")
			}
			
		}
	}
	
	override doGenerateFromResources(ResourceSet resSet, List<Resource> selectedFiles, ICharsetProvidingFileSystemAccess fsa) {
		// create selectedElements list from ProjectRepositories
		var selectedElements = selectedFiles.flatMap[contents].filter(ProjectRepository).flatMap[projects].filter(AElement).toList

		this.doGenerateFromAElements(resSet, selectedElements, fsa)
	}

	def dispatch void compile(ArrayList<AElement> selectedElements, IFileSystemAccess fsa) {
		fsa.generateFile(getValidStructName(selectedElements.get(0).name) + "_cfg.m", '''
	    	«FOR sc : selectedElements»
				«if (sc instanceof SystemComponent) (sc as SystemComponent).createStruct()»
	    	«ENDFOR»
		''')
	}

	def dispatch void compile(EObject m, IFileSystemAccess fsa) {
		println("Should not be printed!")
	}

	def createStruct(SystemComponent sc) '''
		%% component «sc.name»
		«val name = getValidStructName(sc.name)»
		«name» = struct;
		«name».name='«name»';
		«name».description='«sc.description»';
		«createParameters(sc, name)»
		«createChildren(sc, name)»

	'''

	def createParameters(SystemComponent sc, String prefix) '''
		«var idx = 1»
        «FOR param : sc.parameters»
			«val name = encodeName(param.name)»
			«prefix».param{«idx»}.name = '«name»';
			«IF !param.value?.value.nullOrEmpty»
			«prefix».param{«idx»}.value=«param.value.value»;
			«ENDIF»
			«prefix».param{«idx»}.unit='«param.unit?.symbol»';
			«prefix».param{«idx»}.description='«param.description»';
			«{idx=idx+1;""}»
        «ENDFOR»
	'''

	def CharSequence createChildren(SystemComponent sc, String prefix) '''
        «val range = 0..sc.referencedChildren.size-1»
        «IF range.end >= 0»
            «FOR idx : range»
                «val child = sc.referencedChildren.get(idx)»
                «var newPrefix = prefix»
                «val mIdx = idx + 1»
                «IF !child.metaTypes.empty»
                    % «newPrefix = newPrefix + "." + child.metaTypes.get(0).name + "{" + mIdx + "}"»
                «ELSE»
                    % «newPrefix = newPrefix + ".element{" + mIdx + "}"»
                «ENDIF»
                «val name = encodeName(child.name)»
                «newPrefix».name='«name»';
                «newPrefix».description='«child.description»';
                «createParameters(child, newPrefix)»
                «createChildren(child, newPrefix)»
            «ENDFOR»
        «ENDIF»
	'''

    def String encodeName(String name) {
        GeneratorHelper::encodeFileName(name).replace(' ','_').replace('.', '_');
    }
    
	def String getValidStructName(String name) {
		var validName = encodeName(name);
		return validName.replace(' ','').replace('_','').replace('"','').replace('-','');
	}
	
}
