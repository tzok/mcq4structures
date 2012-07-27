package pl.poznan.put.cs.bioserver.gui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;

public class PdbManager {
    private final HashMap<String, Structure> mapStructure;
    private final HashMap<String, String> nameMap;

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
