package net.minecraftforge.gradle.mcp.mapping.detail;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.Sides;
import net.minecraftforge.srgutils.IMappingFile;

public class Node implements IMappingDetail.INode {
    private final String original;
    private final String mapped;
    private final String side;
    private final String javadoc;

    public Node(String original, String mapped, Map<String, String> meta) {
        //TODO: Check that `comment` is the right key
        this(original, mapped, meta.getOrDefault("side", Sides.BOTH), meta.getOrDefault("comment", ""));
    }

    public Node(String original, String mapped, String side, String javadoc) {
        this.original = original;
        this.mapped = mapped;
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

        return new Node(original, mapped, side, javadoc);
    }

    @Override
    public Node withSide(String side) {
        if (Objects.equals(side, this.side)) return this;

        return new Node(original, mapped, side, javadoc);
    }

    @Override
    public Node withJavadoc(String javadoc) {
        if (Objects.equals(javadoc, this.javadoc)) return this;

        return new Node(original, mapped, side, javadoc);
    }

    public static IMappingDetail.INode or(String key, @Nullable IMappingDetail.INode node) {
        return node != null ? node : new Node(key, key, Sides.BOTH, "");
    }

    public static Node from(IMappingFile.INode node) {
        return new Node(node.getOriginal(), node.getMapped(), node.getMetadata());
    }
}
