package net.minecraftforge.gradle.mcp.mapping.providers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import org.gradle.api.Project;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.mcp.mapping.api.ICachableMappingFile;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingFileProvider;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.api.impl.SrgMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.utils.CacheUtils;

public class MappingFileProviderWrapper implements IMappingProvider {

    private final IMappingFileProvider provider;

    public MappingFileProviderWrapper(IMappingFileProvider provider) {
        this.provider = provider;
    }

    @Override
    public Collection<String> getMappingChannels() {
        return provider.getMappingChannels();
    }

    @Override
    public IMappingInfo getMappingInfo(Project project, String channel, String version) throws IOException {
        ICachableMappingFile mapping = provider.getMappingFile(project, channel, version);
        if (mapping == null) {
            return null;
        }

        //TODO: TEMP - Force Resolution
        mapping.get();
        /*
            Caused by: java.lang.RuntimeException: java.io.FileNotFoundException: C:\Users\Harry\.gradle\caches\forge_gradle\mcp_repo\net\minecraft\mapping_example\1.16.4\mapping_example-1.16.4-mapping.tsrg (The system cannot find the path specified)
                at net.minecraftforge.gradle.common.util.HashStore.add(HashStore.java:120)
                at net.minecraftforge.gradle.common.util.HashStore.add(HashStore.java:138)
                at net.minecraftforge.gradle.mcp.mapping.providers.MappingFileProviderWrapper.getMappingInfo(MappingFileProviderWrapper.java:44)
         */

        //TODO: Move to ICachableMappingFile
        String side = Objects.equals(channel, "official") ? "mapping" : "mappings_" + channel;

        File location = CacheUtils.cacheMC(project, side, version, "mapping", "zip");

        HashStore cache = CacheUtils.commonHash(project)
            .load(CacheUtils.cacheMC(project, side, version, "mapping", "zip.input"))
            .add(mapping.getFileLocation())
            .add("codever", "1");

        return new SrgMappingInfo(channel, version, location, cache, mapping);
    }

    @Override
    public String toString() {
        return provider.toString() + " Wrapper";
    }
}
