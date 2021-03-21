package net.minecraftforge.gradle.mcp.mapping;

import java.util.Map;

public interface IMappingDetail {

    Map<String, IDocumentedNode> getClasses();

    Map<String, IDocumentedNode> getFields();

    Map<String, IDocumentedNode> getMethods();

    Map<String, INode> getParameters();

    interface INode {
        String getOriginal();

        String getMapped();

        String getSide();

        INode withMapping(String mapped);

        INode withSide(String side);
    }

    interface IDocumentedNode extends INode {
        String getJavadoc();

        @Override
        IDocumentedNode withMapping(String mapped);

        @Override
        IDocumentedNode withSide(String side);

        IDocumentedNode withJavadoc(String javadoc);
    }
}
