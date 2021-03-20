package net.minecraftforge.gradle.mcp.mapping.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.ZipOutputStream;

import com.google.common.base.Preconditions;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;
import de.siegmar.fastcsv.writer.QuoteStrategy;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.gradle.mcp.mapping.IMappingDetail;

public class MappingZipGenerator {

    public static void generate(File outputZip, IMappingDetail mappings) throws IOException {
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
                .build(new UnclosingWriter(writer));

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

    private static void writeCsvFile(Supplier<CsvWriter> writer, ZipOutputStream zipOut, String fileName, Iterator<IMappingDetail.IDocumentedNode> nodes) throws IOException {
        Consumer<CsvWriter> header = (csv) -> csv.writeRow("searge", "name", "side", "desc");
        BiConsumer<CsvWriter, IMappingDetail.IDocumentedNode> row = (csv, node) ->
            csv.writeRow(node.getOriginal(), node.getMapped(), node.getSide(), node.getJavadoc());

        writeCsvFile(writer, zipOut, fileName, nodes, header, row);
    }

    private static void writeParamCsvFile(Supplier<CsvWriter> writer, ZipOutputStream zipOut, Iterator<IMappingDetail.INode> nodes) throws IOException {
        Consumer<CsvWriter> header = (csv) -> csv.writeRow("param", "name", "side");
        BiConsumer<CsvWriter, IMappingDetail.INode> row = (csv, node) ->
            csv.writeRow(node.getOriginal(), node.getMapped(), node.getSide());

        writeCsvFile(writer, zipOut, "params.csv", nodes, header, row);
    }

    private static <T extends IMappingDetail.INode> void writeCsvFile(Supplier<CsvWriter> csvWriter, ZipOutputStream zipOut, String fileName, Iterator<T> nodes, Consumer<CsvWriter> header, BiConsumer<CsvWriter, T> callback) throws IOException {
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
