package net.minecraftforge.gradle.mcp.mapping.providers.file;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.mcp.mapping.MappingFileProviders;
import net.minecraftforge.gradle.mcp.mapping.api.ICachableMappingFile;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingFileProvider;
import net.minecraftforge.gradle.mcp.mapping.api.impl.CachableMappingFile;
import net.minecraftforge.gradle.mcp.mapping.utils.CacheUtils;
import net.minecraftforge.srgutils.IMappingFile;
import net.minecraftforge.srgutils.IRenamer;

public class ExampleMappingFileProvider implements IMappingFileProvider {

    @Override
    public Collection<String> getMappingChannels() {
        return Collections.singleton("example");
    }

    @Override
    public ICachableMappingFile getMappingFile(Project project, String channel, String version) throws IOException {
        ICachableMappingFile official = MappingFileProviders.getFile(project, "official", version);

        //TODO: TEMP - Force Resolution
        official.get();

        File mappings = CacheUtils.cacheMC(project, "mappings_example", version, "mapping", "tsrg");

        //==============================================================================================================

        Map<String, String> provided_mappings = new HashMap<>();
        provided_mappings.put("p_i45547_1_", "configurationIn");

        //==============================================================================================================

        HashStore cache = CacheUtils.commonHash(project)
            .load(CacheUtils.cacheMC(project, "mappings_example", version, "mapping", "tsrg.input"))
            .add(official.getFileLocation())
            .add("provided", "" + provided_mappings.hashCode())
            .add("codever", "1");

        project.getLogger().lifecycle("Generating Example Mappings");

        return new CachableMappingFile(channel, version, mappings, cache, (destination) -> {
            project.getLogger().lifecycle("Generating Example Mappings Step 2");

            IMappingFile generated = official.get().rename(new IRenamer() {
                @Override public String rename(IMappingFile.IParameter value) {
                    if (value.getOriginal().contains("i45547") || value.getMapped().contains("i45547"))
                        project.getLogger().lifecycle(value.getOriginal() + " " + value.getMapped());

                    return provided_mappings.getOrDefault(value.getOriginal(), value.getMapped());
                }
            });

            generated.write(destination, IMappingFile.Format.TSRG2, false);
        });
    }

    @Override
    public String toString() {
        return "Example Mappings";
    }
}
