/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tilman Stehr
*
*/

package de.dlr.premise.ocl.hacks;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ocl.pivot.Element;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.NamedElement;
import org.eclipse.ocl.pivot.Type;
import org.eclipse.ocl.pivot.evaluation.EvaluationEnvironment;
import org.eclipse.ocl.pivot.evaluation.EvaluationVisitor;
import org.eclipse.ocl.pivot.evaluation.ModelManager;
import org.eclipse.ocl.pivot.ids.IdResolver;
import org.eclipse.ocl.pivot.internal.complete.CompleteEnvironmentInternal;
import org.eclipse.ocl.pivot.internal.complete.CompleteModelInternal;
import org.eclipse.ocl.pivot.internal.complete.StandardLibraryInternal;
import org.eclipse.ocl.pivot.internal.evaluation.ExecutorInternal;
import org.eclipse.ocl.pivot.internal.library.ImplementationManager;
import org.eclipse.ocl.pivot.internal.manager.PivotMetamodelManager;
import org.eclipse.ocl.pivot.internal.manager.TemplateParameterSubstitutionVisitor;
import org.eclipse.ocl.pivot.internal.resource.EnvironmentFactoryAdapter;
import org.eclipse.ocl.pivot.internal.resource.ICSI2ASMapping;
import org.eclipse.ocl.pivot.internal.utilities.EnvironmentFactoryInternal;
import org.eclipse.ocl.pivot.internal.utilities.External2AS;
import org.eclipse.ocl.pivot.internal.utilities.GlobalEnvironmentFactory;
import org.eclipse.ocl.pivot.internal.utilities.GlobalEnvironmentFactory.Listener;
import org.eclipse.ocl.pivot.internal.utilities.OCLInternal;
import org.eclipse.ocl.pivot.internal.utilities.Technology;
import org.eclipse.ocl.pivot.messages.StatusCodes.Severity;
import org.eclipse.ocl.pivot.resource.ProjectManager;
import org.eclipse.ocl.pivot.resource.ProjectManager.IConflictHandler;
import org.eclipse.ocl.pivot.resource.ProjectManager.IResourceLoadStrategy;
import org.eclipse.ocl.pivot.utilities.Option;
import org.eclipse.ocl.pivot.utilities.ParserContext;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.eclipse.ocl.pivot.values.ObjectValue;

