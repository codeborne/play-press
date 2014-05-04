package press;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PluginTest extends AbstractPressTest {
  @Test
  public void addCssWithoutMedia() {
    assertEquals("<link href=\"/public/stylesheets/main.css\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\"></link>\n",
        Plugin.addCSS("public/stylesheets/main.css", false, null));
  }

  @Test
  public void addCssWithMedia() {
    assertEquals("<link href=\"/public/stylesheets/main.css\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" media=\"screen\"></link>\n",
        Plugin.addCSS("public/stylesheets/main.css", false, "screen"));
  }

  @Test
  public void addSingleCss() {
    assertEquals("<link href=\"/public/stylesheets/main.css\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\"></link>\n",
        Plugin.addSingleCSS("public/stylesheets/main.css", null));
  }

  @Test
  public void addSingleCssWithMedia() {
    assertEquals("<link href=\"/public/stylesheets/main.css\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" media=\"print\"></link>\n",
        Plugin.addSingleCSS("public/stylesheets/main.css", "print"));
  }

  @Before
  public void setUp() {
    new Plugin().beforeActionInvocation(null);
  }

  @After
  public void tearDown() {
    new Plugin().afterActionInvocation();
  }
}
