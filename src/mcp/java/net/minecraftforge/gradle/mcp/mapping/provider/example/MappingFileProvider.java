package net.minecraftforge.gradle.mcp.mapping.provider.example;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;

import org.apache.tools.ant.util.ReaderInputStream;
import org.gradle.api.Project;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.mcp.mapping.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.IMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.detail.MappingDetail;
import net.minecraftforge.gradle.mcp.mapping.provider.CachingProvider;
import net.minecraftforge.srgutils.IMappingFile;

/**
 * An example {@link IMappingProvider} that produces mappings via an {@link IMappingFile}.
 */
public class MappingFileProvider extends CachingProvider {
    @Override
    public Collection<String> getMappingChannels() {
        return Collections.singleton("example_file");
    }

    @Override
    public IMappingInfo getMappingInfo(Project project, String channel, String version) throws IOException {
        // Anything not in the mappings will end up as SRG named
        String mappings = "" +
            "tsrg2 left right\n" +
            "net/minecraft/client/Minecraft net/minecraft/client/Minecraft\n" +
            "\tfield_71432_P instance\n" +
            "\tfunc_71410_x ()Lnet/minecraft/client/Minecraft; getInstance\n";

        File destination = cacheMappings(project, channel, version, "zip");
        HashStore cache = commonHash(project)
            .load(cacheMappings(project, channel, version, "zip.input"))
            .add("mappings", mappings)
            .add("codever", "1");

        return fromCachable(channel, version, cache, destination, () ->
            MappingDetail.fromSrg(IMappingFile.load(new ReaderInputStream(new StringReader(mappings))))
        );
    }

    @Override
    public String toString() {
        return "Example IMappingFile";
    }
}
