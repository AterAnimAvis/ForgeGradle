package net.minecraftforge.gradle.mcp.mapping;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;
import de.siegmar.fastcsv.writer.QuoteStrategy;
import net.minecraftforge.gradle.common.util.Utils;

public interface IMappingInfo {
    String getChannel();

    String getVersion();

    File get() throws IOException;

    File getDestination();

    Iterator<IDocumentedNode> getClasses();

    Iterator<IDocumentedNode> getFields();

    Iterator<IDocumentedNode> getMethods();

    Iterator<INode> getParameters();

    interface INode {
        String getOriginal();

        String getMapped();

        @Nullable
        String getMeta(String name);

        default String getSide() {
            String side = getMeta("side");
            return side != null ? side : Sides.BOTH;
        }
    }

    interface IDocumentedNode extends INode {
        String getJavadoc();
    }

    // Expects there is a meta field of 'side' with a number
    static void writeMCPZip(File outputZip, IMappingInfo mappings) throws IOException {
        Preconditions.checkArgument(!outputZip.exists() || outputZip.isFile(), "Output zip must be a file");
        if (outputZip.exists() && !outputZip.delete()) {
            throw new IOException("Could not delete existing file " + outputZip);
        }

        if (outputZip.getParentFile() != null && !outputZip.getParentFile().exists())
            //noinspection ResultOfMethodCallIgnored
            outputZip.getParentFile().mkdirs();

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
            writeCsvFile(csvWriterSupplier, zipOut, "classes.csv", mappings.getClasses());

            // Methods
            writeCsvFile(csvWriterSupplier, zipOut, "methods.csv", mappings.getMethods());

            // Fields
            writeCsvFile(csvWriterSupplier, zipOut, "fields.csv", mappings.getFields());

            // Parameters
            writeParamCsvFile(csvWriterSupplier, zipOut, mappings.getParameters());
        }
    }

    static void writeCsvFile(Supplier<CsvWriter> writer, ZipOutputStream zipOut, String fileName, Iterator<IDocumentedNode> nodes) throws IOException {
        Consumer<CsvWriter> header = (csv) -> csv.writeRow("searge", "name", "side", "desc");
        BiConsumer<CsvWriter, IDocumentedNode> row = (csv, node) ->
            csv.writeRow(node.getOriginal(), node.getMapped(), node.getSide(), node.getJavadoc());

        writeCsvFile(writer, zipOut, fileName, nodes, header, row);
    }

    static void writeParamCsvFile(Supplier<CsvWriter> writer, ZipOutputStream zipOut, Iterator<INode> nodes) throws IOException {
        Consumer<CsvWriter> header = (csv) -> csv.writeRow("param", "name", "side");
        BiConsumer<CsvWriter, INode> row = (csv, node) ->
            csv.writeRow(node.getOriginal(), node.getMapped(), node.getSide());

        writeCsvFile(writer, zipOut, "params.csv", nodes, header, row);
    }

    static <T extends INode> void writeCsvFile(Supplier<CsvWriter> csvWriter, ZipOutputStream zipOut, String fileName, Iterator<T> nodes, Consumer<CsvWriter> header, BiConsumer<CsvWriter, T> callback) throws IOException {
        if (!nodes.hasNext()) {
            zipOut.putNextEntry(Utils.getStableEntry(fileName));

            try (CsvWriter csv = csvWriter.get()) {
                header.accept(csv);

                nodes.forEachRemaining(node -> callback.accept(csv, node));
            }

            zipOut.closeEntry();
        }
    }
}
