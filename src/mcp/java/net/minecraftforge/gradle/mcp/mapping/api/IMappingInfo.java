package net.minecraftforge.gradle.mcp.mapping.api;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import net.minecraftforge.gradle.mcp.mapping.utils.MappingStreams;
import net.minecraftforge.srgutils.IMappingFile;

public interface IMappingInfo {

    String SIDE_CLIENT = "0";
    String SIDE_SERVER = "1";
    String SIDE_BOTH = "2";

    String getChannel();

    String getVersion();

    /**
     * @return IMappingFile mapping from Srg -> Mapped.
     */
    IMappingFile getMappings();

    default String getJavadocForNode(IMappingFile.INode node) {
        return node.getMetadata().getOrDefault("comment", "");
    }

    default String getSideForNode(IMappingFile.INode node) {
        String side = node.getMetadata().getOrDefault("side", SIDE_BOTH);

        if (!SIDE_CLIENT.equals(side) && !SIDE_SERVER.equals(side) && !SIDE_BOTH.equals(side))
            return SIDE_BOTH;

        return side;
    }

    default Collection<IMappingFile.IClass> getClasses() {
        return Collections.unmodifiableCollection(getMappings().getClasses());
    }

    default Collection<IMappingFile.IField> getFields() {
        return MappingStreams.getFields(getMappings()).collect(Collectors.toList());
    }

    default Collection<IMappingFile.IMethod> getMethods() {
        return MappingStreams.getMethods(getMappings()).collect(Collectors.toList());
    }

    default Collection<IMappingFile.IParameter> getParameters() {
        return MappingStreams.getParameters(getMappings()).collect(Collectors.toList());
    }

}
