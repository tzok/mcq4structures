package pl.poznan.put.cs.bioserver.helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    public static Structure getStructure(File file) {
        return PdbManager.MAP_PATH_STRUCTURE.get(file);
    }

    public static String getStructureName(File file) {
        return PdbManager.MAP_PATH_NAME.get(file);
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

        Structure structure;
        try {
            structure = new PDBFileReader().getStructure(file);
        } catch (IOException e) {
            PdbManager.LOGGER.error("Failed to load the structure: " + file, e);
            return null;
        }

        String name = structure.getPDBCode();
        if (name == null || name.trim().equals("")) {
            name = file.getName();
            structure.setPDBCode(name);
        }

        PdbManager.MAP_PATH_STRUCTURE.put(file, structure);
        PdbManager.MAP_PATH_NAME.put(file, name);
        PdbManager.MAP_STRUCTURE_NAME.put(structure, name);
        return structure;
    }

    private PdbManager() {
    }
}
