/* 
 * The MIT License
 *
 * Copyright 2013 Tim Boudreau.
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
package com.mastfrog.giulius.tests;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation which can be applied to test methods, classes or 
 * TestWith iterate= arguments to skip running a particular test
 * (which may be long or expensive to run) when running in "IDE mode".
 * <p/>
 * "IDE mode" is determined by the value of the system property 
 * <code>in.ide</code>.  To set this up correctly, configure your IDE
 * to always pass <code>-DargLine="-Din.ide=true"</code> when running Maven.
 * <p/>
 * This annotation is only honored for tests running using GuiceRunner or
 * subclasses of GuiceTest.
 *
 * @author Tim Boudreau
 * @deprecated Use &#064;SkipWhen("in.ide") for the same effect in a more
 * general way
 */
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface SkipWhenRunInIDE {

}
