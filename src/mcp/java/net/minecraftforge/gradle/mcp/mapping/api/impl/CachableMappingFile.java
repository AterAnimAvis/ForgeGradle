package net.minecraftforge.gradle.mcp.mapping.api.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import net.minecraftforge.gradle.common.util.HashFunction;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.gradle.common.util.func.IOConsumer;
import net.minecraftforge.gradle.mcp.mapping.api.ICachableMappingFile;
import net.minecraftforge.srgutils.IMappingFile;

public class CachableMappingFile implements ICachableMappingFile {

    private final String channel;
    private final String version;
    private final File location;
    private final HashStore cache;
    private final IOConsumer<Path> generator;

    public CachableMappingFile(String channel, String version, File location, HashStore cache, IOConsumer<Path> generator) {
        this.channel = channel;
        this.version = version;
        this.location = location;
        this.cache = cache;
        this.generator = generator;
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
    public File getFileLocation() {
        return location;
    }

    public HashStore getCache() {
        return cache;
    }

    @Override
    public IMappingFile get() throws IOException {
        if (!cache.isSame() || !location.exists()) {
            generator.accept(location.toPath());

            if (!location.exists())
                throw new IllegalStateException("Generator didn't generate mapping file: " + location);

            cache.save();
            Utils.updateHash(location, HashFunction.SHA1);
        }

        return IMappingFile.load(location);
    }
}
