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

import com.mastfrog.giulius.tests.anno.IfBinaryAvailable;
import com.mastfrog.giulius.tests.anno.SkipWhen;
import com.mastfrog.giulius.tests.anno.SkipWhenNetworkUnavailable;

/**
 *
 * @author Tim Boudreau
 */
public final class SkipSupport {

    private static final NetworkCheck DEFAULT_NETWORK_CHECK;

    static {
        String host = System.getProperty("network.check.host", "www.google.com");
        boolean ssl = Boolean.getBoolean("network.check.ssl.port");
        DEFAULT_NETWORK_CHECK = new NetworkCheck(host, ssl);
    }

    private SkipSupport() {
        throw new AssertionError();
    }

    public static boolean isSkipped(SkipWhenNetworkUnavailable net) {
        if (net == null) {
            return false;
        }
        return !DEFAULT_NETWORK_CHECK.isNetworkAvailable();
    }

    public static boolean isSkipped(SkipWhen condition) {
        boolean result;
        if (condition == null) {
            result = false;
        } else {
            String checkFor = condition.value();
            boolean invert = condition.invert();
            String val = System.getProperty(checkFor, System.getenv(checkFor));
            if (val == null) {
                val = System.getenv(checkFor.toUpperCase().replace('.', '_'));
            }
            if (val != null) {
                switch (val.toLowerCase().trim()) {
                    case "true":
                    case "1":
                    case "yes":
                        result = !invert;
                        break;
                    default:
                        result = invert;
                }
            } else {
                result = invert;
            }
        }
        return result;
    }

    public static boolean isSkipped(IfBinaryAvailable bin) {
        if (bin == null) {
            return false;
        }
        return !BinaryChecks.isExecutable(bin.value(), bin.alternateNames());
    }

    public static boolean shouldSkip(AnnotationSupplier supp) {
        return isSkipped(supp.find(IfBinaryAvailable.class))
                || isSkipped(supp.find(SkipWhen.class))
                || isSkipped(supp.find(SkipWhenNetworkUnavailable.class));
    }

}
