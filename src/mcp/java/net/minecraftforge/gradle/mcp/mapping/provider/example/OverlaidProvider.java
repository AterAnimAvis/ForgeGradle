package net.minecraftforge.gradle.mcp.mapping.provider.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.gradle.api.Project;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.IMappingInfo;
import net.minecraftforge.gradle.mcp.mapping.IMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.MappingProviders;
import net.minecraftforge.gradle.mcp.mapping.Sides;
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

            Collection<IMappingDetail.IDocumentedNode> fields = new ArrayList<>(detail.getFields());
            Collection<IMappingDetail.INode> params = new ArrayList<>(detail.getParameters());

            //TODO: Cleanup into an actual Overlay System
            Optional<IMappingDetail.IDocumentedNode> field = fields.stream().filter(node -> node.getOriginal().equals("field_71432_P")).findFirst();
            field.ifPresent(params::remove);
            IMappingDetail.IDocumentedNode f = field.orElse(null);
            fields.add(new Node("field_71432_P", "theTrueMinecraft", Collections.singletonMap("side", f != null ? f.getSide() : Sides.BOTH), "Example JavaDoc"));

            Optional<IMappingDetail.INode> param = params.stream().filter(node -> node.getOriginal().equals("p_i45547_1_")).findFirst();
            param.ifPresent(params::remove);
            IMappingDetail.INode p = param.orElse(null);
            params.add(new Node("p_i45547_1_", "configurationIn", Collections.singletonMap("side", p != null ? p.getSide() : Sides.BOTH), ""));

            return new MappingDetail(detail.getClasses(), fields, detail.getMethods(), params);
        });
    }

    @Override
    public String toString() {
        return "Example Overlay";
    }
}
