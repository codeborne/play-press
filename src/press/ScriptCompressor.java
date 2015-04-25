package press;

import press.io.FileIO;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class ScriptCompressor extends Compressor {
  public static final String EXTENSION = ".js";

  public static int clearCache() {
    return clearCache(PluginConfig.js.compressedDir, EXTENSION);
  }

  @Override
  public void compress(File sourceFile, Writer out, boolean compress) throws IOException {
    FileIO.write(FileIO.getReader(sourceFile), out);
  }

  @Override
  public String getCompressedFileKey(List<FileInfo> componentFiles) {
    Map<String, Long> files = FileInfo.getFileLastModifieds(componentFiles);
    return CacheManager.getCompressedFileKey(files, EXTENSION);
  }
}
