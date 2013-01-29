package pl.poznan.put.cs.bioserver.comparison;

public interface ComparisonListener {
    void stateChanged(long all, long completed);
}
