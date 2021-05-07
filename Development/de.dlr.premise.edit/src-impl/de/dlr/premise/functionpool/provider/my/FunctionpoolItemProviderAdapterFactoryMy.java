/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.functionpool.provider.my;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.edit.provider.IUpdateableItemText;

import de.dlr.premise.functionpool.provider.FunctionpoolItemProviderAdapterFactory;

public class FunctionpoolItemProviderAdapterFactoryMy extends 
	FunctionpoolItemProviderAdapterFactory {

    public FunctionpoolItemProviderAdapterFactoryMy() {
        super();

        // enable support for inline editing of attributes in tree editor
        supportedTypes.add(IUpdateableItemText.class);
    }

	@Override
	public Adapter createFnInputSpreadSheetAdapter() {
		if (fnInputSpreadSheetItemProvider == null)	{
			fnInputSpreadSheetItemProvider = new FnInputSpreadSheetItemProviderMy(this);
		}
		return fnInputSpreadSheetItemProvider;
	}

	@Override
	public Adapter createFnOutputSpreadSheetAdapter() {
		if (fnOutputSpreadSheetItemProvider == null) {
			fnOutputSpreadSheetItemProvider = new FnOutputSpreadSheetItemProviderMy(this);
		}
		return fnOutputSpreadSheetItemProvider;
	}
	
	@Override
	public Adapter createCalcEngineScriptAdapter() {
		if(calcEngineScriptItemProvider == null) {
			calcEngineScriptItemProvider = new CalcEngineScriptItemProviderMy(this);
		}	
		return calcEngineScriptItemProvider;		
	}
	
	@Override
	public Adapter createCalcEngineSpreadSheetAdapter() {
		if(calcEngineSpreadSheetItemProvider == null) {
			calcEngineSpreadSheetItemProvider = new CalcEngineSpreadSheetItemProviderMy(this);
		}
		return calcEngineSpreadSheetItemProvider;
	}

    @Override
    public Adapter createCalcEngineJavaAdapter() {
        if (calcEngineJavaItemProvider == null) {
            calcEngineJavaItemProvider = new CalcEngineJavaItemProviderMy(this);
        }

        return calcEngineJavaItemProvider;
    }
	
	@Override
	public Adapter createFnDefSpreadSheetAdapter() {
		if (fnDefSpreadSheetItemProvider == null) {
			fnDefSpreadSheetItemProvider = new FnDefSpreadSheetItemProviderMy(this);
		}
		return fnDefSpreadSheetItemProvider;		
	}
	
	@Override
	public Adapter createFnDefScriptAdapter() {
		if (fnDefScriptItemProvider == null) {
			fnDefScriptItemProvider = new FnDefScriptItemProviderMy(this);
		}
		return fnDefScriptItemProvider;		
	} 
	
    @Override
    public Adapter createFnDefJavaAdapter() {
        if (fnDefJavaItemProvider == null) {
            fnDefJavaItemProvider = new FnDefJavaItemProviderMy(this);
        }

        return fnDefJavaItemProvider;
    }
	
	@Override
	public Adapter createFnInputAdapter() {
		if(fnInputItemProvider == null) {
			fnInputItemProvider = new FnInputItemProviderMy(this);
		}
		return fnInputItemProvider;		
	}
	
	@Override
	public Adapter createFnOutputAdapter() {
		if(fnOutputItemProvider == null) {
			fnOutputItemProvider = new FnOutputItemProviderMy(this);
		}
		return fnOutputItemProvider;		
	}
}
