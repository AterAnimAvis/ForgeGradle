package net.minecraftforge.gradle.mcp.mapping.provider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.gradle.api.Project;
import net.minecraftforge.gradle.common.config.MCPConfigV2;
import net.minecraftforge.gradle.common.util.HashFunction;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.MavenArtifactDownloader;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.gradle.mcp.mapping.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.detail.MappingDetail;
import net.minecraftforge.srgutils.IMappingFile;

public class OfficialMappingProvider extends CachingProvider {
    
    @Override
    public Collection<String> getMappingChannels() {
        return Collections.singleton("official");
    }

    @Override
    public IMappingInfo getMappingInfo(Project project, String channel, String version) throws IOException {
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

        File tsrgFile = findRenames(project, "obf_to_srg", IMappingFile.Format.TSRG, version, false);
        if (tsrgFile == null)
            throw new IllegalStateException("Could not create " + version + " official mappings due to missing MCP's tsrg");

        File mcp = getMCPConfigZip(project, version);
        if (mcp == null)
            return null; // TODO: handle when MCPConfig zip could not be downloaded

        File mappings = cacheMC(project, "mapping", version, "mapping", "zip");
        HashStore cache = commonHash(project, mcp)
            .load(cacheMC(project, "mapping", version, "mapping", "zip.input"))
            .add("pg_client", clientPG)
            .add("pg_server", serverPG)
            .add("tsrg", tsrgFile)
            .add("codever", "1");

        return fromCachable(channel, version, cache, mappings, () -> {
            // PG file: [MAP -> OBF]
            IMappingFile pgClient = IMappingFile.load(clientPG);
            IMappingFile pgServer = IMappingFile.load(serverPG);

            // MCPConfig TSRG file: [OBF -> SRG]
            IMappingFile tsrg = IMappingFile.load(tsrgFile);

            // Official: [SRG -> MAP]
            //   Note: We chain of the tsrg so that we don't pick up none srg names
            //   [OBF -> SRG] =reverse=> [SRG -> OBF]
            //   [MAP -> SRG] =reverse=> [SRG -> MAP]
            //   [SRG -> OBF] -chain-> [SRG -> MAP] ===> [SRG -> MAP]
            IMappingFile client = tsrg.reverse().chain(pgClient.reverse());
            IMappingFile server = tsrg.reverse().chain(pgServer.reverse());

            return MappingDetail.fromSrg(client, server);
        });
    }

    @Override
    public String toString() {
        return "Official Mappings";
    }

    private File findRenames(Project project, String classifier, IMappingFile.Format format, String version, boolean toObf) throws IOException {
        String ext = format.name().toLowerCase();
        //File names = findNames(version));
        File mcp = getMCPConfigZip(project, version);
        if (mcp == null)
            return null;

        File file = cacheMCP(project, version, classifier, ext);
        HashStore cache = commonHash(project, mcp).load(cacheMCP(project, version, classifier, ext + ".input"));

        if (!cache.isSame() || !file.exists()) {
            String name = MCPConfigV2.getFromArchive(mcp).getData("mappings");
            byte[] data = Utils.getZipData(mcp, name);
            IMappingFile obf_to_srg = IMappingFile.load(new ByteArrayInputStream(data));
            obf_to_srg.write(file.toPath(), format, toObf);
            cache.save();
            Utils.updateHash(file, HashFunction.SHA1);
        }

        return file;
    }

    //TODO: Move these up to `CachingProvider`
    private File getMCPConfigZip(Project project, String version) {
        return MavenArtifactDownloader.manual(project, "de.oceanlabs.mcp:mcp_config:" + version + "@zip", false);
    }

    private File cacheMC(Project project, String side, String version, String classifier, String ext) {
        if (classifier != null)
            return cache(project, "net", "minecraft", side, version, side + '-' + version + '-' + classifier + '.' + ext);
        return cache(project, "net", "minecraft", side, version, side + '-' + version + '.' + ext);
    }

    private File cacheMCP(Project project, String version, String classifier, String ext) {
        if (classifier != null)
            return cache(project, "de", "oceanlabs", "mcp", "mcp_config", version, "mcp_config-" + version + '-' + classifier + '.' + ext);
        return cache(project, "de", "oceanlabs", "mcp", "mcp_config", version, "mcp_config-" + version + '.' + ext);
    }

    private File cacheMCP(Project project, String version) {
        return cache(project, "de", "oceanlabs", "mcp", "mcp_config", version);
    }

    protected File cache(Project project, String... path) {
        return new File(Utils.getCache(project, "mcp_repo"), String.join(File.separator, path)); // TODO: remove hardcoded cache root
    }

    private HashStore commonHash(Project project, File mcp) {
        return new HashStore(Utils.getCache(project, "mcp_repo"))  // TODO: remove hardcoded cache root
            .add("mcp", mcp);
    }
}
