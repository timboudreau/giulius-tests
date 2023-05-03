/*
 * The MIT License
 *
 * Copyright 2023 Mastfrog Technologies.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mastfrog.giulius.junit5.tests;

import com.google.inject.Module;
import com.mastfrog.function.throwing.ThrowingSupplier;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.giulius.DependenciesBuilder;
import com.mastfrog.giulius.test.annotation.support.AnnotationSupplier;
import com.mastfrog.giulius.tests.anno.TestWith;
import com.mastfrog.settings.Settings;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Tim Boudreau
 */
public final class GuiceSupport {

    private final ThrowingSupplier<Settings> settings;
    private final AnnotationSupplier annotations;

    public GuiceSupport(ThrowingSupplier<Settings> settings, AnnotationSupplier annotations) {
        this.settings = settings;
        this.annotations = annotations;
    }

    /**
     * Collect classes from a particular annotation.
     *
     * @param annotation
     * @param classes
     */
    @SuppressWarnings("unchecked")
    private void collectModuleClasses(TestWith annotation, List<? super Class<? extends Module>> classes) {
        if (annotation != null) {
            for (Class<?> type : annotation.value()) {
                if (!Module.class.isAssignableFrom(type)) {
                    throw new AssertionError(type + " is not a guice module");
                }
                classes.add((Class<? extends Module>) type);
            }
        }
    }

    /**
     * Collect a list of all module classes which need to be instantiated to run
     * this test.
     * <p/>
     * Subclasses which wish to support the
     * <code>{@literal @}{@link TestWith}</code> annotation should call
     * <code>super.findModuleClasses()</code> to find any Module classes
     * declared in class or method annotations.
     *
     * @return
     */
    List<Class<? extends Module>> findModuleClasses(TestWith classAnnotation, int iterIndex, TestWith methodAnnotation, int methodIndex) {
        List<Class<? extends Module>> result = new LinkedList<Class<? extends Module>>();
        for (TestWith tw : annotations.all(TestWith.class)) {
            collectModuleClasses(tw, result);
        }
        return result;
    }

    List<Module> createModules(Settings settings) throws Exception {
        return createModules(settings, null, -1, null, -1);
    }

    @SuppressWarnings("unchecked")
    List<Module> createModules(Settings settings, TestWith classAnnotation, int iterIndex, TestWith methodAnnotation, int methodIndex) throws Exception {
        List<Module> result = new LinkedList<Module>();
        for (Class<? extends Module> type : findModuleClasses(classAnnotation, iterIndex, methodAnnotation, methodIndex)) {
            result.add(instantiateModule(type, settings));
        }
        if (iterIndex >= 0) {
            result.add(instantiateModule((Class<? extends Module>) classAnnotation.iterate()[iterIndex], settings));
        }
        if (methodIndex >= 0) {
            result.add(instantiateModule((Class<? extends Module>) methodAnnotation.iterate()[methodIndex], settings));
        }
        return result;
    }

    private Module instantiateModule(Class<? extends Module> moduleClass, Settings settings) throws Exception {
        Module module;
        Constructor c = findUsableModuleConstructor(moduleClass);
        c.setAccessible(true);
        if (c.getParameterTypes().length == 1) {
            module = (Module) c.newInstance(settings);
        } else if (c.getParameterTypes().length == 0) {
            module = (Module) c.newInstance();
        } else {
            throw new AssertionError("Should have rejected module class "
                    + moduleClass.getName()
                    + "with constructor arguments which are not empty or a "
                    + "single Settings object");
        }
        return module;
    }

    public Dependencies createDependencies() throws Exception {
        return createDependencies(null, -1, null, -1);
    }

    public Dependencies createDependencies(TestWith classAnnotation, int iterIndex, TestWith methodAnnotation, int methodIndex) throws Exception {
        DependenciesBuilder builder = Dependencies.builder().useMutableSettings();
        builder.addDefaultSettings();
        Settings settings = this.settings.get();

        for (Module m : createModules(settings, classAnnotation, iterIndex, methodAnnotation, methodIndex)) {
            builder.add(m);
        }
        builder.add(settings);

        return builder.build();
    }

    static Constructor<?> findUsableModuleConstructor(Class<?> type) {
        Constructor<?> con = null;
        try {
            con = type.getDeclaredConstructor(Settings.class);
        } catch (NoSuchMethodException ex) {
            try {
                con = type.getDeclaredConstructor();
            } catch (NoSuchMethodException ex1) {
                //caller will handle null return value
            }
        }
        if (con == null) {
            throw new AssertionError("No usable constructor on guice module " + type
                    + " - must be either no-argument or take a Settings. "
                    + "It need not be public.");
        }
        return con;
    }

}