package pl.poznan.put.cs.bioserver.helper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

public class PdbManager {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = Logger.getLogger(PdbManager.class);
    private final Map<String, Structure> mapStructure;
    private final Map<String, String> nameMap;

    // /**
    // * Give a chain, get information about every other chain this one was
    // * already aligned with.
    // */
    // private final static Map<Chain, Set<Chain>> mapAlignmentHistory = new
    // HashMap<>();
    // /** Give a pair of chains, get a 4-tuple of aligned chains. */
    // private final static Map<AlignmentInput, AlignmentOutput>
    // mapAlignmentCache = new HashMap<>();
    //
    // public static void putAlignmentToCache(AlignmentInput input,
    // AlignmentOutput output) {
    // PdbManager.LOGGER.debug("Putting alignment to cache: " + input);
    // PdbManager.mapAlignmentCache.put(input, output);
    //
    // Chain[] chains = input.getChains();
    // for (int i = 0; i < 2; i++) {
    // if (!PdbManager.mapAlignmentHistory.containsKey(chains[i]))
    // PdbManager.mapAlignmentHistory.put(chains[i],
    // new HashSet<Chain>());
    // Set<Chain> setHistory = PdbManager.mapAlignmentHistory
    // .get(chains[i]);
    // setHistory.add(chains[i ^ 1]);
    // }
    // }

    public PdbManager() {
        mapStructure = new HashMap<>();
        nameMap = new HashMap<>();
    }

    public boolean addStructure(String path) {
        try {
            if (mapStructure.containsKey(path)) {
                return true;
            }

            Structure structure = new PDBFileReader().getStructure(path);
            if (structure == null || structure.size() == 0) {
                throw new IOException();
            }

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

    public String[] getNames(List<String> elements) {
        List<String> vector = new Vector<>();
        for (String element : elements) {
            String name = nameMap.get(element);
            vector.add(name);
        }
        return vector.toArray(new String[vector.size()]);
    }

    public Structure[] getStructures(Enumeration<?> elements) {
        List<String> vector = new Vector<>();
        while (elements.hasMoreElements()) {
            vector.add((String) elements.nextElement());
        }
        return getStructures(vector);
    }

    public Structure[] getStructures(Iterable<String> elements) {
        List<Structure> vector = new Vector<>();
        for (String element : elements) {
            Structure structure = mapStructure.get(element);
            vector.add(structure);
        }
        return vector.toArray(new Structure[vector.size()]);
    }

    public Structure[] getStructures(String[] elements) {
        return getStructures(Arrays.asList(elements));
    }

    // public static AlignmentOutput getAlignmentFromCache(AlignmentInput input)
    // {
    // return PdbManager.mapAlignmentCache.get(input);
    // }
    //
    // public static boolean isAlignmentCached(AlignmentInput input) {
    // return PdbManager.mapAlignmentCache.containsKey(input);
    // }
}
