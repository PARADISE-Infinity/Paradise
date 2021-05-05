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

import de.dlr.calc.engine.dsl.scope.IBalancingScope
import de.dlr.calc.engine.dsl.scope.IUsedBalancingScope
import de.dlr.calc.engine.dsl.scope.IVisibleBalancingScope
import de.dlr.premise.system.Balancing

class DocumentCreator {
	def static String createDefinitions(IBalancingScope scope) '''
		definitions
		«FOR sp : scope.sourceNames»
			sourceParameter «sp»
		«ENDFOR»
		«scope.createTargets»
		«FOR fn : scope.functionNames»
			function «fn» «scope.getFunction(fn).inputs.map['''"«IF !name.nullOrEmpty»«name»«ELSE»UnnamedParameter«ENDIF»"'''].join(" ")» «IF scope.getFunction(fn).lastInputMultiple»lastParameterMultiple«ENDIF»
		«ENDFOR»
	'''
	
	def static String createPrefix(IBalancingScope scope) '''
		«scope.createDefinitions»
		body
	'''
	
	def static String createDocument(IBalancingScope scope, String function) '''
		«scope.createPrefix»
		«function»
	'''
	
	def static String createDocument(IBalancingScope scope, Balancing balancing) {
		createDocument(scope, balancing.function)
	}
	
	private def static dispatch createTargets(IUsedBalancingScope scope) '''
		targetParameter «scope.targetName»
	'''
	
	private def static dispatch createTargets(IVisibleBalancingScope scope) '''
		«FOR tp : scope.targetNames»
			targetParameter «tp»
		«ENDFOR»
	'''
}