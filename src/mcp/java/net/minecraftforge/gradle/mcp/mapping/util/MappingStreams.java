package net.minecraftforge.gradle.mcp.mapping.util;

import java.util.stream.Stream;

import net.minecraftforge.srgutils.IMappingFile;

public class MappingStreams {
    public static Stream<? extends IMappingFile.IClass> getClasses(IMappingFile mappings) {
        return mappings.getClasses().stream();
    }

    public static Stream<? extends IMappingFile.IField> getFields(IMappingFile mappings) {
        return getClasses(mappings)
            .flatMap(MappingStreams::getFields);
    }

    public static Stream<? extends IMappingFile.IField> getFields(IMappingFile.IClass cls) {
        return cls.getFields().stream();
    }

    public static Stream<? extends IMappingFile.IMethod> getMethods(IMappingFile mappings) {
        return getClasses(mappings)
            .flatMap(MappingStreams::getMethods);
    }

    public static Stream<? extends IMappingFile.IMethod> getMethods(IMappingFile.IClass cls) {
        return cls.getMethods().stream();
    }

    public static Stream<? extends IMappingFile.IParameter> getParameters(IMappingFile mappings) {
        return getClasses(mappings)
            .flatMap(MappingStreams::getMethods)
            .flatMap(MappingStreams::getParameters);
    }

    public static Stream<? extends IMappingFile.IParameter> getParameters(IMappingFile.IMethod mtd) {
        return mtd.getParameters().stream();
    }
}
