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

import com.mastfrog.giulius.test.annotation.support.AnnotationSupplier;
import com.mastfrog.giulius.tests.anno.TestWith;
import com.mastfrog.util.service.ServiceProvider;
import java.util.ArrayList;
import static java.util.Collections.shuffle;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(Extension.class)
public final class IterativeContextResolver implements TestTemplateInvocationContextProvider {

    private static Set<TestWith> testWiths(ExtensionContext ec) {
        return AnnotationSupplier.forClassAndMethod(ec.getTestClass(), ec.getTestMethod())
                .all(TestWith.class);
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext ec) {
        return isIterative(ec);
    }

    static boolean isIterative(ExtensionContext ctx) {
        for (TestWith tw : testWiths(ctx)) {
            if (tw.iterate().length > 0 || tw.iterateSettings().length > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext ec) {
        TestWith twClass = AnnotationSupplier.forClass(ec.getTestClass()).find(TestWith.class);
        TestWith twMethod = AnnotationSupplier.forMethod(ec.getTestMethod()).find(TestWith.class);

        Set<TestTemplateInvocationContext> result = new LinkedHashSet<>();
        rng(twClass, false).each(testClassModuleIndex -> {
            rng(twClass, true).each(testClassSettingsIndex -> {
                rng(twMethod, false).each(testMethodModuleIndex -> {
                    rng(twMethod, true).each(testMethodSettingsIndex -> {
                        result.add(new InvContext(ec.getDisplayName(), testClassModuleIndex, testClassSettingsIndex,
                                testMethodModuleIndex, testMethodSettingsIndex, twClass, twMethod));
                    });
                });
            });
        });
        List<TestTemplateInvocationContext> shuffled = new ArrayList<>(result);
        shuffle(shuffled);
        return shuffled.stream();
    }

    static class InvContext implements TestTemplateInvocationContext {

        private final int testClassSettingsIndex;
        private final int testClassModuleIndex;
        private final int testMethodModuleIndex;
        private final int testMethodSettingsIndex;
        private final TestWith testMethodTestWith;
        private final TestWith testClassTestWith;
        private final String mth;

        InvContext(String mth, int testClassModuleIndex, int testClassSettingsIndex, int testMethodModuleIndex,
                int testMethodSettingsIndex, TestWith testClassTestWith, TestWith testMethodTestWith) {
            this.mth = mth;
            this.testClassSettingsIndex = testClassSettingsIndex;
            this.testClassModuleIndex = testClassModuleIndex;
            this.testMethodModuleIndex = testMethodModuleIndex;
            this.testMethodSettingsIndex = testMethodSettingsIndex;
            this.testClassTestWith = testClassTestWith;
            this.testMethodTestWith = testMethodTestWith;
            if ((testClassSettingsIndex >= 0 || testClassModuleIndex >= 0) && testClassTestWith == null) {
                throw new AssertionError();
            }
            if ((testMethodSettingsIndex >= 0 || testMethodModuleIndex >= 0) && testMethodTestWith == null) {
                throw new AssertionError();
            }
        }

        public String toString() {
            return displayName(0);
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return displayName(invocationIndex);
        }

        private String displayName(int invocationIndex) {
            StringBuilder sb = new StringBuilder(128).append(mth);
            sb.append('[');
            Runnable delimit = () -> {
                if (sb.length() > 1) {
                    sb.append(" ");
                }
            };
            if (testClassModuleIndex >= 0) {
                sb.append(testClassTestWith.iterate()[testClassModuleIndex].getSimpleName());
            }
            if (testClassSettingsIndex >= 0) {
                delimit.run();
                sb.append(testClassTestWith.iterateSettings()[testClassSettingsIndex]);
            }
            if (testMethodModuleIndex >= 0) {
                delimit.run();
                sb.append(testMethodTestWith.iterate()[testMethodModuleIndex].getSimpleName());
            }
            if (testMethodSettingsIndex >= 0) {
                delimit.run();
                sb.append(testMethodTestWith.iterateSettings()[testMethodSettingsIndex]);
            }
            return sb.append(']').toString();
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            List<Extension> extensions = new ArrayList<>(3);
            GuiceParameterResolver res = new GuiceParameterResolver(testClassModuleIndex, testClassSettingsIndex,
                    testMethodModuleIndex, testMethodSettingsIndex);
            extensions.add(res);
            return extensions;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 29 * hash + this.testClassSettingsIndex;
            hash = 29 * hash + this.testClassModuleIndex;
            hash = 29 * hash + this.testMethodModuleIndex;
            hash = 29 * hash + this.testMethodSettingsIndex;
            hash = 29 * hash + Objects.hashCode(this.testMethodTestWith);
            hash = 29 * hash + Objects.hashCode(this.testClassTestWith);
            hash = 29 * hash + Objects.hashCode(this.mth);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InvContext other = (InvContext) obj;
            if (this.testClassSettingsIndex != other.testClassSettingsIndex) {
                return false;
            }
            if (this.testClassModuleIndex != other.testClassModuleIndex) {
                return false;
            }
            if (this.testMethodModuleIndex != other.testMethodModuleIndex) {
                return false;
            }
            if (this.testMethodSettingsIndex != other.testMethodSettingsIndex) {
                return false;
            }
            if (!Objects.equals(this.mth, other.mth)) {
                return false;
            }
            if (!Objects.equals(this.testMethodTestWith, other.testMethodTestWith)) {
                return false;
            }
            return Objects.equals(this.testClassTestWith, other.testClassTestWith);
        }
    }

    interface Rng {

        int first();

        int last();

        default void each(IntConsumer supp) {
            for (int i = first(); i <= last(); i++) {
                supp.accept(i);
            }
        }
    }

    static Rng rng(TestWith tw, boolean settings) {
        if (tw == null) {
            return new RngImpl();
        } else {
            return new RngImpl(tw, settings);
        }
    }

    static class RngImpl implements Rng {

        private final int first;
        private final int last;

        RngImpl(TestWith tw, boolean settings) {
            int len = settings ? tw.iterateSettings().length : tw.iterate().length;
            if (len == 0) {
                first = last = -1;
            } else {
                first = 0;
                last = len - 1;
            }
        }

        RngImpl() {
            this.first = this.last = -1;
        }

        @Override
        public int first() {
            return first;
        }

        @Override
        public int last() {
            return last;
        }
    }

}
