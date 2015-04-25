package press;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.cache.Cache;
import play.vfs.VirtualFile;
import press.io.CompressedFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copied and modified from
 * https://github.com/lunatech-labs/play-module-less/blob
 * /master/src/play/modules/less/PlayLessEngine.java LessEngine wrapper for Play
 */
public class PlayLessEngine {
  private static final Logger logger = LoggerFactory.getLogger(PlayLessEngine.class);

  NodeLessEngine lessEngine;
  static Pattern importPattern = Pattern.compile("@import\\s*[\"'](.*?)[\"']");

  public PlayLessEngine() {
    if (!NodeLessEngine.canBeUsed()) throw new RuntimeException("Cannot use lessc, not installed?");
    lessEngine = new NodeLessEngine();
  }

  /**
   * Get the CSS for this less file either from the cache, or compile it.
   */
  public String get(File lessFile, boolean compress) {
    File precompiled = new File(lessFile.getPath() + ".css");
    if (precompiled.exists()) {
      logger.debug("Serving precompiled {}", precompiled);
      return VirtualFile.open(precompiled).contentAsString();
    }

    String cacheKey = lessFile.getName() + "." + latestModified(lessFile);
    CompressedFile cachedFile = CompressedFile.create(cacheKey, PluginConfig.css.compressedDir);
    if (cachedFile.exists()) {
      try (InputStream is = cachedFile.inputStream()) {
        return IOUtils.toString(is, "UTF-8");
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    logger.debug("Compiling {}", lessFile);
    String css = compile(lessFile, compress);
    try {
      Writer out = cachedFile.startWrite();
      out.write(css);
      return css;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally {
      cachedFile.close();
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
    for (File imported : getImportsFromCacheOrFile(VirtualFile.open(lessFile))) {
      if (!imports.contains(imported)) {
        getAllImports(imported, imports);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected static Set<File> getImportsFromCacheOrFile(VirtualFile lessFile) {
    String cacheKey = "less_imports_" + lessFile.getRealFile() + lessFile.lastModified();

    Set<File> files = Cache.get(cacheKey, Set.class);
    if (files == null) {
      try {
        files = getImportsFromFile(lessFile);
        Cache.set(cacheKey, files);
      }
      catch (IOException e) {
        logger.error("IOException trying to determine imports in LESS file", e);
        files = new HashSet<>();
      }
    }
    return files;
  }

  protected static Set<File> getImportsFromFile(VirtualFile lessFile) throws IOException {
    if (!lessFile.exists()) {
      return Collections.emptySet();
    }

    String content = lessFile.contentAsString();

    Set<File> files = new HashSet<>();
    String virtualParentPath = lessFile.relativePath().replaceFirst("^\\{.*?\\}", "").replaceFirst("/[^/]*$", "");
    Matcher m = importPattern.matcher(content);
    while (m.find()) {
      VirtualFile file = Play.getVirtualFile(virtualParentPath + "/" + m.group(1));
      if (file == null && !m.group(1).endsWith(".less"))
        file = Play.getVirtualFile(virtualParentPath + "/" + m.group(1) + ".less");
      if (file != null) {
        files.add(file.getRealFile());
        files.addAll(getImportsFromCacheOrFile(file));
      }
    }

    return files;
  }

  public String compile(File lessFile, boolean compress) {
    try {
      String css = lessEngine.compile(lessFile, compress);
      // There seems to be a bug whereby \n's are sometimes escaped
      return css.replace("\\n", "\n");
    }
    catch (LessException e) {
      return handleException(lessFile, e);
    }
  }

  protected String handleException(File lessFile, LessException e) {
    logger.warn("Less exception", e);

    String filename = e.getFilename();

    // LessEngine reports the file as null when it's not an @imported file
    if (filename == null) {
      filename = lessFile.getName();
    }

    return filename;
    // TODO: return formatMessage(filename, e.getLine(), e.getColumn(), extract, e.getType());
  }

  protected String formatMessage(String filename, int line, int column, String extract,
                                 String errorType) {
    return "body:before {display: block; color: #c00; white-space: pre; font-family: monospace; background: #FDD9E1; border-top: 1px solid pink; border-bottom: 1px solid pink; padding: 10px; content: \"[LESS ERROR] "
        + String.format("%s:%s: %s (%s)", filename, line, extract, errorType) + "\"; }";
  }
}
