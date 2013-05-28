package pl.poznan.put.cs.bioserver.helper;

import java.io.File;
import java.io.IOException;

public interface Exportable {
    void export(File file) throws IOException;

    File suggestName();
}
