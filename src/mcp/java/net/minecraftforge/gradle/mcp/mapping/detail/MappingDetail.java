package net.minecraftforge.gradle.mcp.mapping.detail;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.Sides;
import net.minecraftforge.gradle.mcp.mapping.util.MappingStreams;
import net.minecraftforge.srgutils.IMappingFile;

public class MappingDetail implements IMappingDetail {
    protected final Collection<IDocumentedNode> classes;
    protected final Collection<IDocumentedNode> fields;
    protected final Collection<IDocumentedNode> methods;
    protected final Collection<INode> params;

    public MappingDetail(Collection<IDocumentedNode> classes, Collection<IDocumentedNode> fields, Collection<IDocumentedNode> methods, Collection<INode> params) {
        this.classes = classes;
        this.fields = fields;
        this.methods = methods;
        this.params = params;
    }

    @Override
    public Collection<IDocumentedNode> getClasses() {
        return classes;
    }

    @Override
    public Collection<IDocumentedNode> getFields() {
        return fields;
    }

    @Override
    public Collection<IDocumentedNode> getMethods() {
        return methods;
    }

    @Override
    public Collection<INode> getParameters() {
        return params;
    }

    public static IMappingDetail fromSrg(IMappingFile client, IMappingFile server) {
        Collection<IDocumentedNode> classes = new TreeSet<>(Comparator.comparing(IMappingDetail.INode::getOriginal));
        Collection<IDocumentedNode> fields = new TreeSet<>(Comparator.comparing(IMappingDetail.INode::getOriginal));
        Collection<IDocumentedNode> methods = new TreeSet<>(Comparator.comparing(IMappingDetail.INode::getOriginal));
        Collection<INode> params = new TreeSet<>(Comparator.comparing(IMappingDetail.INode::getOriginal));

        forEach(client, server, MappingDetail::forEachClass, classes::add);
        forEach(client, server, MappingDetail::forEachFields, fields::add);
        forEach(client, server, MappingDetail::forEachMethod, methods::add);
        forEach(client, server, MappingDetail::forEachParam, params::add);

        return new MappingDetail(classes, fields, methods, params);
    }

    private static void forEach(IMappingFile client, IMappingFile server, BiConsumer<IMappingFile, Consumer<Node>> iterator, Consumer<Node> consumer) {
        Map<String, Node> clientNodes = new TreeMap<>();
        Map<String, Node> serverNodes = new TreeMap<>();

        // TODO: merge metadata
        iterator.accept(client, (node) -> clientNodes.put(node.getOriginal(), node));
        iterator.accept(server, (node) -> serverNodes.put(node.getOriginal(), node));

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
        clientNodes.values().stream().map(it -> it.withSide(Sides.CLIENT)).forEach(consumer);
        serverNodes.values().stream().map(it -> it.withSide(Sides.SERVER)).forEach(consumer);
        bothNodes.values().stream().map(it -> it.withSide(Sides.BOTH)).forEach(consumer);
    }

    public static IMappingDetail fromSrg(IMappingFile input) {
        Collection<IDocumentedNode> classes = new TreeSet<>(Comparator.comparing(IMappingDetail.INode::getOriginal));
        Collection<IDocumentedNode> fields = new TreeSet<>(Comparator.comparing(IMappingDetail.INode::getOriginal));
        Collection<IDocumentedNode> methods = new TreeSet<>(Comparator.comparing(IMappingDetail.INode::getOriginal));
        Collection<INode> params = new TreeSet<>(Comparator.comparing(IMappingDetail.INode::getOriginal));

        // TODO: Note: We can have multiple nodes per original name the TreeSet solves this, however we also need to merge metadata

        // TODO: merge metadata
        forEachClass(input, classes::add);
        forEachFields(input, fields::add);
        forEachMethod(input, methods::add);
        forEachParam(input, params::add);

        return new MappingDetail(classes, fields, methods, params);
    }

    private static void forEachClass(IMappingFile input, Consumer<Node> consumer) {
        MappingStreams.getClasses(input).map(Node::from).forEach(consumer);
    }

    private static void forEachFields(IMappingFile input, Consumer<Node> consumer) {
        MappingStreams.getFields(input).map(Node::from).forEach(consumer);
    }

    private static void forEachMethod(IMappingFile input, Consumer<Node> consumer) {
        MappingStreams.getMethods(input).map(Node::from).forEach(consumer);
    }

    private static void forEachParam(IMappingFile input, Consumer<Node> consumer) {
        MappingStreams.getParameters(input).map(Node::from).forEach(consumer);
    }

    public static IMappingDetail fromZip(File input) throws IOException {
        List<IDocumentedNode> classes = Lists.newArrayList();
        List<IDocumentedNode> fields = Lists.newArrayList();
        List<IDocumentedNode> methods = Lists.newArrayList();
        List<INode> params = Lists.newArrayList();

        try (ZipFile zip = new ZipFile(input)) {
            readEntry(zip, "classes.csv", classes::add);
            readEntry(zip, "fields.csv", fields::add);
            readEntry(zip, "methods.csv", methods::add);
            readEntry(zip, "params.csv", params::add);
        }

        return new MappingDetail(classes, fields, methods, params);
    }

    private static void readEntry(ZipFile zip, String entryName, Consumer<Node> consumer) throws IOException {
        Optional<? extends ZipEntry> entry = zip.stream().filter(e -> Objects.equals(entryName, e.getName())).findFirst();

        if (!entry.isPresent()) return;

        try (NamedCsvReader reader = NamedCsvReader.builder().build(new InputStreamReader(zip.getInputStream(entry.get())))) {
            Set<String> headers = reader.getHeader();
            String obf = headers.contains("searge") ? "searge" : "param";

            reader.forEach(row -> {
                String obfuscated = get(headers, row, obf, "");
                String name = get(headers, row, "name", obfuscated);
                String side = get(headers, row, "side", Sides.BOTH);
                String javadoc = get(headers, row, "desc", "");

                consumer.accept(new Node(obfuscated, name, side, javadoc));
            });
        }
    }

    private static String get(Set<String> headers, NamedCsvRow row, String name, String defaultValue) {
        return headers.contains(name) ? row.getField(name) : defaultValue;
    }
}
