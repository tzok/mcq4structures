package pl.poznan.put.cs.bioserver.torsion;

public class McqResult {
    private int incorrectFirst;
    private int incorrectSecond;
    private int incorrectBoth;
    private int total;
    private int correct;
    private double mcq;

    public McqResult(int incorrectFirst, int incorrectSecond,
            int incorrectBoth, int total, int correct, double mcq) {
        super();
        this.incorrectFirst = incorrectFirst;
        this.incorrectSecond = incorrectSecond;
        this.incorrectBoth = incorrectBoth;
        this.total = total;
        this.correct = correct;
        this.mcq = mcq;
    }

    @Override
    public String toString() {
        return "McqResult [incorrectFirst=" + incorrectFirst
                + ", incorrectSecond=" + incorrectSecond + ", incorrectBoth="
                + incorrectBoth + ", total=" + total + ", correct=" + correct
                + ", mcq=" + Math.toDegrees(mcq) + "]";
    }
}
