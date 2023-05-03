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
import com.mastfrog.giulius.test.annotation.support.SkipSupport;
import com.mastfrog.util.service.ServiceProvider;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(Extension.class)
@Order(Integer.MIN_VALUE)
public final class SkipChecker implements ExecutionCondition {

    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        AnnotationSupplier supp = AnnotationSupplier.forClassAndMethod(context.getTestClass(), context.getTestMethod());
        return SkipSupport.shouldSkip(supp) ? ConditionEvaluationResult.disabled("Annotations indicate not to run this test")
                : ConditionEvaluationResult.enabled("Annotation checks passed");
    }
}
