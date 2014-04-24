package pl.poznan.put.comparison.bean;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class McqLocalBean extends LocalBean {
    private List<McqGlobalBean> residuesResults;

    public McqLocalBean(String title, List<McqGlobalBean> residuesResults) {
        super(title);
        this.residuesResults = residuesResults;
    }

    public List<McqGlobalBean> getResiduesResults() {
        return residuesResults;
    }

    @Override
    public String toString() {
        return "McqLocalBean [residuesResults=" + residuesResults + "]";
    }

    @Override
    public void export(File file) throws IOException {
        // FIXME
        /*
         * try (PrintWriter writer = new PrintWriter(file, "UTF-8")) { CsvWriter
         * csvWriter = new CsvWriter(writer, '\t'); csvWriter.write("");
         * 
         * List<Angle> angleArray = new ArrayList<>(getAngles().values()); for
         * (Angle angle : angleArray) { csvWriter.write(angle.getName()); }
         * csvWriter.endRecord();
         * 
         * for (int i = 0; i < angles.size(); i++) {
         * csvWriter.write(ticks.get(i)); double[] deltas =
         * angleArray.get(i).getDeltas(); for (int j = 0; j < deltas.length - 1;
         * j++) { csvWriter.write(Double.toString(deltas[j])); }
         * csvWriter.endRecord(); } }
         */}

    @Override
    public void visualize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void visualize3D() {
        // TODO Auto-generated method stub

    }

    @Override
    public void visualizeHighQuality() {
        // TODO Auto-generated method stub

    }
}
