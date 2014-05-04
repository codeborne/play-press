package press;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScriptCompressorTest extends AbstractPressTest {
  ScriptCompressor compressor = new ScriptCompressor();
  File rawJsFile = findFile("public/javascripts/jquery-2.0.3.js");

  @Test
  public void canOutputJavascriptFileAsIs() throws IOException {
    StringWriter out = new StringWriter();
    compressor.compress(rawJsFile, out, false);
    assertEquals(readFileToString(rawJsFile), out.toString());
  }

  @Test
  public void canCompressJavascriptFile() throws IOException {
    StringWriter out = new StringWriter();
    compressor.compress(rawJsFile, out, true);
    String compressedJS = out.toString();
    assertTrue(compressedJS.length() < readFileToString(rawJsFile).length() / 2);
    assertTrue(compressedJS.substring(0, 50), compressedJS.startsWith("(function("));
    assertTrue(compressedJS.substring(compressedJS.length() - 30), compressedJS.endsWith(")(window);"));
  }
}