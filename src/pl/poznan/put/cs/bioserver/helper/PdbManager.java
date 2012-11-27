package pl.poznan.put.cs.bioserver.helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

/**
 * A common manager of loaded PDB files shared between all classes.
 * 
 * @author tzok
 */
public final class PdbManager {
    private static final Logger LOGGER = Logger.getLogger(PdbManager.class);
    private static final Map<String, Structure> MAP_PATH_STRUCTURE = new HashMap<>();
    private static final Map<String, String> MAP_PATH_NAME = new HashMap<>();
    private static final Map<Structure, String> MAP_STRUCTURE_NAME = new HashMap<>();

    /**
     * Return an array of structure names for the given list of their paths.
     * 
     * @param elements
     *            List of paths to PDB files.
     * @return Array of names of structures.
     */
    public static String[] getNames(List<String> paths) {
        List<String> vector = new ArrayList<>();
        for (String element : paths) {
            String name = PdbManager.MAP_PATH_NAME.get(element);
            vector.add(name);
        }
        return vector.toArray(new String[vector.size()]);
    }

    public static String getStructureName(Structure structure) {
        if (!PdbManager.MAP_STRUCTURE_NAME.containsKey(structure)) {
            PdbManager.LOGGER.warn("A structure name could not be found in "
                    + "cache. Was it loaded by PdbManager class in the first "
                    + "place? The structure is: " + structure);
            return "UNKNOWN!";
        }
        return PdbManager.MAP_STRUCTURE_NAME.get(structure);
    }

    /**
     * Get an array of structures from a list of their paths.
     * 
     * @param elements
     *            A list of paths to PDB files.
     * 
     * @return An array of structures.
     */
    public static Structure[] getStructures(Iterable<String> elements) {
        List<Structure> vector = new ArrayList<>();
        for (String element : elements) {
            Structure structure = PdbManager.MAP_PATH_STRUCTURE.get(element);
            vector.add(structure);
        }
        return vector.toArray(new Structure[vector.size()]);
    }

    /**
     * Load a structure and remember it being already cached.
     * 
     * @param path
     *            Path to the PDB file.
     * @return Structure object..
     */
    public static Structure loadStructure(String path) {
        if (PdbManager.MAP_PATH_STRUCTURE.containsKey(path)) {
            return PdbManager.MAP_PATH_STRUCTURE.get(path);
        }

        Structure structure;
        try {
            structure = new PDBFileReader().getStructure(path);
        } catch (IOException e) {
            PdbManager.LOGGER.error("Failed to load the structure: " + path, e);
            return null;
        }

        String name = structure.getPDBCode();
        if (name == null || name.trim().equals("")) {
            name = new File(path).getName();
            structure.setPDBCode(name);
        }

        PdbManager.MAP_PATH_STRUCTURE.put(path, structure);
        PdbManager.MAP_PATH_NAME.put(path, name);
        PdbManager.MAP_STRUCTURE_NAME.put(structure, name);
        return structure;
    }

    private PdbManager() {
    }
}
