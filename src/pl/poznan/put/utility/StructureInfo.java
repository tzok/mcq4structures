package pl.poznan.put.utility;

import java.io.File;

import org.biojava.bio.structure.Structure;

public class StructureInfo {
    private final Structure structure;
    private final File path;
    private final String name;

    public StructureInfo(Structure structure, File path, String name) {
        super();
        this.structure = structure;
        this.path = path;
        this.name = name;
    }

    public Structure getStructure() {
        return structure;
    }

    public File getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
