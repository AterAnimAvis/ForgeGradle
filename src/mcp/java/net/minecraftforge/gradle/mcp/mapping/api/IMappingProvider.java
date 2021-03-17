package net.minecraftforge.gradle.mcp.mapping.api;

import java.util.Collection;

import org.gradle.api.Project;

public interface IMappingProvider {
    Collection<String> getMappingChannels();

    IMappingInfo getMappingInfo(Project project, String channel, String version) throws Exception;
}
