package pl.poznan.put.cs.bioserver.helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A common manager of loaded PDB files shared between all classes.
 * 
 * @author tzok
 */
public final class PdbManager {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PdbManager.class);
    private static final Map<File, Structure> MAP_PATH_STRUCTURE = new HashMap<>();
    private static final Map<File, String> MAP_PATH_NAME = new HashMap<>();
    private static final Map<Structure, String> MAP_STRUCTURE_NAME = new HashMap<>();
    private static PDBFileReader pdbReader = new PDBFileReader();

    public static Set<File> getAllStructures() {
        return PdbManager.MAP_PATH_NAME.keySet();
    }

    public static Structure[] getStructures(File[] files) {
        Structure[] structures = new Structure[files.length];
        for (int i = 0; i < files.length; i++) {
            structures[i] = PdbManager.MAP_PATH_STRUCTURE.get(files[i]);
        }
        return structures;
    }

    public static String[] getNames(File[] files) {
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            names[i] = PdbManager.MAP_PATH_NAME.get(files[i]);
        }
        return names;
    }

    public static Structure getStructure(File file) {
        return PdbManager.MAP_PATH_STRUCTURE.get(file);
    }

    public static String getName(File path) {
        return PdbManager.MAP_PATH_NAME.get(path);
    }

    public static String getStructureName(Structure structure) {
        return PdbManager.MAP_STRUCTURE_NAME.get(structure);
    }

    /**
     * Load a structure and remember it being already cached.
     * 
     * @param path
     *            Path to the PDB file.
     * @return Structure object..
     */
    public static Structure loadStructure(File file) {
        if (PdbManager.MAP_PATH_STRUCTURE.containsKey(file)) {
            return PdbManager.MAP_PATH_STRUCTURE.get(file);
        }

        try {
            Structure structure = PdbManager.pdbReader.getStructure(file);
            PdbManager.storeStructureInfo(file, structure);
            return structure;
        } catch (IOException e) {
            PdbManager.LOGGER.error("Failed to load the structure: " + file, e);
            return null;
        }
    }

    public static File loadStructure(String pdbId) {
        PdbManager.pdbReader.setAutoFetch(true);
        Structure structure;
        try {
            structure = PdbManager.pdbReader.getStructureById(pdbId);
        } catch (IOException e) {
            PdbManager.LOGGER.error("Failed to fetch PDB id:" + pdbId, e);
            return null;
        }

        File pdbFile = new File(PdbManager.pdbReader.getPath());
        pdbFile = new File(pdbFile, "pdb" + pdbId.toLowerCase() + ".ent.gz");
        if (!pdbFile.exists()) {
            return null;
        }

        PdbManager.storeStructureInfo(pdbFile, structure);
        return pdbFile;
    }

    public static void remove(File path) {
        Structure s = PdbManager.getStructure(path);
        PdbManager.MAP_PATH_NAME.remove(path);
        PdbManager.MAP_PATH_STRUCTURE.remove(path);
        PdbManager.MAP_STRUCTURE_NAME.remove(s);
        assert PdbManager.MAP_PATH_NAME.size() == PdbManager.MAP_PATH_STRUCTURE
                .size();
        assert PdbManager.MAP_PATH_NAME.size() == PdbManager.MAP_STRUCTURE_NAME
                .size();
    }

    private static void storeStructureInfo(File file, Structure structure) {
        String name = structure.getPDBCode();
        if (name == null || name.trim().equals("")) {
            name = file.getName();
            if (name.endsWith(".pdb")) {
                name = name.substring(0, name.length() - 4);
            }
            structure.setPDBCode(name);
        }

        PdbManager.MAP_PATH_STRUCTURE.put(file, structure);
        PdbManager.MAP_PATH_NAME.put(file, name);
        PdbManager.MAP_STRUCTURE_NAME.put(structure, name);
    }

    private PdbManager() {
    }
}
