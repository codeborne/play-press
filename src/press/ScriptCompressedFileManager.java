package press;

public class ScriptCompressedFileManager extends CompressedFileManager {
    public ScriptCompressedFileManager() {
        super(new ScriptCompressor());
    }

    @Override public String getCompressedDir() {
        return PluginConfig.js.compressedDir;
    }
}
