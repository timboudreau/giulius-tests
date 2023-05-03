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

import com.mastfrog.giulius.Dependencies;
import static com.mastfrog.giulius.junit5.tests.IterativeContextResolver.isIterative;
import com.mastfrog.giulius.test.annotation.support.AnnotationSupplier;
import com.mastfrog.giulius.test.annotation.support.GuiceTestException;
import com.mastfrog.giulius.test.annotation.support.SettingsSupport;
import com.mastfrog.giulius.tests.anno.TestWith;
import com.mastfrog.settings.Settings;
import com.mastfrog.settings.SettingsBuilder;
import com.mastfrog.util.preconditions.Exceptions;
import com.mastfrog.util.service.ServiceProvider;
import com.mastfrog.util.streams.Streams;
import com.mastfrog.util.strings.Strings;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Collections.unmodifiableList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(Extension.class)
@Order(Integer.MIN_VALUE)
public final class GuiceParameterResolver implements ParameterResolver {

    private static final List<Object> BASE_OBJS = Arrays.asList("giulius", "tests");
    public static final Namespace TEST_NAMESPACE = Namespace.create(BASE_OBJS.toArray());
    private final int typeAnnotationIndex;
    private final int methodAnnotationIndex;
    private final int typeAnnotationSettingsIndex;
    private final int methodAnnotationSettingsIndex;

    public GuiceParameterResolver() {
        this(-1, -1, -1, -1);
    }

    GuiceParameterResolver(int typeAnnotationIndex, int typeAnnotationSettingsIndex,
            int methodAnnotationIndex, int methodAnnotationSettingsIndex) {
        this.typeAnnotationIndex = typeAnnotationIndex;
        this.methodAnnotationIndex = methodAnnotationIndex;
        this.typeAnnotationSettingsIndex = typeAnnotationSettingsIndex;
        this.methodAnnotationSettingsIndex = methodAnnotationSettingsIndex;
    }

    @Override
    public String toString() {
        return "GuiceParameterResolver{" + "typeAnnotationIndex="
                + typeAnnotationIndex + ", methodAnnotationIndex=" + methodAnnotationIndex + ", typeAnnotationSettingsIndex=" + typeAnnotationSettingsIndex + ", methodAnnotationSettingsIndex=" + methodAnnotationSettingsIndex + '}';
    }

    private boolean isFrameworkType(ParameterContext pc, ExtensionContext ec) {
        return TestInfo.class == pc.getParameter().getType();
    }

    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) throws ParameterResolutionException {
        if (isFrameworkType(pc, ec)) {
            return false;
        }
        boolean result = isGuiceTest(ec) && (isIterativeInstance() == isIterative(ec));
        return result;
    }

    boolean isIterativeInstance() {
        return typeAnnotationIndex >= 0
                || methodAnnotationIndex >= 0
                || typeAnnotationSettingsIndex >= 0
                || methodAnnotationSettingsIndex >= 0;
    }

    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) throws ParameterResolutionException {
        Dependencies deps = dependenciesFor(ec);
        try {
            return deps.getInstance(pc.getParameter().getType());
        } catch (Exception | Error e) {
            throw new ParameterResolutionException("Could not locate " + pc + " in " + ec, e);
        }
    }

    static boolean isGuiceTest(ExtensionContext ctx) {
        return AnnotationSupplier.forClassAndMethod(ctx.getTestClass(),
                ctx.getTestMethod()).has(TestWith.class);
    }

    private Namespace namespace(ExtensionContext ctx) {
        Namespace result = TEST_NAMESPACE.append(ctx.getUniqueId());
        if (isIterativeInstance()) {
            result = result.append(typeAnnotationIndex, typeAnnotationSettingsIndex,
                    methodAnnotationIndex, methodAnnotationSettingsIndex);
        }
        return result;
    }

    private List<Object> namespaceObjects(ExtensionContext ctx) {
        List<Object> result = new ArrayList<>(BASE_OBJS);
        result.add(ctx.getUniqueId());
        if (typeAnnotationIndex >= 0 || methodAnnotationIndex >= 0) {
            result.add(typeAnnotationIndex);
            result.add(methodAnnotationIndex);
        }
        return unmodifiableList(result);
    }

    private String namespaceString(ExtensionContext ctx) {
        return Strings.join('.', namespaceObjects(ctx), Objects::toString);
    }

    private String depsKey(ExtensionContext ctx) {
        return namespaceString(ctx) + ".dependencies";
    }

    SettingsBuilder settings(ExtensionContext ctx) throws Exception {
        AnnotationSupplier supp = AnnotationSupplier.forClassAndMethod(ctx.getTestClass(), ctx.getTestMethod());
        SettingsBuilder sb = Settings.builder();
        sb.add(SettingsSupport.settingsBuilder(ctx.getRequiredTestClass(),
                supp, SettingsSupport.settingsLocations(ctx.getRequiredTestClass(), supp)));

        if (typeAnnotationSettingsIndex >= 0) {
            String path = ctx.getRequiredTestClass().getAnnotation(TestWith.class).iterateSettings()[typeAnnotationSettingsIndex];
            for (InputStream in : Streams.locate(path)) {
                sb.add(in);
            }
        }

        if (methodAnnotationSettingsIndex >= 0) {
            String path = ctx.getRequiredTestMethod().getAnnotation(TestWith.class).iterateSettings()[methodAnnotationSettingsIndex];
            for (InputStream in : Streams.locate(path)) {
                sb.add(in);
            }
        }
        return sb;
    }

    private Dependencies dependenciesFor(ExtensionContext ctx) {
        Namespace ns = namespace(ctx);
        ExtensionContext.Store store = ctx.getStore(namespace(ctx));
        return store.getOrComputeIfAbsent(ns + ".deps", _x -> createDependenciesFor(ctx), Dependencies.class);
    }

    private Dependencies createDependenciesFor(ExtensionContext ctx) {
        try {
            GuiceSupport supp = new GuiceSupport(() -> settings(ctx).build(), AnnotationSupplier.forClassAndMethod(ctx.getTestClass(), ctx.getTestMethod()));
            TestWith classAnno = AnnotationSupplier.forClass(ctx.getTestClass()).find(TestWith.class);
            TestWith methodAnno = AnnotationSupplier.forMethod(ctx.getTestMethod()).find(TestWith.class);
            return supp.createDependencies(classAnno, typeAnnotationIndex, methodAnno, methodAnnotationIndex);
        } catch (Exception e) {
            return Exceptions.chuck(e);
        }
    }
}
