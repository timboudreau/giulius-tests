giulius-tests
-------------

A Guicified JUnit test runner which allows tests to be written very simply:
  * Specify ``@RunWith(GuiceRunner.class)`` on the test class (or subclass ``GuiceTest``)
  * Specify Guice modules using ``@TestWith ( ModuleA.class, ModuleB.class )``
  * Load default ``@Named`` values from ``$PACKAGE/$TEST_NAME.properties`` or can be specified in annotations
  * Write normal JUnit test methods, but with arguments:

         @Test
         public void guiceTest ( InjectedThing thing ) { ... }

