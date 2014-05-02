package press;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.mvc.Router;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class PluginTest {
  @Test
  public void addCssWithoutMedia() {
    assertEquals("<link href=\"/test/main.css\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\"></link>\n",
        Plugin.addCSS("main.css", false, null));
  }

  @Test
  public void addCssWithMedia() {
    assertEquals("<link href=\"/test/main.css\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" media=\"screen\"></link>\n",
        Plugin.addCSS("main.css", false, "screen"));
  }

  @Test
  public void addSingleCss() {
    assertEquals("<link href=\"/test/main.css\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\"></link>\n",
        Plugin.addSingleCSS("main.css", null));
  }

  @Test
  public void addSingleCssWithMedia() {
    assertEquals("<link href=\"/test/main.css\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" media=\"print\"></link>\n",
        Plugin.addSingleCSS("main.css", "print"));
  }

  @Before
  public void setUp() throws URISyntaxException {
    Play.configuration = new Properties();
    Play.configuration.setProperty("press.enabled", "false");
    PluginConfig.readConfig();

    URL mainCss = Thread.currentThread().getContextClassLoader().getResource("main.css");
    File cssDir = new File(mainCss.toURI()).getParentFile();
    Play.applicationPath = cssDir.getParentFile();
    PluginConfig.js.srcDir = cssDir.getName();
    PluginConfig.css.srcDir = cssDir.getName();
    Router.addRoute("GET", "/test/", "staticDir:" + cssDir.getName());

    new Plugin().beforeActionInvocation(null);
  }

  @After
  public void tearDown() {
    new Plugin().afterActionInvocation();
  }
}
