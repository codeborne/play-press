package press;

import play.mvc.Router;
import play.vfs.VirtualFile;
import press.io.FileIO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class RequestHandler {
    private final Set<String> files = new HashSet<>();

    abstract String getTag(String src, String... args);

    abstract SourceFileManager getSourceManager();

    abstract CompressedFileManager getCompressedFileManager();

    abstract String getCompressedUrl(String requestKey);

    abstract String getSingleFileCompressionKey(String fileName);

    protected String getSingleFileCompressionKey(String fileName, SourceFileManager tmpManager) {
        PressLogger.trace("Request to compress single file %s", fileName);
        return tmpManager.addSingleFile(fileName, true);
    }

    public String getSrcDir() {
        return getSourceManager().srcDir;
    }

    public VirtualFile checkFileExists(String fileName) {
        return getSourceManager().checkFileExists(fileName);
    }

    public void add(String fileName, boolean packFile) {
        getSourceManager().add(fileName, packFile);
    }

    public void saveFileList() {
        getSourceManager().saveFileList();
    }

    public String closeRequest() {
        return getSourceManager().closeRequest();
    }

    protected void checkForDuplicates(String fileName) {
        if (!files.contains(fileName)) {
            files.add(fileName);
            return;
        }

        SourceFileManager srcManager = getSourceManager();
        throw new DuplicateFileException(srcManager.getFileType(), fileName, srcManager.getTagName());
    }

    protected static String getCompressedUrl(String action, String requestKey) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key", FileIO.escape(requestKey));
        return Router.reverse(action, params).url;
    }
}
