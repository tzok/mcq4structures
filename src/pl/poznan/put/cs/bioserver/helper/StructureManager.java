package pl.poznan.put.cs.bioserver.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
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

    private static Map<File, Structure[]> mapFileModels = new TreeMap<>();
    private static Map<Structure, File> mapModelFile = new HashMap<>();
    private static Map<Structure, String> mapModelName = new HashMap<>();

    private static PDBFileReader pdbReader = new PDBFileReader();
    private static MMCIFFileReader mmcifReader = new MMCIFFileReader();

    public static SortedSet<Structure> getAllStructures() {
        SortedSet<Structure> set = new TreeSet<>(new Comparator<Structure>() {
            @Override
            public int compare(Structure o1, Structure o2) {
                String name1 = StructureManager.getName(o1);
                String name2 = StructureManager.getName(o2);
                return name1.compareTo(name2);
            }
        });
        set.addAll(StructureManager.mapModelFile.keySet());
        return set;
    }

    public static File getFile(Structure structure) {
        return StructureManager.mapModelFile.get(structure);
    }

    public static Structure[] getModels(File file) {
        return StructureManager.mapFileModels.get(file);
    }

    public static String getName(Structure structure) {
        return StructureManager.mapModelName.get(structure);
    }

    public static String[] getNames(Structure[] structures) {
        String[] names = new String[structures.length];
        for (int i = 0; i < structures.length; i++) {
            names[i] = StructureManager.mapModelName.get(structures[i]);
        }
        return names;
    }

    /**
     * Load a structure and remember it being already cached.
     * 
     * @param path
     *            Path to the PDB file.
     * @return Structure object..
     */
    public static Structure[] loadStructure(File file) throws IOException {
        if (StructureManager.mapFileModels.containsKey(file)) {
            return StructureManager.mapFileModels.get(file);
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
            return StructureManager.storeStructureInfo(file, structure);
        } catch (IOException e) {
            String message = "Failed to load structure: " + file;
            StructureManager.LOGGER.error(message, e);
            throw new IOException(message, e);
        }
    }

    public static Structure[] loadStructure(String pdbId) {
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

        return StructureManager.storeStructureInfo(pdbFile, structure);
    }

    public static void remove(File path) {
        Structure[] models = StructureManager.getModels(path);
        for (Structure model : models) {
            StructureManager.mapModelFile.remove(model);
            StructureManager.mapModelName.remove(model);
        }
        StructureManager.mapFileModels.remove(path);
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

    private static Structure[] storeStructureInfo(File file, Structure structure) {
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

        Structure[] models = new Structure[count];
        for (int i = 0; i < count; i++) {
            Structure clone = structure.clone();
            clone.setChains(structure.getModel(i));
            models[i] = clone;

            StructureManager.mapModelFile.put(clone, file);
            StructureManager.mapModelName.put(clone,
                    String.format(format, name, i + 1));
        }
        StructureManager.mapFileModels.put(file, models);
        return models;
    }

    private StructureManager() {
    }
}
