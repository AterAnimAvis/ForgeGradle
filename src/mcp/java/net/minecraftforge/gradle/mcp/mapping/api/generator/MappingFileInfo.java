package net.minecraftforge.gradle.mcp.mapping.api.generator;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.stream.Collectors;

import net.minecraftforge.gradle.mcp.mapping.utils.MappingStreams;
import net.minecraftforge.srgutils.IMappingFile;

public class MappingFileInfo implements IMappingFileInfo{

    private final IMappingFile source;

    /**
     * @param source An IMappingFile mapping from SRG -> MAPPED.
     */
    public MappingFileInfo(IMappingFile source) {
        this.source = source;
    }

    public Collection<IMappingFile.IClass> getClasses() {
        return MappingStreams
            .getClasses(source)
            .filter(MappingStreams::isSrg)
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(IMappingFile.INode::getOriginal))));
    }

    public Collection<IMappingFile.IField> getFields() {
        return MappingStreams
            .getFields(source)
            .filter(MappingStreams::isSrg)
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(IMappingFile.INode::getOriginal))));
    }

    public Collection<IMappingFile.IMethod> getMethods() {
        return MappingStreams
            .getMethods(source)
            .filter(MappingStreams::isSrg)
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(IMappingFile.INode::getOriginal))));
    }

    public Collection<IMappingFile.IParameter> getParameters() {
        return MappingStreams
            .getParameters(source)
            .filter(MappingStreams::isSrg)
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(IMappingFile.INode::getOriginal))));
    }

}
