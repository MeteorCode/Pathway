package com.meteorcode.pathway.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({

	ScriptContainerTest.class,
	ScriptEnvironmentTest.class,
	ScriptExceptionTest.class
})

public class ScriptTests {
}
