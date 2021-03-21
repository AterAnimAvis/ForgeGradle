package net.minecraftforge.gradle.mcp.mapping.provider.example;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.IMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.MappingProviders;
import net.minecraftforge.gradle.mcp.mapping.detail.MappingDetail;
import net.minecraftforge.gradle.mcp.mapping.detail.Node;
import net.minecraftforge.gradle.mcp.mapping.provider.CachingProvider;
import net.minecraftforge.gradle.mcp.mapping.provider.OfficialMappingProvider;

/**
 * An example {@link IMappingProvider} that produces mappings based on {@link OfficialMappingProvider} with overlaid information.
 */
public class OverlaidJavadocProvider extends CachingProvider {

    @Override
    public Collection<String> getMappingChannels() {
        return Collections.singleton("example_overlay_javadoc");
    }

    @Override
    public IMappingInfo getMappingInfo(Project project, String channel, String sourceMapping) throws IOException {
        //TODO: this needs another version split in for the actual javadoc version

        String sourceChannel = getChannel(sourceMapping);
        String sourceVersion = getVersion(sourceMapping);

        IMappingInfo source = MappingProviders.getInfo(project, sourceChannel, sourceVersion);

        String classes = "" +
            "searge,desc\n" +
            "net/minecraft/client/Minecraft,\"Who's Craft?\"\n";

        String fields = "" +
            "searge,desc\n" +
            "field_71432_P,\"If you stare into the abyss, the abyss stares back\"\n";

        String methods = "" +
            "searge,desc\n" +
            "func_71410_x,\"There can be only one\"\n";

        String params = "" +
            "searge,desc\n" +
            "p_i45547_1_,\n";

        File mappings = cacheMappings(project, channel, sourceMapping, "zip");
        HashStore cache = commonHash(project)
            .load(cacheMappings(project, channel, sourceMapping, "zip.input"))
            .add("source", source.get())
            .add("classes", classes)
            .add("fields", fields)
            .add("methods", methods)
            .add("params", params)
            .add("codever", "1");

        return fromCachable(channel, sourceMapping, cache, mappings, () -> {
            IMappingDetail detail = source.getDetails();

            Map<String, IMappingDetail.INode> classNodes = new HashMap<>(detail.getClasses());
            Map<String, IMappingDetail.INode> fieldNodes = new HashMap<>(detail.getFields());
            Map<String, IMappingDetail.INode> methodNodes = new HashMap<>(detail.getMethods());
            Map<String, IMappingDetail.INode> paramNodes = new HashMap<>(detail.getParameters());

            apply(classes, classNodes);
            apply(fields, fieldNodes);
            apply(methods, methodNodes);
            apply(params, paramNodes);

            return new MappingDetail(classNodes, fieldNodes, methodNodes, paramNodes);
        });
    }

    protected String getChannel(String mapping) {
        int idx = mapping.indexOf('-');

        if (idx == -1 || idx == mapping.length()) {
            throw new IllegalArgumentException("Invalid source mapping channel: " + mapping);
        }

        return mapping.substring(0, idx);
    }

    protected String getVersion(String mapping) {
        int idx = mapping.indexOf('-');

        if (idx == -1 || idx == mapping.length()) {
            throw new IllegalArgumentException("Invalid source mapping channel: " + mapping);
        }

        return mapping.substring(idx + 1);
    }

    private static void apply(String data, Map<String, IMappingDetail.INode> nodes) throws IOException {
        try (NamedCsvReader csv = NamedCsvReader.builder().build(data)) {
            for (NamedCsvRow row : csv) {
                nodes.compute(row.getField("searge"), (k, old) ->
                        Node.or(k, old).withJavadoc(row.getField("desc"))
                );
            }
        }
    }

    @Override
    public String toString() {
        return "Example Overlay";
    }
}
