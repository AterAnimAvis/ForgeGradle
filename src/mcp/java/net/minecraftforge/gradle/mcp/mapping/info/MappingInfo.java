package net.minecraftforge.gradle.mcp.mapping.info;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import net.minecraftforge.gradle.mcp.mapping.IMappingInfo;

public class MappingInfo implements IMappingInfo {
    private final String channel;
    private final String version;
    private final Collection<IDocumentedNode> classes;
    private final Collection<IDocumentedNode> fields;
    private final Collection<IDocumentedNode> methods;
    private final Collection<INode> params;
    private final File destination;

    public MappingInfo(String channel, String version, File destination, Collection<IDocumentedNode> classes, Collection<IDocumentedNode> fields, Collection<IDocumentedNode> methods, Collection<INode> params) {
        this.channel = channel;
        this.version = version;
        this.destination = destination;
        this.classes = classes;
        this.fields = fields;
        this.methods = methods;
        this.params = params;
    }

    @Override
    public String getChannel() {
        return channel;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public File get() throws IOException {
        IMappingInfo.writeMCPZip(destination, this); //TODO: Caching

        return destination;
    }

    @Override
    public File getDestination() {
        return destination;
    }

    @Override
    public Iterator<IDocumentedNode> getClasses() {
        return classes.iterator();
    }

    @Override
    public Iterator<IDocumentedNode> getFields() {
        return fields.iterator();
    }

    @Override
    public Iterator<IDocumentedNode> getMethods() {
        return methods.iterator();
    }

    @Override
    public Iterator<INode> getParameters() {
        return params.iterator();
    }
}
