package net.minecraftforge.gradle.mcp.mapping;

import javax.annotation.Nullable;
import java.util.ServiceLoader;

import net.minecraftforge.gradle.mcp.mapping.api.IMappingProvider;

public class MappingProviders {
    private static final ServiceLoader<IMappingProvider> LOADER = ServiceLoader.load(IMappingProvider.class);

    @Nullable
    public static IMappingProvider getProvider(String channel) {
        for (IMappingProvider provider : LOADER) {
            if (provider.getMappingChannels().contains(channel)) {
                return provider;
            }
        }
        return null;
    }
}
