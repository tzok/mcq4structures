package pl.poznan.put.cs.bioserver.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    private static Map<File, List<Structure>> mapFileModels = new TreeMap<>();
    private static Map<Structure, File> mapModelFile = new HashMap<>();
    private static Map<Structure, String> mapModelName = new HashMap<>();

    private static MMCIFFileReader mmcifReader = new MMCIFFileReader();
    private static PDBFileReader pdbReader = new PDBFileReader();

    public static SortedSet<Structure> getAllStructures() {
        SortedSet<Structure> set = new TreeSet<>(new Comparator<Structure>() {
            @Override
            public int compare(Structure o1, Structure o2) {
                assert o1 != null;
                assert o2 != null;
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

    private static List<Structure> getModels(File file) {
        return StructureManager.mapFileModels.get(file);
    }

    public static String getName(Structure structure) {
        return StructureManager.mapModelName.get(structure);
    }

    public static List<String> getNames(List<Structure> structures) {
        List<String> result = new ArrayList<>();
        for (Structure s : structures) {
            result.add(StructureManager.mapModelName.get(s));
        }
        return result;
    }

    /**
     * Load a structure and remember it being already cached.
     * 
     * @param path
     *            Path to the PDB file.
     * @return Structure object..
     * @throws InvalidInputException
     */
    public static List<Structure> loadStructure(File file) throws IOException,
            InvalidInputException {
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

    public static List<Structure> loadStructure(String pdbId) {
        StructureManager.pdbReader.setAutoFetch(true);
        Structure structure;
        try {
            structure = StructureManager.pdbReader.getStructureById(pdbId);
        } catch (IOException e) {
            StructureManager.LOGGER.error("Failed to fetch PDB id:" + pdbId, e);
            return new ArrayList<>();
        }

        File pdbFile = new File(StructureManager.pdbReader.getPath());
        pdbFile = new File(pdbFile, "pdb" + pdbId.toLowerCase() + ".ent.gz");
        if (!pdbFile.exists()) {
            return new ArrayList<>();
        }

        return StructureManager.storeStructureInfo(pdbFile, structure);
    }

    public static void remove(File path) {
        List<Structure> models = StructureManager.getModels(path);
        for (Structure model : models) {
            StructureManager.mapModelFile.remove(model);
            StructureManager.mapModelName.remove(model);
        }
        StructureManager.mapFileModels.remove(path);
    }

    private static boolean isMmCif(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            if (file.getName().endsWith(".gz")) {
                try (BufferedReader reader =
                        new BufferedReader(new InputStreamReader(
                                new GZIPInputStream(stream), "UTF-8"))) {
                    String line = reader.readLine();
                    return line != null && line.startsWith("data_");
                }
            }

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                String line = reader.readLine();
                return line != null && line.startsWith("data_");
            }
        }
    }

    private static boolean isPdb(File file) throws IOException,
            InvalidInputException {
        try (InputStream stream = new FileInputStream(file)) {
            if (file.getName().endsWith(".gz")) {
                try (BufferedReader reader =
                        new BufferedReader(new InputStreamReader(
                                new GZIPInputStream(stream), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("ATOM")) {
                            StructureManager.validate(reader);
                            return true;
                        }
                    }
                }
            }

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("ATOM")) {
                        StructureManager.validate(reader);
                        return true;
                    }
                }
            }
        } catch (InvalidInputException e) {
            throw new InvalidInputException(e.getMessage() + " (file: " + file
                    + ")", e);
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

            StructureManager.mapModelFile.put(clone, file);
            StructureManager.mapModelName.put(clone,
                    String.format(format, name, i + 1));
        }
        StructureManager.mapFileModels.put(file, models);
        return models;
    }

    private static void validate(BufferedReader reader)
            throws InvalidInputException {
        try {
            Character lastChain = null;
            Integer lastResidue = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TER") || line.startsWith("ENDMDL")) {
                    lastChain = null;
                    lastResidue = null;
                    continue;
                }

                if (line.startsWith("ATOM") || line.startsWith("HETATM")) {
                    boolean found = false;
                    String residueName = line.substring(17, 20);
                    for (String ignored : new String[] { "HOH", " MG", " MN" }) {
                        if (residueName.equals(ignored)) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        continue;
                    }

                    char chain = line.charAt(21);
                    if (lastChain != null && lastChain != chain) {
                        lastResidue = null;
                    }

                    int residue =
                            Integer.valueOf(line.substring(22, 26).trim());
                    if (lastResidue != null && residue - lastResidue != 0
                            && residue - lastResidue != 1) {
                        String message =
                                "Residues in the PDB file are not numbered sequentially, "
                                        + "lastResidue = " + lastResidue
                                        + ", residue = " + residue;
                        StructureManager.LOGGER.error(message);
                        throw new InvalidInputException(message);
                    }

                    lastChain = chain;
                    lastResidue = residue;
                }
            }
        } catch (Exception e) {
            throw new InvalidInputException(e);
        }
    }

    private StructureManager() {
    }
}
