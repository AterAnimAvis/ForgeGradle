package net.minecraftforge.gradle.mcp.mapping.api.impl;

import java.io.File;
import java.io.IOException;

import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.func.IOSupplier;
import net.minecraftforge.gradle.mcp.mapping.api.generator.MCPZipGenerator;
import net.minecraftforge.gradle.mcp.mapping.api.generator.MappingFileInfo;
import net.minecraftforge.srgutils.IMappingFile;

public class SrgMappingInfo extends CachableMappingInfo {

    private final IOSupplier<IMappingFile> mappings;

    public SrgMappingInfo(String channel, String version, File file, HashStore cache, IOSupplier<IMappingFile> mappings) {
        super(channel, version, file, cache, (destination) -> MCPZipGenerator.writeMCPZip(destination, new MappingFileInfo(mappings.get())));

        this.mappings = mappings;
    }

    @Override
    public IMappingFile applyMappings(IMappingFile input) throws IOException {
        return input.chain(mappings.get());
    }

    public static class CachingIOSupplier<T> implements IOSupplier<T> {

        private IOSupplier<T> generator;
        private T result;

        public CachingIOSupplier(IOSupplier<T> generator) {
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
