package press;

import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.cache.Cache;
import play.cache.CacheImpl;
import play.vfs.VirtualFile;

import java.io.IOException;
import java.io.StringWriter;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
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

  @Before
  public void setUpPlayRootToTestFolder() {
    VirtualFile appRoot = VirtualFile.open(findFile("public").getParentFile());
    if (!Play.roots.contains(appRoot)) {
      Play.roots.add(appRoot);
    }
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
}