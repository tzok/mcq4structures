package pl.poznan.put.cs.bioserver.gui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

public class PdbManager {
    private final HashMap<String, Structure> mapStructure;
    private final HashMap<String, String> nameMap;
    private final static HashMap<Set<Chain>, HashMap<Group, Group>> mapAlignment = new HashMap<>();
    private final static HashMap<Chain, Set<Chain>> mapAlignmentHistory = new HashMap<>();
    private final static HashMap<Set<Chain>, Chain[]> mapAlignmentData = new HashMap<>();
    private final static Logger LOGGER = Logger.getLogger(PdbManager.class);

    public static HashMap<Group, Group> getAlignmentInfo(Chain[] chains) {
        HashSet<Chain> set = new HashSet<>(Arrays.asList(chains));
        PdbManager.LOGGER.debug("Getting alignment info with hashcode: "
                + set.hashCode());
        return PdbManager.mapAlignment.get(set);
    }

    public static boolean isAlignmentInfo(Chain[] chains) {
        HashSet<Chain> set = new HashSet<>(Arrays.asList(chains));
        PdbManager.LOGGER.debug("Checking alignment info with hashcode: "
                + set.hashCode());
        return PdbManager.mapAlignment.containsKey(set);
    }

    public static void putAlignmentInfo(Chain[] chains, Chain[] aligned,
            String[][] residuesMapping) {
        HashMap<Group, Group> map = new HashMap<>();
        for (int i = 0; i < residuesMapping[0].length; i++) {
            PdbManager.LOGGER.trace("Mapping between chains: "
                    + residuesMapping[0][i] + " " + residuesMapping[1][i]);
            Group[] groups = new Group[2];
            for (int j = 0; j < 2; j++) {
                String residue = residuesMapping[j][i].split(":")[0];
                try {
                    groups[j] = chains[j].getGroupByPDB(ResidueNumber
                            .fromString(residue));
                } catch (StructureException e) {
                    PdbManager.LOGGER.error(
                            "Failed to store alignment info between "
                                    + residuesMapping[0][i] + " and "
                                    + residuesMapping[1][i], e);
                }
            }
            map.put(groups[0], groups[1]);
        }

        HashSet<Chain> set = new HashSet<>(Arrays.asList(chains));
        PdbManager.LOGGER.debug("Putting alignment info with hashcode: "
                + set.hashCode());
        PdbManager.mapAlignment.put(set, map);

        for (int i = 0; i < 2; i++) {
            if (!PdbManager.mapAlignmentHistory.containsKey(chains[i]))
                PdbManager.mapAlignmentHistory.put(chains[i],
                        new HashSet<Chain>());
            Set<Chain> setHistory = PdbManager.mapAlignmentHistory
                    .get(chains[i]);
            setHistory.add(chains[i ^ 1]);
        }

        PdbManager.mapAlignmentData.put(set, aligned);
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

    public static Chain[] getAlignmentData(Chain[] chains) {
        return PdbManager.mapAlignmentData.get(new HashSet<>(Arrays
                .asList(chains)));
    }
}
