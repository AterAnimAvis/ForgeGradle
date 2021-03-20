package net.minecraftforge.gradle.mcp.mapping.generator;

import java.io.BufferedWriter;
import java.io.FilterWriter;
import java.io.IOException;

public class UnclosingWriter extends FilterWriter {

    public UnclosingWriter(BufferedWriter out) {
        super(out);
    }

    @Override
    public void close() throws IOException {
        super.flush();
    }
}
