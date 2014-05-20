package pl.poznan.put.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.MMCIFFileReader;
import org.biojava.bio.structure.io.PDBFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A common manager of loaded PDB files shared between all classes.
 * 
 * @author tzok
 */
public final class StructureManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(StructureManager.class);

    private static final List<StructureInfo> STRUCTURES = new ArrayList<>();
    private static final MMCIFFileReader MMCIF_READER = new MMCIFFileReader();
    private static final PDBFileReader PDB_READER = new PDBFileReader();

    public static List<Structure> getAllStructures() {
        SortedSet<StructureInfo> set = new TreeSet<>(
                new Comparator<StructureInfo>() {
                    @Override
                    public int compare(StructureInfo o1, StructureInfo o2) {
                        assert o1 != null;
                        assert o2 != null;
                        String name1 = o1.getName();
                        String name2 = o2.getName();
                        return name1.compareTo(name2);
                    }
                });
        set.addAll(StructureManager.STRUCTURES);

        List<Structure> result = new ArrayList<>();
        for (StructureInfo si : StructureManager.STRUCTURES) {
            result.add(si.getStructure());
        }
        return result;
    }

    public static File getFile(Structure structure) {
        for (StructureInfo si : StructureManager.STRUCTURES) {
            if (si.getStructure().equals(structure)) {
                return si.getPath();
            }
        }
        return null;
    }

    public static String getName(Structure structure) {
        for (StructureInfo si : StructureManager.STRUCTURES) {
            if (si.getStructure().equals(structure)) {
                return si.getName();
            }
        }
        return null;
    }

    public static List<Structure> getModels(File file) {
        List<Structure> result = new ArrayList<>();
        for (StructureInfo si : StructureManager.STRUCTURES) {
            if (si.getPath().equals(file)) {
                result.add(si.getStructure());
            }
        }
        return result;
    }

    public static List<String> getNames(List<Structure> structures) {
        List<String> result = new ArrayList<>();
        for (Structure s : structures) {
            result.add(StructureManager.getName(s));
        }
        return result;
    }

    /**
     * Load a structure and remember it being already cached.
     * 
     * @param path
     *            Path to the PDB file.
     * @return Structure object..
     */
    public static List<Structure> loadStructure(File file) throws IOException {
        List<Structure> models = StructureManager.getModels(file);
        if (models.size() > 0) {
            return models;
        }

        try {
            Structure structure;
            String name = file.getName();

            if (name.endsWith(".cif") || name.endsWith(".cif.gz")) {
                if (!StructureManager.isMmCif(file)) {
                    throw new IOException("File is not a mmCIF structure: "
                            + file);
                }
                structure = StructureManager.MMCIF_READER.getStructure(file);
            } else {
                if (!StructureManager.isPdb(file)) {
                    throw new IOException("File is not a PDB structure: "
                            + file);
                }
                structure = StructureManager.PDB_READER.getStructure(file);
            }

            return StructureManager.storeStructureInfo(file, structure);
        } catch (IOException e) {
            String message = "Failed to load structure: " + file;
            StructureManager.LOGGER.error(message, e);
            throw new IOException(message, e);
        }
    }

    public static List<Structure> loadStructure(String pdbId) {
        StructureManager.PDB_READER.setAutoFetch(true);
        Structure structure;

        try {
            structure = StructureManager.PDB_READER.getStructureById(pdbId);
        } catch (IOException e) {
            StructureManager.LOGGER.error("Failed to fetch PDB id:" + pdbId, e);
            return new ArrayList<>();
        }

        File pdbFile = new File(StructureManager.PDB_READER.getPath());
        pdbFile = new File(pdbFile, "pdb" + pdbId.toLowerCase() + ".ent.gz");

        if (!pdbFile.exists()) {
            return new ArrayList<>();
        }

        return StructureManager.storeStructureInfo(pdbFile, structure);
    }

    public static void remove(File path) {
        List<Integer> toRemove = new ArrayList<>();

        for (int i = 0; i < StructureManager.STRUCTURES.size(); i++) {
            StructureInfo si = StructureManager.STRUCTURES.get(i);
            if (si.getPath().equals(path)) {
                toRemove.add(i);
            }
        }

        for (int i : toRemove) {
            StructureManager.STRUCTURES.remove(i);
        }
    }

    private static boolean isMmCif(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            if (file.getName().endsWith(".gz")) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new GZIPInputStream(stream),
                                "UTF-8"))) {
                    String line = reader.readLine();
                    return line != null && line.startsWith("data_");
                }
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, "UTF-8"))) {
                String line = reader.readLine();
                return line != null && line.startsWith("data_");
            }
        }
    }

    private static boolean isPdb(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            if (file.getName().endsWith(".gz")) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new GZIPInputStream(stream),
                                "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("ATOM")) {
                            return true;
                        }
                    }
                }
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("ATOM")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static List<Structure> storeStructureInfo(File file,
            Structure structure) {
        String name = structure.getPDBCode();

        if (name == null || name.trim().equals("")) {
            name = file.getName();
            if (name.endsWith(".pdb") || name.endsWith(".cif")) {
                name = name.substring(0, name.length() - 4);
            } else if (name.endsWith(".pdb.gz") || name.endsWith(".cif.gz")) {
                name = name.substring(0, name.length() - 7);
            }
            structure.setPDBCode(name);
        }

        int count = structure.nrModels();
        int order = 10;
        int leading = 1;
        while (order < count) {
            leading++;
            order *= 10;
        }
        String format = "%s.%0" + leading + "d";

        List<Structure> models = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Structure clone = structure.clone();
            clone.setChains(structure.getModel(i));
            models.add(clone);

            StructureManager.STRUCTURES.add(new StructureInfo(clone, file,
                    String.format(format, name, i + 1)));
        }
        return models;
    }

    private StructureManager() {
    }
}
