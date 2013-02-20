package pl.poznan.put.cs.bioserver.helper;

import java.io.File;

public interface Exportable {
    File suggestName();
    void export(File file);
}
