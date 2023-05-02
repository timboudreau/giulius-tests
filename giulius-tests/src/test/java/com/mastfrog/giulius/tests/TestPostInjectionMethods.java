package com.mastfrog.giulius.tests;

import com.google.inject.Inject;
import com.mastfrog.giulius.Dependencies;
import com.mastfrog.giulius.tests.anno.OnInjection;
import com.mastfrog.giulius.tests.anno.TestWith;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
@TestWith({ModuleA.class, ModuleC.class})
@RunWith(GuiceRunner.class)
public class TestPostInjectionMethods {
    @Inject
    String val;

    StringBuilder sb;
    private Dependencies deps;

    @OnInjection
    public void setThingsUp() {
        sb = new StringBuilder(val);
    }

    @OnInjection
    public void afterInjectionWithDependencies (Dependencies deps) {
        this.deps = deps;
    }

    @Test
    public void testAfterInjectionMethodWasInvoked() {
        assertNotNull (val);
        assertNotNull (sb);
        assertEquals ("A!", val);
        assertEquals ("A!", sb.toString());
        assertNotNull (deps);
    }
}
