package pl.poznan.put.comparison.bean;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import pl.poznan.put.torsion.StructureInTorsionAngleSpace;

public class McqGlobalBean extends GlobalBean {
    private static final DecimalFormat FORMAT =
            (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
    static {
        McqGlobalBean.FORMAT.setMaximumFractionDigits(2);
    }

    private StructureInTorsionAngleSpace structureFirst;
    private StructureInTorsionAngleSpace structureSecond;
    private McqBeanData data;

    public McqGlobalBean(StructureInTorsionAngleSpace structureFirst,
            StructureInTorsionAngleSpace structureSecond, int incorrectFirst,
            int incorrectSecond, int incorrectBoth, int total, int correct,
            double mcq) {
        super();
        this.structureFirst = structureFirst;
        this.structureSecond = structureSecond;
        data =
                new McqBeanData(incorrectFirst, incorrectSecond, incorrectBoth,
                        total, correct, mcq);
    }

    @Override
    public String toString() {
        return "McqGlobalBean [structureFirst=" + structureFirst
                + ", structureSecond=" + structureSecond + ", data=" + data
                + "]";
    }
}
