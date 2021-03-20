package net.minecraftforge.gradle.mcp.mapping.detail;

import java.util.Map;
import javax.annotation.Nullable;

import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;

public class Node implements IMappingDetail.IDocumentedNode {
    private final String original;
    private final String mapped;
    private final Map<String, String> meta;
    private final String javadoc;

    public Node(String original, String mapped, Map<String, String> meta, String javadoc) {
        this.original = original;
        this.mapped = mapped;
        this.meta = meta;
        this.javadoc = javadoc;
    }

    @Override
    public String getOriginal() {
        return original;
    }

    @Override
    public String getMapped() {
        return mapped;
    }

    @Nullable
    @Override
    public String getMeta(String name) {
        return meta.get(name);
    }

    @Override
    public String getJavadoc() {
        return javadoc;
    }
}
