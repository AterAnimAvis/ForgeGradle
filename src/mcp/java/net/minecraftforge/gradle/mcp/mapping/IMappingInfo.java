package net.minecraftforge.gradle.mcp.mapping;

import java.io.File;
import java.io.IOException;

public interface IMappingInfo {
    String getChannel();

    String getVersion();

    File get() throws IOException;

    File getDestination();

    IMappingDetail getDetails() throws IOException;
}
