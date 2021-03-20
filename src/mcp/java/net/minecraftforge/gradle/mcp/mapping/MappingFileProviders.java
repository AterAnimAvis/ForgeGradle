package net.minecraftforge.gradle.mcp.mapping;

import java.io.IOException;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import org.gradle.api.Project;
import net.minecraftforge.gradle.mcp.mapping.api.ICachableMappingFile;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingFileProvider;
import net.minecraftforge.gradle.mcp.mapping.providers.file.ExampleMappingFileProvider;
import net.minecraftforge.gradle.mcp.mapping.providers.file.OfficialMappingFileProvider;

public class MappingFileProviders {
    private static final ServiceLoader<IMappingFileProvider> LOADER = ServiceLoader.load(IMappingFileProvider.class);

    public static ICachableMappingFile getFile(Project project, String mapping) throws IOException {
        int idx = mapping.lastIndexOf('_');

        if (idx == -1) {
            throw new IllegalArgumentException("Unknown mapping format for: " + mapping);
        }

        String channel = mapping.substring(0, idx);
        String version = mapping.substring(idx + 1);

        return getFile(project, channel, version);
    }

    public static ICachableMappingFile getFile(Project project, String channel, String version) throws IOException {
        final IMappingFileProvider provider = get(project, channel);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown mapping provider for channel: " + channel);
        }

        final ICachableMappingFile file = provider.getMappingFile(project, channel, version);
        if (file == null) {
            throw new IllegalArgumentException("Couldn't get mapping: " + channel + "_" + version);
        }

        return file;
    }

    @Nullable
    public static IMappingFileProvider get(Project project, String channel) {
        Consumer<String> log = (msg) -> project.getLogger().lifecycle(msg);

        log.accept("Finding Provider for " + channel);
        for (IMappingFileProvider provider : LOADER) {
            log.accept("Considering Provider: " + provider.toString());
            if (provider.getMappingChannels().contains(channel)) {
                return provider;
            }
        }

        for (IMappingFileProvider provider : Lists.newArrayList(new ExampleMappingFileProvider(), new OfficialMappingFileProvider())) {
            log.accept("Considering Fixed Providers: " + provider.toString());
            if (provider.getMappingChannels().contains(channel)) {
                return provider;
            }
        }

        return null;
    }
}
