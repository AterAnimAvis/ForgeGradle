package net.minecraftforge.gradle.mcp.mapping.api;

import java.io.IOException;
import java.util.Collection;

import org.gradle.api.Project;
import net.minecraftforge.srgutils.IMappingFile;

/**
 * SPI Interface mainly meant for providing a `mappings.zip` <br/>
 * See {@link IMappingFileProvider} if you have an {@link IMappingFile} instead.
 */
public interface IMappingProvider {
    Collection<String> getMappingChannels();

    IMappingInfo getMappingInfo(Project project, String channel, String version) throws IOException;
}
