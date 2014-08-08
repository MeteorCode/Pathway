
//////////////////////////////////////////////////
// THIS TEST FAILS ON JENKINS                   //
// Un-comment it and run it locally to confirm  //
// that it works as expected, but DO NOT commit //
////////////////////////////////////////////////*/
package com.meteorcode.pathway.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.meteorcode.pathway.io.*;

import java.io.IOException;

/**
 * Non-comprehensive test case to assert that the IO package does the Right Thing
 * THIS IS NOT A UNIT TEST - io classes should be unit tested for coverage as well.
 */
public class SimpleIOIntegrationTest {
    private FileHandle underTest;
    private ResourceManager r;

    @Before
    public void setUp() {
        r = new ResourceManager("build/resources/test");
    }

    @Test
    public void testUnzippedFileHandle() throws IOException {
        underTest = r.handle("test1.txt");
        assertEquals("hi!", underTest.readString());
        assertFalse(underTest.isDirectory());
        assertTrue(underTest.writable());
    }

    @Test
    public void testWriteString() throws IOException {
        underTest = r.handle("test5.txt");
        underTest.writeString("hello", false);
        assertEquals("hello", underTest.readString());
    }

    @Test
    public void testZippedFileHandle() throws IOException {
        underTest = r.handle("zippedtest.txt");
        assertFalse("FAIL: File in zip archive claimed to be a directory.", underTest.isDirectory());
        assertEquals("FAIL: Zipped file readString() returned wrong thing.", "also hi!", underTest.readString());
        assertNull("FAIL: Zipped file write() was not null.", underTest.write(true));
    }

    @Test
    public void testJarFileHandle() throws IOException {
        underTest = r.handle("test6.txt");
        assertFalse("FAIL: File in zip archive claimed to be a directory.", underTest.isDirectory());
        assertEquals("FAIL: Zipped file readString() returned wrong thing.", "Continued hi.", underTest.readString());
        assertNull("FAIL: Zipped file write() was not null.", underTest.write(true));
        underTest = r.handle("test7.txt");
        assertFalse("FAIL: File in zip archive claimed to be a directory.", underTest.isDirectory());
        assertEquals("FAIL: Zipped file readString() returned wrong thing.", "Hi continues.", underTest.readString());
        assertNull("FAIL: Zipped file write() was not null.", underTest.write(true));
    }

    @Test
    public void testDirFileHandle() throws IOException {
        underTest = r.handle("testDir");
        assertFalse("FAIL: Directory claimed to be writable.", underTest.writable());
        assertTrue("FAIL: Directory claimed not to be.", underTest.isDirectory());
        assertNull("FAIL: directory gave us an OutputStream?", underTest.write(true));
        System.out.println(underTest.list().toString()); // FUCK IT. we can test for correctness manually.
        try {
            underTest.read();
            fail("FAIL: Exception was not thrown when reading to directory.");
        } catch (IOException e) {
            //meh
        }
    }

}
