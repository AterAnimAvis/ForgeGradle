package net.minecraftforge.gradle.mcp.mapping;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

public interface IMappingInfo extends Supplier<File> {
    /**
     * @return The channel used to generate/provide this IMappingInfo
     */
    String getChannel();

    /**
     * @return The version used to generate/provide this IMappingInfo
     */
    String getVersion();

    /**
     * @return The location of the `mappings.zip`
     */
    @Override
    File get();

    /**
     * @return A representation of the `mappings.zip` in an easy to manipulate format
     */
    IMappingDetail getDetails() throws IOException;
}
