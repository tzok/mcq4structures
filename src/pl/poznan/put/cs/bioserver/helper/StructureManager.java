package pl.poznan.put.cs.bioserver.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
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
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StructureManager.class);
    private static final Map<File, Structure> MAP_PATH_STRUCTURE = new TreeMap<>();
    private static final Map<File, String> MAP_PATH_NAME = new TreeMap<>();
    private static final Map<Structure, String> MAP_STRUCTURE_NAME = new HashMap<>();

    private static PDBFileReader pdbReader = new PDBFileReader();
    private static MMCIFFileReader mmcifReader = new MMCIFFileReader();

    public static SortedSet<File> getAllStructures() {
        return new TreeSet<>(StructureManager.MAP_PATH_NAME.keySet());
    }

    public static String getName(File path) {
        return StructureManager.MAP_PATH_NAME.get(path);
    }

    public static String getName(Structure structure) {
        return StructureManager.MAP_STRUCTURE_NAME.get(structure);
    }

    public static String[] getNames(File[] files) {
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = StructureManager.MAP_PATH_NAME.get(files[i]);
        }
        return names;
    }

    public static String[] getNames(Structure[] structures) {
        String[] names = new String[structures.length];
        for (int i = 0; i < structures.length; i++) {
            names[i] = StructureManager.MAP_STRUCTURE_NAME.get(structures[i]);
        }
        return names;
    }

    public static Structure getStructure(File file) {
        return StructureManager.MAP_PATH_STRUCTURE.get(file);
    }

    public static Structure[] getStructures(File[] files) {
        Structure[] structures = new Structure[files.length];
        for (int i = 0; i < files.length; i++) {
            structures[i] = StructureManager.MAP_PATH_STRUCTURE.get(files[i]);
        }
        return structures;
    }

    /**
     * Load a structure and remember it being already cached.
     * 
     * @param path
     *            Path to the PDB file.
     * @return Structure object..
     */
    public static Structure loadStructure(File file) throws IOException {
        if (StructureManager.MAP_PATH_STRUCTURE.containsKey(file)) {
            return StructureManager.MAP_PATH_STRUCTURE.get(file);
        }

        try {
            Structure structure;
            String name = file.getName();
            if (name.endsWith(".cif") || name.endsWith(".cif.gz")) {
                if (!StructureManager.isMmCif(file)) {
                    String message = "File is not a mmCIF structure: " + file;
                    throw new IOException(message);
                }
                structure = StructureManager.mmcifReader.getStructure(file);
            } else {
                if (!StructureManager.isPdb(file)) {
                    String message = "File is not a PDB structure: " + file;
                    throw new IOException(message);
                }
                structure = StructureManager.pdbReader.getStructure(file);
            }
            StructureManager.storeStructureInfo(file, structure);
            return structure;
        } catch (IOException e) {
            String message = "Failed to load structure: " + file;
            StructureManager.LOGGER.error(message, e);
            throw new IOException(message, e);
        }
    }

    public static File loadStructure(String pdbId) {
        StructureManager.pdbReader.setAutoFetch(true);
        Structure structure;
        try {
            structure = StructureManager.pdbReader.getStructureById(pdbId);
        } catch (IOException e) {
            StructureManager.LOGGER.error("Failed to fetch PDB id:" + pdbId, e);
            return null;
        }

        File pdbFile = new File(StructureManager.pdbReader.getPath());
        pdbFile = new File(pdbFile, "pdb" + pdbId.toLowerCase() + ".ent.gz");
        if (!pdbFile.exists()) {
            return null;
        }

        StructureManager.storeStructureInfo(pdbFile, structure);
        return pdbFile;
    }

    public static void remove(File path) {
        Structure s = StructureManager.getStructure(path);
        StructureManager.MAP_PATH_NAME.remove(path);
        StructureManager.MAP_PATH_STRUCTURE.remove(path);
        StructureManager.MAP_STRUCTURE_NAME.remove(s);
        assert StructureManager.MAP_PATH_NAME.size() == StructureManager.MAP_PATH_STRUCTURE
                .size();
        assert StructureManager.MAP_PATH_NAME.size() == StructureManager.MAP_STRUCTURE_NAME
                .size();
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

    private static void storeStructureInfo(File file, Structure structure) {
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

        StructureManager.MAP_PATH_STRUCTURE.put(file, structure);
        StructureManager.MAP_PATH_NAME.put(file, name);
        StructureManager.MAP_STRUCTURE_NAME.put(structure, name);
    }

    private StructureManager() {
    }
}
