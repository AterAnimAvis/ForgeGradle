package net.minecraftforge.gradle.mcp.mapping;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.srgutils.IMappingBuilder;
import net.minecraftforge.srgutils.IMappingFile;

import static net.minecraftforge.gradle.mcp.mapping.IMappingInfo.SIDE_BOTH;
import static net.minecraftforge.gradle.mcp.mapping.IMappingInfo.SIDE_CLIENT;
import static net.minecraftforge.gradle.mcp.mapping.IMappingInfo.SIDE_SERVER;

public class MappingMerger {

    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String SIDE = "side";

    public static IMappingFile merge(IMappingFile... files) {
        MappingBuilder builder = new MappingBuilder();

        for (IMappingFile file : files)
            builder.inject(file);

        return builder.build();
    }

    public static IMappingFile sidedMerge(IMappingFile client, IMappingFile server) {
        MappingBuilder builder = new MappingBuilder();

        builder.inject(client, SIDE_CLIENT);
        builder.inject(server, SIDE_SERVER);

        return builder.build();
    }

    private static class MappingBuilder {

        private final Map<String, IMappingBuilder.IPackage> packages = new HashMap<>();
        private final Map<String, Class> classes = new HashMap<>();
        private final IMappingBuilder actual = IMappingBuilder.create(LEFT, RIGHT);

        public void inject(IMappingFile file) {
            injectPackages(file);
            injectClasses(file);
        }

        public void inject(IMappingFile file, String side) {
            injectPackages(file, side);
            injectClasses(file, side);
        }

        private void injectPackages(IMappingFile file) {
            for (IMappingFile.IPackage input : file.getPackages()) {
                IMappingBuilder.IPackage pkg = addPackage(input.getOriginal(), input.getMapped());
                input.getMetadata().forEach(pkg::meta);
            }
        }

        private void injectPackages(IMappingFile file, String side) {
            for (IMappingFile.IPackage input : file.getPackages()) {
                IMappingBuilder.IPackage pkg = addPackage(input.getOriginal(), input.getMapped(), side);
                input.getMetadata().forEach(pkg::meta);
            }
        }

        private void injectClasses(IMappingFile file) {
            for (IMappingFile.IClass input : file.getClasses()) {
                Class cls = addClass(input.getOriginal(), input.getMapped());
                cls.inject(input);
            }
        }

        private void injectClasses(IMappingFile file, String side) {
            for (IMappingFile.IClass input : file.getClasses()) {
                Class cls = addClass(input.getOriginal(), input.getMapped(), side);
                cls.inject(input, side);
            }
        }

        private IMappingBuilder.IPackage addPackage(String left, String right) {
            return packages.computeIfAbsent(left + "|" + right, (k) -> actual.addPackage(left, right));
        }

        private IMappingBuilder.IPackage addPackage(String left, String right, String side) {
            return packages.compute(left + "|" + right, (k, v) -> {
                if (v != null) return v.meta(SIDE, SIDE_BOTH);

                return actual.addPackage(left, right).meta(SIDE, side);
            });
        }

        private Class addClass(String left, String right) {
            return classes.computeIfAbsent(left + "|" + right, (k) -> new Class(actual.addClass(left, right)));
        }

