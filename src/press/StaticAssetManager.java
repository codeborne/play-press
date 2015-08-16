package press;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.utils.Utils;
import play.vfs.VirtualFile;

import java.util.Date;

/**
 * Copied and modified from
 * https://github.com/lunatech-labs/play-module-less/blob
 * /master/src/play/modules/less/Plugin.java
 */
public class StaticAssetManager {
    private static final Logger logger = LoggerFactory.getLogger(StaticAssetManager.class);

    long startTime = System.currentTimeMillis();
    private PlayLessEngine lessEngine = StyleCompressor.lessEngine;

    public boolean serveStatic(VirtualFile file, Request request, Response response) {
        if (file.getName().endsWith(".less")) {
            if (PluginConfig.outputRawLess) {
                response.contentType = "text/less";
                return false;
            }

            response.contentType = "text/css";
            try {
                handleResponse(file, request, response);
            } catch (Exception e) {
              response.status = 500;
              response.print("LESS processing failed");
              logger.error("LESS processing failed", e);
            }
            return true;
        }
        return false;
    }

    private void handleResponse(VirtualFile file, Request request, Response response) {
        long lastModified = lessEngine.latestModified(file.getRealFile());
        final String etag = "\"" + lastModified + "-" + file.hashCode() + "\"";

        // If we're in dev mode, and the server was just restarted, reprocess
        // the file because if config changed it can affect how the file is
        // rendered.
        boolean reprocessFile = Play.mode.isDev() && startTime > lastModified;
        if (request.isModified(etag, lastModified) || reprocessFile) {
            handleOk(request, response, file, etag, lastModified);
        } else {
            handleNotModified(request, response, etag);
        }
    }

    private void handleNotModified(Request request, Response response, String etag) {
        if ("GET".equals(request.method)) {
            response.status = Http.StatusCode.NOT_MODIFIED;
        }
        response.setHeader("ETag", etag);
    }

    private void handleOk(Request request, Response response, VirtualFile file, String etag, long lastModified) {
        response.status = 200;
        response.print(lessEngine.get(file.getRealFile(), false));
        response.setHeader("Last-Modified",
                Utils.getHttpDateFormatter().format(new Date(lastModified)));
        response.setHeader("ETag", etag);
    }
}
