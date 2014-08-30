giulius-tests
-------------

A Guicified JUnit test runner which allows tests to be written very simply:

  * Specify ``@RunWith(GuiceRunner.class)`` on the test class (or subclass ``GuiceTest``)
  * Specify Guice modules using ``@TestWith ( ModuleA.class, ModuleB.class )``
  * Load default ``@Named`` values from ``$PACKAGE/$TEST_NAME.properties`` or can be specified in annotations
  * Write normal JUnit test methods, but with arguments:

	  @RunWith(GuiceRunner.class)
	  @TestWith({ModuleA.class, ModuleB.class})
	  public void MyTest {
		 @Test
		 public void guiceTest ( InjectedThing thing ) { ... }
	  }

This makes it possible to write unit tests that use dependency injection, and eliminate complicated set-up code from tests.

Module constructors may be either no-argument, or may take a [Settings](http://timboudreau.com/builds/job/mastfrog-parent/lastSuccessfulBuild/artifact/giulius-modules/giulius-parent/giulius-settings/target/apidocs/com/mastfrog/settings/Settings.html).

[Javadoc here](http://timboudreau.com/builds/job/mastfrog-parent/lastSuccessfulBuild/artifact/giulius-tests-modules/giulius-tests-parent/giulius-tests/target/apidocs/index.html)

## What It Does

Uses [Giulius](https://github.com/timboudreau/giulius) under the hood to provide bindings for `@Named` properties, and its [ShutdownHookRegistry](http://timboudreau.com/builds/job/mastfrog-parent/lastSuccessfulBuild/artifact/giulius-modules/giulius-parent/giulius/target/apidocs/com/mastfrog/giulius/ShutdownHookRegistry.html) to shut down services in the injector after each test method, so you can do things like bind a JDBC connection to an embedded H2 instance, populate it, run your tests, and have the environment completely cleaned up before the next test method runs.

@Named properties are by default bound to ``$PACKAGE/$TEST_NAME.properties``, or you can use the [@Configurations](http://timboudreau.com/builds/job/mastfrog-parent/lastSuccessfulBuild/artifact/giulius-tests-modules/giulius-tests-parent/giulius-tests/target/apidocs/com/mastfrog/giulius/tests/Configurations.html) annotation to load them from elsewhere.

Here is [what a test using giulius-tests looks like](https://github.com/timboudreau/acteur-auth/blob/master/acteur-auth/src/test/java/com/mastfrog/acteur/auth/OAuthPluginsTest.java)


## Advanced Features

### One Test, Multiple Modules

For cases where there are multiple implementations to be tested, you don't want to have to maintain copies of your tests for each one.  For that reason, you can have the JUnit test runner run a test or a test suite repeatedly with different modules:

	@TestWith(iterate={PostgresModule.class, MysqlModule.class})
	public void testIt (Connection connection, Whatever whatever) { ... }

Both `@TestWith` and `@RunWith` have `iterate` parameters.  If you use both, your tests will be run against the cartesian product of the `iterate` module list for both (not terribly useful, but hey, you can do it...).

Note that this feature may break test reporting features in some IDEs, which don't understand the same test methods running multiple times inside one run.


### Network Checks

You can use [@SkipWhenNetworkUnavailable](http://timboudreau.com/builds/job/mastfrog-parent/lastSuccessfulBuild/artifact/giulius-tests-modules/giulius-tests-parent/giulius-tests/target/apidocs/com/mastfrog/giulius/tests/SkipWhenNetworkUnavailable.html) to skip tests that require non-local networking, to avoid spurious failures or the inability to build on an airplane.

### Turn Off Expensive Tests in an IDE

If you set up your IDE to set a system property `in.ide=true` (usually there is a default Maven command-line, for example, that you can include this in), then you can skip expensive tests when that property is true - so tests that take a long time do not encourage developers to skip running any tests when they build.  Annotate a test method with [@SkipWhenRunInIDE](http://timboudreau.com/builds/job/mastfrog-parent/lastSuccessfulBuild/artifact/giulius-tests-modules/giulius-tests-parent/giulius-tests/target/apidocs/com/mastfrog/giulius/tests/SkipWhenRunInIDE.html) for that behavior.


