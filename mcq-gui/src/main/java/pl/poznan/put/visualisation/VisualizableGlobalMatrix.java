package pl.poznan.put.visualisation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.StatUtils;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.analysis.IAnalysis;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.comparison.global.GlobalMatrix;
import pl.poznan.put.constant.Unicode;
import pl.poznan.put.interfaces.Visualizable;
import pl.poznan.put.svg.MDSDrawer;
import pl.poznan.put.types.DistanceMatrix;
import pl.poznan.put.utility.svg.SVGHelper;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class VisualizableGlobalMatrix extends GlobalMatrix implements Visualizable {
  public VisualizableGlobalMatrix(final GlobalMatrix globalMatrix) {
    super(globalMatrix.getComparator(), globalMatrix.getNames(), globalMatrix.getResultsMatrix());
  }

  @Override
  public final SVGDocument visualize() {
    final DistanceMatrix distanceMatrix = getDistanceMatrixWithoutIncomparables();
    final double[][] array = distanceMatrix.matrix();

    if (array.length <= 1) {
      VisualizableGlobalMatrix.log.warn(
          "Cannot visualize this distance matrix, because it contains zero valid comparisons");
      return SVGHelper.emptyDocument();
    }

    return MDSDrawer.scale2DAndVisualizePoints(distanceMatrix);
  }

  @Override
  public final void visualize3D() {
    try {
      final String name = getComparator().getName();
      final double[][] matrix = getDistanceMatrix().matrix();
      final List<String> ticksX = getNames();
      final List<String> ticksY = getNames();
      final NavigableMap<Double, String> valueTickZ = prepareTicksZ();
      final String labelX = "";
      final String labelY = "";
      final String labelZ = "Distance";
      final boolean showAllTicksX = true;
      final boolean showAllTicksY = true;

      final IAnalysis surface3d =
          new Surface3D(
              name,
              matrix,
              ticksX,
              ticksY,
              valueTickZ,
              labelX,
              labelY,
              labelZ,
              showAllTicksX,
              showAllTicksY);
      AnalysisLauncher.open(surface3d);
    } catch (final Exception e) {
      final String message = "Failed to visualize in 3D";
      VisualizableGlobalMatrix.log.error(message, e);
      JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private NavigableMap<Double, String> prepareTicksZ() {
    final NavigableMap<Double, String> valueTickZ = new TreeMap<>();
    valueTickZ.put(0.0, "0");

    if (getComparator().isAngularMeasure()) {
      for (double radians = Math.PI / 12.0;
          radians <= (Math.PI + 1.0e-3);
          radians += Math.PI / 12.0) {
        valueTickZ.put(radians, Math.round(Math.toDegrees(radians)) + Unicode.DEGREE);
      }
    } else {
      final double[][] matrix = getDistanceMatrix().matrix();
      double max = Double.NEGATIVE_INFINITY;

      for (final double[] element : matrix) {
        max = Math.max(max, StatUtils.max(element));
      }

      for (double angstrom = 1.0; angstrom <= (Math.ceil(max) + 1.0e-3); angstrom += 1.0) {
        valueTickZ.put(angstrom, String.format("%d %s", Math.round(angstrom), Unicode.ANGSTROM));
      }
    }

    return valueTickZ;
  }
}
