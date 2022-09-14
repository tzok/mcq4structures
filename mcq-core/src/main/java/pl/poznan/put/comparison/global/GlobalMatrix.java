package pl.poznan.put.comparison.global;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.table.TableModel;
import lombok.Getter;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import pl.poznan.put.interfaces.Clusterable;
import pl.poznan.put.interfaces.Exportable;
import pl.poznan.put.interfaces.Tabular;
import pl.poznan.put.types.DistanceMatrix;
import pl.poznan.put.types.ImmutableDistanceMatrix;
import pl.poznan.put.utility.NonEditableDefaultTableModel;
import pl.poznan.put.utility.TabularExporter;

@Getter
public class GlobalMatrix implements Clusterable, Exportable, Tabular {
  private final DistanceMatrix distanceMatrix;
  private final DistanceMatrix distanceMatrixWithoutIncomparables;

  private final GlobalComparator comparator;
  private final List<String> names;
  private final GlobalResult[][] resultsMatrix;

  public GlobalMatrix(
      final GlobalComparator comparator,
      final List<String> names,
      final GlobalResult[][] resultsMatrix) {
    super();
    this.comparator = comparator;
    this.names = new ArrayList<>(names);
    this.resultsMatrix = resultsMatrix.clone();

    distanceMatrix = prepareDistanceMatrix();
    distanceMatrixWithoutIncomparables = prepareDistanceMatrixWithoutIncomparables();
  }

  private static int[] selectAllButOne(final int excluded, final int count) {
    final int[] selected = new int[count - 1];
    int j = 0;
    for (int i = 0; i < count; i++) {
      if (i != excluded) {
        selected[j] = i;
        j++;
      }
    }
    return selected;
  }

  @Override
  public final DistanceMatrix distanceMatrix() {
    return distanceMatrixWithoutIncomparables;
  }

  @Override
  public final void export(final OutputStream stream) throws IOException {
    TabularExporter.export(asExportableTableModel(), stream);
  }

  @Override
  public final File suggestName() {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.US);
    String filename = sdf.format(new Date());
    filename += "-Global-";
    filename += comparator.getName();
    filename += ".csv";
    return new File(filename);
  }

  @Override
  public final TableModel asExportableTableModel() {
    return asTableModel(false);
  }

  @Override
  public final TableModel asDisplayableTableModel() {
    return asTableModel(true);
  }

  private DistanceMatrix prepareDistanceMatrix() {
    final double[][] matrix = new double[resultsMatrix.length][];

    for (int i = 0; i < resultsMatrix.length; i++) {
      assert resultsMatrix.length == resultsMatrix[i].length;
      matrix[i] = new double[resultsMatrix.length];
      Arrays.fill(matrix[i], Double.NaN);

      for (int j = 0; j < resultsMatrix.length; j++) {
        if (i == j) {
          matrix[i][j] = 0;
        } else if (resultsMatrix[i][j] != null) {
          matrix[i][j] = resultsMatrix[i][j].toDouble();
        }
      }
    }

    return ImmutableDistanceMatrix.of(names, matrix);
  }

  private DistanceMatrix prepareDistanceMatrixWithoutIncomparables() {
    final List<String> selectedNamesSubList = new ArrayList<>(names);
    double[][] selectedSubMatrix = distanceMatrix.matrix().clone();
    int maxErrorCount;

    do {
      maxErrorCount = Integer.MIN_VALUE;
      int argmax = -1;

      for (int i = 0; i < selectedSubMatrix.length; i++) {
        assert selectedSubMatrix.length == selectedSubMatrix[i].length;
        int errorCount = 0;

        for (int j = 0; j < selectedSubMatrix.length; j++) {
          errorCount += Double.isNaN(selectedSubMatrix[i][j]) ? 1 : 0;
        }

        if (errorCount > maxErrorCount) {
          maxErrorCount = errorCount;
          argmax = i;
        }
      }

      if (maxErrorCount > 0) {
        final int[] selected = GlobalMatrix.selectAllButOne(argmax, selectedSubMatrix.length);
        final RealMatrix realMatrix = new Array2DRowRealMatrix(selectedSubMatrix);
        final RealMatrix subMatrix = realMatrix.getSubMatrix(selected, selected);
        selectedSubMatrix = subMatrix.getData();
        selectedNamesSubList.remove(argmax);
      }
    } while (maxErrorCount > 0);

    return ImmutableDistanceMatrix.of(selectedNamesSubList, selectedSubMatrix);
  }

  private TableModel asTableModel(final boolean isDisplay) {
    final String[] columnNames = new String[names.size() + 1];
    columnNames[0] = "";
    for (int i = 0; i < names.size(); i++) {
      columnNames[i + 1] = names.get(i);
    }

    final String[][] values = new String[resultsMatrix.length][];

    for (int i = 0; i < values.length; i++) {
      values[i] = new String[columnNames.length];
      values[i][0] = names.get(i);

      for (int j = 0; j < resultsMatrix[i].length; j++) {
        // diagonal is empty
        if (i == j) {
          values[i][j + 1] = isDisplay ? "" : null;
          continue;
        }

        final GlobalResult result = resultsMatrix[i][j];

        if (result == null) {
          values[i][j + 1] = "Failed";
        } else {
          values[i][j + 1] = isDisplay ? result.longDisplayName() : result.exportName();
        }
      }
    }

    return new NonEditableDefaultTableModel(values, columnNames);
  }
}
