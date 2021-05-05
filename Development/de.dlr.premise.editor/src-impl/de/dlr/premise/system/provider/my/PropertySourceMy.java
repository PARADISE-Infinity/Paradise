package de.dlr.premise.system.provider.my;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.ui.provider.PropertySource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class PropertySourceMy extends PropertySource {

	//private AdapterFactory adapterFactory;

	public PropertySourceMy(AdapterFactory adapterFactory, Object object, IItemPropertySource itemPropertySource) {
		super(object, itemPropertySource);
		//this.adapterFactory = adapterFactory;
	}
	
    protected IPropertyDescriptor createPropertyDescriptor(IItemPropertyDescriptor itemPropertyDescriptor) {
        return new PropertyDescriptorMy(object, itemPropertyDescriptor);
    }
}