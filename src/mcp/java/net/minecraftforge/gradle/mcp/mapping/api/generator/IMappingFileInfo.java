package net.minecraftforge.gradle.mcp.mapping.api.generator;

import java.util.Collection;

import net.minecraftforge.gradle.mcp.mapping.api.Constants;
import net.minecraftforge.srgutils.IMappingFile;

//TODO: Raise this up to ICachableMappingFile ?
public interface IMappingFileInfo {

    default String getJavadocForNode(IMappingFile.INode node) {
        return node.getMetadata().getOrDefault("comment", "");
    }

    default String getSideForNode(IMappingFile.INode node) {
        String side = node.getMetadata().getOrDefault("side", Constants.SIDE_BOTH);

        if (!Constants.SIDE_CLIENT.equals(side) && !Constants.SIDE_SERVER.equals(side) && !Constants.SIDE_BOTH.equals(side))
            return Constants.SIDE_BOTH;

        return side;
    }

    Collection<IMappingFile.IClass> getClasses();

    Collection<IMappingFile.IField> getFields();

    Collection<IMappingFile.IMethod> getMethods();

    Collection<IMappingFile.IParameter> getParameters();

}
