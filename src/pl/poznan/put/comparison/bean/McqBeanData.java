package pl.poznan.put.comparison.bean;

public class McqBeanData {
    private int incorrectFirst;
    private int incorrectSecond;
    private int incorrectBoth;
    private int total;
    private int correct;
    private double mcq;

    public McqBeanData(int incorrectFirst, int incorrectSecond,
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
        return "McqBeanData [incorrectFirst=" + incorrectFirst
                + ", incorrectSecond=" + incorrectSecond + ", incorrectBoth="
                + incorrectBoth + ", total=" + total + ", correct=" + correct
                + ", mcq=" + mcq + "]";
    }
}