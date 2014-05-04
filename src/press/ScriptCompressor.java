package press;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import press.io.FileIO;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.javascript.jscomp.SourceFile.fromFile;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class ScriptCompressor extends Compressor {
  public static final String EXTENSION = ".js";

  public static int clearCache() {
    return clearCache(PluginConfig.js.compressedDir, EXTENSION);
  }

  @Override
  public void compress(File sourceFile, Writer out, boolean compress) throws IOException {
    if (!compress) {
      FileIO.write(FileIO.getReader(sourceFile), out);
      return;
    }
    List<SourceFile> externs = emptyList();
    List<SourceFile> inputs = asList(fromFile(sourceFile));

    CompilerOptions options = new CompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);

    com.google.javascript.jscomp.Compiler compiler = new com.google.javascript.jscomp.Compiler();
    Result result = compiler.compile(externs, inputs, options);

    if (result.success) {
      out.write(compiler.toSource());
    }
    else {
      throw new IllegalArgumentException("Unable to minify " + sourceFile +
          "\nerrors: " + Arrays.toString(result.errors) +
          "\nwarnings: " + Arrays.toString(result.warnings));
    }
  }

  @Override
  public String getCompressedFileKey(List<FileInfo> componentFiles) {
    Map<String, Long> files = FileInfo.getFileLastModifieds(componentFiles);
    return CacheManager.getCompressedFileKey(files, EXTENSION);
  }
}
