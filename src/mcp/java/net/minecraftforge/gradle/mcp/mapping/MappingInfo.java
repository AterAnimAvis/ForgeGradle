package net.minecraftforge.gradle.mcp.mapping;

import net.minecraftforge.srgutils.IMappingFile;

public class MappingInfo implements IMappingInfo {
    private final String channel;
    private final String version;
    private final IMappingFile mapping;

    public MappingInfo(String channel, String version, IMappingFile mapping) {
        this.channel = channel;
        this.version = version;
        this.mapping = mapping;
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
    public IMappingFile getMappings() {
        return mapping;
    }
}
