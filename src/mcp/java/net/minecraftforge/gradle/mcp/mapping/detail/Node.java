package net.minecraftforge.gradle.mcp.mapping.detail;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.Sides;
import net.minecraftforge.srgutils.IMappingFile;

public class Node implements IMappingDetail.IDocumentedNode {
    private final String original;
    private final String mapped;
    private final Map<String, String> meta;
    private final String side;
    private final String javadoc;

    public Node(String original, String mapped, Map<String, String> meta, String javadoc) {
        this(original, mapped, meta, Sides.BOTH, javadoc);
    }

    public Node(String original, String mapped, Map<String, String> meta, String side, String javadoc) {
        this.original = original;
        this.mapped = mapped;
        this.meta = new HashMap<>(meta);
        this.javadoc = javadoc;
        this.side = side;
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
    public String getSide() {
        return side;
    }

    @Override
    public String getJavadoc() {
        return javadoc;
    }

    public Node withSide(String side) {
        if (Objects.equals(side, this.side)) return this;

        return new Node(original, mapped, meta, side, javadoc);
    }

    public Node withJavadoc(String javadoc) {
        if (Objects.equals(javadoc, this.javadoc)) return this;

        return new Node(original, mapped, meta, side, javadoc);
    }

    public static Node from(IMappingFile.INode node) {
        //TODO: Check that `comment` is the right key
        return new Node(node.getOriginal(), node.getMapped(), node.getMetadata(), node.getMetadata().getOrDefault("comment", ""));
    }
}
