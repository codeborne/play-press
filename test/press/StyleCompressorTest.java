package press;

import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.cache.Cache;
import play.cache.CacheImpl;

import java.io.IOException;
import java.io.StringWriter;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class StyleCompressorTest extends AbstractPressTest {
  StyleCompressor compressor = new StyleCompressor();

  @Before
  public void mockCache() {
    Cache.cacheImpl = mock(CacheImpl.class);
    Play.mode = Play.Mode.DEV;
    doReturn(true).when(Cache.cacheImpl).safeSet(anyString(), any(), anyInt());
    PluginConfig.inMemoryStorage = true;
  }

  @Test
  public void canOutputCssFileAsIs() throws IOException {
    StringWriter out = new StringWriter();
    compressor.compress(rawCssFile, out, false);
    assertEquals(readFileToString(rawCssFile), out.toString());
  }

  @Test
  public void compressDoesNotModifyCssFile_becauseItIsCompressedDuringBuild() throws IOException {
    StringWriter out = new StringWriter();
    compressor.compress(rawCssFile, out, true);
    String compressedCss = out.toString();
    assertEquals(readFileToString(rawCssFile), compressedCss);
  }

  @Test
  public void canConvertLessToCssWithoutCompression() throws IOException {
    StringWriter out = new StringWriter();
    compressor.compress(rawLessFile, out, false);
    assertEquals(readFileToString(rawCssFile), out.toString());
  }

  @Test
  public void canConvertLessToCssWithCompression() throws IOException {
    String warning = "The compress option has been deprecated. We recommend you use a dedicated css minifier, for instance see less-plugin-clean-css.\n";

    StringWriter out = new StringWriter();
    compressor.compress(rawLessFile, out, true);
    String compressedLess = out.toString();
    assertEquals(readFileToString(minCssFile), compressedLess.replace(warning, "").trim());
  }
  
  @Test
  public void detectsLessByFileExtension() {
    assertTrue(compressor.isLess("some.less"));
    assertFalse(compressor.isLess("some.css"));
    assertFalse(compressor.isLess("some.js"));
  }
}
