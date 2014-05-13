package press;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.cache.Cache;
import play.cache.CacheImpl;

import java.io.File;
import java.io.FileNotFoundException;
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
  public void canCompressCssFile() throws IOException {
    StringWriter out = new StringWriter();
    compressor.compress(rawCssFile, out, true);
    String compressedCss = out.toString();
    assertTrue(compressedCss.length() <= readFileToString(rawCssFile).length() / 1.2);
    assertEquals(readFileToString(minCssFile), compressedCss);
  }

  @Test
  public void canConvertLessToCssWithoutCompression() throws IOException {
    StringWriter out = new StringWriter();
    compressor.compress(rawLessFile, out, false);
    assertEquals(readFileToString(rawCssFile), out.toString());
  }
  
  @Test
  public void canConvertLessToCssWithCompression() throws IOException {
    StringWriter out = new StringWriter();
    compressor.compress(rawLessFile, out, true);
    String compressedLess = out.toString();
    assertEquals(readFileToString(minCssFile), compressedLess);
  }
  
  @Test
  public void detectsLessByFileExtension() {
    assertTrue(compressor.isLess("some.less"));
    assertFalse(compressor.isLess("some.css"));
    assertFalse(compressor.isLess("some.js"));
  }

  @Test
  public void canCompressLessWithoutLesscWithJavascript() throws IOException {
    compressor.lessEngine.lessEngine = new PlayVirtualFileLessEngine();
    StringWriter out = new StringWriter();
    compressor.compress(rawLessFile, out, false);
    assertEquals(readFileToString(rawCssFile), out.toString());
  }

  @Test @Ignore
  public void throwsFileNotFoundExceptionAndLogsPlayRootsForMissingFile() throws IOException {
    compressor.lessEngine.lessEngine = new PlayVirtualFileLessEngine();
    try {
      StringWriter out = new StringWriter();
      compressor.compress(new File("missing-file.less"), out, false);
      fail("Expected FileNotFoundException, but received: " + out);
    }
    catch (FileNotFoundException e) {
      assertEquals("Virtual path /home/xp/work/play-press/missing-file.less not found in /home/xp/work/ibank/out/test/play-press/ [play-press]", e.toString());
    }
  }
}