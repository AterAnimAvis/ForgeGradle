package net.minecraftforge.gradle.mcp.mapping.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.gradle.api.Project;
import net.minecraftforge.gradle.common.config.MCPConfigV2;
import net.minecraftforge.gradle.common.util.HashFunction;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.MavenArtifactDownloader;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.srgutils.IMappingFile;

public class CacheUtils {

    public static File findRenames(Project project, String classifier, IMappingFile.Format format, String version, boolean toObf) throws IOException {
        String ext = format.name().toLowerCase();
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

    public static File getMCPConfigZip(Project project, String version) {
        return MavenArtifactDownloader.manual(project, "de.oceanlabs.mcp:mcp_config:" + version + "@zip", false);
    }

    public static File cacheMC(Project project, String side, String version, String classifier, String ext) {
        if (classifier != null)
            return cache(project, "net", "minecraft", side, version, side + '-' + version + '-' + classifier + '.' + ext);
        return cache(project, "net", "minecraft", side, version, side + '-' + version + '.' + ext);
    }

    public static File cacheMCP(Project project, String version, String classifier, String ext) {
        if (classifier != null)
            return cache(project, "de", "oceanlabs", "mcp", "mcp_config", version, "mcp_config-" + version + '-' + classifier + '.' + ext);
        return cache(project, "de", "oceanlabs", "mcp", "mcp_config", version, "mcp_config-" + version + '.' + ext);
    }

    public static File cache(Project project, String... path) {
        return new File(Utils.getCache(project, "mcp_repo"), String.join(File.separator, path)); // TODO: remove hardcoded cache root
    }

    public static HashStore commonHash(Project project) {
        return new HashStore(Utils.getCache(project, "mcp_repo"));  // TODO: remove hardcoded cache root
    }

    public static HashStore commonHash(Project project, File mcp) {
        return new HashStore(Utils.getCache(project, "mcp_repo"))  // TODO: remove hardcoded cache root
            .add("mcp", mcp);
    }

}
