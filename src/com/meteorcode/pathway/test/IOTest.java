package com.meteorcode.pathway.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.MockitoAnnotations.*;
import org.mockito.MockitoAnnotations;

import com.meteorcode.pathway.io.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Non-comprehensive test case to assert that the IO package does the Right Thing
 * THIS IS NOT A UNIT TEST - io classes should be unit with mock objects as well
 */
public class IOTest {
    private FileHandle underTest;
    private ResourceManager r;
    @Mock private File fakeFile;
    @Mock private ResourceManager fakeMangler;
    @Mock private FileHandle fakeHandle;
    @Mock private JarEntry fakeEntry;


    @Before
    public void setUp() {
        r = new ResourceManager("build/resources/test", new AlphabeticLoadPolicy());
    }

    @Before public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws IOException {
        // clean up the file so that it won't exist next time tests are run
        Files.deleteIfExists(FileSystems.getDefault().getPath("build/resources/test", "test5.txt"));
    }

    @Test
    public void testFileHandle() throws IOException {
        underTest = r.handle("/test1.txt");
        assertEquals("txt", underTest.extension());
        assertEquals("test1", underTest.name());
        assertTrue(underTest.readString().contains("test1.txt"));
        assertFalse(underTest.isDirectory());
        assertEquals(Collections.emptyList(), underTest.list());
        assertFalse(underTest.writable());
        assertTrue(underTest.read(8) instanceof BufferedInputStream);
        assertTrue(underTest.read() instanceof InputStream);
    }

    @Test
    public void testWriting() throws IOException {
        // Disabled because this behaviour is no longer correct.
        // TODO: Make the test class create a write dir, and make this test attempt to write to the write dir.
        /*
        underTest = r.handle("test5.txt");
        underTest.writeString("hello", false);
        assertEquals("hello", underTest.readString());
        assertTrue(underTest.write(8, true) instanceof BufferedOutputStream);
        assertTrue(underTest.write(true) instanceof OutputStream);
        assertTrue(underTest.write(8, false) instanceof BufferedOutputStream);
        assertTrue(underTest.write(false) instanceof OutputStream);
        */
    }

    @Test
    public void testNonexistantFileHandle() {
        try {
            underTest = r.handle("testDir/I AM NOT A REAL FILE.txt");
        } catch (IOException io) {
            assertEquals("A filehandle to an empty path was requested, and the requested path was not writable",
                    io.getMessage());
        }
    }

    @Test
    public void testZippedFileHandle() throws IOException {
        underTest = r.handle("/zippedtest.txt");
        assertFalse("FAIL: File in zip archive claimed to be a directory.", underTest.isDirectory());
        assertEquals("FAIL: Zipped file readString() returned wrong thing.", "also hi!", underTest.readString());
        assertNull("FAIL: Zipped file write() was not null.", underTest.write(true));
        assertNull("FAIL: Zipped file write() was not null.", underTest.write(8, true));
        try {
            underTest.writeString("hi", true);
            fail();
        } catch (IOException io) {
        }
        try {
            underTest.writeString("hi", Charset.defaultCharset(), true);
            fail();
        } catch (IOException io) {
        }

    }

    @Test
    public void testJarFileHandle() throws IOException {
        underTest = r.handle("/test6.txt");
        assertFalse("FAIL: File in zip archive claimed to be a directory.", underTest.isDirectory());
        assertEquals("FAIL: Zipped file readString() returned wrong thing.", "Continued hi.", underTest.readString());
        assertFalse(underTest.writable());
        assertNull("FAIL: Zipped file write() was not null.", underTest.write(true));
        underTest = r.handle("/testJarDir/test7.md");
        assertFalse("FAIL: File in zip archive claimed to be a directory.", underTest.isDirectory());
        assertEquals("FAIL: Zipped file readString() returned wrong thing.", "Hi continues.", underTest.readString());
        assertFalse(underTest.writable());
        assertNull("FAIL: Zipped file write() was not null.", underTest.write(true));
        assertEquals(underTest.list(), Collections.emptyList());
        assertEquals("build/resources/test/testJar.jar/testJarDir/test7.md", underTest.physicalPath());
        underTest = r.handle("/testJarDir/");
        assertTrue(underTest.isDirectory());
        assertFalse(underTest.writable());
        assertNull(underTest.write(true));
        assertEquals("Hi continues.",underTest.list(".md").get(0).readString());
    }

    @Test
    public void testDirFileHandle() throws IOException {
        underTest = r.handle("/testDir/");
        assertEquals("", underTest.extension());
        assertEquals("testDir", underTest.name());
        assertFalse("FAIL: Directory claimed to be writable.", underTest.writable());
        assertTrue("FAIL: Directory claimed not to be.", underTest.isDirectory());
        assertNull("FAIL: directory gave us an OutputStream?", underTest.write(true));
        assertNull(underTest.read());
    }

    @Test
    public void testEmptyDir() throws IOException {
        underTest = r.handle("/testDir/emptyTestDir");
        java.util.List l = underTest.list();
        assertEquals("FAIL: empty test dir didn't return empty list", l, Collections.emptyList());
    }

    @Test
    public void testZipFileHandle () throws IOException {
        underTest = r.handle("/zippedtest.zip");
        String name = underTest.name();
        assertTrue("FAIL: got " + name + " expected /", name.equals("zippedtest"));
        assertTrue(underTest.isDirectory());
        assertNull(underTest.read());
    }

    @Test
    public void testResourceManagerCaching() throws IOException {
        FileHandle h1 = r.handle("/test1.txt");
        FileHandle h2 = r.handle("/test1.txt");
        assertSame("FAIL: ResourceManager did not return cached FileHandle.", h1, h2);
    }
    /*
    // This test was disabled as it expects incorrect write protection behavior
    // TODO: make this test happen isnide a write dir
    @Test
    public void testPermissionDenied () throws IOException {
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
    */
}
