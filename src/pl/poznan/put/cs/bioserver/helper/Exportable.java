package pl.poznan.put.cs.bioserver.helper;

import java.io.File;

public interface Exportable {
    void export(File file);

    File suggestName();
}
