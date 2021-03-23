package net.minecraftforge.gradle.common.mapping.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.zip.ZipOutputStream;

import com.google.common.base.Preconditions;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;
import de.siegmar.fastcsv.writer.QuoteStrategy;
import net.minecraftforge.gradle.common.util.Utils;
import net.minecraftforge.gradle.common.mapping.IMappingDetail;

public class MappingZipGenerator {

    /**
     * Generates a ForgeGradle compatible `mappings.zip` from an {@link IMappingDetail}
     */
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
                .quoteStrategy(QuoteStrategy.REQUIRED)
                .lineDelimiter(LineDelimiter.LF)
                .build(new UnclosingWriter(writer));

            // Classes
            writeCsvFile(csvWriterSupplier, zipOut, "classes.csv", mappings.getClasses());

            // Methods
            writeCsvFile(csvWriterSupplier, zipOut, "methods.csv", mappings.getMethods());

            // Fields
            writeCsvFile(csvWriterSupplier, zipOut, "fields.csv", mappings.getFields());

            // Parameters
            writeCsvFile(csvWriterSupplier, zipOut, "params.csv", mappings.getParameters());
        }
    }

    private static void writeCsvFile(Supplier<CsvWriter> writer, ZipOutputStream zipOut, String fileName, Map<String, IMappingDetail.INode> input) throws IOException {
        Iterator<IMappingDetail.INode> nodes = input.values().stream().sorted(Comparator.comparing(IMappingDetail.INode::getOriginal)).iterator();

        if (nodes.hasNext()) {
            zipOut.putNextEntry(Utils.getStableEntry(fileName));

            try (CsvWriter csv = writer.get()) {
                csv.writeRow(fileName.equals("params.csv") ? "param" : "searge", "name", "side", "desc");

                nodes.forEachRemaining(node -> csv.writeRow(node.getOriginal().replace("/", "."), node.getMapped().replace("/", "."), node.getSide(), node.getJavadoc()));
            }

            zipOut.closeEntry();
        }
    }
}
