package press;

public class StyleCompressedFileManager extends CompressedFileManager {
    public StyleCompressedFileManager() {
        super(new StyleCompressor());
    }

    @Override public String getCompressedDir() {
        return PluginConfig.css.compressedDir;
    }
}
