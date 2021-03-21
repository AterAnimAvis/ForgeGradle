package net.minecraftforge.gradle.mcp.mapping.detail;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.Sides;
import net.minecraftforge.gradle.mcp.mapping.util.MappingStreams;
import net.minecraftforge.srgutils.IMappingFile;

public class MappingDetail implements IMappingDetail {
    protected final Map<String, IDocumentedNode> classes;
    protected final Map<String, IDocumentedNode> fields;
    protected final Map<String, IDocumentedNode> methods;
    protected final Map<String, INode> params;

    public MappingDetail(Map<String, IDocumentedNode> classes, Map<String, IDocumentedNode> fields, Map<String, IDocumentedNode> methods, Map<String, INode> params) {
        this.classes = classes;
        this.fields = fields;
        this.methods = methods;
        this.params = params;
    }

    @Override
    public Map<String, IDocumentedNode> getClasses() {
        return classes;
    }

    @Override
    public Map<String, IDocumentedNode> getFields() {
        return fields;
    }

    @Override
    public Map<String, IDocumentedNode> getMethods() {
        return methods;
    }

    @Override
    public Map<String, INode> getParameters() {
        return params;
    }

    public static IMappingDetail fromSrg(IMappingFile client, IMappingFile server) {
        Map<String, IDocumentedNode> classes = new HashMap<>();
        Map<String, IDocumentedNode> fields = new HashMap<>();
        Map<String, IDocumentedNode> methods = new HashMap<>();
        Map<String, INode> params = new HashMap<>();

        forEach(client, server, MappingDetail::forEachClass, classes::put);
        forEach(client, server, MappingDetail::forEachFields, fields::put);
        forEach(client, server, MappingDetail::forEachMethod, methods::put);
        forEach(client, server, MappingDetail::forEachParam, params::put);

        return new MappingDetail(classes, fields, methods, params);
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

    public static IMappingDetail fromSrg(IMappingFile input) {
        Map<String, IDocumentedNode> classes = new HashMap<>();
        Map<String, IDocumentedNode> fields = new HashMap<>();
        Map<String, IDocumentedNode> methods = new HashMap<>();
        Map<String, INode> params = new HashMap<>();

        forEachClass(input, classes::put);
        forEachFields(input, fields::put);
        forEachMethod(input, methods::put);
        forEachParam(input, params::put);

        return new MappingDetail(classes, fields, methods, params);
    }

    private static void forEachClass(IMappingFile input, BiConsumer<String, Node> consumer) {
        MappingStreams.getClasses(input).map(Node::from).forEach(node -> consumer.accept(node.getOriginal(), node));
    }

    private static void forEachFields(IMappingFile input, BiConsumer<String, Node> consumer) {
        MappingStreams.getFields(input).map(Node::from).forEach(node -> consumer.accept(node.getOriginal(), node));
    }

    private static void forEachMethod(IMappingFile input, BiConsumer<String, Node> consumer) {
        MappingStreams.getMethods(input).map(Node::from).forEach(node -> consumer.accept(node.getOriginal(), node));
    }

    private static void forEachParam(IMappingFile input, BiConsumer<String, Node> consumer) {
        MappingStreams.getParameters(input).map(Node::from).forEach(node -> consumer.accept(node.getOriginal(), node));
    }

    public static IMappingDetail fromZip(File input) throws IOException {
        Map<String, IDocumentedNode> classes = new HashMap<>();
        Map<String, IDocumentedNode> fields = new HashMap<>();
        Map<String, IDocumentedNode> methods = new HashMap<>();
        Map<String, INode> params = new HashMap<>();

        try (ZipFile zip = new ZipFile(input)) {
            readEntry(zip, "classes.csv", classes::put);
            readEntry(zip, "fields.csv", fields::put);
            readEntry(zip, "methods.csv", methods::put);
            readEntry(zip, "params.csv", params::put);
        }

        return new MappingDetail(classes, fields, methods, params);
    }

    private static void readEntry(ZipFile zip, String entryName, BiConsumer<String, Node> consumer) throws IOException {
        Optional<? extends ZipEntry> entry = zip.stream().filter(e -> Objects.equals(entryName, e.getName())).findFirst();

        if (!entry.isPresent()) return;

        try (NamedCsvReader reader = NamedCsvReader.builder().build(new InputStreamReader(zip.getInputStream(entry.get())))) {
            Set<String> headers = reader.getHeader();
            String obf = headers.contains("searge") ? "searge" : "param";

            reader.forEach(row -> {
                String obfuscated = row.getField(obf);
                String name = get(headers, row, "name", obfuscated);
                String side = get(headers, row, "side", Sides.BOTH);
                String javadoc = get(headers, row, "desc", "");

                consumer.accept(obfuscated, new Node(obfuscated, name, side, javadoc));
            });
        }
    }

    private static String get(Set<String> headers, NamedCsvRow row, String name, String defaultValue) {
        return headers.contains(name) ? row.getField(name) : defaultValue;
    }
}
