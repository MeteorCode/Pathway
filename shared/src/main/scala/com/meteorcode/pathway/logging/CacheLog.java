package com.meteorcode.pathway.logging;

/**
 * A LogDestination backed by a ConcurrentCache: Logs
 * logged to this destination are rolled into the cache
 * with the intention of being viewed later if there is
 * a reason to need these logs.
 * Created by xyzzy on 8/19/14.
 */
public class CacheLog implements LogDestination {

    ConcurrentCache<String> cache;

    /**
     * Creates a new log backed by a String concurrent cache of the default size; 200 cached lines.
     */
    public CacheLog() {
        cache = new ConcurrentCache<String>(200);
    }

    /**
     * Creates a new log backed by the given ConcurrentCache.
     */
    public CacheLog(ConcurrentCache<String> cache) {
        this.cache = cache;
    }

    /**
     * Creates a new log backed by a String concurrent cache of the given size.
     * @param size The size of the cache; i.e. the number of lines which will be cached
     */
    public CacheLog(int size) {
        cache = new ConcurrentCache<String>(size);
    }

    public void log(String message) {
        log(getContextTag(), message);
    }

    public void log(String tag, String message) {
        cache.insert(tag + ": " + message);
    }

    public void log(String message, Throwable t) {
        log(getContextTag(), message, t);
    }

    public void log(String tag, String message, Throwable t) {
        log(tag, message);
        cache.insert("With: " + t.getClass().getName() + " // " + t.getMessage());
        StackTraceElement[] st = t.getStackTrace();
        for(int i = 0; i < ((st.length < 50)? st.length : 50); i++) {
            cache.insert(st.toString());
        }

        if(st.length >= 50) {
            cache.insert("and more...\n\n");
        }
    }

    /**
     * Get the current cache being used by this CacheLog.
     * @return The ConcurrentCache backing this CacheLog.
     */
    public ConcurrentCache<String> getCache() {
        return this.cache;
    }

    private static String getContextTag() {
        return Thread.currentThread().getName();
    }
}
