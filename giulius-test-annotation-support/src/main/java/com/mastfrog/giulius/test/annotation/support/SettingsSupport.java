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
package com.mastfrog.giulius.test.annotation.support;

import com.mastfrog.giulius.test.annotation.support.AnnotationSupplier;
import com.mastfrog.giulius.test.annotation.support.GuiceTestException;
import com.mastfrog.giulius.tests.anno.Configurations;
import com.mastfrog.giulius.tests.anno.ModuleName;
import com.mastfrog.settings.Settings;
import com.mastfrog.settings.SettingsBuilder;
import com.mastfrog.util.streams.Streams;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tim Boudreau
 */
public final class SettingsSupport {

    private SettingsSupport() {
        throw new AssertionError();
    }

    public static LinkedList<String> settingsLocations(Class<?> tc, AnnotationSupplier supp) {
        Set<String> locations = new LinkedHashSet<String>();
        //just in case
        Class<?> outer = tc;
        while (outer.getEnclosingClass() != null) {
            outer = outer.getEnclosingClass();
        }
        String siblingPropertiesName = outer.getSimpleName() + ".properties";
        if (outer.getResource(siblingPropertiesName) != null) {
            String qname = outer.getName().replace('.', '/') + ".properties";
            locations.add(qname);
        }
        //Now look for any in annotations, class first, then method
        Configurations locs = tc.getAnnotation(Configurations.class);
        if (locs != null) {
            locations.addAll(Arrays.asList(locs.value()));
        }
        locs = supp.find(Configurations.class);
        if (locs != null) {
            locations.addAll(Arrays.asList(locs.value()));
        }
        return new LinkedList<>(locations);
    }

    public static String nameOf(Class<? extends com.google.inject.Module> moduleClass) {
        ModuleName mn = moduleClass.getAnnotation(ModuleName.class);
        return mn == null ? moduleClass.getSimpleName() : mn.value();
    }

    public static final Settings loadSettings(Class<?> tc, AnnotationSupplier supp, List<String> settingsLocations) throws Exception {
        SettingsBuilder sb = SettingsBuilder.createDefault();
        for (String loc : settingsLocations) {
            if (Streams.locate(loc) == null) {
                throw new GuiceTestException("No such settings file: " + loc, tc, 0);
            }
            sb.add(loc);
        }
        return sb.build();
    }

    public static final SettingsBuilder settingsBuilder(Class<?> tc, AnnotationSupplier supp, List<String> settingsLocations) throws Exception {
        SettingsBuilder sb = Settings.builder();
        for (String loc : settingsLocations) {
            if (Streams.locate(loc) == null) {
                throw new GuiceTestException("No such settings file: " + loc, tc, 0);
            }
            sb.add(loc);
        }
        return sb;
    }
}
