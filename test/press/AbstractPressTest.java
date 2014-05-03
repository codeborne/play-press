package press;

import org.junit.Before;
import play.Play;
import play.mvc.Router;

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
    Play.configuration = new Properties();
    Play.configuration.setProperty("press.enabled", "false");
    PluginConfig.readConfig();

    cssDir = findFile("public").getParentFile();
    Play.applicationPath = cssDir.getParentFile();
    PluginConfig.js.srcDir = cssDir.getName();
    PluginConfig.css.srcDir = cssDir.getName();
    Router.addRoute("GET", "/test/", "staticDir:" + cssDir.getName());
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
