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

import com.google.inject.Injector;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.giulius.tests.anno.OnInjection;
import org.junit.runner.RunWith;

/**
 * Convenience test base class for use with <code>{@link GuiceRunner}</code>. 
 * See <code>{@link GuiceRunner}</code> for usage instructions.
 *
 * @author Tim Boudreau
 */
@RunWith(GuiceRunner.class)
public abstract class GuiceTest {
    private Dependencies dependencies;
    protected GuiceTest() {}

    @OnInjection
    final void setDependencies (Dependencies dependencies) {
        this.dependencies = dependencies;
    }

    protected final Dependencies getDependencies() {
        return dependencies;
    }

    protected final Injector getInjector() {
        return dependencies.getInjector();
    }

}
