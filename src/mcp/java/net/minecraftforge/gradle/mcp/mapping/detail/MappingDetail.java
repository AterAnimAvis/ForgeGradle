package net.minecraftforge.gradle.mcp.mapping.detail;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import org.apache.groovy.util.Maps;
import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.Sides;

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

                consumer.accept(new Node(obfuscated, name, Maps.of("side", side), javadoc));
            });
        }
    }

    private static String get(Set<String> headers, NamedCsvRow row, String name, String defaultValue) {
        return headers.contains(name) ? row.getField(name) : defaultValue;
    }
}
