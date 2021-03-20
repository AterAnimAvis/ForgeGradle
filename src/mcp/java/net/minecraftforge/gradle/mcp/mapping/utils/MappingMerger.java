package net.minecraftforge.gradle.mcp.mapping.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import net.minecraftforge.srgutils.IMappingBuilder;
import net.minecraftforge.srgutils.IMappingFile;

import static net.minecraftforge.gradle.mcp.mapping.api.Constants.SIDE_BOTH;
import static net.minecraftforge.gradle.mcp.mapping.api.Constants.SIDE_CLIENT;
import static net.minecraftforge.gradle.mcp.mapping.api.Constants.SIDE_SERVER;

public class MappingMerger {

    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String SIDE = "side";

    public static IMappingFile merge(IMappingFile... mappings) {
        MappingBuilder builder = new MappingBuilder();

        Map<String, String> EMPTY = new HashMap<>();

        for (IMappingFile mapping : mappings) {
            builder.inject(mapping, EMPTY, EMPTY, EMPTY, EMPTY);
        }

        return builder.build();
    }

    /**
     * Expects SRG -> OBF
     */
    public static IMappingFile mergeSided(IMappingFile mapped, IMappingFile client, IMappingFile server) {
        MappingBuilder builder = new MappingBuilder();

        Map<String, String> classes = calculateSides(
            MappingStreams.getClasses(client).filter(MappingStreams::isSrg),
            MappingStreams.getClasses(server).filter(MappingStreams::isSrg)
        );

        Map<String, String> fields  = calculateSides(
            MappingStreams.getFields(client).filter(MappingStreams::isSrg),
            MappingStreams.getFields(server).filter(MappingStreams::isSrg)
        );

        Map<String, String> methods = calculateSides(
            MappingStreams.getMethods(client).filter(MappingStreams::isSrg),
            MappingStreams.getMethods(server).filter(MappingStreams::isSrg)
        );

        Map<String, String> params  = calculateSides(
            MappingStreams.getParameters(client).filter(MappingStreams::isSrg),
            MappingStreams.getParameters(server).filter(MappingStreams::isSrg)
        );

        builder.inject(mapped, classes, fields, methods, params);

        return builder.build();
    }

    private static Map<String, String> calculateSides(Stream<? extends IMappingFile.INode> client, Stream<? extends IMappingFile.INode> server) {
        Map<String, String> map = new TreeMap<>();

        client.map(IMappingFile.INode::getOriginal).forEach((entry) -> map.put(entry, SIDE_CLIENT));
        server.map(IMappingFile.INode::getOriginal).forEach((entry) -> map.merge(entry, SIDE_SERVER, (a, b) -> SIDE_BOTH));

        return map;
    }

    private static class MappingBuilder {

        private final Map<String, IMappingBuilder.IPackage> packages = new HashMap<>();
        private final Map<String, Class> classes = new HashMap<>();
        private final IMappingBuilder actual = IMappingBuilder.create(LEFT, RIGHT);

        public void inject(IMappingFile file, Map<String, String> classes, Map<String, String> methods, Map<String, String> fields, Map<String, String> params) {
            injectPackages(file);
            injectClasses(file, classes, methods, fields, params);
        }

        private void injectPackages(IMappingFile file) {
            for (IMappingFile.IPackage input : file.getPackages()) {
                IMappingBuilder.IPackage pkg = addPackage(input.getOriginal(), input.getMapped());
                input.getMetadata().forEach(pkg::meta);
            }
        }

        private void injectClasses(IMappingFile file, Map<String, String> classes, Map<String, String> fields, Map<String, String> methods, Map<String, String> params) {
            for (IMappingFile.IClass input : file.getClasses()) {
                Class cls = addClass(input.getOriginal(), input.getMapped(), classes);
                cls.inject(input, fields, methods, params);
            }
        }