public class ResourceSetAwareDelegatingGlobalEnvironmentFactory
        implements EnvironmentFactoryInternal.EnvironmentFactoryInternalExtension {
	
    private GlobalEnvironmentFactory delegate;

	public ResourceSetAwareDelegatingGlobalEnvironmentFactory(GlobalEnvironmentFactory delegate) {
		this.delegate = delegate;
	}
	
    public @NonNull ModelManager createModelManager(@Nullable Object object) {
        if (object instanceof ObjectValue) {
            object = ((ObjectValue) object).getObject();
        }
        if (object instanceof EObject) {
            return new ResourceSetAwarePivotModelManager(this, (EObject) object);
        }
        return ModelManager.NULL;
    }
    
    public @NonNull OCLInternal createOCL() {
        return new OCLInternal(this);
    }
    
    @Override
    public @NonNull EvaluationVisitor createEvaluationVisitor(@Nullable Object context, @NonNull ExpressionInOCL expression, @Nullable ModelManager modelManager) {
        if (modelManager == null) {
            // let the evaluation environment create one
            modelManager = createModelManager(context);
        }
        return delegate.createEvaluationVisitor(context, expression, modelManager);
    }

    // ONLY DELEGATE METHODS BELOW
    
    public int hashCode() {
        return delegate.hashCode();
    }

    public @NonNull Map<Option<?>, Object> clearOptions() {
        return delegate.clearOptions();
    }

    public Map<Option<?>, Object> getOptions() {
        return delegate.getOptions();
    }

    public <T> @Nullable T getValue(@NonNull Option<@Nullable T> option) {
        return delegate.getValue(option);
    }

    public boolean isEnabled(@NonNull Option<@Nullable Boolean> option) {
        return delegate.isEnabled(option);
    }

    public <T> void putOptions(@NonNull Map<? extends Option<@Nullable T>, ? extends @Nullable T> newOptions) {
        delegate.putOptions(newOptions);
    }

    public <T> @Nullable T removeOption(@NonNull Option<@Nullable T> option) {
        return delegate.removeOption(option);
    }

    public <T> @NonNull Map<Option<@Nullable T>, @Nullable T> removeOptions(@NonNull Collection<Option<@Nullable T>> unwantedOptions) {
        return delegate.removeOptions(unwantedOptions);
    }

    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    public <T> void setOption(@NonNull Option<T> option, @Nullable T value) {
        delegate.setOption(option, value);
    }

    public void addExternal2AS(@NonNull External2AS external2as) {
        delegate.addExternal2AS(external2as);
    }

    public void addListener(@NonNull Listener listener) {
        delegate.addListener(listener);
    }

    public @NonNull EnvironmentFactoryAdapter adapt(@NonNull Notifier notifier) {
        return delegate.adapt(notifier);
    }

    public void removeListener(@NonNull Listener listener) {
        delegate.removeListener(listener);
    }

    public void addExternalResources(@NonNull ResourceSet resourceSet) {
        delegate.addExternalResources(resourceSet);
    }

    public void attach(Object object) {
        delegate.attach(object);
    }

    public void configureLoadFirstStrategy() {
        delegate.configureLoadFirstStrategy();
    }

    public void configureLoadStrategy(@NonNull IResourceLoadStrategy packageLoadStrategy, @Nullable IConflictHandler conflictHandler) {
        delegate.configureLoadStrategy(packageLoadStrategy, conflictHandler);
    }

    public String toString() {
        return delegate.toString();
    }

    public @NonNull ResourceSetImpl createASResourceSet() {
        return delegate.createASResourceSet();
    }

    public @NonNull CompleteEnvironmentInternal createCompleteEnvironment() {
        return delegate.createCompleteEnvironment();
    }

    public @NonNull EvaluationEnvironment createEvaluationEnvironment(@NonNull NamedElement executableObject,
            @NonNull ModelManager modelManager) {
        return delegate.createEvaluationEnvironment(executableObject, modelManager);
    }

    @Deprecated
    public @NonNull EvaluationEnvironment createEvaluationEnvironment(@NonNull EvaluationEnvironment parent,
            @NonNull NamedElement executableObject) {
        return delegate.createEvaluationEnvironment(parent, executableObject);
    }

    public @NonNull EvaluationVisitor createEvaluationVisitor(@NonNull EvaluationEnvironment evaluationEnvironment) {
        return delegate.createEvaluationVisitor(evaluationEnvironment);
    }

    public @NonNull ExecutorInternal createExecutor(@NonNull ModelManager modelManager) {
        return delegate.createExecutor(modelManager);
    }

    public @NonNull IdResolver createIdResolver() {
        return delegate.createIdResolver();
    }

    public @NonNull ImplementationManager createImplementationManager() {
        return delegate.createImplementationManager();
    }

    public @NonNull PivotMetamodelManager createMetamodelManager() {
        return delegate.createMetamodelManager();
    }

    public @NonNull ParserContext createParserContext(@Nullable EObject context) throws ParserException {
        return delegate.createParserContext(context);
    }

    public @NonNull TemplateParameterSubstitutionVisitor createTemplateParameterSubstitutionVisitor(@Nullable Type selfType,
            @Nullable Type selfTypeValue) {
        return delegate.createTemplateParameterSubstitutionVisitor(selfType, selfTypeValue);
    }

    public void detach(Object object) {
        delegate.detach(object);
    }

    public void dispose() {
        delegate.dispose();
    }

    public <T> @Nullable T getAdapter(Class<T> adapterType) {
        return delegate.getAdapter(adapterType);
    }

    public @NonNull CompleteEnvironmentInternal getCompleteEnvironment() {
        return delegate.getCompleteEnvironment();
    }

    public @NonNull CompleteModelInternal getCompleteModel() {
        return delegate.getCompleteModel();
    }

    public @Nullable ICSI2ASMapping getCSI2ASMapping() {
        return delegate.getCSI2ASMapping();
    }

    public @Nullable String getDoSetupName(@NonNull URI uri) {
        return delegate.getDoSetupName(uri);
    }

    public @NonNull IdResolver getIdResolver() {
        return delegate.getIdResolver();
    }

    public @NonNull PivotMetamodelManager getMetamodelManager() {
        return delegate.getMetamodelManager();
    }

    public @NonNull ProjectManager getProjectManager() {
        return delegate.getProjectManager();
    }

    public @NonNull ResourceSet getResourceSet() {
        return delegate.getResourceSet();
    }

    public @Nullable Severity getSeverity(@Nullable Object validationKey) {
        return delegate.getSeverity(validationKey);
    }

    public @NonNull StandardLibraryInternal getStandardLibrary() {
        return delegate.getStandardLibrary();
    }

    public @NonNull Technology getTechnology() {
        return delegate.getTechnology();
    }

    public boolean isEvaluationTracingEnabled() {
        return delegate.isEvaluationTracingEnabled();
    }

    public EPackage loadEPackage(@NonNull EPackage ePackage) {
        return delegate.loadEPackage(ePackage);
    }

    public @Nullable Element loadResource(@NonNull Resource resource, @Nullable URI uri) throws ParserException {
        return delegate.loadResource(resource, uri);
    }

    public void resetSeverities() {
        delegate.resetSeverities();
    }

    public void setCSI2ASMapping(ICSI2ASMapping csi2asMapping) {
        delegate.setCSI2ASMapping(csi2asMapping);
    }

    public void setEvaluationTracingEnabled(boolean b) {
        delegate.setEvaluationTracingEnabled(b);
    }

    public void setProject(@Nullable IProject project) {
        delegate.setProject(project);
    }

    public void setSafeNavigationValidationSeverity(@NonNull Severity severity) {
        delegate.setSafeNavigationValidationSeverity(severity);
    }

    public @Nullable Severity setSeverity(@NonNull Object validationKey, Severity severity) {
        return delegate.setSeverity(validationKey, severity);
    }
   
    


}