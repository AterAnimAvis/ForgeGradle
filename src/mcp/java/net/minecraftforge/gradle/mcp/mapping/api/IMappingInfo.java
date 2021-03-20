package net.minecraftforge.gradle.mcp.mapping.api;

import java.io.File;
import java.io.IOException;

import net.minecraftforge.srgutils.IMappingFile;

public interface IMappingInfo {

    String getChannel();

    String getVersion();

    File find() throws IOException;

    IMappingFile applyMappings(IMappingFile input) throws IOException;

}
