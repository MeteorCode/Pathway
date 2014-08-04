/*
//////////////////////////////////////////////////
// THIS TEST FAILS ON JENKINS                   //
// Un-comment it and run it locally to confirm  //
// that it works as expected, but DO NOT commit //
////////////////////////////////////////////////*/
package com.meteorcode.pathway.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.meteorcode.pathway.io.*;
import java.io.IOException;

/**
 * Non-comprehensive test case to assert that the IO package does the Right Thing
 * THIS IS NOT A UNIT TEST - io classes should be unit tested for coverage as well.
 */
public class SimpleIOIntegrationTest {
    private FileHandle underTest;

	@Test
	public void testUnzippedFileHandle() throws IOException {
	    underTest = ClasspathTestShimDELETEME.c("/test/resources/test1.txt");
	    assertEquals("hi!", underTest.readString());
	}

    @Test
    public void testZippedFileHandle() throws IOException {
        underTest = ClasspathTestShimDELETEME.c("/test/resources/zippedtest.zip");
        assertTrue(underTest.isDirectory());
        assertEquals(underTest.list().get(0).readString(), "also hi!");
    }

    @Test
    public void testDirFileHandle() throws IOException{
        underTest = ClasspathTestShimDELETEME.c("/test/resources/testDir");
        assertTrue(underTest.isDirectory());
        assertEquals("yet again hi", underTest.list().get(0).readString());
        assertEquals("still hi", underTest.list().get(1).readString());
    }

}
