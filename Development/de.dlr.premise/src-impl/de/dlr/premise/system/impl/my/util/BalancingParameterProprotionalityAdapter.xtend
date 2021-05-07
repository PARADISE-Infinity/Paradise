/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.system.impl.my.util

import de.dlr.premise.registry.AParameterDef
import de.dlr.premise.system.Balancing
import de.dlr.premise.system.ParameterProportionality
import de.dlr.premise.system.SystemPackage
import java.util.Map
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.common.notify.Notification
import org.eclipse.emf.common.notify.impl.AdapterImpl

class BalancingParameterProprotionalityAdapter extends AdapterImpl {
	static interface IBalancingSourceParameterProportionalityAnalyzer {
		def Map<AParameterDef, ParameterProportionality> getAnalysis(Balancing balancing)
	}
	
	def static findOrCreateParameterProprotionalityAdapter(Balancing balancing) {
		var adapter = balancing.eAdapters.filter(BalancingParameterProprotionalityAdapter).head
		if (adapter == null) {
			adapter = new BalancingParameterProprotionalityAdapter(balancing)
			balancing.eAdapters.add(adapter)
		}
		return adapter
	}
	
	val Balancing balancing
	
	var BalancingParameterProprotionalityAdapter.IBalancingSourceParameterProportionalityAnalyzer balancingSourceParameterProportionalityAnalyzer
	var Map<AParameterDef, ParameterProportionality> cachedAnalyis = null
	
	new(Balancing balancing) {
		this.balancing = balancing
		

	}
	
	def getParameterProportionality(AParameterDef param) {
		val analysis = getAnalysis()
		if (analysis == null) {
			return null
		}
		return analysis.get(param)
	}
	
	override notifyChanged(Notification msg) {
		if (msg.feature == SystemPackage.Literals.BALANCING__FUNCTION) {
			println("Cleaning " + balancing)
			cachedAnalyis = null
		}
	}
	
	private def getAnalysis() {
		if (cachedAnalyis == null) {
			val analyzer = getAnalyzer()
			if (analyzer != null) {
				cachedAnalyis = analyzer.getAnalysis(this.balancing)
			}
		}
		return cachedAnalyis
	}
	
	private def getAnalyzer() {
		if (balancingSourceParameterProportionalityAnalyzer == null) {
			val config = Platform.getExtensionRegistry().getConfigurationElementsFor("de.dlr.premise.balancingSourceParameterProportionalityAnalyzer");
			var BalancingParameterProprotionalityAdapter.IBalancingSourceParameterProportionalityAnalyzer classifier = null
	        try {
	           classifier = config.head.createExecutableExtension("class") as BalancingParameterProprotionalityAdapter.IBalancingSourceParameterProportionalityAnalyzer
	        } catch (Throwable e) {
	        	System.err.println("Couldn't create BalancingSourceParameterProportionalityAnalyzer")
	        	e.printStackTrace
	        }
	        balancingSourceParameterProportionalityAnalyzer = classifier
		}
		return balancingSourceParameterProportionalityAnalyzer
	}
}