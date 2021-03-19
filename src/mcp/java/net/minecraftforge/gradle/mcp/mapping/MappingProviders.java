package net.minecraftforge.gradle.mcp.mapping;

import java.util.ServiceLoader;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import org.gradle.api.Project;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingProvider;

public class MappingProviders {
    private static final ServiceLoader<IMappingProvider> LOADER = ServiceLoader.load(IMappingProvider.class);

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
