package pl.poznan.put.gui.panel;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.comparison.global.GlobalComparator;
import pl.poznan.put.comparison.global.GlobalMatrix;
import pl.poznan.put.comparison.global.ParallelGlobalComparator;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.gui.component.SVGComponent;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.StructureManager;
import pl.poznan.put.utility.svg.SVGHelper;
import pl.poznan.put.visualisation.VisualizableGlobalMatrix;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public final class GlobalMatrixPanel extends JPanel {
  private static final long serialVersionUID = 4604714903697643882L;

  private final JTextPane labelInfoMatrix = new JTextPane();
  private final JTable tableMatrix = new JTable();
  private final JProgressBar progressBar = new JProgressBar(0, 1);
  private final SVGComponent visualization = new SVGComponent(SVGHelper.emptyDocument(), "matrix");
  private List<? extends PdbModel> structures = Collections.emptyList();

  public GlobalMatrixPanel() {
    super(new BorderLayout());

    labelInfoMatrix.setBorder(new EmptyBorder(10, 10, 10, 0));
    labelInfoMatrix.setContentType("text/html");
    labelInfoMatrix.setEditable(false);
    labelInfoMatrix.setFont(UIManager.getFont("Label.font"));
    labelInfoMatrix.setOpaque(false);

    final JPanel panelInfo = new JPanel(new BorderLayout());
    panelInfo.add(labelInfoMatrix, BorderLayout.CENTER);

    final JPanel panelProgressBar = new JPanel(new BorderLayout());
    panelProgressBar.add(progressBar, BorderLayout.CENTER);

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add("Distance matrix", new JScrollPane(tableMatrix));
    tabbedPane.add("Visualization", visualization);

    add(panelInfo, BorderLayout.PAGE_START);
    add(tabbedPane, BorderLayout.CENTER);
    add(panelProgressBar, BorderLayout.PAGE_END);
  }

  public void setStructures(final List<? extends PdbModel> structures) {
    this.structures = new ArrayList<>(structures);
    tableMatrix.setModel(new DefaultTableModel());
    visualization.setSVGDocument(SVGHelper.emptyDocument());
    updateHeader(false, "");
  }

  private void updateHeader(final boolean readyResults, final String measureName) {
    final StringBuilder builder = new StringBuilder();
    builder.append("<html>Structures selected for global distance measure: ");
    int i = 0;

    for (final PdbModel structure : structures) {
      assert structure != null;
      builder
          .append("<span style=\"color: ")
          .append(((i % 2) == 0) ? "blue" : "green")
          .append("\">");
      builder.append(StructureManager.getName(structure));
      builder.append("</span>, ");
      i++;
    }

    builder.delete(builder.length() - 2, builder.length());

    if (readyResults) {
      builder.append("<br>Global distance matrix (");
      builder.append(measureName);
      builder.append("):");
    }

    builder.append("</html>");
    labelInfoMatrix.setText(builder.toString());
  }

  public void compareAndDisplayMatrix(final GlobalComparator measure, final Callback callback) {
    try {
      final List<StructureSelection> selections = new ArrayList<>();

      for (final PdbModel structure : structures) {
        final String name = StructureManager.getName(structure);
        selections.add(SelectionFactory.create(name, structure));
      }

      progressBar.setMinimum(0);
      progressBar.setMaximum((structures.size() * (structures.size() - 1)) / 2);
      progressBar.setValue(0);

      final ParallelGlobalComparator comparator =
          new ParallelGlobalComparator(
              measure,
              selections,
              new ParallelGlobalComparator.ProgressListener() {
                @Override
                public void setProgress(final int progress) {
                  progressBar.setValue(progress);
                }

                @Override
                public void complete(final GlobalMatrix matrix) {
                  final VisualizableGlobalMatrix visualizableMatrix =
                      new VisualizableGlobalMatrix(matrix);
                  final GlobalComparator measureType = visualizableMatrix.getComparator();
                  final SVGDocument document = visualizableMatrix.visualize();

                  tableMatrix.setModel(visualizableMatrix.asDisplayableTableModel());
                  updateRowHeights();

                  visualization.setSVGDocument(document);
                  updateHeader(true, measureType.getName());
                  callback.complete(new ProcessingResult(visualizableMatrix));
                }
              });

      comparator.start();
    } catch (final InvalidCircularValueException e) {
      final String message = "Failed to compare structures";
      GlobalMatrixPanel.log.error(message, e);
      JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updateRowHeights() {
    if (tableMatrix.getColumnCount() <= 1) {
      return;
    }

    int rowHeight = tableMatrix.getRowHeight();

    for (int row = 0; row < tableMatrix.getRowCount(); row++) {
      final TableCellRenderer cellRenderer = tableMatrix.getCellRenderer(row, 1);
      final Component comp = tableMatrix.prepareRenderer(cellRenderer, row, 1);
      rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
    }

    for (int row = 0; row < tableMatrix.getRowCount(); row++) {
      tableMatrix.setRowHeight(row, rowHeight);
    }
  }

  @FunctionalInterface
  public interface Callback {
    void complete(ProcessingResult processingResult);
  }
}
