package press;

import play.PlayPlugin;
import play.cache.Cache;
import play.libs.Crypto;
import play.templates.JavaExtensions;
import play.vfs.VirtualFile;
import press.io.FileIO;

import java.util.*;
import java.util.Map.Entry;

public abstract class SourceFileManager extends PlayPlugin {

    // File type, eg "JavaScript"
    String fileType;

    // File extension, eg ".js"
    String extension;

    // Tag name, eg "#{press.script}"
    String tagName;

    // Compressed file tag name, eg "#{press.compressed-script}"
    String compressedTagName;

    // Directory where the source files are read from, eg "public/javascripts"
    String srcDir;

    // The key used to identify this request
    String requestKey = null;

    // The list of files compressed as part of this request
    Map<String, FileInfo> fileInfos;

    protected SourceFileManager(String fileType, String extension, String tagName, String compressedTagName, String srcDir) {
        this.fileInfos = new LinkedHashMap<>();
        this.fileType = fileType;
        this.extension = extension;
        this.tagName = tagName;
        this.compressedTagName = compressedTagName;
        this.srcDir = PluginConfig.addTrailingSlash(srcDir);
    }

    public String getTagName() {
        return tagName;
    }

    public String getFileType() {
        return fileType;
    }

    /**
     * Adds a file to the list of files to be compressed
     */
    public void add(String fileName, boolean compress) {
        if (compress) {
            PressLogger.trace("Adding %s to output", fileName);
        } else {
            PressLogger.trace("Adding uncompressed file %s to output", fileName);
        }

        if (fileInfos.containsKey(fileName)) {
            throw new DuplicateFileException(fileType, fileName, tagName);
        }

        // Add the file to the list of files to be compressed
        fileInfos.put(fileName, new FileInfo(compress, checkFileExists(fileName)));
    }

    /**
     * This must only be called once, as it indicates that the file is ready to
     * be output
     * 
     * @return the request key used to retrieve the compressed file
     */
    public String closeRequest() {
        if (requestKey != null) {
            String msg = "There is more than one " + compressedTagName
                    + " tag in the template output. " + "There must be one only.";
            throw new PressException(msg);
        }
        requestKey = getRequestKey(fileInfos);

        PressLogger
                .trace("Adding key %s for compression of %d files", requestKey, fileInfos.size());
        return requestKey;
    }

    /**
     * The request key is is derived from the list of files - for the same list
     * of files we should always return the same compressed javascript or css.
     */
    public String getRequestKey(Map<String, FileInfo> fileInfoMap) {
        String key = "";
        for (Entry<String, FileInfo> entry : fileInfoMap.entrySet()) {
            key += entry.getKey();
            // If we use the 'Change' caching strategy, make the modified
            // timestamp of each file part of the key.
            if (PluginConfig.cache == CachingStrategy.Change) {
                key += entry.getValue().getLastModified();
            }
        }

        // Get a hash of the url to keep it short
        String hashed = Crypto.passwordHash(key);
        return FileIO.lettersOnly(hashed) + extension;
    }

    public void saveFileList() {
        // If the request key has not been set, that means there was no request
        // for compressed source anywhere in the template file, so we don't
        // need to generate anything
        if (requestKey == null) {
            // If the file list is not empty, then there have been files added
            // to compression but they will not be output. So throw an
            // exception telling the user he needs to add some files.
            if (!fileInfos.isEmpty()) {
                String msg = fileInfos.size() + " files added to compression with ";
                msg += tagName + " tag but no " + compressedTagName + " tag was found. ";
                msg += "You must include a " + compressedTagName + " tag in the template ";
                msg += "to output the compressed content of these files: ";
                msg += JavaExtensions.join(fileInfos.keySet(), ", ");

                throw new PressException(msg);
            }

            return;
        }

        // Add the list of files to the cache.
        // When the server receives a request for the compressed file, it will
        // retrieve the list of files and compress them.
        addFileListToCache(requestKey, fileInfos.values());
    }

    public static void addFileListToCache(String cacheKey, Collection<FileInfo> originalList) {
        // Clone the file list
        List<FileInfo> newList = new ArrayList<>(originalList);

        // Add a mapping between the request key and the list of files that
        // are compressed for the request
        Cache.safeSet(cacheKey, newList, PluginConfig.compressionKeyStorageTime);
    }

    public String addSingleFile(String fileName, boolean compress) {
        VirtualFile file = checkFileExists(fileName);
        Map<String, FileInfo> files = new HashMap<String, FileInfo>(1);
        files.put(fileName, new FileInfo(compress, file));
        String cacheKey = getRequestKey(files);
        addFileListToCache(cacheKey, files.values());
        return cacheKey;
    }

    /**
     * Gets the file with the given name. If the file does not exist in the
     * source directory, throws an exception.
     */
    public VirtualFile checkFileExists(String fileName) {
        return FileIO.checkFileExists(fileName, srcDir);
    }

    /**
     * Gets the the list of source files for the given request key
     */
    public static List<FileInfo> getSourceFiles(String key) {
        return (List<FileInfo>) Cache.get(key);
    }
}
