package com.meteorcode.pathway.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.meteorcode.pathway.io.*;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;

/**
 * Non-comprehensive test case to assert that the IO package does the Right Thing
 * THIS IS NOT A UNIT TEST - io classes should be unit with mock objects as well
 */
public class IOIntegrationTest {
    private FileHandle underTest;
    private ResourceManager r;
    private File fakeFile;

    @Before
    public void setUp() {
        r = new ResourceManager("build/resources/test");
        fakeFile = mock(File.class);
    }

    @After
    public void tearDown() throws IOException {
        // clean up the file so that it won't exist next time tests are run
        Files.deleteIfExists(FileSystems.getDefault().getPath("build/resources/test", "test5.txt"));
    }

    @Test
    public void testFileHandle() throws IOException {
        underTest = r.handle("test1.txt");
        assertEquals("txt", underTest.extension());
        assertEquals("test1", underTest.name());
        assertEquals("hi!", underTest.readString());
        assertFalse(underTest.isDirectory());
        assertEquals(Collections.emptyList(), underTest.list());
        assertTrue(underTest.writable());
        assertTrue(underTest.read(8) instanceof BufferedInputStream);
        assertTrue(underTest.read() instanceof InputStream);
    }

    @Test
    public void testWriting() throws IOException {
        underTest = r.handle("test5.txt");
        underTest.writeString("hello", false);
        assertEquals("hello", underTest.readString());
        assertTrue(underTest.write(8, true) instanceof BufferedOutputStream);
        assertTrue(underTest.write(true) instanceof OutputStream);
        assertTrue(underTest.write(8, false) instanceof BufferedOutputStream);
        assertTrue(underTest.write(false) instanceof OutputStream);
    }

    @Test
    public void testNonexistantFileHandle() {
        underTest = r.handle("testDir/I AM NOT A REAL FILE.txt");
        try {
            underTest.read();
            fail();
        } catch (IOException e) {
            assertEquals("Could not read file:testDir/I AM NOT A REAL FILE.txt, the requested file does not exist.",
                    e.getMessage());
        }
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
        underTest = r.handle("test7.md");
        assertFalse("FAIL: File in zip archive claimed to be a directory.", underTest.isDirectory());
        assertEquals("FAIL: Zipped file readString() returned wrong thing.", "Hi continues.", underTest.readString());
        assertNull("FAIL: Zipped file write() was not null.", underTest.write(true));
    }

    @Test
    public void testDirFileHandle() throws IOException {
        underTest = r.handle("testDir");
        assertEquals("", underTest.extension());
        assertEquals("testDir", underTest.name());
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

    @Test
    public void testEmptyDir() throws IOException {
        underTest = r.handle("testDir/emptyTestDir");
        java.util.List l = underTest.list();
        assertEquals("FAIL: empty test dir didn't return empty list", l, Collections.emptyList());
    }

    @Test
    public void testGetLogicalPath () {
        assertTrue("FAIL: ResourceManager.getLogicalPath() did not return expected path for file.",
                r.getLogicalPath("build/resources/test/test1.txt").equals("test1.txt"));

        assertTrue(
                "FAIL: ResourceManager.getLogicalPath() did not return expected path for file in zip.",
                r.getLogicalPath("build/resources/test/zippedtest.zip/zippedtest.txt").equals("zippedtest.txt"));
    }

    @Test
    public void testResourceManagerCaching() {
        FileHandle h1 = r.handle("test1.txt");
        FileHandle h2 = r.handle("test1.txt");
        assertSame("FAIL: ResourceManager did not return cached FileHandle.", h1, h2);
    }

    @Test
    public void testFileErrors () throws IOException {
        when(fakeFile.createNewFile())
                .thenThrow(new IOException("Permission denied"))
                .thenThrow(new IOException("LOL FAKE STRING"));
        when(fakeFile.isDirectory()).thenReturn(false);
        when(fakeFile.exists()).thenReturn(false);
        underTest = new DesktopFileHandle("lolfakepath", "lolfakepath", fakeFile, r);
        assertFalse(underTest.writable());
        try {
            underTest.writable();
            fail("FAIL: DesktopFileHandle did not throw after catching non-permission IOException");
        } catch (IOException e) {
            assertEquals("LOL FAKE STRING", e.getMessage());
        }
        verify(fakeFile, times(2)).isDirectory();
        verify(fakeFile, times(2)).exists();
        verify(fakeFile, times(2)).createNewFile();
    }
}
