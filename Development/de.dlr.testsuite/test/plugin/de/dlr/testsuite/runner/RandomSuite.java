/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.testsuite.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class RandomSuite extends Suite {

    public RandomSuite(Class<?> klass, RunnerBuilder builder)
            throws InitializationError {
        super(klass, builder);
    }

    public RandomSuite(RunnerBuilder builder, Class<?>[] classes)
            throws InitializationError {
        super(builder, classes);
    }

    protected RandomSuite(Class<?> klass, List<Runner> runners)
            throws InitializationError {
        super(klass, runners);
    }

    @Override
    protected List<Runner> getChildren() {
        final List<Runner> children = new ArrayList<>(super.getChildren());
        Collections.shuffle(children);
        for (Runner child : children) {
            System.out.println(child.getDescription().getClassName() + ".class, ");
        }
        System.out.println("\n\n");
        return children;
    }

}
