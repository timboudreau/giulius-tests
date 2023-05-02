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

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.mastfrog.giulius.junit5.tests.GuiceParameterResolverTest.M;
import com.mastfrog.giulius.junit5.tests.GuiceParameterResolverTest.MA;
import com.mastfrog.giulius.junit5.tests.GuiceParameterResolverTest.MB;
import com.mastfrog.giulius.tests.anno.SkipWhen;
import com.mastfrog.giulius.tests.anno.TestWith;
import com.mastfrog.settings.Settings;
import java.util.Arrays;
import static java.util.Collections.sort;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author Tim Boudreau
 */
@TestWith(value = M.class, iterate = {MA.class, MB.class},
        iterateSettings = {
            "com/mastfrog/giulius/junit5/tests/a.properties",
            "com/mastfrog/giulius/junit5/tests/b.properties",})
public class GuiceParameterResolverTest {

    static List<String> all = new CopyOnWriteArrayList<>();
    static List<String> testNames = new CopyOnWriteArrayList<>();

    @TestWith(iterate = {OBAModule.class, OBBModule.class}, iterateSettings = {
        "com/mastfrog/giulius/junit5/tests/c.properties",
        "com/mastfrog/giulius/junit5/tests/d.properties",
        "com/mastfrog/giulius/junit5/tests/e.properties",})
    @TestTemplate
    public void testTemplated(Thing thing) throws Exception {
        all.add(thing.toString());
    }

    @SkipWhen("dontRunMe")
    @TestTemplate
    public void testShouldNeverRun(Settings settings) {
        throw new AssertionError("dontRunMe should be true: " + settings.allKeys());
    }

    @AfterAll
    public static void whenDone() {
        sort(all);
        assertEquals(Arrays.asList(
                "manx BA 1 alaskan malamute",
                "manx BA 1 french poodle",
                "manx BA 1 german shepard",
                "manx BA 2 alaskan malamute",
                "manx BA 2 french poodle",
                "manx BA 2 german shepard",
                "manx BB 1 alaskan malamute",
                "manx BB 1 french poodle",
                "manx BB 1 german shepard",
                "manx BB 2 alaskan malamute",
                "manx BB 2 french poodle",
                "manx BB 2 german shepard",
                "siamese BA 1 alaskan malamute",
                "siamese BA 1 french poodle",
                "siamese BA 1 german shepard",
                "siamese BA 2 alaskan malamute",
                "siamese BA 2 french poodle",
                "siamese BA 2 german shepard",
                "siamese BB 1 alaskan malamute",
                "siamese BB 1 french poodle",
                "siamese BB 1 german shepard",
                "siamese BB 2 alaskan malamute",
                "siamese BB 2 french poodle",
                "siamese BB 2 german shepard"
        ), all, "Combinatorics are not as expected");

        sort(testNames);
        assertEquals(Arrays.asList(
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/a.properties OBAModule com/mastfrog/giulius/junit5/tests/c.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/a.properties OBAModule com/mastfrog/giulius/junit5/tests/d.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/a.properties OBAModule com/mastfrog/giulius/junit5/tests/e.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/a.properties OBBModule com/mastfrog/giulius/junit5/tests/c.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/a.properties OBBModule com/mastfrog/giulius/junit5/tests/d.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/a.properties OBBModule com/mastfrog/giulius/junit5/tests/e.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/b.properties OBAModule com/mastfrog/giulius/junit5/tests/c.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/b.properties OBAModule com/mastfrog/giulius/junit5/tests/d.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/b.properties OBAModule com/mastfrog/giulius/junit5/tests/e.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/b.properties OBBModule com/mastfrog/giulius/junit5/tests/c.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/b.properties OBBModule com/mastfrog/giulius/junit5/tests/d.properties]",
                "testTemplated(Thing)[MA com/mastfrog/giulius/junit5/tests/b.properties OBBModule com/mastfrog/giulius/junit5/tests/e.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/a.properties OBAModule com/mastfrog/giulius/junit5/tests/c.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/a.properties OBAModule com/mastfrog/giulius/junit5/tests/d.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/a.properties OBAModule com/mastfrog/giulius/junit5/tests/e.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/a.properties OBBModule com/mastfrog/giulius/junit5/tests/c.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/a.properties OBBModule com/mastfrog/giulius/junit5/tests/d.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/a.properties OBBModule com/mastfrog/giulius/junit5/tests/e.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/b.properties OBAModule com/mastfrog/giulius/junit5/tests/c.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/b.properties OBAModule com/mastfrog/giulius/junit5/tests/d.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/b.properties OBAModule com/mastfrog/giulius/junit5/tests/e.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/b.properties OBBModule com/mastfrog/giulius/junit5/tests/c.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/b.properties OBBModule com/mastfrog/giulius/junit5/tests/d.properties]",
                "testTemplated(Thing)[MB com/mastfrog/giulius/junit5/tests/b.properties OBBModule com/mastfrog/giulius/junit5/tests/e.properties]"
        ), testNames, "Test names are not as expected");
    }

    @BeforeEach
    public void beforeEach(TestInfo info) {
        testNames.add(info.getDisplayName());
    }

    static class M implements Module {

        private final Settings settings;

        M(Settings settings) {
            this.settings = settings;
            assert settings != null : "Settings is null";
        }

        @Override
        public void configure(Binder binder) {
            String s = settings.getString("cat", "Tabby");
            binder.bind(String.class).annotatedWith(Names.named("theCat")).toInstance(s);
            binder.bind(Thing.class).toProvider(ThingProvider.class);
        }
    }

    static class ThingProvider implements Provider<Thing> {

        private final Thing thing;

        @Inject
        ThingProvider(@Named("theCat") String cat, Bound bound, OtherBound ob, Dog dog) {
            this.thing = new Thing(cat, bound, ob, dog);
        }

        public Thing get() {
            return this.thing;
        }
    }

    static class MA implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bind(Bound.class).toInstance(new BA());
        }
    }

    static class MB implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bind(Bound.class).toInstance(new BB());
        }
    }

    static final class Thing implements Comparable<Thing> {

        final String str;
        final Bound bound;
        final OtherBound otherBound;
        private final Dog dog;

        Thing(String str, Bound bound, OtherBound otherBound, Dog dog) {
            this.str = str;
            this.dog = dog;
            assert str != null : "str is null";
            this.bound = bound;
            this.otherBound = otherBound;
        }

        @Override
        public String toString() {
            return str + " " + bound.getClass().getSimpleName() + " "
                    + otherBound.value() + " " + dog;
        }

        @Override
        public int compareTo(Thing o) {
            return toString().compareTo(o.toString());
        }
    }

    public static class Dog {

        private final String dog;

        @Inject
        Dog(@Named("dog") String dog) {
            this.dog = dog;
        }

        public String toString() {
            return dog;
        }
    }

    interface Bound {
    }

    static class BA implements Bound {
    };

    static class BB implements Bound {
    };

    static class OBAModule implements Module {

        public void configure(Binder binder) {
            binder.bind(OtherBound.class).to(OBA.class);
        }
    }

    static class OBBModule implements Module {

        public void configure(Binder binder) {
            binder.bind(OtherBound.class).to(OBB.class);
        }
    }

    interface OtherBound {

        int value();
    }

    static class OBA implements OtherBound {

        @Override
        public int value() {
            return 1;
        }
    }

    static class OBB implements OtherBound {

        @Override
        public int value() {
            return 2;
        }
    }

}
