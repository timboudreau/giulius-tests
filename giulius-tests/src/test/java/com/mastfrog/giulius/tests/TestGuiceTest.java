package com.mastfrog.giulius.tests;

import com.mastfrog.giulius.tests.anno.TestWith;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
@TestWith(ModuleB.class)
public class TestGuiceTest extends GuiceTest {
    @Test
    public void test (String val) {
        assertNotNull (val);
        assertEquals ("B!", val);
        assertNotNull (getDependencies());
    }
}
