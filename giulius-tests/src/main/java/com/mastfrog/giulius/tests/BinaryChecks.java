/*
 * The MIT License
 *
 * Copyright 2019 Mastfrog.
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

import com.mastfrog.util.collections.ArrayUtils;
import com.mastfrog.util.strings.Strings;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Tim Boudreau
 */
final class BinaryChecks {

    private static final String[] DEFAULT_PATHS = {
        "/bin",
        "/usr/bin",
        "/usr/local/bin",
        "/opt/local/bin",
        "/opt/homebrew/bin",};

    private BinaryChecks() {
        throw new AssertionError();
    }

    private static String[] split(String prop) {
        return Strings.split(File.pathSeparatorChar, prop);
    }

    private static String[] splitSystemProperty(String prop, String[] defaultValue) {
        String s = System.getProperty(prop);
        return s == null ? defaultValue : split(s);
    }

    private static String[] stringPaths() {
        String[] all = splitSystemProperty(IfBinaryAvailable.REPLACE_PATHS_SYSTEM_PROP, DEFAULT_PATHS);
        String[] more = splitSystemProperty(IfBinaryAvailable.ADDITIONAL_PATHS_SYSTEM_PROP, null);
        if (more != null) {
            all = ArrayUtils.concatenate(more, all);
        }
        return all;
    }

    private static Iterable<? extends Path> paths(String binaryName, String[] alternates) {
        List<Path> result = new ArrayList<>();
        for (String pth : stringPaths()) {
            Path p = Paths.get(pth);
            if (Files.exists(p) && Files.isDirectory(p)) {
                if (Files.exists(p.resolve(binaryName))) {
                    result.add(p.resolve(binaryName));
                }
                for (String alt : alternates) {
                    if (Files.exists(p.resolve(alt))) {
                        result.add(p.resolve(alt));
                    }
                }
            }
        }
        return result;
    }

    static boolean isExecutable(String binaryName, String[] alternates) {
        for (Path pth : paths(binaryName, alternates)) {
            if (Files.exists(pth) && !Files.isDirectory(pth) && Files.isExecutable(pth)) {
                return true;
            } else {
                System.out.println("Nope: " + pth);
            }
        }
        return false;
    }

    static boolean test(IfBinaryAvailable test) {
        boolean result = isExecutable(test.value(), test.alternateNames());
        if (!result) {
            System.err.println("Binary " + test.value() + " not available.  Test will be skipped.");
            System.err.println("Checked " + paths(test.value(), test.alternateNames()));
        }
        return result;
    }
}
