package net.minecraftforge.gradle.mcp.mapping;

import java.util.Collection;
import javax.annotation.Nullable;

//TODO: Consider moving these to Map<String, INode>
public interface IMappingDetail {

    Collection<IDocumentedNode> getClasses();

    Collection<IDocumentedNode> getFields();

    Collection<IDocumentedNode> getMethods();

    Collection<INode> getParameters();

    interface INode {
        String getOriginal();

        String getMapped();

        @Nullable
        String getMeta(String name);

        default String getSide() {
            String side = getMeta("side");
            return side != null ? side : Sides.BOTH;
        }

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
