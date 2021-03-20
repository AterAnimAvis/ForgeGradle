package net.minecraftforge.gradle.mcp.mapping.api;

import java.io.File;
import java.io.IOException;

import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.func.IOSupplier;
import net.minecraftforge.srgutils.IMappingFile;

public interface ICachableMappingFile extends IOSupplier<IMappingFile> {

    String getChannel();

    String getVersion();

    File getFileLocation();

}
