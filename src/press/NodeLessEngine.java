package press;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.vfs.VirtualFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class NodeLessEngine {
  private static final Logger logger = LoggerFactory.getLogger(NodeLessEngine.class);

  public static boolean canBeUsed() {
    try {
      Process lessc = new ProcessBuilder("lessc", "-v").start();
      try (InputStream in = lessc.getInputStream()) {
        String version = IOUtils.toString(in, "UTF-8");
        logger.info("Using " + version.trim());
        return true;
      }
    }
    catch (IOException e) {
      logger.info("No lessc in path, will work in precompiled mode only");
      return false;
    }
  }

  public String compile(File input, boolean compress) throws LessException {
    try {
      String shellPath = Play.configuration.getProperty("press.shell", "bash");
      String lesscPrefix = Play.configuration.getProperty("press.lessc.prefix", "lessc");
      String lesscSuffix = Play.configuration.getProperty("press.lessc.suffix", "");
      Process lessc = new ProcessBuilder(shellPath, "-o", "pipefail", "-c", lesscPrefix + " " + (compress ? "-x" : "") + " --no-color " +
          "--include-path=" + joinPlayRoots() + " " + input.getPath() + lesscSuffix)
          .directory(Play.applicationPath).redirectErrorStream(true).start();
      try (InputStream in = lessc.getInputStream()) {
        String css = IOUtils.toString(in, "UTF-8");
        int exitCode = lessc.waitFor();
        if (exitCode != 0) {
          throw new LessException("lessc exited with error code " + exitCode + ": " + css, input.getAbsolutePath());
        }
        return css;
      }
    }
    catch (IOException|InterruptedException e) {
      throw new LessException("Failed to launch lessc for " + input, e, input.getAbsolutePath());
    }
  }

  private String joinPlayRoots() {
    StringBuilder roots = new StringBuilder();
    for (VirtualFile root : Play.roots) {
      File dir = new File(root.getRealFile(), "public/stylesheets");
      if (dir.exists())
        roots.append(dir).append(':');
    }
    return roots.toString();
  }
}
