package net.minecraftforge.gradle.mcp.mapping;

import java.io.IOException;
import java.util.Collection;

import org.gradle.api.Project;

public interface IMappingProvider {
    /**
     * @return The collection of channels that this provider handles.
     */
    Collection<String> getMappingChannels();

    /**
     * TODO: DOCS
     * @param project The current gradle project
     * @param channel The requested channel
     * @param version The requested version
     * @return An enhanced Supplier for the location of the `mappings.zip`
     * @throws IOException
     */
    IMappingInfo getMappingInfo(Project project, String channel, String version) throws IOException;
}
