package pl.poznan.put.structure;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import pl.poznan.put.pdb.analysis.CifParser;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;

/**
 * A common manager of loaded PDB files shared between all classes.
 *
 * @author tzok
 */
public final class StructureManager {
  private static final String ENCODING_UTF_8 = "UTF-8";
  private static final Collection<StructureInfo> STRUCTURES = new ArrayList<>();

  private StructureManager() {
    super();
  }

  public static List<PdbModel> getAllStructures() {
    return StructureManager.STRUCTURES.stream()
        .map(StructureInfo::structure)
        .collect(Collectors.toList());
  }

  public static List<String> getAllNames() {
    return StructureManager.STRUCTURES.stream()
        .map(StructureInfo::name)
        .collect(Collectors.toList());
  }

  public static List<String> getNames(final Iterable<PdbModel> structures) {
    final List<String> result = new ArrayList<>();
    for (final PdbModel model : structures) {
      result.add(StructureManager.getName(model));
    }
    return result;
  }

  public static String getName(final PdbModel structure) {
    for (final StructureInfo si : StructureManager.STRUCTURES) {
      if (Objects.equals(si.structure(), structure)) {
        return si.name();
      }
    }
    throw new IllegalArgumentException("Failed to find PdbModel");
  }

  public static File getFile(final PdbModel structure) {
    for (final StructureInfo si : StructureManager.STRUCTURES) {
      if (Objects.equals(si.structure(), structure)) {
        return si.path();
      }
    }
    throw new IllegalArgumentException("Failed to find PdbModel");
  }

  public static PdbModel getStructure(final String name) {
    for (final StructureInfo si : StructureManager.STRUCTURES) {
      if (Objects.equals(si.name(), name)) {
        return si.structure();
      }
    }
    throw new IllegalArgumentException("Failed to find structure");
  }

  /**
   * Load a structure and remember it being already cached.
   *
   * @param file Path to the PDB file.
   * @return Structure object.
   */
  public static List<? extends PdbModel> loadStructure(final File file) throws IOException {
    final List<PdbModel> models = StructureManager.getModels(file);
    if (!models.isEmpty()) {
      return models;
    }

    final String fileContent = StructureManager.readFile(file);
    final String name = file.getName();

    if (name.endsWith(".cif") || name.endsWith(".cif.gz")) {
      if (!StructureManager.isCif(fileContent)) {
        throw new IOException("File is not a mmCIF structure: " + file);
      }
      final List<? extends PdbModel> pdbModels = new CifParser().parse(fileContent);
      StructureManager.storeStructureInfo(file, pdbModels);
      return pdbModels;
    }

    if (!StructureManager.isPdb(fileContent)) {
      throw new IOException("File is not a PDB structure: " + file);
    }
    final List<PdbModel> pdbModels = new PdbParser(false).parse(fileContent);
    StructureManager.storeStructureInfo(file, pdbModels);
    return pdbModels;
  }

  public static List<PdbModel> getModels(final File file) {
    return StructureManager.STRUCTURES.stream()
        .filter(si -> Objects.equals(si.path(), file))
        .map(StructureInfo::structure)
        .collect(Collectors.toList());
  }

  public static List<? extends PdbModel> loadStructure(final String pdbId) throws IOException {
    if (pdbId.length() != 4) {
      throw new IllegalArgumentException("Invalid PDB id: " + pdbId);
    }

    final URL url = new URL(String.format("http://files.rcsb.org/download/%s.pdb.gz", pdbId));
    final String pdbContent = StructureManager.unzipContent(IOUtils.toByteArray(url));

    final File pdbFile = File.createTempFile("bio-commons", ".pdb");
    FileUtils.writeStringToFile(pdbFile, pdbContent, StructureManager.ENCODING_UTF_8);

    final List<PdbModel> models = new PdbParser(false).parse(pdbContent);
    StructureManager.storeStructureInfo(pdbFile, models);
    return models;
  }

  public static void remove(final File path) {
    final Collection<StructureInfo> toRemove =
        StructureManager.STRUCTURES.stream().filter(si -> Objects.equals(si.path(), path)).toList();

    for (final StructureInfo si : toRemove) {
      StructureManager.STRUCTURES.remove(si);
    }
  }

  private static String readFile(final File file) throws IOException {
    final byte[] bytes = FileUtils.readFileToByteArray(file);

    if (StructureManager.isGzip(bytes)) {
      return StructureManager.unzipContent(bytes);
    }

    return new String(bytes, Charset.defaultCharset());
  }

  private static boolean isGzip(final byte[] bytes) {
    if (bytes.length < 2) {
      return false;
    }

    final int head = (bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
    return head == GZIPInputStream.GZIP_MAGIC;
  }

  private static String unzipContent(final byte[] bytes) throws IOException {
    try (final InputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
      return IOUtils.toString(gzipInputStream, Charset.defaultCharset());
    }
  }

  private static boolean isCif(final String fileContent) {
    return fileContent.startsWith("data_");
  }

  private static boolean isPdb(final CharSequence fileContent) {
    final Pattern pdbPattern = Pattern.compile("^ATOM", Pattern.MULTILINE);
    final Matcher matcher = pdbPattern.matcher(fileContent);
    return matcher.find();
  }

  private static void storeStructureInfo(
      final File file, final List<? extends PdbModel> structures) {
    String format = "%s";

    if (structures.size() > 1) {
      final int count = structures.size();
      int order = 10;
      int leading = 1;
      while (order < count) {
        leading++;
        order *= 10;
      }
      format = String.format("%%s.%%0%dd", leading);
    }

    for (int i = 0; i < structures.size(); i++) {
      final PdbModel model = structures.get(i);
      String name = model.idCode();

      if (StringUtils.isBlank(name)) {
        name = file.getName();
        if (name.endsWith(".pdb") || name.endsWith(".cif")) {
          name = name.substring(0, name.length() - 4);
        } else {
          if (name.endsWith(".pdb.gz") || name.endsWith(".cif.gz")) {
            name = name.substring(0, name.length() - 7);
          }
        }
      }

      StructureManager.STRUCTURES.add(
          ImmutableStructureInfo.of(model, file, String.format(format, name, i + 1)));
    }
  }
}
