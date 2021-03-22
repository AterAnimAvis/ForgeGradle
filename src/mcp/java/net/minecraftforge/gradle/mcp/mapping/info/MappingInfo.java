package net.minecraftforge.gradle.mcp.mapping.info;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.function.IOSupplier;
import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.detail.MappingDetail;

public class MappingInfo implements IMappingInfo {

    protected final String channel;
    protected final String version;
    protected final File destination;
    protected final IOSupplier<IMappingDetail> detail;

    protected MappingInfo(String channel, String version, File destination, IOSupplier<IMappingDetail> detail) {
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
    public File get() {
        return destination;
    }

    @Override
    public IMappingDetail getDetails() throws IOException {
        return detail.get();
    }

    public static MappingInfo of(String channel, String version, File destination) {
        return of(channel, version, destination, () -> MappingDetail.fromZip(destination));
    }

    public static MappingInfo of(String channel, String version, File destination, IMappingDetail detail) {
        return of(channel, version, destination, () -> detail);
    }

    public static MappingInfo of(String channel, String version, File destination, IOSupplier<IMappingDetail> detail) {
        return new MappingInfo(channel, version, destination, detail);
    }
}
