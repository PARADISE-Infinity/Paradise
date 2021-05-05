package de.dlr.premise.system.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.ui.views.properties.IPropertySource;

public class AdapterFactoryContentProviderMy extends AdapterFactoryContentProvider {

	public AdapterFactoryContentProviderMy(AdapterFactory adapterFactory) {
		super(adapterFactory);
		// TODO Auto-generated constructor stub
	}

	protected IPropertySource createPropertySource(Object object, IItemPropertySource itemPropertySource) {
		return new PropertySourceMy(adapterFactory, object, itemPropertySource);
	}
}
