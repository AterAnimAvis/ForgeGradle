package net.minecraftforge.gradle.common.mapping.detail;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import net.minecraftforge.gradle.common.mapping.IMappingDetail;
import net.minecraftforge.gradle.common.mapping.Sides;
import net.minecraftforge.gradle.common.mapping.util.MappingStreams;
import net.minecraftforge.srgutils.IMappingFile;

public class MappingDetails {
    public static IMappingDetail fromSrg(IMappingFile client, IMappingFile server) {
        Map<String, IMappingDetail.INode> classes = new HashMap<>();
        Map<String, IMappingDetail.INode> fields = new HashMap<>();
        Map<String, IMappingDetail.INode> methods = new HashMap<>();
        Map<String, IMappingDetail.INode> params = new HashMap<>();

        forEach(client, server, MappingDetails::forEachClass, classes::put);
        forEach(client, server, MappingDetails::forEachFields, fields::put);
        forEach(client, server, MappingDetails::forEachMethod, methods::put);
        forEach(client, server, MappingDetails::forEachParam, params::put);

        return new MappingDetail(classes, fields, methods, params);
    }

    public static IMappingDetail fromSrg(IMappingFile input) {
        Map<String, IMappingDetail.INode> classes = new HashMap<>();
        Map<String, IMappingDetail.INode> fields = new HashMap<>();
        Map<String, IMappingDetail.INode> methods = new HashMap<>();
        Map<String, IMappingDetail.INode> params = new HashMap<>();

        forEachClass(input, classes::put);
        forEachFields(input, fields::put);
        forEachMethod(input, methods::put);
        forEachParam(input, params::put);

        return new MappingDetail(classes, fields, methods, params);
    }

    public static IMappingDetail fromZip(File input) throws IOException {
        try (ZipFile zip = new ZipFile(input)) {
            Map<String, IMappingDetail.INode> classes = readEntry(zip, "classes.csv");
            Map<String, IMappingDetail.INode> fields = readEntry(zip, "fields.csv");
            Map<String, IMappingDetail.INode> methods = readEntry(zip, "methods.csv");
            Map<String, IMappingDetail.INode> params = readEntry(zip, "params.csv");

            return new MappingDetail(classes, fields, methods, params);
        }
    }

    private static void forEach(IMappingFile client, IMappingFile server, BiConsumer<IMappingFile, BiConsumer<String, Node>> iterator, BiConsumer<String, Node> consumer) {
        Map<String, Node> clientNodes = new HashMap<>();
        Map<String, Node> serverNodes = new HashMap<>();

        iterator.accept(client, clientNodes::put);
        iterator.accept(server, serverNodes::put);

        Set<String> clientKeys = clientNodes.keySet();
        Set<String> serverKeys = clientNodes.keySet();

        // Calculate Intersection between Client and Server
        Set<String> bothKeys = new HashSet<>(clientKeys);
        bothKeys.retainAll(serverNodes.keySet());
        Map<String, Node> bothNodes = new TreeMap<>();
        bothKeys.forEach(key -> bothNodes.put(key, clientNodes.get(key)));

        // Remove Both from Client / Server
        clientKeys.removeAll(bothKeys);
        serverKeys.removeAll(bothKeys);

        // Provide upwards
        clientNodes.values().stream().map(it -> it.withSide(Sides.CLIENT)).forEach(node -> consumer.accept(node.getOriginal(), node));
        serverNodes.values().stream().map(it -> it.withSide(Sides.SERVER)).forEach(node -> consumer.accept(node.getOriginal(), node));
        bothNodes.values().stream().map(it -> it.withSide(Sides.BOTH)).forEach(node -> consumer.accept(node.getOriginal(), node));
    }

    //TODO: v SRG ONLY? v
    private static void forEachClass(IMappingFile input, BiConsumer<String, Node> consumer) {
        MappingStreams.classes(input).map(Node::of).forEach(node -> consumer.accept(node.getOriginal(), node));
    }

    private static void forEachFields(IMappingFile input, BiConsumer<String, Node> consumer) {
        MappingStreams.fields(input).map(Node::of).forEach(node -> consumer.accept(node.getOriginal(), node));
    }

    private static void forEachMethod(IMappingFile input, BiConsumer<String, Node> consumer) {
        MappingStreams.methods(input).map(Node::of).forEach(node -> consumer.accept(node.getOriginal(), node));
    }

    private static void forEachParam(IMappingFile input, BiConsumer<String, Node> consumer) {
        MappingStreams.parameters(input).map(Node::of).forEach(node -> consumer.accept(node.getOriginal(), node));
    }
    //TODO: ^ SRG ONLY? ^

    private static Map<String, IMappingDetail.INode> readEntry(ZipFile zip, String entryName) throws IOException {
        Optional<? extends ZipEntry> entry = zip.stream().filter(e -> Objects.equals(entryName, e.getName())).findFirst();

        if (!entry.isPresent()) return Collections.emptyMap();

        Map<String, IMappingDetail.INode> nodes = new HashMap<>();

        try (NamedCsvReader reader = NamedCsvReader.builder().build(new InputStreamReader(zip.getInputStream(entry.get())))) {
            Set<String> headers = reader.getHeader();
            String obf = headers.contains("searge") ? "searge" : "param";

            reader.forEach(row -> {
                String obfuscated = row.getField(obf);
                String name = get(headers, row, "name", obfuscated);
                String side = get(headers, row, "side", Sides.BOTH);
                String javadoc = get(headers, row, "desc", "");

                nodes.put(obfuscated.replace(".", "/"), Node.of(obfuscated.replace(".", "/"), name.replace(".", "/"), side, javadoc.replaceAll("\\n", "\n")));
            });
        }

        return nodes;
    }

    private static String get(Set<String> headers, NamedCsvRow row, String name, String defaultValue) {
        return headers.contains(name) ? row.getField(name) : defaultValue;
    }
}
