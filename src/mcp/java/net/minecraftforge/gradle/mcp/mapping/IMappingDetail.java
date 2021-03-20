package net.minecraftforge.gradle.mcp.mapping;

import java.util.Iterator;
import javax.annotation.Nullable;

public interface IMappingDetail {

    Iterator<IDocumentedNode> getClasses();

    Iterator<IDocumentedNode> getFields();

    Iterator<IDocumentedNode> getMethods();

    Iterator<INode> getParameters();

    interface INode {
        String getOriginal();

        String getMapped();

        @Nullable
        String getMeta(String name);

        default String getSide() {
            String side = getMeta("side");
            return side != null ? side : Sides.BOTH;
        }
    }

    interface IDocumentedNode extends INode {
        String getJavadoc();
    }
}
