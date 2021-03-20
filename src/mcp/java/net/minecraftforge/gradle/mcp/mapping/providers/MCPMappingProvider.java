package net.minecraftforge.gradle.mcp.mapping.providers;

import java.util.Collection;

import com.google.common.collect.Lists;
import org.gradle.api.Project;
import net.minecraftforge.gradle.common.util.BaseRepo;
import net.minecraftforge.gradle.common.util.MavenArtifactDownloader;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.api.impl.MappingInfo;

public class MCPMappingProvider implements IMappingProvider {

    private void debug(Project project, String message) {
        if (BaseRepo.DEBUG)
            project.getLogger().lifecycle(message);
    }

    @Override
    public Collection<String> getMappingChannels() {
        return Lists.newArrayList("snapshot", "snapshot_nodoc", "stable", "stable_nodoc");
    }

    @Override
    public IMappingInfo getMappingInfo(Project project, String channel, String version) {
        String desc = "de.oceanlabs.mcp:mcp_" + channel + ":" + version + "@zip";

        debug(project, "    Mapping: " + desc);

        return new MappingInfo(channel, version, MavenArtifactDownloader.manual(project, desc, false));
    }

    @Override
    public String toString() {
        return "MCP Community Mappings";
    }
}