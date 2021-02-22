package pl.poznan.put.gui.panel;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.svg.SVGDocument;
import pl.poznan.put.circular.Angle;
import pl.poznan.put.circular.exception.InvalidCircularOperationException;
import pl.poznan.put.circular.exception.InvalidCircularValueException;
import pl.poznan.put.datamodel.ProcessingResult;
import pl.poznan.put.gui.component.SVGComponent;
import pl.poznan.put.matching.SelectionFactory;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.structure.StructureManager;
import pl.poznan.put.torsion.MasterTorsionAngleType;
import pl.poznan.put.visualisation.AngularHistogram;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.util.List;

@Slf4j
public final class TorsionAngleValuesMatrixPanel extends JPanel {
  private static final long serialVersionUID = -59286774381464603L;

  private final JTextPane labelInfoMatrix = new JTextPane();
  private final JTable tableMatrix = new JTable();
  private final JTabbedPane tabbedPane = new JTabbedPane();

  public TorsionAngleValuesMatrixPanel() {
    super(new BorderLayout());

    labelInfoMatrix.setBorder(new EmptyBorder(10, 10, 10, 0));
    labelInfoMatrix.setContentType("text/html");
    labelInfoMatrix.setEditable(false);
    labelInfoMatrix.setFont(UIManager.getFont("Label.font"));
    labelInfoMatrix.setOpaque(false);

    final JPanel panelInfo = new JPanel(new BorderLayout());
    panelInfo.add(labelInfoMatrix, BorderLayout.CENTER);

    final JScrollPane scrollPane = new JScrollPane(tableMatrix);
    tabbedPane.add("Torsion angles", scrollPane);

    add(panelInfo, BorderLayout.PAGE_START);
    add(tabbedPane, BorderLayout.CENTER);
  }

  public ProcessingResult calculateTorsionAngles(final PdbModel structure) {
    removeAllButFirstTab();
    updateHeader(structure);

    final StructureSelection selection =
        SelectionFactory.create(StructureManager.getName(structure), structure);
    tableMatrix.setModel(selection.asDisplayableTableModel());

    for (final MasterTorsionAngleType masterType : selection.getCommonTorsionAngleTypes()) {
      final List<Angle> angles = selection.getValidTorsionAngleValues(masterType);

      if (angles.isEmpty()) {
        continue;
      }

      try {
        final String title = masterType.longDisplayName();
        final AngularHistogram histogram = new AngularHistogram(angles);
        histogram.draw();
        final SVGDocument svgDocument = histogram.finalizeDrawing();
        final SVGComponent component = new SVGComponent(svgDocument, masterType.exportName());
        tabbedPane.add(title, component);
      } catch (final InvalidCircularValueException | InvalidCircularOperationException e) {
        TorsionAngleValuesMatrixPanel.log.warn(
            "Failed to visualize torsion angles of type: {}", masterType, e);
      }
    }

    return new ProcessingResult(selection);
  }

  private void removeAllButFirstTab() {
    while (tabbedPane.getComponentCount() > 1) {
      tabbedPane.remove(1);
    }
  }

  private void updateHeader(final PdbModel structure) {
    labelInfoMatrix.setText(
        String.format(
            "<html>Structure selected for torsion angles calculation: <span style=\"color: blue\">%s</span></html>",
            StructureManager.getName(structure)));
  }
}
