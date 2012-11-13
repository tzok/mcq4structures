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
    private static final Map<String, Structure> MAP_STRUCTURES = new HashMap<>();
    private static final Map<String, String> MAP_NAMES = new HashMap<>();

    /**
     * Load a structure and remember it being already cached.
     * 
     * @param path
     *            Path to the PDB file.
     * @return True, if the file was loaded successfully.
     */
    public static boolean addStructure(String path) {
        if (PdbManager.MAP_STRUCTURES.containsKey(path)) {
            return true;
        }

        Structure structure;
        try {
            structure = new PDBFileReader().getStructure(path);
        } catch (IOException e) {
            PdbManager.LOGGER.error("Failed to load the structure: " + path, e);
            return false;
        }

        String name = structure.getPDBCode();
        if (name == null || name.trim().equals("")) {
            name = new File(path).getName();
            structure.setPDBCode(name);
        }

        PdbManager.MAP_STRUCTURES.put(path, structure);
        PdbManager.MAP_NAMES.put(path, name);
        return true;
    }

    /**
     * Return an array of structure names for the given list of their paths.
     * 
     * @param elements
     *            List of paths to PDB files.
     * @return Array of names of structures.
     */
    public static String[] getNames(List<String> elements) {
        List<String> vector = new ArrayList<>();
        for (String element : elements) {
            String name = PdbManager.MAP_NAMES.get(element);
            vector.add(name);
        }
        return vector.toArray(new String[vector.size()]);
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
            Structure structure = PdbManager.MAP_STRUCTURES.get(element);
            vector.add(structure);
        }
        return vector.toArray(new Structure[vector.size()]);
    }

    private PdbManager() {
    }
}
