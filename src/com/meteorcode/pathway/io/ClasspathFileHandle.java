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
	
	/**
	 * Constructor for grabbing a thing off the Classpath.
	 * Note that this constructor silently swallows errors that may arise
	 * during construction.
	 * @param path The path to grab.
	 */
	protected ClasspathFileHandle(String path) {
		this.opath = path;
		URL url = this.getClass().getResource(path);
		try {
			back = new File(url.toURI());
		} catch (URISyntaxException e) {
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
	public boolean writeable() {
		//ALL classpath files are not writable as far as I know.
		return false;
	}

	@Override
	public String path() {
		return "";
	}

	@Override
	public List<FileHandle> list() {
		if(back != null && back.isDirectory()) {
			ArrayList<FileHandle> r = new ArrayList<FileHandle>();
			for(String s : back.list()) {
				r.add(new ClasspathFileHandle(this.opath + "/" + s));
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
	
	@Override public String readString() throws IOException{
		if(back != null && !back.isDirectory()) {
			StringBuffer b = new StringBuffer("");
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
