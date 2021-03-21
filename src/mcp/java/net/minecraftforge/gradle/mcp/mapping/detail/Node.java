package net.minecraftforge.gradle.mcp.mapping.detail;

import java.util.Collections;
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

    public Node(String original) {
        this(original, original, Collections.emptyMap(), Sides.BOTH, "");
    }

    public Node(String original, String mapped, String side, String javadoc) {
        this(original, mapped, Collections.emptyMap(), side, javadoc);
    }

    public Node(String original, String mapped, Map<String, String> meta) {
        //TODO: Check that `comment` is the right key
        this(original, mapped, meta, meta.getOrDefault("side", Sides.BOTH), meta.getOrDefault("comment", ""));
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

    @Override
    public Node withMapping(String mapped) {
        if (Objects.equals(mapped, this.mapped)) return this;

        return new Node(original, mapped, meta, side, javadoc);
    }

    @Override
    public Node withSide(String side) {
        if (Objects.equals(side, this.side)) return this;

        return new Node(original, mapped, meta, side, javadoc);
    }

    @Override
    public Node withJavadoc(String javadoc) {
        if (Objects.equals(javadoc, this.javadoc)) return this;

        return new Node(original, mapped, meta, side, javadoc);
    }

    public static Node from(IMappingFile.INode node) {
        return new Node(node.getOriginal(), node.getMapped(), node.getMetadata());
    }
}
