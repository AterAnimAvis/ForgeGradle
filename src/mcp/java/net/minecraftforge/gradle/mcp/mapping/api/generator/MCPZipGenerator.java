package net.minecraftforge.gradle.mcp.mapping.api.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.ZipOutputStream;

import com.google.common.base.Preconditions;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;
import de.siegmar.fastcsv.writer.QuoteStrategy;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.srgutils.IMappingFile;

public class MCPZipGenerator {

    public static void writeMCPZip(File outputZip, IMappingFileInfo mappings) throws IOException {
        Preconditions.checkArgument(outputZip.isFile(), "Output zip must be a file");
        if (outputZip.exists() && !outputZip.delete()) {
            throw new IOException("Could not delete existing file " + outputZip);
        }

        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputZip));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zipOut))) {
            Supplier<CsvWriter> csvWriterSupplier
                = () -> CsvWriter.builder()
                .quoteStrategy(QuoteStrategy.ALWAYS)
                .lineDelimiter(LineDelimiter.LF)
                .build(new FilterWriter(writer) {
                    @Override
                    public void close() throws IOException {
                        // Prevent flushing
                        super.flush();
                    }
                });

            // Classes
            writeCsvFile(csvWriterSupplier, zipOut, "classes.csv", mappings, mappings.getClasses());

            // Methods
            writeCsvFile(csvWriterSupplier, zipOut, "methods.csv", mappings, mappings.getMethods());

            // Fields
            writeCsvFile(csvWriterSupplier, zipOut, "fields.csv", mappings, mappings.getFields());

            // Parameters
            writeParamCsvFile(csvWriterSupplier, zipOut, mappings, mappings.getParameters());
        }
    }

    private static void writeCsvFile(Supplier<CsvWriter> writer, ZipOutputStream zipOut, String fileName, IMappingFileInfo mapping, Collection<? extends IMappingFile.INode> nodes) throws IOException {
        Consumer<CsvWriter> header = (csv) -> csv.writeRow("searge", "name", "side", "desc");

        BiConsumer<CsvWriter, IMappingFile.INode> row = (csv, node) -> {
            csv.writeRow(node.getOriginal(), node.getMapped(), mapping.getSideForNode(node), mapping.getJavadocForNode(node));
        };

        writeCsvFile(writer, zipOut, fileName, nodes, header, row);
    }

    private static void writeParamCsvFile(Supplier<CsvWriter> writer, ZipOutputStream zipOut, IMappingFileInfo mapping, Collection<? extends IMappingFile.INode> nodes) throws IOException {
        Consumer<CsvWriter> header = (csv) -> csv.writeRow("param", "name", "side");

        BiConsumer<CsvWriter, IMappingFile.INode> row = (csv, node) -> {
            csv.writeRow(node.getOriginal(), node.getMapped(), mapping.getSideForNode(node));
        };

        writeCsvFile(writer, zipOut, "params.csv", nodes, header, row);
    }

    private static <T> void writeCsvFile(Supplier<CsvWriter> csvWriter, ZipOutputStream zipOut, String fileName, Collection<? extends T> nodes, Consumer<CsvWriter> header, BiConsumer<CsvWriter, T> callback) throws IOException {
        if (!nodes.isEmpty()) {
            zipOut.putNextEntry(Utils.getStableEntry(fileName));
            try (CsvWriter csv = csvWriter.get()) {
                header.accept(csv);
                for (T node : nodes)
                    callback.accept(csv, node);
            }
            zipOut.closeEntry();
        }
    }

}
