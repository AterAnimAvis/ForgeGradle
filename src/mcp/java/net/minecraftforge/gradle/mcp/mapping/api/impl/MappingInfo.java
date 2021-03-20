package net.minecraftforge.gradle.mcp.mapping.api.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import net.minecraftforge.gradle.mcp.mapping.api.IMappingInfo;
import net.minecraftforge.srgutils.IMappingFile;
import net.minecraftforge.srgutils.IRenamer;

public class MappingInfo implements IMappingInfo {
    protected final String channel;
    protected final String version;
    protected final File file;

    public MappingInfo(String channel, String version, File file) {
        this.channel = channel;
        this.version = version;
        this.file = file;
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
    public File find() throws IOException {
        return file;
    }

    @Override
    public IMappingFile applyMappings(IMappingFile input) throws IOException {
        File mappingsZip = find();

        Map<String, String> names = new HashMap<>();
        try (ZipFile zip = new ZipFile(mappingsZip)) {
            List<ZipEntry> entries = zip.stream().filter(e -> e.getName().endsWith(".csv")).collect(Collectors.toList());
            for (ZipEntry entry : entries) {
                try (NamedCsvReader reader = NamedCsvReader.builder().build(new InputStreamReader(zip.getInputStream(entry)))) {
                    String obf = reader.getHeader().contains("searge") ? "searge" : "param";
                    reader.forEach(row -> names.put(row.getField(obf), row.getField("name")));
                }
            }
        }

        return input.rename(new IRenamer() {
            @Override
            public String rename(IMappingFile.IClass value) {
                return names.getOrDefault(value.getMapped(), value.getMapped());
            }

            @Override
            public String rename(IMappingFile.IField value) {
                return names.getOrDefault(value.getMapped(), value.getMapped());
            }

            @Override
            public String rename(IMappingFile.IMethod value) {
                return names.getOrDefault(value.getMapped(), value.getMapped());
            }

            @Override
            public String rename(IMappingFile.IParameter value) {
                return names.getOrDefault(value.getMapped(), value.getMapped());
            }
        });
    }
}
