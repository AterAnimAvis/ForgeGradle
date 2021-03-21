package net.minecraftforge.gradle.mcp.mapping.provider.example;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
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

        int rev = 0;
        //TODO: Move actual Overlay System Here

        File mappings = cacheMappings(project, channel, version, "zip");
        HashStore cache = commonHash(project)
            .load(cacheMappings(project, channel, version, "zip.input"))
            .add("official", official.get())
            .add("mappings", rev)
            .add("codever", "1");

        return fromCachable(channel, version, cache, mappings, () -> {
            IMappingDetail detail = official.getDetails();

            Map<String, IMappingDetail.IDocumentedNode> fields = new HashMap<>();
            Map<String, IMappingDetail.INode> params = new HashMap<>();

            detail.getFields().forEach(node -> fields.put(node.getOriginal(), node));
            detail.getParameters().forEach(node -> params.put(node.getOriginal(), node));

            fields.compute("field_71432_P", (k, old) -> (old != null ? old : new Node(k)).withMapping("theTrueMinecraft").withJavadoc("Example JavaDoc"));
            params.compute("p_i45547_1_", (k, old) -> (old != null ? old : new Node(k)).withMapping("configurationIn"));

            return new MappingDetail(detail.getClasses(), fields.values(), detail.getMethods(), params.values());
        });
    }

    @Override
    public String toString() {
        return "Example Overlay";
    }
}
