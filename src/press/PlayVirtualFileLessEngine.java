package press;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessOptions;
import com.asual.lesscss.loader.ResourceLoader;
import play.Play;
import play.vfs.VirtualFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PlayVirtualFileLessEngine extends LessEngine {
  public PlayVirtualFileLessEngine() {
    super(new LessOptions(), new ResourceLoader() {
      @Override public boolean exists(String path) {
        return Play.getVirtualFile(toRelative(path)) != null;
      }

      @Override public String load(String path, String charset) throws IOException {
        VirtualFile file = Play.getVirtualFile(toRelative(path));
        if (file == null) {
          throw new FileNotFoundException("Virtual path " + toRelative(path) + " not found in " + Play.applicationPath + "/ " + Play.roots);
        }
        return file.contentAsString().replace("\r", "");
      }

      private String toRelative(String path) {
        return path.replace(Play.applicationPath.getAbsolutePath() + File.separator, "");
      }
    });
  }
}
