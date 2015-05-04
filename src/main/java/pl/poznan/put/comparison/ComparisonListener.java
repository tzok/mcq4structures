package pl.poznan.put.comparison;

public interface ComparisonListener {
    void stateChanged(long all, long completed);
}

class IgnoringComparisonListener implements ComparisonListener {
    private static final ComparisonListener INSTANCE = new IgnoringComparisonListener();

    public static ComparisonListener getInstance() {
        return IgnoringComparisonListener.INSTANCE;
    }

    @Override
    public void stateChanged(long all, long completed) {
        // do nothing
    }

    private IgnoringComparisonListener() {
        // do nothing
    }
}