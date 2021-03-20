package net.minecraftforge.gradle.mcp.mapping.info;

import java.io.File;
import java.io.IOException;

import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.detail.MappingDetail;

public class MappingInfo implements IMappingInfo {

    protected final String channel;
    protected final String version;
    protected final File destination;
    protected final IMappingDetail detail;

    public MappingInfo(String channel, String version, File destination) throws IOException {
        this(channel, version, destination, MappingDetail.fromZip(destination));
    }

    public MappingInfo(String channel, String version, File destination, IMappingDetail detail) {
        this.channel = channel;
        this.version = version;
        this.destination = destination;
        this.detail = detail;
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
    public File get() throws IOException {
        return destination;
    }

    @Override
    public File getDestination() {
        return destination;
    }

    @Override
    public IMappingDetail getDetails() {
        return detail;
    }
}
