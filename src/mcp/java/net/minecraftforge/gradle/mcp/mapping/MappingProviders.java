package net.minecraftforge.gradle.mcp.mapping;

import java.io.IOException;
import java.util.ServiceLoader;
import javax.annotation.Nullable;

import org.gradle.api.Project;

public class MappingProviders {
    private static final ServiceLoader<IMappingProvider> LOADER = ServiceLoader.load(IMappingProvider.class);

    public static IMappingInfo getInfo(Project project, String mapping) throws IOException {
        int idx = mapping.lastIndexOf('_');

        if (idx == -1) {
            throw new IllegalArgumentException("Invalid mapping format: " + mapping);
        }

        String channel = mapping.substring(0, idx);
        String version = mapping.substring(idx + 1);

        final IMappingProvider provider = MappingProviders.getProvider(project, channel);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown mapping provider: " + mapping);
        }

        final IMappingInfo info = provider.getMappingInfo(project, channel, version);
        if (info == null) {
            throw new IllegalArgumentException("Couldn't get mapping info: " + mapping);
        }

        return info;
    }

    @Nullable
    public static IMappingProvider getProvider(Project project, String channel) {
        //TODO: Change Logging Level

        project.getLogger().lifecycle("Looking for: " + channel);
        for (IMappingProvider provider : LOADER) {
            project.getLogger().lifecycle("Considering: " + provider + " provider");

            if (provider.getMappingChannels().contains(channel)) {
                return provider;
            }
        }

        return null;
    }
}
