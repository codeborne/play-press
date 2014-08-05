package press;

import org.junit.Before;
import org.junit.Test;
import play.Play;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static press.PluginConfig.contentHostingDomain;
import static press.PluginConfig.htmlCompatible;

public class StyleRequestHandlerTest {
  StyleRequestHandler handler = new StyleRequestHandler();

  @Before
  public void setUp() {
    if (Play.configuration == null) Play.configuration = new Properties();
    PluginConfig.readConfig();
  }

  @Test
  public void composesCssLinkTag() {
    assertEquals("<link href=\"main.less\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\"></link>\n", handler.getTag("main.less"));
  }

  @Test
  public void canAddMediaToTag() {
    assertEquals("<link href=\"main.less\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" " +
        "media=\"screen\"></link>\n", handler.getTag("main.less", "screen"));
  }

  @Test
  public void canAddContentHostingDomainToTag() {
    contentHostingDomain = "http://cdn.mycompany.com/";
    assertEquals("<link href=\"http://cdn.mycompany.com/main.less\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\" " +
        "media=\"print\"></link>\n", handler.getTag("main.less", "print"));
  }

  @Test
  public void canGenerateHtmlCompatibleTag() {
    htmlCompatible = true;
    assertEquals("<link href=\"main.less\" rel=\"stylesheet\" type=\"text/css\" charset=\"utf-8\">\n", handler.getTag("main.less"));
  }
}
