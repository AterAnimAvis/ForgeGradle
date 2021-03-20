package net.minecraftforge.gradle.mcp.mapping.api.impl;

import java.io.File;
import java.io.IOException;

import net.minecraftforge.gradle.common.util.HashFunction;
import net.minecraftforge.gradle.common.util.HashStore;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.gradle.common.util.func.IOConsumer;

public class CachableMappingInfo extends MappingInfo {

    private final HashStore cache;
    private final IOConsumer<File> generator;

    public CachableMappingInfo(String channel, String version, File file, HashStore cache, IOConsumer<File> generator) {
        super(channel, version, file);
        this.cache = cache;
        this.generator = generator;
    }

    @Override
    public File find() throws IOException {
        if (!cache.isSame() || !file.exists()) {
            generator.accept(file);

            if (!file.exists())
                throw new IllegalStateException("Generator didn't generate mapping zip: " + file);

            cache.save();
            Utils.updateHash(file, HashFunction.SHA1);
        }

        return file;
    }


}
