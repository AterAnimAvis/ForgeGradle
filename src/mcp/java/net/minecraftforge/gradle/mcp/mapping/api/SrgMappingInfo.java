package net.minecraftforge.gradle.mcp.mapping.api;

import java.io.File;
import java.io.IOException;

import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.gradle.mcp.mapping.api.generator.MCPZipGenerator;
import net.minecraftforge.gradle.mcp.mapping.api.generator.MappingFileInfo;
import net.minecraftforge.srgutils.IMappingFile;

public class SrgMappingInfo extends CachableMappingInfo {

    private final Utils.IOSupplier<IMappingFile> mappings;

    public SrgMappingInfo(String channel, String version, File file, HashStore cache, Utils.IOSupplier<IMappingFile> mappings) {
        super(channel, version, file, cache, (destination) -> MCPZipGenerator.writeMCPZip(destination, new MappingFileInfo(mappings.get())));

        this.mappings = mappings;
    }

    @Override
    public IMappingFile applyMappings(IMappingFile input) throws IOException {
        return input.chain(mappings.get());
    }

    public static class CachingIOSupplier<T> implements Utils.IOSupplier<T> {

        private Utils.IOSupplier<T> generator;
        private T result;

        public CachingIOSupplier(Utils.IOSupplier<T> generator) {
            this.generator = generator;
        }

        @Override
        public T get() throws IOException {
            if (generator != null) {
                result = generator.get();
                generator = null;
            }

            return result;
        }
    }
}
