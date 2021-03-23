package net.minecraftforge.gradle.common.mapping;

import java.io.IOException;
import java.util.Collection;

import org.gradle.api.Project;

public interface IMappingProvider {

    /**
     * Channels should match the regex of [a-z_]+
     * @return The collection of channels that this provider handles.
     */
    Collection<String> getMappingChannels();

    /**
     * TODO: DOCS
     * Channels should match the regex of [a-z_]+
     * Versions should match the regex of [0-9a-z-.]+ (not enforced but may cause problems)
     * @param project The current gradle project
     * @param channel The requested channel
     * @param version The requested version
     * @return An enhanced Supplier for the location of the `mappings.zip`
     * @throws IOException
     */
    IMappingInfo getMappingInfo(Project project, String channel, String version) throws IOException;

    /**
     * TODO: DOCS MARK AS IMPORTANT FOR CACHING UPSTREAM
     * @param channel
     * @param version
     * @return
     */
    default String getMappingString(Project project, String channel, String version) {
        return channel + "_" + version;
    }
}
