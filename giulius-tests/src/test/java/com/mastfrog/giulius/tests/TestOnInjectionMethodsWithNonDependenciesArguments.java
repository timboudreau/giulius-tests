package com.mastfrog.giulius.tests;

import com.mastfrog.giulius.tests.anno.OnInjection;
import java.util.ArrayList;
import java.util.List;

import com.mastfrog.giulius.tests.anno.TestWith;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
@TestWith(ModuleA.class)
public class TestOnInjectionMethodsWithNonDependenciesArguments extends GuiceTest {
    String val;
    List<StringBuilder> sbs;

    @OnInjection
    void stuff (String val, List<String> l) {
        this.val = val;
        sbs = new ArrayList<StringBuilder>();
        assertNotNull (l);
        assertFalse (l.isEmpty());
        for (String s : l) {
            sbs.add(new StringBuilder(s));
        }
    }

    @Test
    public void test() {
        assertNotNull (val);
        assertEquals ("A!", val);
        assertNotNull (sbs);
        assertEquals (3, sbs.size());
    }
}