        private IMappingBuilder.IPackage addPackage(String left, String right) {
            return packages.computeIfAbsent(left + "|" + right, (k) -> actual.addPackage(left, right));
        }

        private Class addClass(String left, String right, Map<String, String> sides) {
            return classes.computeIfAbsent(left + "|" + right, (k) -> {
                IMappingBuilder.IClass cls = actual.addClass(left, right);

                if (sides.containsKey(left))
                    cls.meta(SIDE, sides.getOrDefault(left, SIDE_BOTH));

                return new Class(cls);
            });
        }

        public IMappingFile build() {
            return actual.build().getMap(LEFT, RIGHT);
        }

        private static class Class {

            private final Map<String, Method> methods = new HashMap<>();
            private final Map<String, IMappingBuilder.IField> fields = new HashMap<>();
            private final IMappingBuilder.IClass actual;

            public Class(IMappingBuilder.IClass actual) {
                this.actual = actual;
            }

            public void inject(IMappingFile.IClass clazz, Map<String, String> fields, Map<String, String> methods, Map<String, String> params) {
                clazz.getMetadata().forEach(actual::meta);

                injectFields(clazz, fields);
                injectMethods(clazz, methods, params);
            }

            private void injectFields(IMappingFile.IClass clazz, Map<String, String> sides) {
                for (IMappingFile.IField input : clazz.getFields()) {
                    IMappingBuilder.IField field = field(input.getDescriptor(), input.getOriginal(), input.getMapped(), sides);
                    input.getMetadata().forEach(field::meta);
                }
            }

            private void injectMethods(IMappingFile.IClass clazz, Map<String, String> methods, Map<String, String> params) {
                for (IMappingFile.IMethod input : clazz.getMethods()) {
                    Method method = method(input.getDescriptor(), input.getOriginal(), input.getMapped(), methods);
                    method.inject(input, params);
                }
            }

            private IMappingBuilder.IField field(String descriptor, String left, String right, Map<String, String> sides) {
                return fields.compute(descriptor + " " + left + "|" + right, (k, v) -> {
                    IMappingBuilder.IField field = actual.field(left, right).descriptor(descriptor);

                    if (sides.containsKey(left))
                        field.meta(SIDE, sides.getOrDefault(left, SIDE_BOTH));

                    return field;
                });
            }

            private Method method(String descriptor, String left, String right, Map<String, String> sides) {
                return methods.compute(descriptor + " " + left + "|" + right, (k, v) -> {
                    IMappingBuilder.IMethod method = actual.method(descriptor, left, right);

                    if (sides.containsKey(left))
                        method.meta(SIDE, sides.getOrDefault(left, SIDE_BOTH));

                    return new Method(method);
                });
            }

            private static class Method {

                private final Map<String, IMappingBuilder.IParameter> parameters = new HashMap<>();
                private final IMappingBuilder.IMethod actual;

                public Method(IMappingBuilder.IMethod actual) {
                    this.actual = actual;
                }

                public void inject(IMappingFile.IMethod input, Map<String, String> params) {
                    input.getMetadata().forEach(actual::meta);

                    injectParameters(input, params);
                }

                private void injectParameters(IMappingFile.IMethod method, Map<String, String> params) {
                    for (IMappingFile.IParameter input : method.getParameters()) {
                        IMappingBuilder.IParameter parameter = parameter(input.getIndex(), input.getOriginal(), input.getMapped(), params);
                        input.getMetadata().forEach(parameter::meta);
                    }
                }

                private IMappingBuilder.IParameter parameter(int index, String left, String right, Map<String, String> sides) {
                    return parameters.compute(index + " " + left + "|" + right, (k, v) -> {
                        IMappingBuilder.IParameter parameter = actual.parameter(index, left, right);

                        if (sides.containsKey(left))
                            parameter.meta(SIDE, sides.getOrDefault(left, SIDE_BOTH));

                        return parameter;
                    });
                }
            }
        }
    }

}
