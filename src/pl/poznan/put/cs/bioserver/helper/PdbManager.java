package pl.poznan.put.cs.bioserver.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.poznan.put.cs.bioserver.gui.PdbManagerDialog;

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

        try {
            Structure structure = pdbReader.getStructure(file);
            storeStructureInfo(file, structure);
            return structure;
        } catch (IOException e) {
            PdbManager.LOGGER.error("Failed to load the structure: " + file, e);
            return null;
        }
    }

    private static void storeStructureInfo(File file, Structure structure) {
        String name = structure.getPDBCode();
        if (name == null || name.trim().equals("")) {
            name = file.getName();
            structure.setPDBCode(name);
        }

        PdbManager.MAP_PATH_STRUCTURE.put(file, structure);
        PdbManager.MAP_PATH_NAME.put(file, name);
        PdbManager.MAP_STRUCTURE_NAME.put(structure, name);

        PdbManagerDialog.model.addElement(file);
    }

    private PdbManager() {
    }

    public static int getSize() {
        assert PdbManager.MAP_PATH_NAME.size() == PdbManager.MAP_PATH_STRUCTURE
                .size();
        assert PdbManager.MAP_PATH_NAME.size() == PdbManager.MAP_STRUCTURE_NAME
                .size();
        return PdbManager.MAP_PATH_NAME.size();
    }

    public static File[] getAllStructures() {
        Set<File> set = PdbManager.MAP_PATH_NAME.keySet();
        List<File> list = new ArrayList<>(set);
        Collections.sort(list);
        return list.toArray(new File[list.size()]);
    }

    public static Structure[] getSelectedStructures(List<File> files) {
        int size = files.size();
        Structure[] structures = new Structure[size];
        for (int i = 0; i < size; i++) {
            File path = files.get(i);
            structures[i] = PdbManager.MAP_PATH_STRUCTURE.get(path);
        }
        return structures;
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

    public static void setAutoFetch(boolean autoFetch) {
        pdbReader.setAutoFetch(autoFetch);
    }

    public static Structure loadStructure(String pdbId) throws IOException {
        pdbReader.setAutoFetch(true);
        Structure structure = pdbReader.getStructureById(pdbId);

        File pdbFile = new File(pdbReader.getPath());
        pdbFile = new File(pdbFile, "pdb" + pdbId.toLowerCase() + ".ent.gz");
        if (!pdbFile.exists()) {
            throw new FileNotFoundException("File not found: " + pdbFile);
        }

        storeStructureInfo(pdbFile, structure);
        return structure;
    }

    public static String[] getSelectedStructuresNames(ArrayList<File> files) {
        int size = files.size();
        String[] names = new String[size];
        for (int i = 0; i < size; i++) {
            File path = files.get(i);
            names[i] = PdbManager.MAP_PATH_NAME.get(path);
        }
        return names;
    }
}
