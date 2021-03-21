package net.minecraftforge.gradle.mcp.mapping.util;

import java.util.stream.Stream;

import net.minecraftforge.srgutils.IMappingFile;

public class MappingStreams {
    public static Stream<? extends IMappingFile.IClass> getClasses(IMappingFile mappings) {
        return mappings.getClasses().stream();
    }

    public static boolean isSrg(IMappingFile.IClass cls) {
        return cls.getOriginal().contains("C_") && cls.getOriginal().endsWith("_");
    }

    public static Stream<? extends IMappingFile.IField> getFields(IMappingFile mappings) {
        return getClasses(mappings)
            .flatMap(MappingStreams::getFields);
    }

    public static Stream<? extends IMappingFile.IField> getFields(IMappingFile.IClass cls) {
        return cls.getFields().stream();
    }

    public static boolean isSrg(IMappingFile.IField field) {
        return field.getOriginal().startsWith("field_") || field.getOriginal().startsWith("f_");
    }

    public static Stream<? extends IMappingFile.IMethod> getMethods(IMappingFile mappings) {
        return getClasses(mappings)
            .flatMap(MappingStreams::getMethods);
    }

    public static Stream<? extends IMappingFile.IMethod> getMethods(IMappingFile.IClass cls) {
        return cls.getMethods().stream();
    }

    public static boolean isSrg(IMappingFile.IMethod method) {
        return method.getOriginal().startsWith("func_") || method.getOriginal().startsWith("m_");
    }

    public static Stream<? extends IMappingFile.IParameter> getParameters(IMappingFile mappings) {
        return getClasses(mappings)
            .flatMap(MappingStreams::getMethods)
            .flatMap(MappingStreams::getParameters);
    }

    public static Stream<? extends IMappingFile.IParameter> getParameters(IMappingFile.IMethod mtd) {
        return mtd.getParameters().stream();
    }

    public static boolean isSrg(IMappingFile.IParameter parameter) {
        return parameter.getOriginal().startsWith("p_");
    }
}
