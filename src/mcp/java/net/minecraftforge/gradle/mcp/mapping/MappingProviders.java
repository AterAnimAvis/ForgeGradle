package net.minecraftforge.gradle.mcp.mapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import org.gradle.api.Project;
import net.minecraftforge.gradle.common.util.BaseRepo;
import net.minecraftforge.gradle.mcp.mapping.provider.McpMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.provider.OfficialMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.provider.example.MappingFileProvider;
import net.minecraftforge.gradle.mcp.mapping.provider.example.OverlaidProvider;

public class MappingProviders {

    /*
     * Can't use SPI due to Gradle's ClassLoading https://discuss.gradle.org/t/loading-serviceloader-providers-in-plugin-project-apply-project/28121
     */
    private static final Set<IMappingProvider> PROVIDERS = Sets.newHashSet();

    static {
        /* The default ForgeGradle IMappingProviders */
        MappingProviders.register(new McpMappingProvider(), new OfficialMappingProvider());

        /* Example IMappingProviders - TODO: Move to Separate Plugin */
        MappingProviders.register(new MappingFileProvider(), new OverlaidProvider());
    }

    public static void register(IMappingProvider... providers) {
        PROVIDERS.addAll(Arrays.asList(providers));
    }

    public static boolean unregister(IMappingProvider provider) {
        return PROVIDERS.remove(provider);
    }

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

    private static void debug(Project project, String message) {
        if (BaseRepo.DEBUG) project.getLogger().lifecycle(message);
    }

    @Nullable
    public static IMappingProvider getProvider(Project project, String channel) {
        debug(project, "Looking for: " + channel);
        for (IMappingProvider provider : PROVIDERS) {
            debug(project, "Considering: " + provider + " provider");

            if (provider.getMappingChannels().contains(channel)) {
                return provider;
            }
        }

        return null;
    }
}
