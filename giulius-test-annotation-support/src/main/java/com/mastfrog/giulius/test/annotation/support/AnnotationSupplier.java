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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.checkerframework.checker.units.qual.A;

/**
 * Abstraction for test class or method which can supply annotations.
 *
 * @author Tim Boudreau
 */
public interface AnnotationSupplier {

    <A extends Annotation> A find(Class<A> type);

    default <A extends Annotation> Set<A> all(Class<A> type) {
        A result = find(type);
        return result == null ? emptySet() : singleton(result);
    }

    default boolean has(Class<? extends Annotation> type) {
        return find(type) != null;
    }

    public static AnnotationSupplier forClass(Class<?> type) {
        assert type != null;
        return new AnnotationSupplier() {
            @Override
            public <A extends Annotation> A find(Class<A> annoType) {
                return type.getAnnotation(annoType);
            }
        };
    }

    default AnnotationSupplier or(AnnotationSupplier other) {
        assert other != null;
        return new AnnotationSupplier() {
            @Override
            public <A extends Annotation> A find(Class<A> type) {
                A result = AnnotationSupplier.this.find(type);
                if (result == null) {
                    result = other.find(type);
                }
                return result;
            }

            @Override
            public <A extends Annotation> Set<A> all(Class<A> type) {
                Set<A> result = new LinkedHashSet<>();
                result.addAll(AnnotationSupplier.this.all(type));
                result.addAll(other.all(type));
                return result;
            }
        };
    }

    public static AnnotationSupplier forMethod(Optional<Method> opt) {
        return opt.map(AnnotationSupplier::forMethod).orElse(NO_OP);
    }

    public static AnnotationSupplier forClass(Optional<Class<?>> opt) {
        return opt.map(AnnotationSupplier::forClass).orElse(NO_OP);
    }

    public static AnnotationSupplier forMethod(Method mth) {
        assert mth != null;
        return new AnnotationSupplier() {
            @Override
            public <A extends Annotation> A find(Class<A> type) {
                return mth.getAnnotation(type);
            }
        };
    }

    public static AnnotationSupplier forClassAndMethod(Class<?> type, Method mth) {
        return forClass(type).or(forMethod(mth));
    }

    public static AnnotationSupplier forClassAndMethod(Optional<Class<?>> type, Optional<Method> method) {
        AnnotationSupplier supp = type.map(t -> forClass(t)).orElse(NO_OP);
        AnnotationSupplier msupp = method.map(m -> forMethod(m)).orElse(NO_OP);
        return supp.or(msupp);
    }

    public static final AnnotationSupplier NO_OP = new AnnotationSupplier() {
        @Override
        public <A extends Annotation> A find(Class<A> type) {
            return null;
        }

        @Override
        public <A extends Annotation> Set<A> all(Class<A> type) {
            return emptySet();
        }
    };

}
