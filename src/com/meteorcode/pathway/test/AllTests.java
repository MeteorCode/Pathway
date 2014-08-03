package com.meteorcode.pathway.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

	SampleTest.class,
	ScriptTests.class,
	EventTests.class,
	ModelTests.class,
	ConcurrentCachingTest.class,
    SimpleIOIntegrationTest.class

})

public class AllTests {
}
