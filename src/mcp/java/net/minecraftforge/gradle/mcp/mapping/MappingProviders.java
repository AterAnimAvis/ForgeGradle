package net.minecraftforge.gradle.mcp.mapping;

import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import org.gradle.api.Project;
import net.minecraftforge.gradle.mcp.mapping.provider.McpMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.provider.OfficialMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.provider.example.MappingFileProvider;
import net.minecraftforge.gradle.mcp.mapping.provider.example.OverlaidProvider;

public class MappingProviders {
    //TODO: Migrate from SPI
    // https://discuss.gradle.org/t/loading-serviceloader-providers-in-plugin-project-apply-project/28121
    private static final ServiceLoader<IMappingProvider> LOADER = ServiceLoader.load(IMappingProvider.class);

    private static final List<IMappingProvider> PROVIDERS = Lists.newArrayList(
        new McpMappingProvider(),
        new OfficialMappingProvider(),
        new MappingFileProvider(),
        new OverlaidProvider()
    );

    public static IMappingInfo getInfo(Project project, String mapping) throws IOException {
        int idx = mapping.lastIndexOf('_');

        if (idx == -1) {
            throw new IllegalArgumentException("Invalid mapping format: " + mapping);
        }

        String channel = mapping.substring(0, idx);
        String version = mapping.substring(idx + 1);

        return getInfo(project, channel, version);
    }

    public static IMappingInfo getInfo(Project project, String channel, String version) throws IOException {
        String mapping = channel + "_" + version;

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

        project.getLogger().lifecycle("Looking for: " + channel + " via SPI");
        for (IMappingProvider provider : LOADER) {
            project.getLogger().lifecycle("Considering: " + provider + " provider");

            if (provider.getMappingChannels().contains(channel)) {
                return provider;
            }
        }

        project.getLogger().lifecycle("Looking for: " + channel);
        for (IMappingProvider provider : PROVIDERS) {
            project.getLogger().lifecycle("Considering: " + provider + " provider");

            if (provider.getMappingChannels().contains(channel)) {
                return provider;
            }
        }

        return null;
    }
}
