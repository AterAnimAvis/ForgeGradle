package net.minecraftforge.gradle.mcp.mapping.provider;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.google.common.collect.Lists;
import org.gradle.api.Project;
import net.minecraftforge.gradle.common.util.BaseRepo;
import net.minecraftforge.gradle.common.util.MavenArtifactDownloader;
import net.minecraftforge.gradle.mcp.mapping.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.IMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.info.MappingInfo;

public class McpMappingProvider implements IMappingProvider {

    //TODO: Move this up to IMappingProvider?
    private void debug(Project project, String message) {
        if (BaseRepo.DEBUG)
            project.getLogger().lifecycle(message);
    }

    @Override
    public Collection<String> getMappingChannels() {
        return Lists.newArrayList("snapshot", "snapshot_nodoc", "stable", "stable_nodoc");
    }

    @Override
    public IMappingInfo getMappingInfo(Project project, String channel, String version) throws IOException {
        String desc = "de.oceanlabs.mcp:mcp_" + channel + ":" + version + "@zip";

        debug(project, "    Mapping: " + desc);

        File destination = MavenArtifactDownloader.manual(project, desc, false);

        return new MappingInfo(channel, version, destination);
    }

    @Override
    public String toString() {
        return "MCP Community Mappings";
    }
}
