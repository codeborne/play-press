package controllers.press;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.exceptions.UnexpectedException;
import play.mvc.Controller;
import play.db.jpa.NoTransaction;
import press.*;
import press.io.CompressedFile;
import press.io.FileIO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

@NoTransaction
public class Press extends Controller {
    private static final DateTimeFormatter httpDateTimeFormatter = DateTimeFormat
            .forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

    private static final Logger logger = LoggerFactory.getLogger(Press.class);

    public static void getCompressedJS(String key) {
        key = FileIO.unescape(key);
        CompressedFile compressedFile = new ScriptCompressedFileManager().getCompressedFile(key);
        renderCompressedFile(key, compressedFile, "JavaScript");
    }

    public static void getCompressedCSS(String key) {
        key = FileIO.unescape(key);
        CompressedFile compressedFile = new StyleCompressedFileManager().getCompressedFile(key);
        renderCompressedFile(key, compressedFile, "CSS");
    }

    private static void renderCompressedFile(String key, CompressedFile compressedFile, String type) {
        flash.keep();

        if (compressedFile == null) {
            renderBadResponse(key, type);
            return;
        }

        InputStream inputStream = compressedFile.inputStream();

        // This seems to be buggy, so instead of passing the file length we
        // reset the input stream and allow play to manually copy the bytes from
        // the input stream to the response
        // renderBinary(inputStream, compressedFile.name(),
        // compressedFile.length());

        try {
            if (inputStream.markSupported()) {
                inputStream.reset();
            }
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }

        // If the caching strategy is always, the timestamp is not part of the
        // key. If we let the browser cache, then the browser will keep holding
        // old copies, even after changing the files at the server and
        // restarting the server, since the key will stay the same.
        // If the caching strategy is never, we also don't want to cache at the
        // browser, for obvious reasons.
        // If the caching strategy is Change, then the modified timestamp is a
        // part of the key, so if the file changes, the key in the html file
        // will be modified, and the browser will request a new version. Each
        // version can therefore be cached indefinitely.
        if (PluginConfig.cache == CachingStrategy.Change) {
            // Cache for a year
            response.setHeader("Cache-Control", "max-age=" + 31536000);
            response.setHeader("Expires", httpDateTimeFormatter.print(new DateTime().plusYears(1)));
            if(!PluginConfig.p3pHeader.isEmpty()) {
                response.setHeader("P3P", PluginConfig.p3pHeader);
            }
        }

        renderBinary(inputStream, compressedFile.name());
    }

    public static void clearJSCache() {
        if (!PluginConfig.cacheClearEnabled) {
            forbidden();
        }

        int count = ScriptRequestHandler.clearCache();
        renderText("Cleared " + count + " JS files from cache");
    }

    public static void clearCSSCache() {
        if (!PluginConfig.cacheClearEnabled) {
            forbidden();
        }

        int count = StyleRequestHandler.clearCache();
        renderText("Cleared " + count + " CSS files from cache");
    }

    private static void renderBadResponse(String key, String fileType) {
        String response = "/*\n";
        response += "The compressed " + fileType + " file could not be generated.\n";
        response += "This can occur in two situations:\n";
        response += "1. The time between when the page was rendered by the ";
        response += "server and when the browser requested the compressed ";
        response += "file was greater than the timeout. (The timeout is ";
        response += "currently configured to be ";
        response += PluginConfig.compressionKeyStorageTime + ")\n";
        response += "2. There was an exception thrown while rendering the ";
        response += "page.\n";
        response += "*/";
        logger.error("Bad response to " + key + " (" + fileType + "): " + response);
        renderBinaryResponse(response);
    }

    private static void renderBinaryResponse(String response) {
        try {
            renderBinary(new ByteArrayInputStream(response.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new UnexpectedException(e);
        }
    }
}
