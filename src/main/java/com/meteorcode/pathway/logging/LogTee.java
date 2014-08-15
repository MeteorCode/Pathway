package com.meteorcode.pathway.logging;

/**
 * This class used to have documentation.
 * Quite a lot of it
 * And then Eclipse crashed, and I had
 * to rewrite this class from scratch.
 * So uh.... I guess this is a
 * //TODO: finish this documentation
 * huh.
 * @author xyzzy
 *
 */
public class LogTee {
	ConcurrentCache<String> cache;
	
	public LogTee(int cacheSize) {
		this.cache = new ConcurrentCache<String>(cacheSize);
	}
	
	public void debug(String message) {
		debugPrint(getContextTag(), message);
		
		cache.insert(getContextTag() + ": " + message);
	}
	
	public void debug(String message, Throwable e) {
		debugPrint(getContextTag(), message, e);
		
		cache.insert(getContextTag() + ": " + message);
		cache.insert("With: " + e.getClass().getName() + " // " + e.getMessage());
		StackTraceElement[] st = e.getStackTrace();
		for(int i = 0; i < ((st.length < 50)? st.length : 50); i++) {
			cache.insert(st.toString());
		}
		
		if(st.length >= 50) {
			cache.insert("and more...\n\n");
		}
	}
	
	private static void debugPrint(String tag, String message) {
	    debugPrint(tag, message, null);
	}
	
	private static void debugPrint(String tag, String message, Throwable t) {
	    System.err.println("> " + tag + "; " + message);
	    if(t != null) {
	        t.printStackTrace();
	    }
	}
	
	private static String getContextTag() {
		return Thread.currentThread().getName();
	}
}
