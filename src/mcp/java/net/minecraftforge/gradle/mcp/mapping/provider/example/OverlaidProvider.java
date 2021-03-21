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
public class OverlaidProvider extends CachingProvider {

    @Override
    public Collection<String> getMappingChannels() {
        return Collections.singleton("example_overlay");
    }

    @Override
    public IMappingInfo getMappingInfo(Project project, String channel, String version) throws IOException {
        IMappingInfo official = MappingProviders.getInfo(project, "official", version);

        String fields = "" +
            "searge,name,side,desc\n" +
            "field_71432_P,theAbyss,0,\"If you stare into the abyss, the abyss stares back\"\n";

        String methods = "" +
            "searge,name,side,desc\n" +
            "func_71410_x,getHighlander,0,\"There can be only one\"\n";

        String params = "" +
            "param,name,side\n" +
            "p_i45547_1_,configurationIn,0\n";

        File mappings = cacheMappings(project, channel, version, "zip");
        HashStore cache = commonHash(project)
            .load(cacheMappings(project, channel, version, "zip.input"))
            .add("official", official.get())
            .add("fields", fields)
            .add("methods", methods)
            .add("params", params)
            .add("codever", "1");

        return fromCachable(channel, version, cache, mappings, () -> {
            IMappingDetail detail = official.getDetails();

            Map<String, IMappingDetail.IDocumentedNode> fieldNodes = new HashMap<>(detail.getFields());
            Map<String, IMappingDetail.IDocumentedNode> methodNodes = new HashMap<>(detail.getMethods());
            Map<String, IMappingDetail.INode> paramNodes = new HashMap<>(detail.getParameters());

            apply(fields, fieldNodes);
            apply(methods, methodNodes);
            applyParams(params, paramNodes);

            return new MappingDetail(detail.getClasses(), fieldNodes, methodNodes, paramNodes);
        });
    }

    private static void apply(String data, Map<String, IMappingDetail.IDocumentedNode> nodes) throws IOException {
        try (NamedCsvReader csv = NamedCsvReader.builder().build(data)) {
            for (NamedCsvRow row : csv) {
                nodes.compute(row.getField("searge"), (k, old) -> (old != null ? old : new Node(k)).withMapping(row.getField("name")).withJavadoc(row.getField("desc")));
            }
        }
    }

    private static void applyParams(String data, Map<String, IMappingDetail.INode> nodes) throws IOException {
        try (NamedCsvReader csv = NamedCsvReader.builder().build(data)) {
            for (NamedCsvRow row : csv) {
                nodes.compute(row.getField("param"), (k, old) -> (old != null ? old : new Node(k)).withMapping(row.getField("name")));
            }
        }
    }

    @Override
    public String toString() {
        return "Example Overlay";
    }
}
