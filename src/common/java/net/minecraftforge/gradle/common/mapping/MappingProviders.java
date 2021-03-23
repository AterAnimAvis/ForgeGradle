package net.minecraftforge.gradle.common.mapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import org.gradle.api.Project;
import net.minecraftforge.gradle.common.util.BaseRepo;
import net.minecraftforge.gradle.common.mapping.provider.McpMappingProvider;
import net.minecraftforge.gradle.common.mapping.provider.OfficialMappingProvider;

public class MappingProviders {

    /*
     * Can't use SPI due to Gradle's ClassLoading https://discuss.gradle.org/t/loading-serviceloader-providers-in-plugin-project-apply-project/28121
     */
    private static final List<IMappingProvider> PROVIDERS = Lists.newArrayList();

    static {
        /* The default ForgeGradle IMappingProviders */
        register(new McpMappingProvider(), new OfficialMappingProvider());
    }

    /**
     * Registers {@link IMappingProvider}s which will then be considered for resolution of a `mappings.zip`.
     */
    public static void register(IMappingProvider... providers) {
        PROVIDERS.addAll(Arrays.asList(providers));
    }

    /**
     * Unregisters an {@link IMappingProvider}
     */
    public static boolean unregister(IMappingProvider provider) {
        return PROVIDERS.remove(provider);
    }

    /**
     * TODO: DOCS
     */
    public static IMappingInfo getInfo(Project project, String mapping) throws IOException {
        int idx = mapping.lastIndexOf('_');

        if (idx == -1) {
            throw new IllegalArgumentException("Invalid mapping format: " + mapping);
        }

        String channel = mapping.substring(0, idx);
        String version = mapping.substring(idx + 1);

        return getInfo(project, channel, version);
    }

    /**
     * TODO: DOCS
     */
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

    public static String getMappingString(Project project, String channel, String version) {
        final IMappingProvider provider = MappingProviders.getProvider(project, channel);

        if (provider == null) return channel + "_" + version;

        return provider.getMappingString(project, channel, version);
    }

    /**
     * TODO: DOCS
     */
    @Nullable
    public static IMappingProvider getProvider(Project project, String channel) {
        debug(project, "Looking for: " + channel);
        for (IMappingProvider provider : PROVIDERS) {
            debug(project, "Considering: " + provider + " provider");

            if (provider.getMappingChannels().contains(channel)) {
                debug(project, "Selected: " + provider + " provider");
                return provider;
            }
        }

        return null;
    }

    /**
     * @return an Unmodifiable view of the registered Providers
     */
    public static Collection<IMappingProvider> getProviders() {
        return Collections.unmodifiableList(PROVIDERS);
    }

    private static void debug(Project project, String message) {
        if (BaseRepo.DEBUG) project.getLogger().lifecycle(message);
    }
}
