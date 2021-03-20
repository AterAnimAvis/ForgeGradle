package net.minecraftforge.gradle.mcp.mapping.info;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import org.apache.groovy.util.Maps;
import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import net.minecraftforge.gradle.mcp.mapping.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.Sides;

public class ZipMappingInfo implements IMappingInfo {

    private final String channel;
    private final String version;
    private final File destination;

    public ZipMappingInfo(String channel, String version, File destination) {
        this.channel = channel;
        this.version = version;
        this.destination = destination;
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
        return destination;
    }

    @Override
    public File getDestination() {
        return destination;
    }

    @Override
    public Iterator<IDocumentedNode> getClasses() {
        return get("classes.csv");
    }

    @Override
    public Iterator<IDocumentedNode> getFields() {
        return get("fields.csv");
    }

    @Override
    public Iterator<IDocumentedNode> getMethods() {
        return get("methods.csv");
    }

    @Override
    public Iterator<INode> getParameters() {
        return getAndConvert("params.csv");
    }

    private Iterator<IDocumentedNode> get(String entryName) {
        List<IDocumentedNode> list = Lists.newArrayList();
        try (ZipFile zip = new ZipFile(destination)) {
            Optional<? extends ZipEntry> entry = zip.stream().filter(e -> Objects.equals(entryName, e.getName())).findFirst();

            if (!entry.isPresent()) return Collections.emptyIterator();

            try (NamedCsvReader reader = NamedCsvReader.builder().build(new InputStreamReader(zip.getInputStream(entry.get())))) {
                Set<String> headers = reader.getHeader();
                String obf = headers.contains("searge") ? "searge" : "param";

                reader.forEach(row -> {
                    String obfuscated = get(headers, row, obf, "");
                    String name = get(headers, row, "name", obfuscated);
                    String side = get(headers, row, "side", Sides.BOTH);
                    String javadoc = get(headers, row, "desc", "");

                    list.add(new Node(obfuscated, name, Maps.of("side", side), javadoc));
                });
            }
        } catch (IOException ignored) {

        }

        return list.iterator();
    }

    private Iterator<INode> getAndConvert(String entryName) {
        List<INode> list = Lists.newArrayList();
        get(entryName).forEachRemaining(list::add);
        return list.iterator();
    }

    private String get(Set<String> headers, NamedCsvRow row, String name, String defaultValue) {
        return headers.contains(name) ? row.getField(name) : defaultValue;
    }
}
