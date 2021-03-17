package net.minecraftforge.gradle.mcp.mapping.api;

import java.io.File;
import java.io.IOException;

public class MappingInfo implements IMappingInfo {
    protected final String channel;
    protected final String version;
    protected final File file;

    public MappingInfo(String channel, String version, File file) {
        this.channel = channel;
        this.version = version;
        this.file = file;
    }

    @Override
    public String getChannel() {
        return channel;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public File find() throws IOException {
        return file;
    }
}
