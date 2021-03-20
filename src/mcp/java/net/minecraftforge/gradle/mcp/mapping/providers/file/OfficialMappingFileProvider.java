package net.minecraftforge.gradle.mcp.mapping.providers.file;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.gradle.api.Project;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.MavenArtifactDownloader;
import net.minecraftforge.gradle.mcp.mapping.api.ICachableMappingFile;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingFileProvider;
import net.minecraftforge.gradle.mcp.mapping.api.impl.CachableMappingFile;
import net.minecraftforge.gradle.mcp.mapping.utils.CacheUtils;
import net.minecraftforge.gradle.mcp.mapping.utils.MappingMerger;
import net.minecraftforge.srgutils.IMappingFile;

public class OfficialMappingFileProvider implements IMappingFileProvider {
    @Override
    public Collection<String> getMappingChannels() {
        return Collections.singleton("official");
    }

    @Override
    public ICachableMappingFile getMappingFile(Project project, String channel, String version) throws IOException {
        String mcVersion = version;
        int idx = mcVersion.lastIndexOf('-');
        if (idx != -1 && mcVersion.substring(idx + 1).matches("\\d{8}\\.\\d{6}")) {
            // The regex matches a timestamp attached to the version, like 1.16.5-20210101.010101
            // This removes the timestamp part, so mcVersion only contains the minecraft version (for getting the mappings)
            mcVersion = mcVersion.substring(0, idx);
        }

        File clientPG = MavenArtifactDownloader.generate(project, "net.minecraft:client:" + mcVersion + ":mappings@txt", true);
        File serverPG = MavenArtifactDownloader.generate(project, "net.minecraft:server:" + mcVersion + ":mappings@txt", true);
        if (clientPG == null || serverPG == null)
            throw new IllegalStateException("Could not create " + version + " official mappings due to missing ProGuard mappings.");

        File tsrgFile = CacheUtils.findRenames(project, "obf_to_srg", IMappingFile.Format.TSRG, version, false);
        if (tsrgFile == null)
            throw new IllegalStateException("Could not create " + version + " official mappings due to missing MCP's tsrg");

        File mcp = CacheUtils.getMCPConfigZip(project, version);
        if (mcp == null)
            return null; // TODO: handle when MCPConfig zip could not be downloaded

        File mappings = CacheUtils.cacheMC(project, "mapping", version, "mapping", "tsrg");
        HashStore cache = CacheUtils.commonHash(project, mcp)
            .load(CacheUtils.cacheMC(project, "mapping", version, "mapping", "tsrg.input"))
            .add("pg_client", clientPG)
            .add("pg_server", serverPG)
            .add("tsrg", tsrgFile)
            .add("codever", "1");

        return new CachableMappingFile(channel, version, mappings, cache, (destination) -> {
            // Note: IMappingFile from PG file has getMapped() as obfuscated name and getOriginal() as original name
            IMappingFile pgClient = IMappingFile.load(clientPG);
            IMappingFile pgServer = IMappingFile.load(serverPG);

            //Verify that the PG files merge, merge in MCPConfig, but doesn't hurt to double check here.
            //And if we don't we need to write a handler to spit out correctly sided info.

            // MCPConfig TSRG file: OBF -> SRG
            IMappingFile tsrg = IMappingFile.load(tsrgFile);

            // [MAPPED -> OBF] -chain-> [OBF -> SRG] => [MAPPED -> SRG] =reverse=> [SRG -> MAPPED]
            IMappingFile client = pgClient.chain(tsrg).reverse();
            IMappingFile server = pgServer.chain(tsrg).reverse();

            // merge([MAPPED -> OBF] =reverse=> [OBF -> MAPPED])
            IMappingFile merged = MappingMerger.merge(pgClient.reverse(), pgServer.reverse());

            // [OBF -> SRG] =reverse=> [SRG-OBF] -chain-> [OBF->MAPPED] => [SRG-MAPPED]
            IMappingFile mapped = tsrg.reverse().chain(merged);

            IMappingFile sided = MappingMerger.mergeSided(mapped, client, server);

            sided.write(destination, IMappingFile.Format.TSRG2, false);
        });
    }

    @Override
    public String toString() {
        return "Official Mappings";
    }
}
