package press;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.WrappedException;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.vfs.VirtualFile;
import press.io.CompressedFile;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copied and modified from
 * https://github.com/lunatech-labs/play-module-less/blob
 * /master/src/play/modules/less/PlayLessEngine.java LessEngine wrapper for Play
 */
public class PlayLessEngine {

    LessEngine lessEngine;
    static Pattern importPattern = Pattern.compile(".*@import\\s*\"(.*?)\".*");

    public PlayLessEngine() {
        lessEngine = NodeLessEngine.canBeUsed() ? new NodeLessEngine() : new PlayVirtualFileLessEngine();
    }

    /**
     * Get the CSS for this less file either from the cache, or compile it.
     */
    public String get(File lessFile, boolean compress) {
        VirtualFile precompiled = VirtualFile.open(new File(Play.applicationPath, lessFile.getPath() + ".css"));
        if (precompiled.exists())
          return precompiled.contentAsString();

        try {
            String cacheKey = lessFile.getName() + "." + latestModified(lessFile);
            CompressedFile cachedFile = CompressedFile.create(cacheKey, PluginConfig.css.compressedDir);
            if (cachedFile.exists())
              return IOUtils.toString(cachedFile.inputStream(), "UTF-8");

            String css = compile(lessFile, compress);
            cachedFile.startWrite().write(css);
            cachedFile.close();
            return css;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the latest of the last modified dates of this file and all files
     * it imports
     */
    public long latestModified(File lessFile) {
        long lastModified = lessFile.lastModified();
        for (File imported : getAllImports(lessFile)) {
            lastModified = Math.max(lastModified, imported.lastModified());
        }
        return lastModified;
    }

    /**
     * Returns a set composed of the file itself, followed by all files that it
     * imports, the files they import, etc
     */
    public static Set<File> getAllImports(File lessFile) {
        Set<File> imports = new HashSet<>();
        getAllImports(lessFile, imports);
        return imports;
    }

    protected static void getAllImports(File lessFile, Set<File> imports) {
        imports.add(lessFile);
        for (File imported : getImportsFromCacheOrFile(lessFile)) {
            if (!imports.contains(imported)) {
                getAllImports(imported, imports);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected static Set<File> getImportsFromCacheOrFile(File lessFile) {
        String cacheKey = "less_imports_" + lessFile.getPath() + lessFile.lastModified();

        Set<File> files = Cache.get(cacheKey, Set.class);
        if (files == null) {
            try {
                files = getImportsFromFile(lessFile);
                Cache.set(cacheKey, files);
            } catch (IOException e) {
                Logger.error(e, "IOException trying to determine imports in LESS file");
                files = new HashSet<>();
            }
        }
        return files;
    }

    protected static Set<File> getImportsFromFile(File lessFile) throws IOException {
        if (!lessFile.exists()) {
            return Collections.emptySet();
        }

        BufferedReader r = new BufferedReader(new FileReader(lessFile));
        try {
            Set<File> files = new HashSet<>();
            String line;
            while ((line = r.readLine()) != null) {
                Matcher m = importPattern.matcher(line);
                while (m.find()) {
                    VirtualFile file = Play.getVirtualFile(lessFile.getParent() + "/" + m.group(1));
                    if (file == null && !m.group(1).endsWith(".less"))
                        file = Play.getVirtualFile(lessFile.getParent() + "/" + m.group(1) + ".less");
                    if (file != null) {
                      files.add(file.getRealFile());
                      files.addAll(getImportsFromCacheOrFile(file.getRealFile()));
                    }
                }
            }
            return files;
        } finally {
            IOUtils.closeQuietly(r);
        }
    }

    public String compile(File lessFile, boolean compress) {
        try {
            String css = lessEngine.compile(lessFile, compress);
            // There seems to be a bug whereby \n's are sometimes escaped
            return css.replace("\\n", "\n");
        } catch (LessException e) {
            return handleException(lessFile, e);
        }
    }

    protected String handleException(File lessFile, LessException e) {
        Logger.warn(e, "Less exception");

        String filename = e.getFilename();
        List<String> extractList = e.getExtract();
        String extract = null;
        if (extractList != null) {
            extract = extractList.toString();
        }

        // LessEngine reports the file as null when it's not an @imported file
        if (filename == null) {
            filename = lessFile.getName();
        }

        // Try to detect missing imports (flaky)
        if (extract == null && e.getCause() instanceof WrappedException) {
            WrappedException we = (WrappedException) e.getCause();
            if (we.getCause() instanceof FileNotFoundException) {
                FileNotFoundException fnfe = (FileNotFoundException) we.getCause();
                extract = fnfe.getMessage();
            }
        }

        return formatMessage(filename, e.getLine(), e.getColumn(), extract, e.getType());
    }

    protected String formatMessage(String filename, int line, int column, String extract,
            String errorType) {
        return "body:before {display: block; color: #c00; white-space: pre; font-family: monospace; background: #FDD9E1; border-top: 1px solid pink; border-bottom: 1px solid pink; padding: 10px; content: \"[LESS ERROR] "
                + String.format("%s:%s: %s (%s)", filename, line, extract, errorType) + "\"; }";
    }
}
