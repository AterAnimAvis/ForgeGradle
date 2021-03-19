package net.minecraftforge.gradle.mcp.mapping;

import java.io.IOException;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import org.gradle.api.Project;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingProvider;

public class MappingProviders {
    private static final ServiceLoader<IMappingProvider> LOADER = ServiceLoader.load(IMappingProvider.class);

    public static IMappingInfo getInfo(Project project, String mapping) throws IOException {
        int idx = mapping.lastIndexOf('_');

        if (idx == -1) {
            throw new IllegalArgumentException("Unknown mapping format for: " + mapping);
        }

        String channel = mapping.substring(0, idx);
        String version = mapping.substring(idx + 1);

        final IMappingProvider provider = MappingProviders.getProvider(project, channel);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown mapping provider for channel: " + channel);
        }

        final IMappingInfo info = provider.getMappingInfo(project, channel, version);
        if (info == null) {
            throw new IllegalArgumentException("Couldn't get mapping info: " + mapping);
        }

        return info;
    }

    @Nullable
    public static IMappingProvider getProvider(Project project, String channel) {
        Consumer<String> log = (msg) -> project.getLogger().lifecycle(msg);

        log.accept("Finding Provider for " + channel);
        for (IMappingProvider provider : LOADER) {
            log.accept("Considering Provider: " + provider.toString());
            if (provider.getMappingChannels().contains(channel)) {
                return provider;
            }
        }
        return null;
    }
}
