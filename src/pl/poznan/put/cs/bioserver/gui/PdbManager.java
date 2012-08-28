package pl.poznan.put.cs.bioserver.gui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

public class PdbManager {
    private final HashMap<String, Structure> mapStructure;
    private final HashMap<String, String> nameMap;
    private final static HashMap<Integer, HashMap<Group, Group>> mapAlignment = new HashMap<>();
    private final static Logger LOGGER = Logger.getLogger(PdbManager.class);

    public static HashMap<Group, Group> getAlignmentInfo(Chain[] chains) {
        Chain[] inverted = new Chain[] { chains[1], chains[0] };
        int[] hashCodes = new int[] { chains.hashCode(), inverted.hashCode() };
        PdbManager.LOGGER.debug("Trying to get hash codes: " + hashCodes[0]
                + " and " + hashCodes[1]);

        HashMap<Group, Group> map = PdbManager.mapAlignment.get(hashCodes[0]);
        if (map == null)
            map = PdbManager.mapAlignment.get(hashCodes[1]);
        return map;
    }

    public static boolean isAlignmentInfo(Chain[] chains) {
        Chain[] inverted = new Chain[] { chains[1], chains[0] };
        int[] hashCodes = new int[] { chains.hashCode(), inverted.hashCode() };
        PdbManager.LOGGER.debug("Checking for hash codes: " + hashCodes[0]
                + " and " + hashCodes[1]);

        return PdbManager.mapAlignment.containsKey(hashCodes[0])
                || PdbManager.mapAlignment.containsKey(hashCodes[1]);
    }

    public static void putAlignmentInfo(Chain[] original,
            String[][] residuesMapping) {
        HashMap<Group, Group> map = new HashMap<>();
        for (int i = 0; i < residuesMapping[0].length; i++) {
            int[] indices = new int[2];
            for (int j = 0; j < 2; j++) {
                String residue = residuesMapping[j][i].split(":")[0];
                indices[j] = Integer.valueOf(residue);
            }

            Group[] groups = new Group[2];
            for (int j = 0; j < 2; j++)
                groups[j] = original[j].getAtomGroup(indices[j]);
            map.put(groups[0], groups[1]);
        }
        int hashCode = original.hashCode(); // FIXME: hashCode nie działa!
        PdbManager.LOGGER.debug("Hash code of the new alignment info: "
                + hashCode);
        PdbManager.mapAlignment.put(hashCode, map);
    }

    public PdbManager() {
        mapStructure = new HashMap<>();
        nameMap = new HashMap<>();
    }

    public boolean addStructure(String path) {
        try {
            if (mapStructure.containsKey(path))
                return true;

            Structure structure = new PDBFileReader().getStructure(path);
            if (structure == null || structure.size() == 0)
                throw new IOException();

            String name = structure.getPDBCode();
            if (name == null || name.trim().equals("")) {
                name = new File(path).getName();
                structure.setPDBCode(name);
            }

            mapStructure.put(path, structure);
            nameMap.put(path, name);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String[] getNames(Vector<String> elements) {
        Vector<String> vector = new Vector<>();
        for (String element : elements) {
            String name = nameMap.get(element);
            vector.add(name);
        }
        return vector.toArray(new String[vector.size()]);
    }

    public Structure[] getStructures(Enumeration<?> elements) {
        Vector<String> vector = new Vector<>();
        while (elements.hasMoreElements())
            vector.add((String) elements.nextElement());
        return getStructures(vector);
    }

    public Structure[] getStructures(Iterable<String> elements) {
        Vector<Structure> vector = new Vector<>();
        for (String element : elements) {
            Structure structure = mapStructure.get(element);
            vector.add(structure);
        }
        return vector.toArray(new Structure[vector.size()]);
    }

    public Structure[] getStructures(String[] elements) {
        return getStructures(Arrays.asList(elements));
    }
}
