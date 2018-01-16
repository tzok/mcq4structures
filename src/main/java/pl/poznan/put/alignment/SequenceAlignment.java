package pl.poznan.put.alignment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.types.ExportFormat;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SequenceAlignment implements Exportable {
  private static final Logger LOGGER = LoggerFactory.getLogger(SequenceAlignment.class);

  private final boolean isGlobal;
  private final String alignment;

  public SequenceAlignment(boolean isGlobal, String alignment) {
    this.isGlobal = isGlobal;
    this.alignment = alignment;
  }

  public boolean isGlobal() {
    return isGlobal;
  }

  @Override
  public String toString() {
    return alignment;
  }

  @Override
  public void export(OutputStream stream) throws IOException {
    try (Writer writer = new OutputStreamWriter(stream, "UTF-8")) {
      writer.write(isGlobal ? "Global" : "Local");
      writer.write(" sequence alignment: ");
      writer.write("\n\n");
      writer.write(alignment);
    } catch (UnsupportedEncodingException e) {
      SequenceAlignment.LOGGER.error("Failed to export sequence alignment", e);
      throw new IOException(e);
    }
  }

  @Override
  public ExportFormat getExportFormat() {
    return ExportFormat.TXT;
  }

  @Override
  public File suggestName() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
    StringBuilder filename = new StringBuilder();
    filename.append(sdf.format(new Date()));
    filename.append(isGlobal ? "-GSA-" : "-LSA-");
    filename.append(".txt");
    return new File(filename.toString());
  }
}
