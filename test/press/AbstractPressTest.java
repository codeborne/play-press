package press;

import org.junit.Before;
import play.Play;
import play.mvc.Router;
import play.vfs.VirtualFile;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class AbstractPressTest {
  protected File cssDir;

  protected File rawLessFile = findFile("public/stylesheets/main.less");
  protected File rawCssFile = findFile("public/stylesheets/main.css");
  protected File minCssFile = findFile("public/stylesheets/main.css.min");

  @Before
  public void configureCssJsLocation() {
    if (Play.configuration == null) Play.configuration = new Properties();
    Play.configuration.setProperty("press.enabled", "false");
    PluginConfig.readConfig();

    cssDir = findFile("public").getParentFile();
    Play.applicationPath = cssDir;
    PluginConfig.js.srcDir = "";
    PluginConfig.css.srcDir = "";
    Router.addRoute("GET", "/public/", "staticDir:public");

    VirtualFile appRoot = VirtualFile.open(Play.applicationPath);
    if (!Play.roots.contains(appRoot)) {
      Play.roots.add(appRoot);
    }
  }
  
  protected static File findFile(String filePath) {
    URL fileInClasspath = Thread.currentThread().getContextClassLoader().getResource(filePath);
    assertNotNull("File not found in classpath: " + filePath, filePath);
    try {
      return new File(fileInClasspath.toURI());
    }
    catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid file URI: " + fileInClasspath, e);
    }
  }
}
