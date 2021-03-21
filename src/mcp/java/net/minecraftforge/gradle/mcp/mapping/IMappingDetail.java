package net.minecraftforge.gradle.mcp.mapping;

import java.util.Map;

public interface IMappingDetail {

    Map<String, INode> getClasses();

    Map<String, INode> getFields();

    Map<String, INode> getMethods();

    Map<String, INode> getParameters();

    interface INode {
        String getOriginal();

        String getMapped();

        String getSide();

        String getJavadoc();

        INode withMapping(String mapped);

        INode withSide(String side);

        INode withJavadoc(String javadoc);
    }
}
