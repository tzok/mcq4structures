package pl.poznan.put.alignment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.interfaces.Exportable;

public class SequenceAlignment implements Exportable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceAlignment.class);

    private final boolean isGlobal;
    private final String alignment;
    private final String title;

    public SequenceAlignment(boolean isGlobal, String alignment, String title) {
        this.isGlobal = isGlobal;
        this.alignment = alignment;
        this.title = title;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return alignment;
    }

    @Override
    public void export(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.write(isGlobal ? "Global" : "Local");
            writer.write(" sequence alignment: ");
            writer.write(title);
            writer.write("\n\n");
            writer.write(alignment);
        } catch (UnsupportedEncodingException e) {
            SequenceAlignment.LOGGER.error("Failed to export sequence alignment", e);
            throw new IOException(e);
        }
    }

    @Override
    public File suggestName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        StringBuilder filename = new StringBuilder();
        filename.append(sdf.format(new Date()));
        filename.append(isGlobal ? "-GSA-" : "-LSA-");
        filename.append(title.replace(", ", "-"));
        filename.append(".txt");
        return new File(filename.toString());
    }
}
