package pl.poznan.put.cs.bioserver.sandbox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.PDBFileReader;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.ChartLauncher;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import pl.poznan.put.cs.bioserver.comparison.MCQ;

public class Jzy3d {
    public static void main(String[] args) {
        List<File> pdbs = BenchmarkReference.list(new File("/home/tzok/pdb/puzzles/Challenge1/"));
        List<Structure> structures = new ArrayList<>();
        PDBFileReader reader = new PDBFileReader();
        for (int i = 0; i < pdbs.size(); i++) {
            try {
                structures.add(reader.getStructure(pdbs.get(i)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final double[][] result = new MCQ().compare(structures, null);

        // Define a function to plot
        // Mapper mapper = new Mapper() {
        // @Override
        // public double f(double x, double y) {
        // int ix = (int) x;
        // int iy = (int) y;
        // return result[ix][iy];
        // }
        // };

        // Define range and precision for the function to plot
        // Range range = new Range(0, result.length);
        // int steps = 1;

        List<Coord3d> coordinates = new ArrayList<>();
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                coordinates.add(new Coord3d(i, j, result[i][j]));
            }
        }
        // Create a surface drawing that function
        Shape surface = Builder.buildDelaunay(coordinates);
        // Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range,
        // steps, range, steps),
        // mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(),
                surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1,
                        .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);
        surface.setWireframeColor(Color.BLACK);

        // Create a chart and add the surface
        Chart chart = new Chart(Quality.Advanced);
        chart.getScene().getGraph().add(surface);
        ChartLauncher.openChart(chart);
    }
}
