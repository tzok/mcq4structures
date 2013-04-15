package pl.poznan.put.cs.bioserver.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface ClustalW extends Library {
    public static ClustalW INSTANCE = (ClustalW) Native.loadLibrary("clustalw",
            ClustalW.class);

    public int main(int argc, String[] argv);
}
