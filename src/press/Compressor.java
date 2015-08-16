package press;

import press.io.CompressedFile;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public abstract class Compressor {
    /**
     * A key unique for the list of component files and their last modified date
     */
    public abstract String getCompressedFileKey(List<FileInfo> componentFiles);

    public abstract void compress(File file, Writer out, boolean compress) throws IOException;
    
    protected static int clearCache(String compressedDir, String extension) {
        return CompressedFile.clearCache(compressedDir, extension);
    }
}
