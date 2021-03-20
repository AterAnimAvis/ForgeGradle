package net.minecraftforge.gradle.mcp.mapping.api;

import java.io.IOException;
import java.util.Collection;

import org.gradle.api.Project;
import net.minecraftforge.gradle.mcp.mapping.providers.MappingFileProviderWrapper;
import net.minecraftforge.srgutils.IMappingFile;

/**
 * <p>
 *     SPI Interface mainly meant for providing a {@link IMappingFile} <br>
 *     See {@link IMappingProvider} if you have a zip file instead.
 * </p>
 */
public interface IMappingFileProvider {
    Collection<String> getMappingChannels();

    ICachableMappingFile getMappingFile(Project project, String channel, String version) throws IOException;

    default IMappingProvider wrap() {
        return new MappingFileProviderWrapper(this);
    }
}
