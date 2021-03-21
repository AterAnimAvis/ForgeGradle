package net.minecraftforge.gradle.mcp.mapping.provider;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.function.IOSupplier;
import net.minecraftforge.gradle.common.util.HashFunction;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;
import net.minecraftforge.gradle.mcp.mapping.IMappingProvider;
import net.minecraftforge.gradle.mcp.mapping.generator.MappingZipGenerator;
import net.minecraftforge.gradle.mcp.mapping.info.MappingInfo;

public abstract class CachingProvider implements IMappingProvider {

    public MappingInfo fromCachable(String channel, String version, HashStore cache, File destination, IOSupplier<IMappingDetail> detailSupplier) throws IOException {
        if (!cache.isSame() || !destination.exists()) {
            IMappingDetail detail = detailSupplier.get();

            MappingZipGenerator.generate(destination, detail);

            cache.save();
            Utils.updateHash(destination, HashFunction.SHA1);

            return new MappingInfo(channel, version, destination, detail);
        }

        return new MappingInfo(channel, version, destination);
    }
}
