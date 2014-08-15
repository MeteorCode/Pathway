package com.meteorcode.pathway.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ClasspathFileHandle extends FileHandle {

	File back;
	String opath;
    String virtualPath;
    ResourceManager manager;

	/**
	 * Constructor for grabbing a thing off the Classpath.
	 * Note that this constructor silently swallows errors that may arise
	 * during construction.
	 * @param physicalPath The physicalPath to grab.
	 */
	protected ClasspathFileHandle(String physicalPath, String virtualPath, ResourceManager manager) throws IOException {
        super(virtualPath, manager);
		this.opath = physicalPath;
        this.manager = manager;         //FIXME: dumb Scala/Java interop behaviour, apparently this class doesn't
        this.virtualPath = virtualPath; //have access to fields of the superclass
		URL url = this.getClass().getResource(physicalPath);
		try {
			back = new File(url.toURI());
		} catch (URISyntaxException e) {
            throw new IOException("Could not instantiate ClasspathFileHandle for path " + virtualPath +
                    ", a URISyntaxException occurred.", e);
		}
	}

	@Override
	public boolean exists() {
		return (back != null && back.exists());
	}

	@Override
	public boolean isDirectory() {
		return (back != null && back.isDirectory());
	}

    @Override
    public File file() {return back;}

	@Override
	public boolean writable() {
		//ALL classpath files are not writable as far as I know.
		return false;
	}

	@Override
	public String physicalPath() {
		return back.getPath();
	}

	@Override
	public List<FileHandle> list() throws IOException {
		if(back != null && back.isDirectory()) {
			ArrayList<FileHandle> r = new ArrayList<FileHandle>();
			for(String s : back.list()) {
				r.add(new ClasspathFileHandle(this.opath + "/" + s, this.path() + "/" + s, this.manager));
			}
			return r;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public InputStream read() throws IOException {
		if(back != null) {
			return new FileInputStream(back);
		} else {
			return null;
		}
	}

	@Override
	public OutputStream write(boolean append) {
		return null;
	}

    @Override
    public long length() {
        if (isDirectory())
            return 0;
        else return back.length();
    }

    @Override
    public boolean delete() {
        if (!writable())
            return false;
        else
            return back.delete();
    }

    @Override public String readString() throws IOException{
		if(back != null && !back.isDirectory()) {
			StringBuilder b = new StringBuilder("");
			Scanner s = new Scanner(this.read());
			while(s.hasNextLine()) {
				b.append(s.nextLine());
			}
			s.close();
			return b.toString();
		}
		throw new IOException("File does not exist or is directory");
	}

}
