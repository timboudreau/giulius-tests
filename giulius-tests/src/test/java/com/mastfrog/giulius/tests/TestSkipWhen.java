/*
 * The MIT License
 *
 * Copyright 2018 Tim Boudreau.
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

import com.google.inject.AbstractModule;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mastfrog.giulius.tests.anno.SkipWhen;
import com.mastfrog.giulius.tests.anno.TestWith;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Tim Boudreau
 */
@RunWith(GuiceRunner.class)
public class TestSkipWhen {

    static boolean ran1;
    static boolean ran2;

    @BeforeClass
    public static void setUpSysprops() {
        System.setProperty("woogle", "true");
        System.setProperty("gork", "TRUE");
        System.setProperty("boogle", "yes");
        System.setProperty("snork", "1");
        System.setProperty("garg", "0");
        System.setProperty("glorp", "false");
    }

    @AfterClass
    public static void checkTestsThatShouldHaveRunRan() {
        assertTrue("Test did not run", ran1);
        assertTrue("Test did not run", ran2);
    }

    @Test
    @SkipWhen("woogle")
    public void testTrueSysprop() {
        fail("test should not have run");
    }

    @Test
    @SkipWhen(value = "woogle", invert = true)
    public void testTrueSyspropInverted() {
        ran2 = true;
    }

    @Test
    @SkipWhen("gork")
    public void testUpperCaseTrueSysprop() {
        fail("test should not have run");
    }

    @Test
    @SkipWhen("boogle")
    public void testYesSysprop() {
        fail("test should not have run");
    }

    @Test
    @SkipWhen("snork")
    public void test1Sysprop() {
        fail("test should not have run");
    }

    @Test
    @SkipWhen(value = "garg", invert = true)
    public void testInvertedSysprop() {
        fail("test should not have run");
    }

    @Test
    @SkipWhen(value = "glorp", invert = true)
    public void testInvertedSysprop2() {
        fail("test should not have run");
    }

    @Test
    @SkipWhen(value = "garg")
    public void testWhichWillRun() {
        ran1 = true;
    }

    @TestWith(value = WillRunModule.class, iterate = {WillAlsoRunModule.class, WillNotRunModule.class})
    public void testModuleAnnotations(@Named("value") String value, @Named("foo") int foo) {
        assertEquals("hey", value);
        assertEquals(23, foo);
    }

    @SkipWhen("woogle")
    public static final class WillNotRunModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(Integer.class).annotatedWith(Names.named("foo")).toInstance(-1);
        }
    }

    @SkipWhen(value = "woogle", invert = true)
    public static final class WillAlsoRunModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(Integer.class).annotatedWith(Names.named("foo")).toInstance(23);
        }
    }

    @SkipWhen("garg")
    public static final class WillRunModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(String.class).annotatedWith(Names.named("value")).toInstance("hey");
        }

    }
}