        private Class addClass(String left, String right, String side) {
            return classes.compute(left + "|" + right, (k, v) -> {
                if (v != null) {
                    v.actual.meta(SIDE, SIDE_BOTH);
                    return v;
                }

                return new Class(actual.addClass(left, right).meta(SIDE, side));
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

            public void inject(IMappingFile.IClass clazz) {
                clazz.getMetadata().forEach(actual::meta);

                injectFields(clazz);
                injectMethods(clazz);
            }

            public void inject(IMappingFile.IClass clazz, String side) {
                clazz.getMetadata().forEach(actual::meta);

                injectFields(clazz, side);
                injectMethods(clazz, side);
            }

            private void injectFields(IMappingFile.IClass clazz) {
                for (IMappingFile.IField input : clazz.getFields()) {
                    IMappingBuilder.IField field = field(input.getDescriptor(), input.getOriginal(), input.getMapped());
                    input.getMetadata().forEach(field::meta);
                }
            }

            private void injectFields(IMappingFile.IClass clazz, String side) {
                for (IMappingFile.IField input : clazz.getFields()) {
                    IMappingBuilder.IField field = field(input.getDescriptor(), input.getOriginal(), input.getMapped(), side);
                    input.getMetadata().forEach(field::meta);
                }
            }

            private void injectMethods(IMappingFile.IClass clazz) {
                for (IMappingFile.IMethod input : clazz.getMethods()) {
                    Method method = method(input.getDescriptor(), input.getOriginal(), input.getMapped());
                    method.inject(input);
                }
            }

            private void injectMethods(IMappingFile.IClass clazz, String side) {
                for (IMappingFile.IMethod input : clazz.getMethods()) {
                    Method method = method(input.getDescriptor(), input.getOriginal(), input.getMapped(), side);
                    method.inject(input, side);
                }
            }

            private IMappingBuilder.IField field(String descriptor, String left, String right) {
                return fields.computeIfAbsent(descriptor + " " + left + "|" + right, (k) -> actual.field(left, right).descriptor(descriptor));
            }

            private IMappingBuilder.IField field(String descriptor, String left, String right, String side) {
                return fields.compute(descriptor + " " + left + "|" + right, (k, v) -> {
                    if (v != null) return v.meta(SIDE, SIDE_BOTH);

                    return actual.field(left, right).descriptor(descriptor).meta(SIDE, side);
                });
            }

            private Method method(String descriptor, String left, String right) {
                return methods.computeIfAbsent(descriptor + " " + left + "|" + right, (k) -> new Method(actual.method(descriptor, left, right)));
            }

            private Method method(String descriptor, String left, String right, String side) {
                return methods.compute(descriptor + " " + left + "|" + right, (k, v) -> {
                    if (v != null) {
                        v.actual.meta(SIDE, SIDE_BOTH);
                        return v;
                    }

                    return new Method(actual.method(descriptor, left, right).meta(SIDE, side));
                });
            }

            private static class Method {

                private final Map<String, IMappingBuilder.IParameter> parameters = new HashMap<>();
                private final IMappingBuilder.IMethod actual;

                public Method(IMappingBuilder.IMethod actual) {
                    this.actual = actual;
                }

                public void inject(IMappingFile.IMethod input) {
                    input.getMetadata().forEach(actual::meta);

                    injectParameters(input);
                }

                public void inject(IMappingFile.IMethod input, String side) {
                    input.getMetadata().forEach(actual::meta);

                    injectParameters(input, side);
                }

                private void injectParameters(IMappingFile.IMethod method) {
                    for (IMappingFile.IParameter input : method.getParameters()) {
                        IMappingBuilder.IParameter parameter = parameter(input.getIndex(), input.getOriginal(), input.getMapped());
                        input.getMetadata().forEach(parameter::meta);
                    }
                }

                private void injectParameters(IMappingFile.IMethod method, String side) {
                    for (IMappingFile.IParameter input : method.getParameters()) {
                        IMappingBuilder.IParameter parameter = parameter(input.getIndex(), input.getOriginal(), input.getMapped(), side);
                        input.getMetadata().forEach(parameter::meta);
                    }
                }

                private IMappingBuilder.IParameter parameter(int index, String left, String right) {
                    return parameters.computeIfAbsent(index + " " + left + "|" + right, (k) -> actual.parameter(index, left, right));
                }

                private IMappingBuilder.IParameter parameter(int index, String left, String right, String side) {
                    return parameters.compute(index + " " + left + "|" + right, (k, v) -> {
                        if (v != null) return v.meta(SIDE, SIDE_BOTH);

                        return actual.parameter(index, left, right).meta(SIDE, side);
                    });
                }
            }
        }
    }

}
